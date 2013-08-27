package com.taobao.android.base;

import static android.content.pm.ApplicationInfo.FLAG_DEBUGGABLE;

import java.io.File;

import javax.annotation.NonNullByDefault;

import android.app.Application;
import android.content.pm.ApplicationInfo;
import android.os.Environment;

import com.taobao.android.ski.BuildConfig;
import com.taobao.android.task.Coordinator;
import com.taobao.android.task.Coordinator.TaggedRunnable;

/** @author Oasis */
@NonNullByDefault
public class Versions {

	public static boolean isDebug() {
		return DEBUG;
	}
	
	/** MTL Test Mode, this mode will logcat some performace info*/
	public static boolean isMTLMode(){
		return IOTMODE;
	}
	
	/** Get current process name*/
	public static String getCurrentProcessName() {
		return PROCESSNAME;
	}

    /** Call this method in Application.onCreate() */
    public static void init(final Application application) {
    	
    	Coordinator.runTasks(new TaggedRunnable("CheckMTLMode") { @Override public void run() {
    		StringBuilder sb = (new StringBuilder()).append(
    				Environment.getExternalStorageDirectory().toString())
    				.append(File.separator).append("MTL_IOT");
        	
    		File file = new File(sb.toString());
        	if(file.exists())
        		IOTMODE = true;
        	
        	PROCESSNAME = Tools.currentProcessName(application.getApplicationContext());
		}});

  	
        if (! DEBUG) return;
        // To workaround the unreliable "BuildConfig.DEBUG".
        //   See http://code.google.com/p/android/issues/detail?id=27940
        try {		// This will never be executed on RELEASE build, thus no performance issue.
            final ApplicationInfo app_info = application.getApplicationInfo();
            DEBUG = (app_info.flags & FLAG_DEBUGGABLE) != 0;
        } catch (final Exception e) {}      // Including NPE
    }

    private static boolean DEBUG = BuildConfig.DEBUG;
    
    private static boolean IOTMODE = false;
    
    private static String PROCESSNAME = null;
}
