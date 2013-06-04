package com.taobao.android.ski.hud;

import android.app.Activity;
import android.content.ComponentName;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;

import com.taobao.android.ski.Dock;
import com.taobao.android.ski.R;

/** @author Oasis */
public class LaunchPad extends Activity {

	@Override protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.launchpad);
		findViewById(R.id.launch_timing).setOnClickListener(new OnClickListener() { @Override public void onClick(View v) {
			start(Dock.FLAG_LAUNCH_TIMING);
		}});
		findViewById(R.id.launch_profiling).setOnClickListener(new OnClickListener() { @Override public void onClick(View v) {
			start(Dock.FLAG_LAUNCH_PROFILING);
		}});
	}

	private void start(int flags) {
		Bundle options = new Bundle();
		options.putInt(Dock.KEY_FLAGS, flags);
		boolean result = startInstrumentation(new ComponentName(this, Dock.class), null, options);
		if (! result) throw new LinkageError("Instrumentation is not correctly configured.");
	}
}
