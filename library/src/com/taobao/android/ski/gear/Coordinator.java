package com.taobao.android.ski.gear;

/** @deprecated Moved to package com.taobao.android.task */
@Deprecated public class Coordinator extends com.taobao.android.task.Coordinator {
	
	@Deprecated
	public static abstract class TaggedRunnable extends com.taobao.android.task.Coordinator.TaggedRunnable {

		public TaggedRunnable(String tag) {
			super(tag);
		}
	}
}
