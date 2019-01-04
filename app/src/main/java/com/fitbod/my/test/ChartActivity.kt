package com.fitbod.my.test

import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.os.Bundle
import android.support.v7.app.AppCompatActivity;
import android.view.Menu
import android.view.View

import kotlinx.android.synthetic.main.activity_chart.*

// The intent used in MainActivity to launch this activity
fun Context.chartActivityIntent(headerArrayValues : ArrayList<String>): Intent {
    return Intent(this, ChartActivity::class.java).apply {
        putExtra(HEADER_VALUES, headerArrayValues)
    }
}

private const val HEADER_VALUES = "headerArrayValues"

class ChartActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chart)

        // From MainActivity
        val headerArrayValues : ArrayList<String> = intent.getStringArrayListExtra(HEADER_VALUES)
        val exerciseName = headerArrayValues[0]

        setSupportActionBar(chart_toolbar)
        supportActionBar?.title = exerciseName
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        // Init all of the Views
        val chartFragmentLayout = findViewById(R.id.fragment) as android.support.constraint.ConstraintLayout
        val my_canvas = myCanvas(this)
        my_canvas.addHeaderArray(headerArrayValues)
        chartFragmentLayout.addView(my_canvas)
    }

    // Dummy options menu to display the icon
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    class myCanvas(context: Context) : View(context) {

        private lateinit var mHeaderArrayValues : ArrayList<String>

        fun addHeaderArray(headerArrayValues : ArrayList<String>) {
            mHeaderArrayValues = headerArrayValues
            mHeaderArrayValues.add(context.getResources().getString(R.string.lbs_literal))
        }

        // We don't want any conditionals or helper methods. To do so would mean we'd have to Unit Test
        // this class which we don't want to do.
        override fun onDraw(canvas: Canvas) {

            super.onDraw(canvas)

            // The 'brains' behind the activity's positioning
            val mChartPositionCalculator : IChartCalc = ChartPositionCalculator(
                context,
                mHeaderArrayValues,
                canvas
            )

            // Draw Header Text
            mChartPositionCalculator.drawHeaderText()

            // Draw the Left Labels (RM Weights) and Horizontal Lines
            for(idx in 0..3) {
                mChartPositionCalculator.drawLeftLabel(idx)
                mChartPositionCalculator.drawHorizontalLine(idx)
            }

            // Draw the Bottom Labels (Dates) and Vertical Lines
            for(idx in 0..4) {
                mChartPositionCalculator.drawBottomLabel(idx)
                mChartPositionCalculator.drawVerticalLine(idx)
            }

            // Draw the Plotted graph circles (Values) and connecting lines
            mChartPositionCalculator.drawPlottedCirclesLines()
        }
    }
}
