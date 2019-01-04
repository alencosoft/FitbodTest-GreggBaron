package com.fitbod.my.test

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView

class ExerciseListAdapter(
    private val context: Context,
    private var dataSource: MutableMap<String,
    MutableList<String>>) : BaseAdapter() {

    // Inflater object framework base class
    private val inflater: LayoutInflater
            = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

    override fun getItem(position: Int): MutableList<String> {
        val dataSourceKeys : MutableSet<String> = dataSource.keys
        return dataSource[dataSourceKeys.elementAt(position)] as MutableList<String>
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        // Get view for row item
        val rowView = inflater.inflate(R.layout.exercise_list_view, parent, false)

        // Exercise Name
        val exerciseNameTextView = rowView.findViewById(R.id.exercise_name) as TextView

        // Exercise Number of Records
        val exerciseNumRecordsTextView = rowView.findViewById(R.id.exercise_num_records) as TextView

        // RM Lbs Number
        val rmLbsNumberTextView = rowView.findViewById(R.id.rm_lbs_number) as TextView

        // rm_lbs_label (Literally, just "lbs")
        val rmLbsLabelTextView = rowView.findViewById(R.id.rm_lbs_lable) as TextView

        val listItem: List<String>? = getItem(position)

        // Populate the various textFields
        if (listItem != null) {
            exerciseNameTextView.text = listItem[0]

            var exerciseNumRecords : String = listItem[2] + " " +
                    this.context.getResources().getString(R.string.rm_record)

            if(listItem[2].toInt() > 1) {
                exerciseNumRecords += this.context.getResources().getString(R.string.plural)
            }

            exerciseNumRecordsTextView.text = exerciseNumRecords
            rmLbsNumberTextView.text = listItem[1]
            rmLbsLabelTextView.text = this.context.getResources().getString(R.string.lbs_literal)
        }

        return rowView
    }

    // Always a good idea to refresh the adapter when new data arrives
    fun changeDataSource(newDataSource : MutableMap<String, MutableList<String>>) {
        dataSource = newDataSource
        this.notifyDataSetChanged()
    }

    // These are NOT needed but the compiler complains since it's a requirement
    // of base interface "adapter" that the base class "BaseAdapter" implements. uggh
    override fun getCount(): Int { return dataSource.size }
    override fun getItemId(position: Int): Long { return position.toLong() }
}