package com.taobao.android.ski.gear;

import android.os.AsyncTask;

/** @author Oasis */
public abstract class SafeAsyncTask<Params, Result> extends AsyncTask<Params, Void, Result> {

	/** This method should be called in application initialization, to make sure
	 *  AsyncTask's internal handler is created on the main thread. */
	static final void init() { /* Do nothing besides static initialization. */ }

	public SafeAsyncTask(FragmentActivityCompat activity) {
		mActivity = activity;
	}

	@Override protected final void onPostExecute(Result result) {
		// Check activity status to avoid crash due to referencing activity which is no longer available.
		if (mActivity.isFinishing() || ((ActivityCompatJellyBean) mActivity).isDestroyed()) return;
		onResult(result);
	}

	protected abstract void onResult(Result result);

	private final FragmentActivityCompat mActivity;

	public static void execute(Runnable runnable) {
		Coordinator.getCurrentExecutor().execute(runnable);
	}
}
