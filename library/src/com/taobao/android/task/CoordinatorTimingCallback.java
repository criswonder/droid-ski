package com.taobao.android.task;


/** @author baiyi */
public interface CoordinatorTimingCallback {

	/** call back to app the timing data  */
	public void onTimingCallback(String name, long cpu, long real);
}
