package com.huayuxun.whale.whalelinechart;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class MainActivity extends AppCompatActivity {

    WhaleLineChart whaleLineChart;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        whaleLineChart = (WhaleLineChart) findViewById(R.id.whaleLineChart);
        whaleLineChart.setScore(15000);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (whaleLineChart.getRectFValueAnimator() != null && whaleLineChart.getLineValueAnimator() != null) {
            whaleLineChart.getRectFValueAnimator().pause();
            whaleLineChart.getLineValueAnimator().pause();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (whaleLineChart.getRectFValueAnimator() != null && whaleLineChart.getLineValueAnimator() != null) {
            whaleLineChart.getRectFValueAnimator().resume();
            whaleLineChart.getLineValueAnimator().resume();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (whaleLineChart.getRectFValueAnimator() != null && whaleLineChart.getLineValueAnimator() != null) {
            whaleLineChart.getRectFValueAnimator().end();
            whaleLineChart.getLineValueAnimator().end();
        }
    }
}
