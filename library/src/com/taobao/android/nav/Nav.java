package com.taobao.android.nav;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.NonNullByDefault;
import javax.annotation.Nullable;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

/**
 * Cross-Platform Navigation (based on URI)
 *
 * <p><b>Instance should NEVER be kept, due to possible memory-leak to context.</b></p>
 *
 *  @see DemoActivity
 *  @author Oasis
 */
@NonNullByDefault
public class Nav {

	public static final String KExtraReferrer = "referrer";

	/** Preprocessor for every navigation attempts */
	public static interface NavPreprocessor {

		/**
		 * Check the intent and make changes if needed.
		 *
		 * @return true to pass, or false to abort the navigation request.
		 */
		boolean beforeNavTo(Intent intent);
	}

	/** Exception handler to be triggered for navigation exception. */
	public static interface NavExceptionHandler {

		/**
		 * Called when failed to navigate to the specific destination.
		 *
		 * @param intent the activity intent to be started which caused the exception.
		 *   It is expected to be changed if retry is needed.
		 * @param e the exception (most probably {@link android.content.ActivityNotFoundException ActivityNotFoundException})
		 * @return whether to retry the navigation. <b>When failed in retry, this called will not be called again.</b>
		 */
		boolean onException(Intent intent, Exception e);
	}

	/** @param context use current Activity if possible */
	public static Nav from(final Context context) {
		return new Nav(context);
	}

	/** Allow navigation to escape current application (for 3rd-party activity) */
	public Nav allowEscape() {
		mAllowLeaving = true;
		return this;
	}

	/** Extras to be put into activity intent. */
	public Nav withExtras(final Bundle extras) {
		mIntent.putExtras(extras);
		return this;
	}

	/** Flags to be added to activity intent */
	public Nav withFlags(final int flags) {
		mIntent.addFlags(flags);
		return this;
	}

	/** Start activity associated with the specific URI. */
	public void toUri(final Uri uri) {
		final Intent intent = to(uri);
		if (intent == null) return;
		NavExceptionHandler exception_handler = mExceptionHandler;
		for (;;) try {
			mContext.startActivity(intent);
			break;
		} catch (final ActivityNotFoundException e) {
			if (exception_handler != null && exception_handler.onException(intent, e)) {
				exception_handler = null;		// To avoid dead-loop.
				continue;
			}
			break;
		}
	}

	/** @deprecated Use {@link #toUri(Uri uri)} instead. */
	@Deprecated public @Nullable Intent to(final Uri uri) {
		mIntent.setData(uri);
		if (! mAllowLeaving) mIntent.setPackage(mContext.getPackageName());
		// Add referrer extra
		if (mContext instanceof Activity) {
			final Intent from_intent = ((Activity) mContext).getIntent();
			if (from_intent != null) {
				final Uri referrer_uri = from_intent.getData(); ComponentName comp;
				if (referrer_uri != null) mIntent.putExtra(KExtraReferrer, referrer_uri.toString());
				else if ((comp = from_intent.getComponent()) != null)		// Compact (component only)
					mIntent.putExtra(KExtraReferrer, new Intent().setComponent(comp).toUri(0));
				else mIntent.putExtra(KExtraReferrer, from_intent.toUri(0));	// Legacy
			}
		}
		// Run preprocessors
		if (! mPreprocessor.isEmpty())
			for (final NavPreprocessor preprocessor : mPreprocessor)
				if (! preprocessor.beforeNavTo(mIntent))
					return null;
		return mIntent;
	}

	/** @deprecated Use {@link #toUri(Uri uri)} instead. */
	@Deprecated public @Nullable Intent toActivity(final Uri uri) {
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

	public static void setExceptionHandler(final NavExceptionHandler handler) {
		mExceptionHandler = handler;
	}

	private Nav(final Context context) {
		mContext = context;
		mIntent = new Intent(Intent.ACTION_VIEW);
	}

	private final Context mContext;
	private final Intent mIntent;
	private boolean mAllowLeaving;

	private static final List<NavPreprocessor> mPreprocessor = new ArrayList<NavPreprocessor>();
	private static @Nullable NavExceptionHandler mExceptionHandler;
}

/** Demonstrate the usage of {@link Nav} */
@NonNullByDefault
class DemoActivity extends Activity {

	void openUri(final Uri uri) {
		Nav.from(this).toUri(uri);
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
