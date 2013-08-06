package com.taobao.android.nav;

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

	public static Nav from(Context context) {
		return new Nav(context);
	}

	public Nav with(Bundle extras) {
		mExtras = extras;
		return this;
	}

	public Intent to(Uri uri) {
		Intent intent = new Intent(Intent.ACTION_VIEW, uri).setPackage(mContext.getPackageName());
		if (mExtras != null) intent.putExtras(mExtras);
		return intent;
	}

	private Nav(Context context) {
		mContext = context;
	}

	private final Context mContext;
	private Bundle mExtras;

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