package com.taobao.android.ski.gear;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Build;
import android.os.Build.VERSION_CODES;
import android.os.Looper;
import android.preference.PreferenceManager;

/**
 * Utilities for better shared preferences performance.
 *
 * @author Oasis
 */
public class FastSharedPreferences {

	/** Always use default instance to benefit the shared cache. */
	public static SharedPreferences getDefault(Context context) {
		return wrap(PreferenceManager.getDefaultSharedPreferences(context));
	}

	/** Wrap existent SharedPreferences instance to apply the optimization */
	public static SharedPreferences wrap(final SharedPreferences prefs) {
		if (mMethodApply == null) return prefs;			// No "apply()" method

		if (Proxy.isProxyClass(prefs.getClass()))
			throw new IllegalArgumentException("The SharedPreferences instance is already proxied.");

		return (SharedPreferences) Proxy.newProxyInstance(prefs.getClass().getClassLoader(), new Class<?>[] { SharedPreferences.class }, new InvocationHandler() {

			@Override public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
				Object result = method.invoke(prefs, args);
				if (args.length != 0 /* fast first */ || ! "edit".equals(method.getName())) return result;

				final Editor editor = (Editor) result;
				return Proxy.newProxyInstance(prefs.getClass().getClassLoader(), new Class<?>[] { Editor.class }, new InvocationHandler() {
					
					@Override public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
						if (args.length == 0 /* fast first */ && "commit".equals(method.getName()) 
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
		Method method = null;
		try {
			method = Build.VERSION.SDK_INT < VERSION_CODES.GINGERBREAD ? null : Editor.class.getMethod("apply");
		} catch (NoSuchMethodException e) { /* Just ignore for ROM compatibility */ }
		mMethodApply = method;
	}
}
