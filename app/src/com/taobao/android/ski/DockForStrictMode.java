package com.taobao.android.ski;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;

import com.taobao.android.ski.radar.StrictModeMon;
import com.taobao.android.ski.rose.RoseService;
import com.taobao.android.ski.rose.RoseServiceConnection;

/** @author baiyi */
public class DockForStrictMode extends Dock {

	public DockForStrictMode() {
		super();

		Log.e(TAG, "Create dock ex");
		StrictModeMon.start(this);
	}

	@Override public void onCreate(final Bundle arguments) {
		
		RoseServiceConnection.bindRoseService(this.getContext());
		super.onCreate(arguments);
	}

	@Override public void onStart() {		

//		StrictModeMon.init(this);
		super.onStart();
//		StrictModeMon.stop();
	}

	@Override
	public void onDestroy() {
		
		RoseServiceConnection.unbindRoseService(this.getContext());
		super.onDestroy();
	}

	@Override public void callActivityOnResume(Activity activity) {
		super.callActivityOnResume(activity);
	}

	private static final String TAG = "DockForStrictMode";
}
