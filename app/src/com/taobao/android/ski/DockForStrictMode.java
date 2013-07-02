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

/** @author baiyi */
public class DockForStrictMode extends Dock {

	public DockForStrictMode() {
		super();

		Log.e(TAG, "Create dock ex");
		StrictModeMon.start(this);
	}

	@Override public void onCreate(final Bundle arguments) {
		
        // Bind to the service
		Intent intent = new Intent();
		intent.setComponent(new ComponentName("com.taobao.android.ski", "com.taobao.android.ski.rose.RoseService"));
		this.getTargetContext().bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
		
		super.onCreate(arguments);
	}

	@Override public void onStart() {		

//		StrictModeMon.init(this);
		super.onStart();
//		StrictModeMon.stop();
	}

	@Override
	public void onDestroy() {
		
		this.getContext().unbindService(mConnection);
		Log.v("tag", "onDestroy");
		super.onDestroy();
	}

    /** Messenger for communicating with the service. */
    Messenger mService = null;

    /** Flag indicating whether we have called bind on the service. */
    boolean mBound;

    /**
     * Class for interacting with the main interface of the service.
     */
	private ServiceConnection mConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder service) {
            // This is called when the connection with the service has been
            // established, giving us the object we can use to
            // interact with the service.  We are communicating with the
            // service using a Messenger, so here we get a client-side
            // representation of that from the raw IBinder object.
            mService = new Messenger(service);
            mBound = true;
        }

        public void onServiceDisconnected(ComponentName className) {
            // This is called when the connection with the service has been
            // unexpectedly disconnected -- that is, its process crashed.
            mService = null;
            mBound = false;
        }
    };
	
    public void sayHello() {
        if (!mBound) return;
        // Create and send a message to the service, using a supported 'what' value
        Message msg = Message.obtain();
        msg.what = 2;
        msg.arg1 = 2;
        Bundle bundle = new Bundle();
        bundle.putString("Text", "hello android!");
        msg.obj = bundle;
        try {
            mService.send(msg);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }
    
	
	@Override public void callActivityOnResume(Activity activity) {
		super.callActivityOnResume(activity);
	}

	private static final String TAG = "DockForStrictMode";
}
