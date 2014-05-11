
package com.yenhsun.colorfilter;

import java.lang.reflect.Method;

import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.Spinner;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.purplebrain.adbuddiz.sdk.AdBuddiz;

public class MainActivity extends Activity implements OnCheckedChangeListener,
        SeekBar.OnSeekBarChangeListener, Spinner.OnItemSelectedListener {
    public static final String SHARF_FILE_NAME = "filter_sharf";

    public static final String SHARF_KEY_ENABLE_FILTER = "enable_filter_sharf";

    public static final String SHARF_KEY_FILTER_COLOR = "filter_color_sharf";

    public static final String SHARF_KEY_SHOW_NOTIFICATION = "show_notification_sharf";

    public static final String CHANG_STATE_FROM_NOTIFICATION = "change_state_from_notification";

    public static final String FILTER_COLOR = "filter_color";

    public static final boolean DEBUG = false;

    public static final String TAG = "Colorfilter";

    public static final int DEFAULT_FILTER_COLOR = 0x30666600;

    public static final int CUSTOMIZED_COLOR = 4;

    private RadioGroup mEnableGroup;

    private SharedPreferences mSharf;

    private SeekBar mAlphaSeekBar, mRedSeekBar, mGreenSeekBar, mBlueSeekBar;

    private CheckBox mShowNotiCb;

    private AdView mAdView;

    private Spinner mRecommandColorSpinner;

    private FrameLayout mMainLayout;

    private boolean mIsForeGround = false;

    private int mFilterColor;

    private static final int NOTIFICATION_ID = MainActivity.class.hashCode();

    private static final String NOTIFICATION_TAG = "COLOR_FILTER_NOTIFICATION";

    private IColorChanged mService;

    private ServiceConnection mConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder service) {
            mService = IColorChanged.Stub.asInterface(service);

        }

        public void onServiceDisconnected(ComponentName className) {
            mService = null;
        }
    };

    private IColorChanged getService() {
        if (mService == null) {
            bindService();
        }
        return mService;
    }

    private void setColorFilterServiceBackground(int color) {
        try {
            getService().changeBackground(color);
        } catch (RemoteException e) {
            e.printStackTrace();
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
    }

    private void bindService() {
        Intent intent = new Intent(MainActivity.this, ColorFilterPanelService.class);
        bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_main);
        // initMainLayout();
        initComponents();
        addAdView();
        AdBuddiz.cacheAds(this);
        startService(new Intent(this, ColorFilterPanelService.class));
    }

    private void initMainLayout() {
        boolean hasNavigationBar = true;
        try {
            Class<?> c = Class.forName("android.view.WindowManagerGlobal");
            Method m = c.getDeclaredMethod("getWindowManagerService", new Class<?>[] {});
            Object windowManagerService = m.invoke(null, new Object[] {});
            c = windowManagerService.getClass();
            m = c.getDeclaredMethod("hasNavigationBar", new Class<?>[] {});
            hasNavigationBar = (Boolean) m.invoke(windowManagerService, new Object[] {});
            if (DEBUG)
                Log.d(TAG, "hasNavigationBar: " + hasNavigationBar);
        } catch (Exception e) {
            if (DEBUG)
                Log.w(TAG, "failed to get windowManagerService", e);
        }
        int statusBarHeight = (int) getResources().getDimension(R.dimen.status_bar_height);
        int navigationBarHeight = hasNavigationBar ? (int) getResources().getDimension(
                R.dimen.navigation_bar_height) : 0;
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            mMainLayout.setPadding(mMainLayout.getPaddingLeft(), statusBarHeight,
                    mMainLayout.getPaddingRight(), navigationBarHeight);
        }
    }

    public void onBackPressed() {
        // AdBuddiz.showAd(this);
        super.onBackPressed();
    }

    protected void onDestroy() {
        mAdView.destroy();
        super.onDestroy();
    }

    private void addAdView() {
        final FrameLayout fl = (FrameLayout) findViewById(R.id.ad_view_parent);
        Button btn = (Button) findViewById(R.id.close_adview_btn);
        btn.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                fl.setVisibility(View.GONE);
            }
        });
        mAdView = (AdView) findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);
    }

    protected void onResume() {
        super.onResume();
        bindService();
        mIsForeGround = true;
        if (mSharf != null) {
            boolean enableFilter = mSharf.getBoolean(SHARF_KEY_ENABLE_FILTER, false);
            mEnableGroup.check(enableFilter ? R.id.enable_filter : R.id.disable_filter);
        }
    }

    protected void onPause() {
        super.onPause();
        mIsForeGround = false;
        this.unbindService(mConnection);
    }

    private void initComponents() {
        mMainLayout = (FrameLayout) findViewById(R.id.main_activity);
        mSharf = getSharedPreferences(SHARF_FILE_NAME, 0);
        boolean enableFilter = mSharf.getBoolean(SHARF_KEY_ENABLE_FILTER, false);
        mFilterColor = mSharf.getInt(SHARF_KEY_FILTER_COLOR, DEFAULT_FILTER_COLOR);
        mEnableGroup = (RadioGroup) findViewById(R.id.enable_group);
        mEnableGroup.check(enableFilter ? R.id.enable_filter : R.id.disable_filter);
        mEnableGroup.setOnCheckedChangeListener(this);

        mRecommandColorSpinner = (Spinner) findViewById(R.id.recommand_color_spinnner);
        String[] recommandColors = getResources().getStringArray(R.array.recommand_color);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_item, recommandColors);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mRecommandColorSpinner.setAdapter(adapter);
        mRecommandColorSpinner.setSelection(CUSTOMIZED_COLOR);
        mRecommandColorSpinner.setOnItemSelectedListener(this);

        mAlphaSeekBar = (SeekBar) findViewById(R.id.alpha_seek_bar);
        mAlphaSeekBar.setProgress(Color.alpha(mFilterColor));
        mAlphaSeekBar.setEnabled(enableFilter);
        mAlphaSeekBar.setOnSeekBarChangeListener(this);

        mRedSeekBar = (SeekBar) findViewById(R.id.red_seek_bar);
        mRedSeekBar.setProgress(Color.red(mFilterColor));
        mRedSeekBar.setEnabled(enableFilter);
        mRedSeekBar.setOnSeekBarChangeListener(this);

        mGreenSeekBar = (SeekBar) findViewById(R.id.green_seek_bar);
        mGreenSeekBar.setProgress(Color.green(mFilterColor));
        mGreenSeekBar.setEnabled(enableFilter);
        mGreenSeekBar.setOnSeekBarChangeListener(this);

        mBlueSeekBar = (SeekBar) findViewById(R.id.blue_seek_bar);
        mBlueSeekBar.setProgress(Color.blue(mFilterColor));
        mBlueSeekBar.setEnabled(enableFilter);
        mBlueSeekBar.setOnSeekBarChangeListener(this);

        mShowNotiCb = (CheckBox) findViewById(R.id.show_notification_check_box);
        mShowNotiCb.setChecked(mSharf.getBoolean(SHARF_KEY_SHOW_NOTIFICATION, false));
        mShowNotiCb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean showNotification) {
                mSharf.edit().putBoolean(SHARF_KEY_SHOW_NOTIFICATION, showNotification).commit();
                handleSimpleNotification(MainActivity.this);
            }
        });
    }

    public static void handleSimpleNotification(final Context context) {
        new Thread(new Runnable() {

            @Override
            public void run() {
                boolean showNoti = context.getSharedPreferences(MainActivity.SHARF_FILE_NAME, 0)
                        .getBoolean(MainActivity.SHARF_KEY_SHOW_NOTIFICATION, false);
                boolean enableFilter = context
                        .getSharedPreferences(MainActivity.SHARF_FILE_NAME, 0).getBoolean(
                                MainActivity.SHARF_KEY_ENABLE_FILTER, false);
                NotificationManager nm = (NotificationManager) context
                        .getSystemService(Context.NOTIFICATION_SERVICE);
                Intent notifyIntent = new Intent(context, ColorFilterPanelService.class).putExtra(
                        CHANG_STATE_FROM_NOTIFICATION, true);
                PendingIntent appIntent = PendingIntent.getService(context, 0, notifyIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT);
                if (showNoti) {
                    try {
                        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                            Notification.Builder builder = new Notification.Builder(context);
                            builder.setContentTitle(
                                    context.getResources()
                                            .getString(R.string.click_to_switch_state))
                                    .setContentText(
                                            context.getResources().getString(
                                                    enableFilter ? R.string.enable_filter
                                                            : R.string.disable_filter))
                                    .setSmallIcon(R.drawable.ic_launcher)
                                    .setContentIntent(appIntent).setAutoCancel(false)
                                    .setOngoing(true);
                            nm.cancelAll();
                            int currentapiVersion = android.os.Build.VERSION.SDK_INT;
                            if (currentapiVersion >= android.os.Build.VERSION_CODES.JELLY_BEAN) {
                                builder.setPriority(Notification.PRIORITY_LOW);
                                nm.notify(NOTIFICATION_TAG, NOTIFICATION_ID, builder.build());
                            } else {
                                nm.notify(NOTIFICATION_TAG, NOTIFICATION_ID,
                                        builder.getNotification());
                            }
                        } else {
                            NotificationCompat.Builder builder = new NotificationCompat.Builder(
                                    context);
                            builder.setContentTitle(
                                    context.getResources()
                                            .getString(R.string.click_to_switch_state))
                                    .setContentText(
                                            context.getResources().getString(
                                                    enableFilter ? R.string.enable_filter
                                                            : R.string.disable_filter))
                                    .setSmallIcon(R.drawable.ic_launcher)
                                    .setContentIntent(appIntent)
                                    .setPriority(Notification.PRIORITY_LOW).setAutoCancel(false)
                                    .setOngoing(true);
                            nm.cancelAll();
                            nm.notify(NOTIFICATION_TAG, NOTIFICATION_ID, builder.build());
                        }
                    } catch (Exception e) {
                        Log.w(TAG, "failed to create notification", e);
                    }
                } else {
                    nm.cancel(NOTIFICATION_TAG, NOTIFICATION_ID);
                }
            }
        }).start();

    }

    @Override
    public void onCheckedChanged(RadioGroup group, int id) {
        if (id == R.id.enable_filter) {
            mAlphaSeekBar.setEnabled(true);
            mRedSeekBar.setEnabled(true);
            mGreenSeekBar.setEnabled(true);
            mBlueSeekBar.setEnabled(true);
            mSharf.edit().putBoolean(SHARF_KEY_ENABLE_FILTER, true).commit();
            startService(new Intent(this, ColorFilterPanelService.class));
            handleSimpleNotification(MainActivity.this);
        } else if (id == R.id.disable_filter) {
            mAlphaSeekBar.setEnabled(false);
            mRedSeekBar.setEnabled(false);
            mGreenSeekBar.setEnabled(false);
            mBlueSeekBar.setEnabled(false);
            mSharf.edit().putBoolean(SHARF_KEY_ENABLE_FILTER, false).commit();
            startService(new Intent(this, ColorFilterPanelService.class));
            handleSimpleNotification(MainActivity.this);
        }
    }

    private void setSeekBarProgress(int color) {
        mAlphaSeekBar.setProgress(Color.alpha(color));
        mRedSeekBar.setProgress(Color.red(color));
        mGreenSeekBar.setProgress(Color.green(color));
        mBlueSeekBar.setProgress(Color.blue(color));
    }

    private int getSeekBarColor() {
        return Color.argb(mAlphaSeekBar.getProgress(), mRedSeekBar.getProgress(),
                mGreenSeekBar.getProgress(), mBlueSeekBar.getProgress());
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        mFilterColor = getSeekBarColor();
        setColorFilterServiceBackground(mFilterColor);
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
        mRecommandColorSpinner.setSelection(CUSTOMIZED_COLOR);
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        mFilterColor = getSeekBarColor();
        mSharf.edit().putInt(SHARF_KEY_FILTER_COLOR, mFilterColor).commit();
        setColorFilterServiceBackground(mFilterColor);
    }

    @Override
    public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
        switch (arg2) {
            case 0:
                mFilterColor = 0x30666600;
                setSeekBarProgress(mFilterColor);
                mSharf.edit().putInt(SHARF_KEY_FILTER_COLOR, mFilterColor).commit();
                setColorFilterServiceBackground(mFilterColor);
                break;
            case 1:
                mFilterColor = 0x30550000;
                setSeekBarProgress(mFilterColor);
                mSharf.edit().putInt(SHARF_KEY_FILTER_COLOR, mFilterColor).commit();
                setColorFilterServiceBackground(mFilterColor);
                break;
            case 2:
                mFilterColor = 0x30005500;
                setSeekBarProgress(mFilterColor);
                mSharf.edit().putInt(SHARF_KEY_FILTER_COLOR, mFilterColor).commit();
                setColorFilterServiceBackground(mFilterColor);
                break;
            case 3:
                mFilterColor = 0xA0000000;
                setSeekBarProgress(mFilterColor);
                mSharf.edit().putInt(SHARF_KEY_FILTER_COLOR, mFilterColor).commit();
                setColorFilterServiceBackground(mFilterColor);
                break;
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> arg0) {
    }
}
