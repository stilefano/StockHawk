package com.udacity.stockhawk.ui;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
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
import com.google.common.collect.Lists;
import com.udacity.stockhawk.R;
import com.udacity.stockhawk.data.Contract;
import com.udacity.stockhawk.widget.StockHawkWidget;

import java.io.IOException;
import java.io.StringReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import au.com.bytecode.opencsv.CSVReader;
import butterknife.BindView;
import butterknife.ButterKnife;

public class DetailActivity extends AppCompatActivity {

    @BindView(R.id.chart)
    LineChart chart;
    @BindView(R.id.chart_title)
    TextView chartTitle;

    public static final String EXTRA_SYMBOL = "extra:symbol";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.detail_activity);
        ButterKnife.bind(this);

        Intent intent = getIntent();
        String symbol = intent.getStringExtra(DetailActivity.EXTRA_SYMBOL);

        Log.d("symbol", symbol);

        plot(symbol);
    }

    public String plot(String symbol) {
        String history = getHistoryString(symbol);

        final List<String[]> lines = getLines(history);
        final List<String[]> reverseLines = Lists.reverse(lines);
        final String[] date = new String[lines.size()];
        List<Entry> entries = new ArrayList<Entry>();
        final List<Long> xAxisValues = new ArrayList<>();
        int xAxisPosition = 0;

        chartTitle.setText(symbol);

        for (String[] line : lines){
            xAxisValues.add(Long.valueOf(line[0]));
        }

        for (String[] line : reverseLines) {

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

        dataSet.setCircleRadius(5);
        dataSet.setCircleColor(Color.MAGENTA);

        dataSet.setColors(Color.WHITE);
        dataSet.setValueTextColor(Color.WHITE);

        chart.setData(lineData);
        chart.invalidate();
        chart.getLegend().setTextColor(Color.WHITE);

        XAxis xAxis = chart.getXAxis();
        xAxis.setTextSize(11f);
        xAxis.setLabelRotationAngle(15);
        xAxis.setTextColor(Color.WHITE);
        xAxis.setDrawAxisLine(true);
        xAxis.setDrawGridLines(true);

        YAxis yAxis = chart.getAxisLeft();
        yAxis.setTextSize(11f);
        yAxis.setTextColor(Color.WHITE);
        yAxis.setDrawAxisLine(true);
        yAxis.setDrawGridLines(true);

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
