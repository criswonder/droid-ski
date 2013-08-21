package com.taobao.android.platform;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Build;
import android.util.Log;

/** @author Oasis */
public class GoogleServices {

	public static int KCapGoogleServicesFramework = 1;
	public static int KCapGoogleCloudMessaging = 2;
	public static int KCapGoogleAccount = 4;
	public static int KCapGooglePlayStore = 8;
	public static int KCapGooglePlayServices = 16;

	public static int queryAllCapabilities(Context context) {
		int capabilities = 0;
        PackageManager pm = context.getPackageManager();
        try {
        	pm.getPackageInfo("com.google.android.gsf", 0);
        	capabilities |= KCapGoogleServicesFramework;

        	if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1)
            	capabilities |= KCapGoogleCloudMessaging;		// 4.0.4+ with GSF (no Google account needed)
            @SuppressLint("InlinedApi")
	        AccountManager am = (AccountManager) context.getSystemService(Context.ACCOUNT_SERVICE);
	        Account[] accounts = am.getAccountsByType("com.google");
	        if (accounts.length > 0) {
	        	capabilities |= KCapGoogleAccount;
	        	if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.FROYO)
	        		capabilities |= KCapGoogleCloudMessaging;	// 2.2+ with GSF and Google account
	        }
        } catch (NameNotFoundException e) {
        } catch (SecurityException e) {
		} catch (RuntimeException e) {
			Log.w(TAG, "Failed to query capabilities of Google services.", e);
		}

        try {
        	pm.getPackageInfo("com.android.vending", 0);
        	capabilities |= KCapGooglePlayStore;
        } catch (NameNotFoundException e) {}

        try {
        	pm.getPackageInfo("com.google.android.gms", 0);
        	capabilities |= KCapGooglePlayServices;
        } catch (NameNotFoundException e) {}
        return capabilities;
	}

	public static int queryGooglePlayServicesVersion(Context context) {
        try {
        	PackageInfo info = context.getPackageManager().getPackageInfo("com.google.android.gms", 0);
        	return info.versionCode;
        } catch (NameNotFoundException e) { return 0; }
	}

	private static final String TAG = "GoogleSvcCap";
}
