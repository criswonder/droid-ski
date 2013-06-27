package com.taobao.android.ski.radar;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;

import android.app.Instrumentation;
import android.content.Context;
import android.os.AsyncTask;
import android.os.DropBoxManager;
import android.os.StrictMode;
import android.util.Log;

import com.google.dexmaker.DexMaker;
import com.google.dexmaker.TypeId;
import com.google.dexmaker.stock.ProxyBuilder;

/** @author hwjump */
public class StrictModeMon {

	public static class StrictModeTask extends AsyncTask<Void, Void, Void> {

		
//		public StrictModeTask(Context context){
//			
//		}

		@Override
		protected Void doInBackground(Void... params) {
			
//			DropBoxManager dropbox = (DropBoxManager) mInstrumentation.getTargetContext().getSystemService(Context.DROPBOX_SERVICE);
//			dropbox.getNextEntry(tag, msec)
//			
//			dropbox.
//			
			return null;
		}
	}
	
	
	public static void start(Instrumentation instr) {

		StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
				.detectAll().penaltyFlashScreen().build());

		StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder()
				.detectAll().penaltyLog().penaltyDeath().build());

//		new StrictModeTask().execute();
		// todo:
		// mInstrumentation = instr;
		// init();
	}

	public static void init(Instrumentation instr) {

		mInstrumentation = instr;
		InvocationHandler handler = new InvocationHandler() {

			@Override
			public Object invoke(Object proxy, Method method, Object[] args)
					throws Throwable {

				if (method.getName().equals("nextInt")) {
					// Chosen by fair dice roll, guaranteed to be random.
					return 4;
				}
				Object result = ProxyBuilder.callSuper(proxy, method, args);
				Log.v("StrictModeMon", "Method: " + method.getName()
						+ " args: " + Arrays.toString(args) + " result: "
						+ result);
				return result;
			}
		};

		try {

			DexMaker dexMaker = new DexMaker();
			TypeId<?> AndroidBlockGuardPolicyEx = TypeId
					.get("Landroid/os/StrictMode$AndroidBlockGuardPolicyEx;");
			
			Class<?> androidBlockGuardPolicy = Class
					.forName("android.os.StrictMode$AndroidBlockGuardPolicy");
			
			TypeId<?> AndroidBlockGuardPolicy = TypeId
					.get(androidBlockGuardPolicy);

			dexMaker.declare(AndroidBlockGuardPolicyEx,
					"AndroidBlockGuardPolicy.generated", Modifier.PUBLIC,
					AndroidBlockGuardPolicy);
			File outputDir = mInstrumentation.getTargetContext().getDir("dx",
					Context.MODE_PRIVATE);

			ClassLoader loader = dexMaker.generateAndLoad(
					AndroidBlockGuardPolicy.getClass().getClassLoader(),
					outputDir);
			Class<?> AndroidBlockGuardPolicyExClass = loader
					.loadClass("android.os.StrictMode$AndroidBlockGuardPolicyEx");



			File file = mInstrumentation.getTargetContext().getDir("dx",
					Context.MODE_PRIVATE);

			file.createNewFile();

			Class<?> polocy = (Class<?>) ProxyBuilder
					.forClass(androidBlockGuardPolicy).dexCache(file)
					.handler(handler).build();

			// Constructor<?> construct = polocy.getConstructor(int.class);
			//
			// //StrictMode.setThreadPolicy((ThreadPolicy)construct.newInstance(0xff));
			//
			//
			// Class<?> blockGuard = Class
			// .forName("dalvik.system.BlockGuard");
			//
			// Method med = blockGuard.getMethod("setThreadPolicy",
			// AndroidBlockGuardPolicy.getClasses());
			//
			// med.invoke(null, construct.newInstance(0xff));

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public static void stop() {

	}

	private static Instrumentation mInstrumentation;

}
