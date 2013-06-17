package com.taobao.android.ski.gear;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Build;
import android.os.Build.VERSION_CODES;
import android.os.Looper;

/**
 * Use apply() when commit() is called on main thread.
 *
 * @author Oasis
 */
public class FastSharedPreferences {

	/** Wrap existent SharedPreferences instance to apply the optimization */
	public static SharedPreferences wrap(final SharedPreferences prefs) {
		if (Build.VERSION.SDK_INT < VERSION_CODES.GINGERBREAD) return prefs;	// No "apply()" before Gingerbread.

		if (Proxy.isProxyClass(prefs.getClass()))
			throw new IllegalArgumentException("The SharedPreferences instance is already proxied.");

		return (SharedPreferences) Proxy.newProxyInstance(prefs.getClass().getClassLoader(), new Class<?>[] { SharedPreferences.class }, new InvocationHandler() {

			@Override public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
				Object result = method.invoke(prefs, args);
				if (! "edit".equals(method.getName()) || args.length != 0) return result;

				final Editor editor = (Editor) result;
				return Proxy.newProxyInstance(prefs.getClass().getClassLoader(), new Class<?>[] { Editor.class }, new InvocationHandler() {
					
					@Override public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
						if ("commit".equals(method.getName()) && args.length == 0
								&& Thread.currentThread() == Looper.getMainLooper().getThread()) {
							mMethodApply.invoke(editor);	// Use apply() instead of commit() if running on UI thread.
							return true;					// Always return "true".
						}
						return method.invoke(editor, args);
					}
					
				});
			}
		});
	}

	private static final Method mMethodApply;
	static {
		try {
			mMethodApply = Editor.class.getMethod("apply");
		} catch (NoSuchMethodException e) {
			throw new NoSuchMethodError(e.getMessage());
		}
	}
}
