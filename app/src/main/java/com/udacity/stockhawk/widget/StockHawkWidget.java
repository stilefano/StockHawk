package com.udacity.stockhawk.widget;


import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.widget.RemoteViews;

import com.udacity.stockhawk.R;
import com.udacity.stockhawk.data.StockProvider;
import com.udacity.stockhawk.ui.DetailActivity;
import com.udacity.stockhawk.ui.MainActivity;

import timber.log.Timber;

public class StockHawkWidget extends AppWidgetProvider {

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {

        for(int appWidgetId : appWidgetIds){

            Intent intent = new Intent(context, MainActivity.class);
            PendingIntent penIntent = PendingIntent.getActivity(context,0,intent,0);

            RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.app_widget_provider);
            remoteViews.setOnClickPendingIntent(R.id.container, penIntent);

            Intent wIntent = new Intent(context, StockHawkWidgetService.class);
            wIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
            wIntent.setData(Uri.parse(intent.toUri(Intent.URI_INTENT_SCHEME)));

            Intent detailIntent = new Intent(context, DetailActivity.class);
            PendingIntent pendingIntentDetail = PendingIntent.getActivity(context, 0, detailIntent, 0);
            remoteViews.setRemoteAdapter(R.id.list, wIntent);
            remoteViews.setPendingIntentTemplate(R.id.list, pendingIntentDetail);

            appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetId, R.id.list);
            appWidgetManager.updateAppWidget(appWidgetId, remoteViews);

        }

    }

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);
        if (StockProvider.ACTION_DATA_UPDATED.equals(intent.getAction())) {
            final AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
            final int[] appWidgetIds = appWidgetManager.getAppWidgetIds(
                    new ComponentName(context, getClass()));
            appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetIds,
                    R.id.list);
        }
    }
}