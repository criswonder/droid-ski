package com.taobao.android.ski.hud;

import java.io.File;
import java.io.IOException;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;

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
		
		EditText threashold = (EditText)findViewById(R.id.edit_content);
		String thd = threashold.getText().toString().trim();
		try {
			int thdint = Integer.parseInt(thd);
			options.putInt(Dock.KEY_THREASHOLD, thdint);
		} catch (NumberFormatException e){
			options.putInt(Dock.KEY_THREASHOLD, 5);
		}
		
		boolean result = startInstrumentation(new ComponentName(this, Dock.class), null, options);
		if (! result) throw new LinkageError("Instrumentation is not correctly configured.");
	}
}
