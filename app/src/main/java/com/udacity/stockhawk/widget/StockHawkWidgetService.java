package com.udacity.stockhawk.widget;


import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Binder;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.udacity.stockhawk.R;
import com.udacity.stockhawk.data.Contract;
import com.udacity.stockhawk.ui.DetailActivity;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Locale;

public class StockHawkWidgetService extends RemoteViewsService{


    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {

        return new StockHawkWidgetViewFactory(getApplicationContext());

    }

    private class StockHawkWidgetViewFactory implements RemoteViewsFactory {

        private final Context mApplicationContext;
        private ArrayList<ContentValues> mCvlist = new ArrayList<ContentValues>();
        private DecimalFormat dollarFormat;
        private DecimalFormat dollarFormatWithPlus;
        private DecimalFormat percentageFormat;

        public StockHawkWidgetViewFactory(Context applicationContext) {

            mApplicationContext = applicationContext;

            dollarFormat = (DecimalFormat) NumberFormat.getCurrencyInstance(Locale.US);
            dollarFormatWithPlus = (DecimalFormat) NumberFormat.getCurrencyInstance(Locale.US);
            dollarFormatWithPlus.setPositivePrefix("+$");
            percentageFormat = (DecimalFormat) NumberFormat.getPercentInstance(Locale.getDefault());
            percentageFormat.setMaximumFractionDigits(2);
            percentageFormat.setMinimumFractionDigits(2);
            percentageFormat.setPositivePrefix("+");

        }

        @Override
        public void onCreate() {

            getData();

        }

        private void getData() {

            mCvlist.clear();
            long identity = Binder.clearCallingIdentity();

            try{




                ContentResolver contentResolver = mApplicationContext.getContentResolver();
                Cursor cursor = contentResolver.query(
                        Contract.Quote.URI,
                        null,
                        null,
                        null,
                        null
                );


                while(cursor.moveToNext()) {

                    ContentValues cv = new ContentValues();

                    String symbol = cursor.getString(cursor.getColumnIndex(Contract.Quote.COLUMN_SYMBOL));
                    Float price = cursor.getFloat(cursor.getColumnIndex(Contract.Quote.COLUMN_PRICE));
                    Float abschange = cursor.getFloat(cursor.getColumnIndex(Contract.Quote.COLUMN_ABSOLUTE_CHANGE));
                    Float perchange = cursor.getFloat(cursor.getColumnIndex(Contract.Quote.COLUMN_PERCENTAGE_CHANGE));

                    cv.put(Contract.Quote.COLUMN_SYMBOL, symbol);
                    cv.put(Contract.Quote.COLUMN_PRICE, price);
                    cv.put(Contract.Quote.COLUMN_ABSOLUTE_CHANGE, abschange);
                    cv.put(Contract.Quote.COLUMN_PERCENTAGE_CHANGE, perchange);

                    mCvlist.add(cv);

                }

                cursor.close();
            } finally {

                Binder.restoreCallingIdentity(identity);

            }

        }

        @Override
        public void onDataSetChanged() {

            getData();

        }

        @Override
        public void onDestroy() {

        }

        @Override
        public int getCount() {
            return mCvlist.size();
        }

        @Override
        public RemoteViews getViewAt(int i) {

            ContentValues cv = mCvlist.get(i);

            RemoteViews remoteViews = new RemoteViews(

                    mApplicationContext.getPackageName(),
                    R.layout.list_item_quote

            );

            String symbol = cv.getAsString(Contract.Quote.COLUMN_SYMBOL);

            remoteViews.setTextViewText(R.id.symbol, symbol);
            remoteViews.setTextViewText(R.id.price, dollarFormat.format(cv.getAsFloat(Contract.Quote.COLUMN_PRICE)));
            remoteViews.setTextColor(R.id.symbol, getResources().getColor(R.color.white));
            remoteViews.setTextColor(R.id.price, getResources().getColor(R.color.white));

            float abschange = cv.getAsFloat(Contract.Quote.COLUMN_ABSOLUTE_CHANGE);
            float perchange = cv.getAsFloat(Contract.Quote.COLUMN_PERCENTAGE_CHANGE);

            if (abschange >= 0) {

                remoteViews.setInt(R.id.change, "setBackgroundResource", R.drawable.percent_change_pill_green);

            } else {

                remoteViews.setInt(R.id.change, "setBackgroundResource", R.drawable.percent_change_pill_red);

            }

            remoteViews.setTextViewText(R.id.change, percentageFormat.format( perchange / 100 ));


            Intent fillIntent = new Intent(mApplicationContext, DetailActivity.class);
            fillIntent.putExtra(DetailActivity.EXTRA_SYMBOL, symbol);
            remoteViews.setOnClickFillInIntent(R.id.list_item, fillIntent);

            return remoteViews;
        }

        @Override
        public RemoteViews getLoadingView() {
            return null;
        }

        @Override
        public int getViewTypeCount() {
            return 1;
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public boolean hasStableIds() {
            return true;
        }
    }
}
