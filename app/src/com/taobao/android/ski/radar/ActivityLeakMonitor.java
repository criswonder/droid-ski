package com.taobao.android.ski.radar;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

import android.app.Activity;
import android.content.Context;
import android.os.Debug;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.taobao.android.ski.hud.Yell;


public class ActivityLeakMonitor {
	
	private final static String TAG = "ActivityLeakMonitor";

	private final static int ACTIVITY_MONITOR_INTVL = 15000;
		
	private static Context mAppContext;
	private static Handler mHandler;
	
	private static String mTimeForName;

	private static Yell mYell;
	
	public static void install(final Context context){

		mAppContext = context.getApplicationContext();
		mYell = Yell.get(context);
		
		mHandler = new Handler(Looper.getMainLooper());
		Date date = new Date();
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat(
				"yyyy-MM-dd-HH-mm-ss"); // HH:mm:ss
		mTimeForName = simpleDateFormat.format(date);		
	}
	
	public static void addDestroyedActivity(Activity activity){
		WeakReference<Object> ref = new WeakReference<Object>(activity); 
		Log.d(TAG, "add destroyed activity "+activity.toString());
		mHandler.postDelayed(new CheckPearl(ref), ACTIVITY_MONITOR_INTVL);
		
		Log.d(TAG, "call GC");
		System.gc();
		System.gc();
	}
	
	private static class CheckPearl implements Runnable {

		public WeakReference<Object> mObj;
		public String mIdentifier;
		public CheckPearl(WeakReference<Object> ref) {
			this.mObj = ref;
			this.mIdentifier = mObj.get().toString();
		}
		
		@Override
		public void run() {

			Object ss = mObj.get();
			if(ss == null){
				Log.d(TAG, mIdentifier + "has bean cycle by gc");
			} else {
				Log.d(TAG, mIdentifier + "was still occupied, dump");
				
				mYell.showNotification(TAG, mIdentifier, "has bean leaked");

			    new Thread(new LeakActivityDumpThread(mIdentifier)).start();
			}
		}
	}

	static class LeakActivityDumpThread implements Runnable{
		private String leakActivity;
		public LeakActivityDumpThread(String leakActivitys){
			this.leakActivity = leakActivitys;
		}
		@Override
		public void run() {
			String state = Environment.getExternalStorageState();
	        if (state != null && state.equals(android.os.Environment.MEDIA_MOUNTED)){
	        	String sdcard = Environment.getExternalStorageDirectory().toString();
	        	File fileDir = new File(sdcard,"MemoryMonitor/"+mAppContext.getPackageName()+"/leak_activity/"+ mTimeForName + "/" + leakActivity);
	        	fileDir.mkdirs();
	        	
	        	try {
	        		File hprofFile = new File(fileDir.getAbsolutePath(),"com.taobao.taobao.hprof");
	        		hprofFile.delete();
					Debug.dumpHprofData(hprofFile.getAbsolutePath());
				} catch (Exception e) {
					e.printStackTrace();
				}
	        }
		}
	}
}
