
package com.yenhsun.colorfilter;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.widget.RemoteViews;

public class SwithWidget extends AppWidgetProvider {
    public static String UPDATE_FROM_WIDGET = "com.yenhsun.colorfilter.switch_button";

    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);
        if (UPDATE_FROM_WIDGET.equals(intent.getAction())) {
            SharedPreferences sharf = context
                    .getSharedPreferences(MainActivity.SHARF_FILE_NAME, 0);
            boolean enableFilter = sharf
                    .getBoolean(MainActivity.SHARF_KEY_ENABLE_FILTER,
                            false);
            sharf.edit().putBoolean(MainActivity.SHARF_KEY_ENABLE_FILTER, !enableFilter).commit();
            context.startService(new Intent(context, ColorFilterPanelService.class));
        }
    }

    public static void performUpdate(Context context) {
        AppWidgetManager awm = AppWidgetManager.getInstance(context);
        performUpdate(context, awm);
    }

    private static void performUpdate(Context context, AppWidgetManager awm) {
        int widgetId[] = awm.getAppWidgetIds(new ComponentName(context, SwithWidget.class));
        performUpdate(context, awm, widgetId);
    }

    private static void performUpdate(Context context, AppWidgetManager awm, int[] widgetIds) {
        for (int appWidgetId : widgetIds) {
            awm.updateAppWidget(appWidgetId, updateView(context));
        }
    }

    private static RemoteViews updateView(Context context) {
        boolean enableFilter = context
                .getSharedPreferences(MainActivity.SHARF_FILE_NAME, 0)
                .getBoolean(MainActivity.SHARF_KEY_ENABLE_FILTER,
                        false);
        RemoteViews updateViews = new RemoteViews(context.getPackageName(),
                R.layout.switch_widget);
        updateViews.setImageViewResource(R.id.switch_widget_view,
                enableFilter ? R.drawable.switch_on : R.drawable.switch_off);
        Intent intent = new Intent(UPDATE_FROM_WIDGET);
        intent.setClass(context, SwithWidget.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT);
        updateViews.setOnClickPendingIntent(R.id.switch_widget_view, pendingIntent);
        return updateViews;
    }

    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        performUpdate(context, appWidgetManager, appWidgetIds);
    }
}
