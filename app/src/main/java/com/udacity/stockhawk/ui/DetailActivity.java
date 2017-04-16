package com.udacity.stockhawk.ui;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.format.DateFormat;
import android.util.Log;
import android.widget.TextView;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.udacity.stockhawk.R;
import com.udacity.stockhawk.data.Contract;

import java.io.IOException;
import java.io.StringReader;
import java.security.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import au.com.bytecode.opencsv.CSVReader;
import butterknife.BindView;
import butterknife.ButterKnife;
import timber.log.Timber;

public class DetailActivity extends AppCompatActivity {

    @BindView(R.id.chart)
    LineChart chart;
    @BindView(R.id.chart_title)
    TextView chartTitle;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.detail_activity);
        ButterKnife.bind(this);

        Intent intent = getIntent();
        String symbol = intent.getStringExtra("Symbol");

        plot(symbol);
    }

    public String plot(String symbol) {
        String history = getHistoryString(symbol);

        final List<String[]> lines = getLines(history);
        final String[] date = new String[lines.size()];
        List<Entry> entries = new ArrayList<Entry>();
        final List<Long> xAxisValues = new ArrayList<>();
        int xAxisPosition = 0;

        chartTitle.setText(symbol);
        for (String[] line : lines) {
//            Calendar cal = Calendar.getInstance(Locale.ENGLISH);
//            cal.setTimeInMillis(Long.valueOf(line[0]));
//            date[xAxisPosition++] = DateFormat.format("dd-MM-yyyy", cal).toString();
            xAxisValues.add(Long.valueOf(line[0]));

            Entry entry = new Entry(
                    xAxisPosition,
                    Float.valueOf(line[1])
            );
            entries.add(entry);
            xAxisPosition++;

        }

        LineDataSet dataSet = new LineDataSet(entries, symbol); // add entries to dataset
        dataSet.setAxisDependency(YAxis.AxisDependency.LEFT);

        LineData lineData = new LineData(dataSet);
//        lineData.setValueTextColor(R.color.colorAccent);
        dataSet.setCircleRadius(5);

        chart.setData(lineData);
        chart.invalidate();

        XAxis xAxis = chart.getXAxis();

//        xAxis.setGranularity(100f);
//        // minimum axis-step (interval) is 1
        xAxis.setValueFormatter(new IAxisValueFormatter() {

            @Override
            public String getFormattedValue(float value, AxisBase axis) {
                Date date = new Date(xAxisValues.get(xAxisValues.size() - (int)value - 1));
                return new SimpleDateFormat( "yyyy-MM-dd", Locale.ENGLISH)
                        .format(date);

            }

        });


        return symbol;
    }

    private List<String[]> getLines(String history) {

        List<String[]> lines = new ArrayList<>();
        CSVReader reader = new CSVReader(new StringReader(history));
        try {
            lines.addAll(reader.readAll());
        } catch (IOException e) {
            e.printStackTrace();
        }

        Log.d("lines", lines.toString());
        return lines;

    }

    public String getHistoryString(String symbol) {
        Cursor cursor = getContentResolver().query(Contract.Quote.makeUriForStock(symbol), null, null, null, null);

        String history = "";
        if (cursor.moveToFirst()) {
            history = cursor.getString(cursor.getColumnIndex(Contract.Quote.COLUMN_HISTORY));
            cursor.close();
        }


        return history;
    }

}
