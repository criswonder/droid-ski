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

	public static final String KExtraReferrer = "referrer";

	public static interface NavPreprocessor {
		/**
		 * Check the intent and make changes if needed.
		 *
		 * @return true to pass, or false to abort the navigation request.
		 */
		boolean beforeNavTo(Intent intent);
	}

	/** @param context use current Activity if possible */
	public static Nav from(final Context context) {
		return new Nav(context);
	}

	/**
	 *  <b>Intent returned by this method should NEVER be kept</b>, but used immediately in
	 *  {@link android.content.Context#startActivity(Intent)} or its variants.
	 */
	public @Nullable Intent to(final Uri uri) {
		final Intent intent = new Intent(Intent.ACTION_VIEW, uri).setPackage(mContext.getPackageName());
		if (mContext instanceof Activity) {
			final Intent from_intent = ((Activity) mContext).getIntent();
			if (from_intent != null) {
				final Uri referrer_uri = from_intent.getData(); ComponentName comp;
				if (referrer_uri != null) intent.putExtra(KExtraReferrer, referrer_uri.toString());
				else if ((comp = from_intent.getComponent()) != null)		// Compact (component only)
					intent.putExtra(KExtraReferrer, new Intent().setComponent(comp).toUri(0));
				else intent.putExtra(KExtraReferrer, from_intent.toUri(0));	// Legacy
			}
		}
		if (mExtras != null) intent.putExtras(mExtras);
		if (! mPreprocessor.isEmpty())
			for (final NavPreprocessor preprocessor : mPreprocessor)
				if (! preprocessor.beforeNavTo(intent))
					return null;
		return intent;
	}

	public @Nullable Intent toActivity(final Uri uri) {
		final Intent intent = to(uri);
		if (intent == null) return null;
		final ComponentName component = intent.resolveActivity(mContext.getPackageManager());
		intent.setComponent(component);
		return intent;
	}

	public static void registerPreprocessor(final NavPreprocessor preprocessor) {
		mPreprocessor.add(preprocessor);
	}

	public static void unregisterPreprocessor(final NavPreprocessor preprocessor) {
		mPreprocessor.remove(preprocessor);
	}

	private Nav(final Context context) {
		mContext = context;
	}

	private final Context mContext;
	private Bundle mExtras;
	private static final List<NavPreprocessor> mPreprocessor = new ArrayList<Nav.NavPreprocessor>();
}

/** Demonstrate the usage of {@link Nav} */
class DemoActivity extends Activity {

	void openUri(final Uri uri) {
		startActivity(Nav.from(this).to(uri));
	}

	void openUriWithinWebview(final Uri uri) {
		final Intent intent = Nav.from(this).toActivity(uri);
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
