package com.taobao.android.compat;

public interface ActivityCompatJellyBean {

	/**
	 * Provide this method for API level ~16.
	 *
	 * <p>Note: It should <b>NEVER</b> be used in {@link Fragment#onDestroy()},
	 *   since the return value may be different from the one in API level 17.
	 */
    boolean isDestroyed();
}
