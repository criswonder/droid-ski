package com.taobao.android.hurdle;

import android.util.Log;

import com.taobao.android.base.Versions;

/** @author Oasis */
public class Hurdle {

	public static void block(final String tag, final String message) {
		if (Versions.isDebug())
			throw new HurdleBlockedError(message);
	}

	public static void warn(final String tag, final String message) {
		Log.w(tag, message);
		// TODO: Visual warning
	}
}

class HurdleBlockedError extends Error {

	public HurdleBlockedError(final String message) { super(message); }
	public HurdleBlockedError(final String message, final Throwable throwable) { super(message, throwable); }

	private static final long serialVersionUID = -6375344270185808756L;
}