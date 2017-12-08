package iiitd.gritlab.distractionMonitor;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

import org.achartengine.ChartFactory;
import org.achartengine.GraphicalView;
import org.achartengine.chart.BarChart;
import org.achartengine.model.XYMultipleSeriesDataset;
import org.achartengine.model.XYSeries;
import org.achartengine.model.XYValueSeries;
import org.achartengine.renderer.XYMultipleSeriesRenderer;
import org.achartengine.renderer.XYSeriesRenderer;

/**
 * Created by garvita on 27-11-2017.
 */

public class GraphCount extends AppCompatActivity {

    static XYSeries series;
    static XYSeriesRenderer renderer;
    static XYMultipleSeriesDataset dataset;
    static XYMultipleSeriesRenderer mRenderer;
    static Context context;
    static LinearLayout chart;
    static GraphicalView chartView;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_graph);
        context = this;
        renderer = new XYSeriesRenderer();
        renderer.setLineWidth(2);
        renderer.setDisplayBoundingPoints(true);
        chart = (LinearLayout) findViewById(R.id.chart);
        series = new XYValueSeries("Student count");
        dataset = new XYMultipleSeriesDataset();
        dataset.addSeries(series);
        mRenderer = new XYMultipleSeriesRenderer();
        mRenderer.setLabelsTextSize(28);
        mRenderer.setLegendTextSize(28);
        mRenderer.setYLabelsPadding(10);
        mRenderer.setMargins(new int[]{20, 30, 15, 0});
        mRenderer.setPanEnabled(true, true);
        mRenderer.setZoomEnabled(true, true);
        mRenderer.setZoomButtonsVisible(true);
        mRenderer.setXLabels(20);
        mRenderer.setYLabels(20);
        mRenderer.setShowGrid(true);
        mRenderer.setGridColor(Color.BLACK);
        mRenderer.setApplyBackgroundColor(true);
        mRenderer.setBackgroundColor(Color.WHITE);
        mRenderer.addSeriesRenderer(renderer);
        chartView = ChartFactory.getBarChartView(context, dataset, mRenderer, BarChart.Type.DEFAULT);
        populateSeries(-1);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    static void populateSeries(int value) {
        System.out.println("value = " + value);
        System.out.println("inside populate series");
        System.out.println("series.getItemCount() = " + series.getItemCount());
        if (value == -1) {
            series.add(0, 0);
        }
        else {
            series.add(series.getItemCount(), series.getItemCount(), value);
        }
        drawChart();
    }

    static void drawChart() {
        dataset.clear();
        dataset.addSeries(series);
         if (chart != null) {
            chart.removeView(chartView);
        }

        chartView = ChartFactory.getBarChartView(context, dataset, mRenderer, BarChart.Type.DEFAULT);
        chartView.repaint();
        chart.addView(chartView);
    }

    public void cancelAlarm(View v) {
        MainActivity.alarmMgr.cancel(MainActivity.alarmIntent);
        Toast.makeText(this.getApplicationContext(), "No longer monitoring students", Toast.LENGTH_SHORT).show();
    }

}