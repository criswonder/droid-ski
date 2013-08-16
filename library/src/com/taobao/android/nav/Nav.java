package com.taobao.android.nav;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

/**
 * Navigation by URL
 *
 * <p><b>Instance should NEVER be kept, due to possible memory-leak to context.</b></p>
 *
 *  @see DemoActivity
 *  @author Oasis
 */
public class Nav {

	public static interface NavPreprocessor {
		/**
		 * Check the intent and make changes if needed.
		 *
		 * @return true to pass, or false to abort the navigation request.
		 */
		boolean beforeNavTo(Intent intent);
	}

	/** @param context use current Activity if possible */
	public static Nav from(Context context) {
		return new Nav(context);
	}

	/**
	 *  <b>Intent returned by this method should NEVER be kept</b>, but used immediately in
	 *  {@link android.content.Context#startActivity(Intent)} or its variants.
	 */
	public @Nullable Intent to(Uri uri) {
		Intent intent = new Intent(Intent.ACTION_VIEW, uri).setPackage(mContext.getPackageName());
		if (mExtras != null) intent.putExtras(mExtras);
		if (! mPreprocessor.isEmpty())
			for (NavPreprocessor preprocessor : mPreprocessor)
				if (! preprocessor.beforeNavTo(intent))
					return null;
		return intent;
	}

	public @Nullable Intent toActivity(Uri uri) {
		Intent intent = to(uri);
		if (intent == null) return null;
		ComponentName component = intent.resolveActivity(mContext.getPackageManager());
		intent.setComponent(component);
		return intent;
	}

	public static void registerPreprocessor(NavPreprocessor preprocessor) {
		mPreprocessor.add(preprocessor);
	}

	public static void unregisterPreprocessor(NavPreprocessor preprocessor) {
		mPreprocessor.remove(preprocessor);
	}

	private Nav(Context context) {
		mContext = context;
	}

	private final Context mContext;
	private Bundle mExtras;
	private static final List<NavPreprocessor> mPreprocessor = new ArrayList<Nav.NavPreprocessor>();
}

/** Demonstrate the usage of {@link Nav} */
class DemoActivity extends Activity {

	void openUri(Uri uri) {
		startActivity(Nav.from(this).to(uri));
	}

	void openUriWithinWebview(Uri uri) {
		Intent intent = Nav.from(this).toActivity(uri);
		if (intent == null) {
			// Nothing to open
		} else if (intent.getComponent() == null) {
			// Try to resolve it in system scope.
			// If succeed, start the intent, otherwise open in current WebView.
		} else if (getComponentName().equals(intent.getComponent())) {		// Is target me?
			// Open URI in current WebView.
		} else {
			startActivity(intent);
		}
	}
}
