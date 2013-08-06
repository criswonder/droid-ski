package com.taobao.android.lifecycle;

import java.lang.annotation.Annotation;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.annotation.NonNullByDefault;
import javax.annotation.Nullable;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.util.SparseArray;

import com.taobao.android.base.Versions;
import com.taobao.android.lifecycle.DemoApplication.DemoInitializers;
import com.taobao.android.lifecycle.PanguApplication.CrossActivityLifecycleCallback;
import com.taobao.android.task.Coordinator;
import com.taobao.android.task.Coordinator.TaggedRunnable;

/**
 * Derive a class to add initialization methods named "initXXX()".
 *
 * @see DemoInitializers
 * @author Oasis
 */
@NonNullByDefault
public abstract class PanguInitializers {

	public static class UnqualifiedInitializerError extends Error {
		public UnqualifiedInitializerError(String message) { super(message); }
		private static final long serialVersionUID = 1L;
	}

	/** Annotate a initXXX() method to be run synchronously in the specific priority, 0 if absent.
	 *  Initializers with lowest priority start first. No particular order for same priority. */
	@Target(ElementType.METHOD) @Retention(RetentionPolicy.RUNTIME)
	protected @interface Priority {
		int value();
	}

	/** Annotate initXXX() method to be run asynchronously in no particular order */
	@Target(ElementType.METHOD) @Retention(RetentionPolicy.RUNTIME)
	protected @interface Async {}

	/** Annotate initXXX() method to be run asynchronously when the main thread became idle,
	 *  in no particular order */
	@Target(ElementType.METHOD) @Retention(RetentionPolicy.RUNTIME)
	protected @interface Delayed {}

	/** Annotate initXXX() method which is needed not only by UI, but also by other components
	 *  (including Service, global BroadcastReceiver, ContentProvider, etc.).
	 *  (optional, to be used together with above annotations) */
	@Target(ElementType.METHOD) @Retention(RetentionPolicy.RUNTIME)
	protected @interface Global {}

	/** Should only be called in initializer method */
	@SuppressWarnings("null")	// Never be null when called in initializer method
	protected PanguApplication getApplication() {
		return mApplication;
	}

	public abstract void onInitializerException(Method method, Exception exception);

	public void start(final PanguApplication application) {
		mApplication = application;

		Coordinator.runTask(new TaggedRunnable("PangoInitializers.parse") { @Override public void run() {
			parse();
		}});

		startInitializersAnnotatedBy(Global.class);

		application.registerCrossActivityLifecycleCallback(new CrossActivityLifecycleCallback() {

			@Override public void onCreated(Activity activity) {
				startInitializersAnnotatedBy(null);
			}

			@Override public void onStarted(Activity activity) {
				application.unregisterCrossActivityLifecycleCallback(this);
				Coordinator.scheduleIdleTasks();
			}

			@Override public void onStopped(Activity activity) {}
			@Override public void onDestroyed(Activity activity) {}
		});
	}

	private void startInitializersAnnotatedBy(@Nullable Class<? extends Annotation> annotation) {
		// Post asynchronous initializers
		Iterator<Method> iterator = mAsyncInitializers.iterator();
		while(iterator.hasNext()) {
			final Method method = iterator.next();
			if (annotation == null || ! method.isAnnotationPresent(annotation)) continue;
			Coordinator.postTask(new TaggedRunnable(method.getName()) { @Override public void run() {
				invokeInitializer(method);
			}});
			iterator.remove();
		}

		// Start synchronous initializers
		for (int i = 0; i < mSyncInitializers.size(); i ++) {
			int priority = mSyncInitializers.keyAt(i);
			iterator = mSyncInitializers.get(priority).iterator();
			while(iterator.hasNext()) {
				final Method method = iterator.next();
				Coordinator.runTask(new TaggedRunnable(method.getName()) { @Override public void run() {
					invokeInitializer(method);
				}});
				iterator.remove();
			}
		}

		// Post delayed initializers
		iterator = mDelayedInitializers.iterator();
		while(iterator.hasNext()) {
			final Method method = iterator.next();
			Coordinator.postIdleTask(new TaggedRunnable(method.getName()) { @Override public void run() {
				invokeInitializer(method);
			}});
			iterator.remove();
		}
	}

	private void parse() {
		boolean debug = Versions.isDebug();
		Method[] methods = getClass().getDeclaredMethods();
		for (Method method : methods) {
			String name = method.getName();
			if (name.length() < 5 || ! name.startsWith("init")
					|| ! Character.isUpperCase(name.charAt(4))) continue;
			if (debug) {		// Only check qualification in DEBUG build.
				if ((method.getModifiers() & Modifier.PRIVATE) != 0)
					throw new UnqualifiedInitializerError("Private: " + name);
				if ((method.getModifiers() & Modifier.STATIC) != 0)
					throw new UnqualifiedInitializerError("Static: " + name);
				if (method.getParameterTypes().length != 0)
					throw new UnqualifiedInitializerError("With parameters: " + name);
				if (method.getReturnType() != void.class)
					throw new UnqualifiedInitializerError("Non-void return type: " + name);
			}

			if (method.isAnnotationPresent(Delayed.class)) {
				mDelayedInitializers.add(method);
			} else if (method.isAnnotationPresent(Async.class)) {
				mAsyncInitializers.add(method);
			} else {
				Priority priority_annonation = method.getAnnotation(Priority.class);
				int priority = priority_annonation == null ? 0 : priority_annonation.value();
				List<Method> initializers = mSyncInitializers.get(priority);
				if (initializers == null) mSyncInitializers.put(priority, initializers = new ArrayList<Method>());
				initializers.add(method);
			}
		}
	}

	private void invokeInitializer(final Method method) {
		try {
			method.invoke(this);
		} catch (Exception e) {
			onInitializerException(method, e);
		}
	}

	private @Nullable PanguApplication mApplication;
	private final SparseArray<List<Method>> mSyncInitializers = new SparseArray<List<Method>>();
	private final List<Method> mAsyncInitializers = new ArrayList<Method>();
	private final List<Method> mDelayedInitializers = new ArrayList<Method>();
}

@NonNullByDefault
class DemoApplication extends PanguApplication {

	@Override public void onCreate() {
		super.onCreate();
		new DemoInitializers().start(this);
	}

	/** Demonstrate the usage of PangoInitializers */
	static class DemoInitializers extends PanguInitializers {

		@Priority(2)
		public void initImageManager() {}

		@Async
		public void initDnsPrefetcher() {}

		@Delayed @Global
		public void initGoogleAnalytics() {
			@SuppressWarnings("unused")
			Context context = getApplication().getApplicationContext();
			// Use context to initialize Google Analytics
		}

		@Override
		public void onInitializerException(Method method, Exception exception) {
			// Send exception report
			// Attempt to recovery from exception
			// Prepare for safe-mode restart
			// ...
		}
	}
}
