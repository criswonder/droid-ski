package com.taobao.android.ski.hud;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.os.Bundle;
import android.os.Debug;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.ToggleButton;

import com.taobao.android.ski.Dock;
import com.taobao.android.ski.DockForStrictMode;
import com.taobao.android.ski.R;

/** @author Oasis */
public class LaunchPad extends Activity {

	private Context mContext;
	@Override protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.launchpad);
		findViewById(R.id.launch_timing).setOnClickListener(new OnClickListener() { @Override public void onClick(View v) {
			start(Dock.FLAG_LAUNCH_TIMING);
		}});
		findViewById(R.id.launch_profiling).setOnClickListener(new OnClickListener() { @Override public void onClick(View v) {
			start(Dock.FLAG_LAUNCH_PROFILING);
		}});

		mContext = this;
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();


	}

	private void start(int flags) {
		//activity leak switch
		ToggleButton actleakbut = (ToggleButton)findViewById(R.id.activityleakbutton);
		if(actleakbut.isChecked()) {
			flags = flags | Dock.FLAG_MONITOR_ACTIVITY_LEAK;
		}

		//activity perf switch
		ToggleButton actbut = (ToggleButton)findViewById(R.id.activityperfbutton);
		if(actbut.isChecked()) {
			flags = flags | Dock.FLAG_MONITOR_ACTIVITY_PERF;
		}

		//animation perf switch
		ToggleButton anibut = (ToggleButton)findViewById(R.id.animationperfbutton);
		if(anibut.isChecked()) {
			flags = flags | Dock.FLAG_MONITOR_ANIMATION_PERF;
		}

		Bundle options = new Bundle();
		options.putInt(Dock.KEY_FLAGS, flags);
		if (Debug.isDebuggerConnected()) options.putBoolean(Dock.KEY_ATTACH_DEBUGGER, true);

		//am_lanch_time threashold
		if(actbut.isChecked()) {
			EditText launch_time = (EditText)findViewById(R.id.launch_time);
			String thd = launch_time.getText().toString().trim();
			try {
				int thdint = Integer.parseInt(thd);
				options.putInt(Dock.KEY_ACTIVITY_LAUNCH_TIME, thdint);
			} catch (NumberFormatException e){
				options.putInt(Dock.KEY_ACTIVITY_LAUNCH_TIME, 500);
			}
		}

		EditText threashold = (EditText)findViewById(R.id.edit_content);
		String thd = threashold.getText().toString().trim();
		try {
			int thdint = Integer.parseInt(thd);
			options.putInt(Dock.KEY_THREASHOLD, thdint);
		} catch (NumberFormatException e){
			options.putInt(Dock.KEY_THREASHOLD, 5);
		}

		ToggleButton tbut = (ToggleButton)findViewById(R.id.strictmodebutton);
		boolean result = false;
		try {
			if (tbut.isChecked()) {
				result = startInstrumentation(new ComponentName(this, DockForStrictMode.class), null, options);
			} else {
				result = startInstrumentation(new ComponentName(this, Dock.class), null, options);
			}
		} catch (SecurityException e) {
			new AlertDialog.Builder(mContext).setTitle("Error")
					.setMessage("Signature mismatch. Please ensure target app is signed with the same certificate as me.")
					.show();
		}

		if (! result) throw new LinkageError("Instrumentation is not correctly configured. May be your target app didn't installed");
	}
}
