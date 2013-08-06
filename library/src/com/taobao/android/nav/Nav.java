package com.taobao.android.nav;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

/** @author Oasis */
public class Nav {

	public static Nav from(Context context) {
		return new Nav(context);
	}

	public Nav with(Bundle extras) {
		mExtras = extras;
		return this;
	}

	public Intent intent(Uri uri) {
		Intent intent = new Intent(Intent.ACTION_VIEW, uri).setPackage(mContext.getPackageName());
		if (mExtras != null) intent.putExtras(mExtras);
		return intent;
	}

	public void toActivity(Uri uri) {
		mContext.startActivity(intent(uri));
	}

	private Nav(Context context) {
		mContext = context;
	}

	private final Context mContext;
	private Bundle mExtras;

	static class DemoActivity extends Activity {

		void startActivity(Uri uri) {
			Nav.from(this).toActivity(uri);
		}

		void startActivity(Uri uri, Bundle extra) {
			Nav.from(this).with(extra).toActivity(uri);
		}
	}
}