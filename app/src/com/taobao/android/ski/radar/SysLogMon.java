package com.taobao.android.ski.radar;

import static android.Manifest.permission.READ_LOGS;
import static android.content.pm.PackageManager.PERMISSION_GRANTED;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.app.Instrumentation;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.taobao.android.ski.hud.Yell;

/** @author hwjump */
public class SysLogMon {

	private static final int threashold_skipframe = 5;
	
	public static class LogMoniterTask extends AsyncTask<Void, Void, Void> {

		private final Context mContext;
		private final String mPackageName;
		public LogMoniterTask(Context context, String packageName) {
			mContext = context;
			mPackageName = packageName;
		}

		@Override
		protected Void doInBackground(Void... params) {
			Process mLogcatProc = null;  
			BufferedReader reader = null;  
			try {
				int pid = -1;
				do {
					pid = getAppPid(mContext, mPackageName);
					if(pid == -1){
						Log.v(TAG, "taobao doesn't ready");
						try {
							Thread.sleep(500);
						}catch(InterruptedException e){
							
						}
					}
				}while(pid == -1);
				
				Log.v(TAG, "taobao pid:"+pid);
				
//				Runtime.getRuntime().exec(new String[] { "logcat","-c" }).waitFor();
				
				//{"logcat", "-d", "AndroidRuntime:E [Your Log Tag Here]:V *:S" });
				mLogcatProc = Runtime.getRuntime().exec(new String[] { "logcat","-d", "Choreographer:V *:S" });
//				mLogcatProc = Runtime.getRuntime().exec(new String[] { "logcat","-d"});
				reader = new BufferedReader(new InputStreamReader(mLogcatProc.getInputStream()));  
  
				
				String line;
      
				while (!bStoped) {
					
					line = reader.readLine();
					if(line == null){
//						Log.v(TAG, "null");
						try {
							Thread.sleep(100);
						}catch(InterruptedException e){
							
						}
						continue;
					}
					
					Log.v(TAG, line);

					int isk = line.indexOf("Skipped");
					int ifr = line.indexOf("frames");
					String frame;
					if(isk != -1 && ifr != -1){
						frame = line.substring(isk, ifr+7);
						mYell.showNotification(TAG, "skipframe", frame);
					}
					
					if(!isAppOnForeground(mContext, mPackageName)){
						Log.v(TAG, "appgoback or exit, stop monitor");
						break;
					}
				} 
  
			} catch (Exception e) {
  
				e.printStackTrace();  
			} finally {
				
				Log.v(TAG, "LogMoniterTask exit");
				try {
					if(reader != null)
						reader.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
				
				if(mLogcatProc != null)
					mLogcatProc.destroy();
			}
			
			return null;
		}
	}
	
	private static boolean isAppOnForeground(Context context, String monitorPackage) {
		
		// Returns a list of application processes that are running on the device
		List<RunningAppProcessInfo> appProcesses = mActivityManager.getRunningAppProcesses();
		if (appProcesses == null) {
			Log.d(TAG, "[isAppOnForeground] because app processes list is null, make app on background");
			return false;
		}

		for (RunningAppProcessInfo appProcess : appProcesses) {
			if (appProcess.processName.equals(monitorPackage)
					&& appProcess.importance == RunningAppProcessInfo.IMPORTANCE_FOREGROUND) {
//				Log.d(TAG, "[isAppOnForeground] app is on foreground");
				return true;
			}
		}
		Log.d(TAG, "[isAppOnForeground] app is on background");
		return false;
	}
	
	private static int getAppPid(Context params, String monitorPackage) {
		
		// Returns a list of application processes that are running on the device
		List<RunningAppProcessInfo> appProcesses = mActivityManager.getRunningAppProcesses();
		if (appProcesses == null) {
			Log.d(TAG, "[getAppPid] because app processes list is null, return invalide pid -1");
			return -1;
		}

		for (RunningAppProcessInfo appProcess : appProcesses) {
			if (appProcess.processName.equals(monitorPackage)) {
				
				Log.d(TAG, "[getAppPid] return pid");
				return appProcess.pid;
			}
		}
		Log.d(TAG, "[getAppPid] app not found return invalide pid -1");
		return -1;
	}
	
	public static void start(Instrumentation instruments) {
	    if (PERMISSION_GRANTED != instruments.getContext().getPackageManager().checkPermission(READ_LOGS, instruments.getContext().getPackageName()))
	        throw new IllegalStateException("Permission not granted: " + READ_LOGS);

	    mYell = Yell.get(instruments.getTargetContext());

	    mActivityManager = (ActivityManager) instruments.getContext().getSystemService(Context.ACTIVITY_SERVICE); 
	    
	    mContext = instruments.getContext();
	    
	    mLogTask = new LogMoniterTask(instruments.getContext(), "com.taobao.taobao");
	    mLogTask.execute();
	    
	    bStoped = false;
	    
	    try {
	    	final Class<?> clsPMS = Class.forName("android.os.SystemProperties");
	    	Method medgetboolean = clsPMS.getDeclaredMethod("getBoolean", String.class, boolean.class);
	    	boolean USE_VSYNC = (Boolean) medgetboolean.invoke(null, "debug.choreographer.vsync", true);
	    	boolean USE_FRAME_TIME = (Boolean) medgetboolean.invoke(null, "debug.choreographer.frametime", true);
	    	
	    	Method medgetint = clsPMS.getDeclaredMethod("getInt", String.class, int.class);
	    	int SKIPPED_FRAME_WARNING_LIMIT = (Integer) medgetint.invoke(null, "debug.choreographer.skipwarning", 30);
	    	
	    	Log.e(TAG, "USE_VSYNC:"+ USE_VSYNC);
	    	Log.e(TAG, "USE_FRAME_TIME:"+USE_FRAME_TIME);
	    	Log.e(TAG, "SKIPPED_FRAME_WARNING_LIMIT:"+SKIPPED_FRAME_WARNING_LIMIT);
	    	
	    	
	    	final Class<?> clsChoregrapher = Class.forName("android.view.Choreographer");
	    	
	    	Field f = clsChoregrapher.getDeclaredField("SKIPPED_FRAME_WARNING_LIMIT");
	    	f.setAccessible(true);
	    	f.setInt(null, threashold_skipframe);
	    	Log.e(TAG, "set SKIPPED_FRAME_WARNING_LIMIT:" + threashold_skipframe);

		} catch (Exception e) {
			Log.e(TAG, "Cannot get invoke method");
		}
	    
	}
	
	public static void stop() {
		bStoped = true;
	}
	
	private static final String TAG = "SysLog";
	private static Yell mYell;
	private static LogMoniterTask mLogTask;
	private static boolean bStoped = false;
	private static ActivityManager mActivityManager;
	private static Context mContext;

}
