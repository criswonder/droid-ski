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
import android.util.Log;

import com.taobao.android.base.Versions;

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
		 * @param intent the activity intent to be started which caused the exception
		 *   (may be modified by {@link NavPreprocessor}). It is supposed to be changed if retry is needed.
		 * @param e the exception (most probably {@link android.content.ActivityNotFoundException})
		 * @return whether to retry the navigation.
		 *   <b>When failed in retry, this called will not be called again.</b>
		 */
		boolean onException(Intent intent, Exception e);
	}

	public static class NavigationCanceledException extends Exception {

		private static final long serialVersionUID = 5015146091187397488L;
	}

	/** @param context use current Activity if possible */
	public static Nav from(final Context context) {
		if (Versions.isDebug() && context == context.getApplicationContext())
			throw new IllegalArgumentException("Application context is not allowed, use actual context instead.");
		return new Nav(context);
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

	/** Allow navigation to escape current application (for 3rd-party activity) */
	public Nav allowEscape() {
		mAllowLeaving = true;
		return this;
	}

	/** Disallow navigation to current activity itself (specified by {@link #from(Context)}). */
	public Nav disallowLoopback() {
		mDisallowLoopback = true;
		return this;
	}

	public Nav skipPreprocess() {
		mSkipPreprocess = true;
		return this;
	}

	/** Start activity associated with the specific URI. */
	@SuppressWarnings("null")		// SDK lacks nullness annotation.
	public void toUri(final String uri) {
		toUri(Uri.parse(uri));
	}

	/** Start activity associated with the specific URI. */
	public boolean toUri(final Uri uri) {
		NavExceptionHandler exception_handler = mExceptionHandler;
		final Intent intent = to(uri);
		if (intent == null) {
			if (exception_handler != null)
				exception_handler.onException(mIntent, new NavigationCanceledException());
			return false;
		}
		for (;;) try {
			if (mDisallowLoopback && mContext instanceof Activity) {
				final ComponentName target = intent.resolveActivity(mContext.getPackageManager());
				if (target == null) throw new ActivityNotFoundException("No Activity found to handle " + intent);
				if (target.equals(((Activity) mContext).getComponentName())) {
					Log.w(TAG, "Loopback disallowed: " + uri);
					return false;
				}
			}
			mContext.startActivity(intent);
			return true;
		} catch (final ActivityNotFoundException e) {
			if (exception_handler != null && exception_handler.onException(intent, e)) {
				exception_handler = null;		// To avoid dead-loop.
				continue;
			}
			return false;
		}
	}

	/** @deprecated Use {@link #toUri(Uri uri)} instead. */
	@Deprecated public @Nullable Intent to(final Uri uri) {
		mIntent.setData(uri);
		if (! mAllowLeaving) mIntent.setPackage(mContext.getPackageName());
		// Add referrer extra if not present
		if (mContext instanceof Activity && ! mIntent.hasExtra(KExtraReferrer)) {
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
		if (! mSkipPreprocess && ! mPreprocessor.isEmpty())
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
	private boolean mDisallowLoopback;
	private boolean mSkipPreprocess;

	private static final List<NavPreprocessor> mPreprocessor = new ArrayList<NavPreprocessor>();
	private static @Nullable NavExceptionHandler mExceptionHandler;
	private static final String TAG = "Nav";
}

/** Demonstrate the usage of {@link Nav} */
@NonNullByDefault
class DemoActivity extends Activity {

	void openUri(final Uri uri) {
		Nav.from(this).toUri(uri);
	}

	void openUriWithinWebview(final Uri uri) {
		final Nav nav = Nav.from(this);
		// Try to resolve it in app scope.
		if (! nav.disallowLoopback().toUri(uri)) {
			// Try to resolve it in system scope.
			if (nav.skipPreprocess().allowEscape().toUri(uri)) {
				// Open it in current WebView.
				// ...
			}
		}
	}
}
