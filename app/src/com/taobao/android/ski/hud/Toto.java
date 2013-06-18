package com.taobao.android.ski.hud;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;

public class Toto {

	public static Toto get(final Context context) {
		if (sInstance == null)
			sInstance = new Toto(context.getApplicationContext());
		return sInstance;
	}

    public void showToast(String text){

    	mHandler.postDelayed(new showTototo(text), 100);
    }
    
    private class showTototo implements Runnable {

    	private String mText;
    	public showTototo(String text){
    		mText = text;
    	}
    	
		@Override
		public void run() {			
			Toast.makeText(mContext, mText, 5000).show();
		}
    	
    }

	private Toto(final Context context) {
		mContext = context;
		mHandler = new Handler(Looper.getMainLooper());
	}

	private final Context mContext;
	private final Handler mHandler;

	private static Toto sInstance;
}
