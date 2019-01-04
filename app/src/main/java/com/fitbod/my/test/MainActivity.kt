package com.fitbod.my.test

import android.os.Bundle
import android.support.v7.app.AppCompatActivity;
import android.view.Menu
import android.widget.ListView

import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.IOException
import java.io.InputStream

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Try to get the file based on the name in Resources (Strings)
        if(DataSingleton.getInstance().getIsCSVEmpty()) {

            val inputStream = try {
                getAssets().open(getString(R.string.csv_filename))

            // Oops, the file doesn't exist.
            } catch (e: IOException) {
                throw e
            }

            // Breakdown CSV into a usable Map
            DataSingleton.getInstance().createMutableMapFromCSV(inputStream as InputStream)
        }

        // The list adapter with data injected
        val adapter = createAndPopulateExerciseList(DataSingleton.getInstance().doGetExerciseMap())

        setSupportActionBar(toolbar)
        supportActionBar?.title = ""

        // This is expensive so only do this once...
        if( ! DataSingleton.getInstance().getRMHasBeenCalculated()) {

            // Calculates RMs -> Asynchronous launch of a Global Coroutine
            GlobalScope.launch(Dispatchers.Main) {
                DataSingleton.getInstance().doCalculateRM()
                DataSingleton.getInstance().setRMHasBeenCalculated(true)
                adapter.changeDataSource(DataSingleton.getInstance().doGetExerciseMap())
                adapter.notifyDataSetChanged()
            }
        }
    }

    // The setup for the list
    private fun createAndPopulateExerciseList(
            theList: MutableMap<String,
            MutableList<String>>) : ExerciseListAdapter {

        val listView = findViewById<ListView>(R.id.exercise_list_view)
        val adapter = ExerciseListAdapter(this, theList)
        listView.adapter = adapter
        listView.setOnItemClickListener { _, _, position, _ -> exerciseOnClick(theList, position) }
        return adapter
    }

    // Our callback when someone "clicks" a list item
    private fun exerciseOnClick(
            theList: MutableMap<String,
            MutableList<String>>,
            position: Int) {

        val dataSourceKeys : MutableSet<String> = theList.keys
        val dataList : ArrayList<String> = theList[dataSourceKeys.elementAt(position)] as ArrayList<String>
        startActivity(chartActivityIntent(dataList))
    }

    // Just a dummy menu so it looks like mockup
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }
}
