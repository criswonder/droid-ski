/*
 * Copyright (C) 2007 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.taobao.android.ski.rose;


import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.graphics.Rect;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;

public class RoseService extends Service {
    
	private static final int UPDATE_MSG = 1;
	
	private LoadView mView;
    
    public class LoadView extends View  implements RoseStatsChange{
        private Handler mHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                if (msg.what == UPDATE_MSG) {
                    updateDisplay();
                }
            }
        };
        
    	@Override
    	public void notifyDataSetChange() {
            Message m = mHandler.obtainMessage(UPDATE_MSG);
            mHandler.sendMessage(m);
    	}

    	@Override
    	public Rect getTextRect(String text) {
    		Rect rect = new Rect();  
    		mNormalPaint.getTextBounds(text, 0, text.length(), rect);  
    		int w = rect.width();  
    		int h = rect.height();  
    		Log.d(TAG, "w=" +w+"  h="+h);
    		
    		return rect;
    	}
        
        private Paint mNormalPaint;
        private Paint mShadowPaint;
        private Paint mShadow2Paint;
        
        private Paint mErrorPaint;
        private Paint mWarnPaint;

        private int mNeededWidth;
        private int mNeededHeight;

        LoadView(Context c) {
            super(c);

            setPadding(4, 4, 4, 4);
            
            // Need to scale text size by density...  but we won't do it
            // linearly, because with higher dps it is nice to squeeze the
            // text a bit to fit more of it.  And with lower dps, trying to
            // go much smaller will result in unreadable text.
            int textSize = 10;
            float density = c.getResources().getDisplayMetrics().density;
            if (density < 1) {
                textSize = 9;
            } else {
                textSize = (int)(10*density);
                if (textSize < 10) {
                    textSize = 10;
                }
            }
            mNormalPaint = new Paint();
            mNormalPaint.setAntiAlias(true);
            mNormalPaint.setTextSize(textSize);
            mNormalPaint.setARGB(255, 255, 255, 255);

            mErrorPaint = new Paint();
            mErrorPaint.setAntiAlias(true);
            mErrorPaint.setTextSize(textSize);
            mErrorPaint.setARGB(255, 128, 255, 128);

            mWarnPaint = new Paint();
            mWarnPaint.setAntiAlias(true);
            mWarnPaint.setStrikeThruText(true);
            mWarnPaint.setTextSize(textSize);
            mWarnPaint.setARGB(255, 255, 128, 128);

            mShadowPaint = new Paint();
            mShadowPaint.setAntiAlias(true);
            mShadowPaint.setTextSize(textSize);
            //mShadowPaint.setFakeBoldText(true);
            mShadowPaint.setARGB(192, 0, 0, 0);
            mNormalPaint.setShadowLayer(4, 0, 0, 0xff000000);

            mShadow2Paint = new Paint();
            mShadow2Paint.setAntiAlias(true);
            mShadow2Paint.setTextSize(textSize);
            //mShadow2Paint.setFakeBoldText(true);
            mShadow2Paint.setARGB(192, 0, 0, 0);
            mNormalPaint.setShadowLayer(2, 0, 0, 0xff000000);

            RoseStatsCenter.instance().init();
            RoseStatsCenter.instance().setRoseStatsChange(this);
            updateDisplay();
        }

        @Override
        protected void onAttachedToWindow() {
            super.onAttachedToWindow();
            mHandler.sendEmptyMessage(UPDATE_MSG);
        }

        @Override
        protected void onDetachedFromWindow() {
            super.onDetachedFromWindow();
            mHandler.removeMessages(UPDATE_MSG);
        }

        @Override
        protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
            setMeasuredDimension(resolveSize(mNeededWidth, widthMeasureSpec),
                    resolveSize(mNeededHeight, heightMeasureSpec));
        }

        @Override
        public void onDraw(Canvas canvas) {
        	
            super.onDraw(canvas);
            final int W = mNeededWidth;
            final int RIGHT = getWidth()-1;

            int x = RIGHT - 4;
            int top = 4 + 2;
            int bottom = 4 + 2;

            int y = 4;
            canvas.drawText("hahah", RIGHT-4-100-1,
                    y-1, mShadowPaint);
            canvas.drawText("hahah", RIGHT-4-100-1,
                    y+1, mShadowPaint);
            canvas.drawText("hahah", RIGHT-4-100+1,
                    y-1, mShadow2Paint);
            canvas.drawText("hahah", RIGHT-4-100+1,
                    y+1, mShadow2Paint);
            canvas.drawText("hahah", RIGHT-4-100,
                    y, mNormalPaint);
        }

        void updateDisplay() {
        	
            int maxWidth = 100;
            int maxHeight = 100;
            
            int neededWidth = 4 + 4 + maxWidth;
            int neededHeight = 4 + 4 + maxHeight;
            if (neededWidth != mNeededWidth || neededHeight != mNeededHeight) {
                mNeededWidth = neededWidth;
                mNeededHeight = neededHeight;
                requestLayout();
            } else {
                invalidate();
            }
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        
        Log.v(TAG, "onCreate");
        mView = new LoadView(this);
        WindowManager.LayoutParams params = new WindowManager.LayoutParams(
        	WindowManager.LayoutParams.MATCH_PARENT,
        	WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.TYPE_SYSTEM_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE|
            WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
            PixelFormat.RGBA_8888);
        params.gravity = Gravity.RIGHT | Gravity.TOP;
        params.setTitle("Ski Debug");
        WindowManager wm = (WindowManager)getSystemService(WINDOW_SERVICE);
        wm.addView(mView, params);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        ((WindowManager)getSystemService(WINDOW_SERVICE)).removeView(mView);
        mView = null;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
    
    private final static String TAG = "RoseService";

}
