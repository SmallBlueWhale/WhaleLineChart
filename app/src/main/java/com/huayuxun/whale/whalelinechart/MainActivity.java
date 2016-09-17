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
        whaleLineChart.setScore(12000);
    }

}
