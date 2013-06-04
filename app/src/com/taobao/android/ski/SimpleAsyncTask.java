package com.taobao.android.ski;

import android.app.Activity;
import android.os.AsyncTask;

/** @author Oasis */
public abstract class SimpleAsyncTask<Params, Result> extends AsyncTask<Params, Void, Result> {

	public SimpleAsyncTask(Activity activity) {
		mActivity = activity;
	}

	@Override protected final void onPostExecute(Result result) {
		if (mActivity.isFinishing()) return;
		onResult(result);
	}

	protected abstract void onResult(Result result);

	private final Activity mActivity;
}
