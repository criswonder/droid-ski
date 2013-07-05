/*
 * Copyright (C) 2007 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.taobao.android.ski.rose;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;



public class RoseServiceConnection {
    
	public static void bindRoseService(Context context) {
        // Bind to the service
		Intent intent = new Intent();
		intent.setComponent(new ComponentName("com.taobao.android.ski", "com.taobao.android.ski.rose.RoseService"));
		context.bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
	}
	
	public static void unbindRoseService(Context context) {
	    
		context.unbindService(mConnection);
	}

    /** Messenger for communicating with the service. */
	static Messenger mService = null;

    /** Flag indicating whether we have called bind on the service. */
	static boolean mBound;

    /**
     * Class for interacting with the main interface of the service.
     */
	private static ServiceConnection mConnection = new ServiceConnection() {
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
    
    public static void helloRose(int level, String name, String content) {
        if (!mBound) return;
	        // Create and send a message to the service, using a supported 'what' value
	        Message msg = Message.obtain();
	        msg.what = RoseService.SEND_MSG;
	        msg.arg1 = RoseService.ROSE_LEVEL_VERBOSE;
	        Bundle bundle = new Bundle();
	        bundle.putString("Name", name);
	        bundle.putString("Content", content);
	        msg.obj = bundle;
        try {
            mService.send(msg);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }
	
    private final static String TAG = "RoseService";

}
