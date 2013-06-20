package com.taobao.android.ski.gear;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Build;
import android.os.Build.VERSION_CODES;
import android.preference.PreferenceManager;

/**
 * Provide {@link SharedPreferences.Editor#apply()} for API level ~8.
 *
 * @author Oasis
 */
public class SharedPreferencesCompat {

	public interface EditorCompat extends SharedPreferences.Editor {
		@Override public void apply();
	}

	/** Get the default SharedPreferences instance of current package, with compatibility wrap. */
	public static SharedPreferences getDefault(Context context) {
		return wrap(PreferenceManager.getDefaultSharedPreferences(context));
	}

	/** Wrap built-in SharedPreferences instance for compatibility if needed. */
	public static SharedPreferences wrap(final SharedPreferences prefs) {
		if (Build.VERSION.SDK_INT >= VERSION_CODES.GINGERBREAD) return prefs;	// Pass-through

		if (Proxy.isProxyClass(prefs.getClass()))
			throw new IllegalArgumentException("The SharedPreferences instance is already proxied.");

		return (SharedPreferences) Proxy.newProxyInstance(prefs.getClass().getClassLoader(),
				new Class<?>[] { SharedPreferences.class }, new InvocationHandler() {

			@Override public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
				Object result = method.invoke(prefs, args);
				if (args.length != 0 /* fast first */ || ! "edit".equals(method.getName())) return result;

				final Editor editor = (Editor) result;
				return Proxy.newProxyInstance(prefs.getClass().getClassLoader(), new Class<?>[] { EditorCompat.class }, new InvocationHandler() {
					
					@Override public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
						if (args.length == 0 /* fast first */ && "apply".equals(method.getName())) {
							editor.commit();		// Use commit() instead
							return null;
						}
						return method.invoke(editor, args);
					}
					
				});
			}
		});
	}
}
