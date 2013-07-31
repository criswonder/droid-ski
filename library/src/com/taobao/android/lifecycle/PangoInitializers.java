package com.taobao.android.lifecycle;

import java.lang.annotation.Annotation;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import android.app.Activity;
import android.util.SparseArray;

import com.taobao.android.base.Versions;
import com.taobao.android.lifecycle.PanguApplication.CrossActivityLifecycleCallback;
import com.taobao.android.task.Coordinator;
import com.taobao.android.task.Coordinator.TaggedRunnable;

/**
 * Derive a class to add initialization methods named "initXXX()".
 *
 * @author Oasis
 */
public abstract class PangoInitializers {

	public static class UnqualifiedInitializerError extends Error {
		public UnqualifiedInitializerError(String message) { super(message); }
		private static final long serialVersionUID = 1L;
	}

	/** Annotate a initXXX() method to be run synchronously */
	@Retention(RetentionPolicy.CLASS)
	protected @interface Sync {
		/** Initializers with lowest priority start first.
		 *  No particular order for initializers with same priority. */
		int priority();
	}

	/** Annotate initXXX() method to be run asynchronously */
	@Retention(RetentionPolicy.CLASS)
	protected @interface Async {}

	/** Annotate initXXX() method to be run asynchronously when the main thread became idle. */
	@Retention(RetentionPolicy.CLASS)
	protected @interface Delayed {}

	/** Annotate initXXX() method needs to be run only if UI is going to be started. (optional)
	 *  Can be used together with above annotations */
	@Retention(RetentionPolicy.CLASS)
	protected @interface UiOnly {}

	/** Should only be called in initializer method */
	protected PanguApplication getApplication() {
		return mApplication;
	}

	abstract void onInitializerException(Method method, Exception exception);

	public void start(final PanguApplication application) {
		mApplication = application;
		parse();

		startInitializersWithout(UiOnly.class);

		application.registerCrossActivityLifecycleCallback(new CrossActivityLifecycleCallback() {

			@Override public void onCreated(Activity activity) {
				application.unregisterCrossActivityLifecycleCallback(this);
				startInitializersWithout(null);
			}

			@Override public void onStopped(Activity activity) {}
			@Override public void onStarted(Activity activity) {}
			@Override public void onDestroyed(Activity activity) {}
		});
	}

	private void startInitializersWithout(Class<? extends Annotation> annotation) {
		// Post asynchronous initializers
		Iterator<Method> iterator = mAsyncInitializers.iterator();
		while(iterator.hasNext()) {
			final Method method = iterator.next();
			if (annotation != null && method.isAnnotationPresent(annotation)) continue;
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
		Method[] methods = getClass().getMethods();
		for (Method method : methods) {
			String name = method.getName();
			if (name.length() < 5 || ! name.startsWith("init")
					|| ! Character.isUpperCase(name.charAt(4))) continue;
			if (Versions.isDebug()) {		// Only check qualification in DEBUG build.
				if ((method.getModifiers() & Modifier.STATIC) == 0)
					throw new UnqualifiedInitializerError("Non-static: " + name);
				if (method.getParameterTypes().length != 0)
					throw new UnqualifiedInitializerError("With parameters: " + name);
				if (method.getReturnType() != Void.class)
					throw new UnqualifiedInitializerError("Non-void return type: " + name);
			}

			for (Annotation annotation : method.getAnnotations()) {
				Class<? extends Annotation> type = annotation.getClass();
				if (Sync.class.isAssignableFrom(type)) {
					int priority = ((Sync) annotation).priority();
					List<Method> initializers = mSyncInitializers.get(priority);
					if (initializers == null) mSyncInitializers.put(priority, initializers = new ArrayList<Method>());
					initializers.add(method);
				} else if (Delayed.class.isAssignableFrom(type)) {
					mDelayedInitializers.add(method);
				} else if (Async.class.isAssignableFrom(type)) {
					mAsyncInitializers.add(method);
				} else if (Versions.isDebug())
					throw new UnqualifiedInitializerError("No mandatory annotation: " + name);
			}
		}
	}

	private void invokeInitializer(final Method method) {
		try {
			method.invoke(null);
		} catch (Exception e) {
			onInitializerException(method, e);
		}
	}

	private PanguApplication mApplication;
	private final SparseArray<List<Method>> mSyncInitializers = new SparseArray<List<Method>>();
	private final List<Method> mAsyncInitializers = new ArrayList<Method>();
	private final List<Method> mDelayedInitializers = new ArrayList<Method>();
}

class DemoInitializers extends PangoInitializers {

	@Sync(priority=2) @UiOnly
	static void initImageManager() {}

	@Delayed
	static void initGoogleAnalytics() {}

	@Override
	void onInitializerException(Method method, Exception exception) {
		// Send exception report
		// Attempt to recovery from exception
		// Prepare for safe-mode restart
		// ...
	}

	public static void main(String[] args) {
		PanguApplication application = null;
		new DemoInitializers().start(application);
	}
}