package com.taobao.android.nav;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
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

	/** @param context use current Activity if possible */
	public static Nav from(Context context) {
		return new Nav(context);
	}

	public Nav with(Bundle extras) {
		mExtras = extras;
		return this;
	}

	/**
	 *  <b>Intent returned by this method should NEVER be kept</b>, but used in
	 *  {@link android.content.Context#startActivity(Intent) Context.startActivity()} or its variants.
	 */
	public Intent to(Uri uri) {
		if (! mPreprocessor.isEmpty()) {
			for (NavPreprocessor preprocessor : mPreprocessor)
				uri = preprocessor.beforeNavToUri(uri);
		}
		Intent intent = new Intent(Intent.ACTION_VIEW, uri).setPackage(mContext.getPackageName());
		if (mExtras != null) intent.putExtras(mExtras);
		return intent;
	}

	public static interface NavPreprocessor {
		Uri beforeNavToUri(Uri uri);
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

	/** Demonstrate the usage of {@link Nav} */
	static class DemoActivity extends Activity {

		void startActivity(Uri uri) {
			startActivity(Nav.from(this).to(uri));
		}

		void startActivity(Uri uri, Bundle extra) {
			startActivity(Nav.from(this).to(uri));
		}
	}
}