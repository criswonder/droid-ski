package com.taobao.android.ski.hud;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.view.Gravity;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

public class Toto {

	public static Toto get(final Context context) {
		if (sInstance == null)
			sInstance = new Toto(context.getApplicationContext());
		return sInstance;
	}

    /*
     * 从布局文件中加载布局并且自定义显示Toast
     */
    public void showCustomToast(String text){

    	mRelativeLayout.setBackgroundColor(0xff0000);
    	RelativeLayout.LayoutParams rlp = new RelativeLayout.LayoutParams(200, 100);
    	TextView tv = new TextView(mContext);
    	tv.setTextColor(0xffffff);
    	tv.setText(text);
    	mRelativeLayout.addView(tv, rlp);
    	mHandler.postDelayed(showToto, 100);
    }
    
	private Runnable showToto = new Runnable() { 
		
		@Override 
		public void run() {
			Toast toast = new Toast(mContext);
			toast.setDuration(Toast.LENGTH_LONG);
			toast.setGravity(Gravity.CENTER_VERTICAL, 0, 0);
			toast.setView(mRelativeLayout);
			toast.show();
		}
	};
	

	private Toto(final Context context) {
		mContext = context;
		mHandler = new Handler(Looper.getMainLooper());
		mRelativeLayout = new RelativeLayout(mContext);
	}

	private final Context mContext;
	private final Handler mHandler;
	private final RelativeLayout mRelativeLayout;

	private static Toto sInstance;
}
