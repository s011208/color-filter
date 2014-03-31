
package com.yenhsun.colorfilter;

import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

public class ColorFilterPanelService extends Service {
    private WindowManager mWm;

    private WindowManager.LayoutParams mWmParams;

    private int mScreenWidth, mScreenHeight;

    private View mColorFilterPanel;

    private static final boolean DEBUG = MainActivity.DEBUG;

    private static final String TAG = MainActivity.TAG;

    private BroadcastReceiver mReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            // TODO Auto-generated method stub
            if (intent == null)
                return;
            if (intent.getAction().equals(Intent.ACTION_SCREEN_ON)) {
                boolean enable = getSharedPreferences(MainActivity.SHARF_FILE_NAME, 0).getBoolean(
                        MainActivity.SHARF_KEY_ENABLE_FILTER, false);
                int filterColor = getSharedPreferences(MainActivity.SHARF_FILE_NAME, 0).getInt(
                        MainActivity.SHARF_KEY_FILTER_COLOR, MainActivity.DEFAULT_FILTER_COLOR);
                if (enable) {
                    try {
                        mWm.removeView(mColorFilterPanel);
                    } catch (Exception e) {// ignore
                    }
                    mWm.addView(mColorFilterPanel, mWmParams);
                    mColorFilterPanel.setBackgroundColor(filterColor);
                } else {
                    try {
                        mWm.removeView(mColorFilterPanel);
                    } catch (Exception e) {// ignore
                    }
                }
            }
        }
    };

    public ColorFilterPanelService() {
    }

    public void onCreate() {
        super.onCreate();
        if (DEBUG)
            Log.d(TAG, "oncreate");
        mScreenWidth = getResources().getDisplayMetrics().widthPixels;
        mScreenHeight = getResources().getDisplayMetrics().heightPixels;
        mWm = (WindowManager)getSystemService("window");
        mWmParams = getWindowManagerParamsSettings();
        mColorFilterPanel = new View(this);
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_SCREEN_ON);
        this.registerReceiver(mReceiver, filter);
    }

    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        if (mWm != null && mColorFilterPanel != null && mWmParams != null) {
            boolean enable = getSharedPreferences(MainActivity.SHARF_FILE_NAME, 0).getBoolean(
                    MainActivity.SHARF_KEY_ENABLE_FILTER, false);
            int filterColor = getSharedPreferences(MainActivity.SHARF_FILE_NAME, 0).getInt(
                    MainActivity.SHARF_KEY_FILTER_COLOR, MainActivity.DEFAULT_FILTER_COLOR);
            if (intent != null && intent.getExtras() != null) {
                boolean callFromNotification = intent.getExtras().getBoolean(
                        MainActivity.CHANG_STATE_FROM_NOTIFICATION, false);
                if (callFromNotification) {
                    enable = !enable;
                    getSharedPreferences(MainActivity.SHARF_FILE_NAME, 0).edit()
                            .putBoolean(MainActivity.SHARF_KEY_ENABLE_FILTER, enable).commit();
                    MainActivity.handleSimpleNotification(this);
                }
            }
            if (enable) {
                try {
                    mWm.removeView(mColorFilterPanel);
                } catch (Exception e) {// ignore
                }
                mWm.addView(mColorFilterPanel, mWmParams);
                mColorFilterPanel.setBackgroundColor(filterColor);
            } else {
                try {
                    mWm.removeView(mColorFilterPanel);
                } catch (Exception e) {// ignore
                }
            }
            Toast.makeText(this, enable ? R.string.toast_show_filter : R.string.toast_hide_filter,
                    Toast.LENGTH_SHORT).show();
        }
        SwithWidget.performUpdate(this);
        return Service.START_STICKY;
    }

    public void onDestroy() {
        super.onDestroy();
    }

    public int getStatusBarHeight() {
        int result = 0;
        int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            result = getResources().getDimensionPixelSize(resourceId);
        }
        return result;
    }

    public int getNavigationBarHeight() {
        int result = 0;
        int resourceId = getResources().getIdentifier("navigation_bar_height", "dimen", "android");
        if (resourceId > 0) {
            result = getResources().getDimensionPixelSize(resourceId);
        }
        return result;
    }

    public WindowManager.LayoutParams getWindowManagerParamsSettings() {
        WindowManager.LayoutParams params = new WindowManager.LayoutParams();
        int statusBarHeight = getStatusBarHeight();
        int deviceHeight = mScreenHeight + statusBarHeight + getNavigationBarHeight();
        params.x = 0;
        params.y = -statusBarHeight;
        params.height = deviceHeight + 200;
        params.width = WindowManager.LayoutParams.MATCH_PARENT;
        params.type = WindowManager.LayoutParams.TYPE_PHONE;
        params.format = PixelFormat.RGBA_8888;
        params.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
                | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                | WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
                | WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS;
        params.gravity = Gravity.CENTER;
        params.horizontalWeight = 0;
        params.verticalWeight = 0;
        params.windowAnimations = android.R.style.Animation_Toast;
        params.softInputMode = WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN;

        return params;
    }

    private final IColorChanged.Stub mBinder = new IColorChanged.Stub() {

        @Override
        public void changeBackground(int color) throws RemoteException {
            // TODO Auto-generated method stub
            mColorFilterPanel.setBackgroundColor(color);
        }
    };

    @Override
    public IBinder onBind(Intent arg0) {
        // TODO Auto-generated method stub
        return mBinder;
    }

}
