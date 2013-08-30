package com.taobao.android.nav;

import javax.annotation.NonNullByDefault;
import javax.annotation.Nullable;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

/** @author Oasis */
@NonNullByDefault
public class NavRouterActivity extends Activity {

	@Override protected void onCreate(final @SuppressWarnings("null") Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		finish();		// We don't need this activity afterward
		final Intent intent = getIntent();
		Uri uri = intent.getData();
		if (uri == null || ! uri.isHierarchical()) return;
		uri = processUri(uri);
		if (uri == null) return;
		Nav.from(this).disallowLoopback().toUri(uri);
	}

	/**
	 * Override this method to provide custom conversion rules.
	 *
	 * @return the converted URI, or null to abort the default navigation.
	 */
	@SuppressWarnings("static-method")		// Supposed to be overridden
	protected @Nullable Uri processUri(final Uri uri) {
		final String fragment = uri.getFragment();
		if (fragment != null && fragment.startsWith("!/"))
			return uri.buildUpon().fragment(null).path(fragment.substring(1)).build();
		return null;
	}
}
