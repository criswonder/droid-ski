
package com.taobao.android.ski.rose;

import android.graphics.Rect;

public interface RoseStatsChange {
	
    public void notifyDataSetChange();

    public Rect getTextRect(String text);
}
