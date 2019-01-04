package com.fitbod.my.test

import kotlin.coroutines.CoroutineContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import java.io.InputStream
import java.text.SimpleDateFormat
import java.util.*

// All of the entries in the Raw and calculated mutable map.
private const val WORKOUT_LIST_DATE_EPOCH : Int = 0
private const val WORKOUT_LIST_DATE_STRING : Int = 1
private const val WORKOUT_LIST_EXERCISE_NAME : Int = 2
private const val WORKOUT_LIST_SETS : Int = 3
private const val WORKOUT_LIST_REPS : Int = 4
private const val WORKOUT_LIST_WEIGHT : Int = 5
private const val WORKOUT_LIST_RM : Int = 6

// Brzycki formula constants. The formula looks like this:
//     weight / ((37 / 36) - ((1 / 36) * reps)) or:
//     weight / (1.0278 - (.0278 * reps))

// Represents the 37/36 portion of the formula (ie; 1.0278)
private const val BRZYCKI_FORMULA_VALUE_ONE : Double = 1.0278

// Represents the 1/36 portion of the formula (ie; .0278)
private const val BRZYCKI_FORMULA_VALUE_TWO : Double = .0278

class DataSingleton private constructor() : CoroutineScope
{
    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main

    // Make the date format a static value. Easier to modify later if needed:
    private val mSimpleDateFormat : SimpleDateFormat = SimpleDateFormat("MMM dd yyyy")

    // Final conversion data object (usable Map/List - data from CSV):
    private var mWorkoutList: MutableMap<Int, List<String>> = mutableMapOf()

    // Final exercise list (usable Map/List - contains caluclated RMs):
    private var mExerciseMap : MutableMap<String, MutableList<String>> = mutableMapOf()

    // We only want to calculate RMs once.
    private var mHasRMBeenCalculated : Boolean = false

    /**
     * <p>Put all of the data from CSV into a MutableMap (mWorkoutList). Below is what the MutableMap looks like:</p>
     *
     *      <p>{1=[1507705200000, Oct 11 2017, Back Squat, 1, 10, 45, 60],
     *          2=[1507705200000, Oct 11 2017, Back Squat, 1, 10, 135, 180],
     *          3=[1507705200000, Oct 11 2017, Back Squat, 1, 3, 185, 195] ... }</p>
     *
     *      <ul>Map explanation:
     *           <li>First, is an integer (the map key). This is very similar to a database primary key. Why have this?
     *               Because we could have the same exercise with the same exact data but representing 2 different
     *               sets or maybe even a morning workout and an evening workout.
     *               How would they be distinguishable?</li>
     *           <li>The actual data or map value (List<String>):
     *                <ul>
     *                    <li>Long: represents the date as an number (milliseconds since epoch)</li>
     *                    <li>String: the date of the workout.
     *                        Really not needed but helps for testing and debugging.</li>
     *                    <li>String: the exercise (ie; Back Squat)</li>
     *                    <li>Int: Number of sets</li>
     *                    <li>Int: Number of reps</li>
     *                    <li>Int: Weight of barbell/dumbbell/machine...</li>
     *                    <li>Int: One Rep Max (RM)</li>
     *                </ul>
     *            </li>
     *      </ul>
     */
    fun createMutableMapFromCSV(inputStream: InputStream) : MutableMap<Int, List<String>>
    {
        // Represents separated lines of the CSV (ie; row). Note: Each line is read until a "hard return" is reached.
        val workoutList : List<String> = inputStream.bufferedReader().readLines()

        // We need to be sure the data is valid. If 'invalid' silently do nothing
        if (validateCSVdata(workoutList) == false) { return mutableMapOf() }

        // The "Id" representing a row:
        var id : Int = 0

        // Each row but represented as an Array:
        var tempArray : List<String>

        // Cycle through each line (row)
        workoutList.forEach {

            // Split the line into separate Array elements:
            tempArray = it.split(",")

            // Re-initiate this local variable each time to clear out old data.
            // TODO: declare this variable above the loop and empty it each time.  This will save cycles when iterating
            // over large amounts of data.
            val inputArray : MutableList<String> = mutableListOf()

            // Convert CSV dates to sortable dates (ie; Long numbers representing milliseconds since epoch)
            val date : Date = mSimpleDateFormat.parse(tempArray[0])
            val dateToLong = date.getTime()
            inputArray.add(dateToLong.toString())

            // Simply "stuff" the rows data into the remaining 5 columns/array items:
            //      1) date (ie; Sep 5, 2018)
            //      2) exercise (ie; Back Squat)
            //      3) sets
            //      4) reps
            //      5) weight
            for(iter in 0..4) { inputArray.add(tempArray[iter]) }

            // Now add the RM placeholder value of "0". This value is calculated asynchronously in "calculateRM()"
            inputArray.add("0")

            // Increment the rows "Id"
            id++

            // Finally "stuff" all of it into the class level MutableMap var:
            mWorkoutList.put( id, inputArray )
        }

        inputStream.close()
        return mWorkoutList
    }

    private fun validateCSVdata(workoutList : List<String>) : Boolean {

        // Each row but represented as an Array:
        var tempArray : List<String>

        workoutList.forEach {

            // Split the line into separate Array elements:
            tempArray = it.split(",")

            // Check for failure
            if(tempArray.size != 5) {
                return false
            }
        }

        // Success
        return true
    }

    /**
     * <p>This is the way to create a Singleton in Kotlin. The "companion object" is fully accessible (public). However,
     * in my opinion, accessing just the class instance is the best approach for this object. "Globbing" on getters
     * and setters inside the companion object goes beyond the intent of the object. Instead, use getters/setters
     * as class level functions instead (see further below for examples).</p>
     */
    companion object
    {
        private val mDataSingletonInstance : DataSingleton = DataSingleton()

        @Synchronized
        fun getInstance(): DataSingleton
        {
            return mDataSingletonInstance
        }
    }

    /** #########################################################################
        ########################    PUBLIC ACCESSORS     ########################
        ######################################################################### */

    /**
     * This is set as an async coroutine from MainActivity. It calculates the 'One Rep Max' using
     * the Brzycki formula. The formula looks like this:
     *
     *      weight / ((37 / 36) - ((1 / 36) * reps)) or:
     *      weight / (1.0278 - (.0278 * reps))
     *
     * Both of the immutable vales are declared as constants:
     *
     *     BRZYCKI_FORMULA_VALUE_ONE = 1.0278
     *     BRZYCKI_FORMULA_VALUE_TWO = .0278
     */
    fun calculateRM(workoutList : MutableMap<Int, List<String>>) : MutableMap<Int, List<String>> {
        // This is why this method is so expensive and why we need to make this method an async one.
        // We have to iterate through the entire list of workout entries and calculate the 'One Rep Max'
        // for each of them.
        workoutList.forEach {

            // First, get the 'value' portion of the map (a mutable list)
            val arrayWorkoutList = it.value as MutableList<String>

            // Calculate the 'One Rep Max'
            val rmValue =
                    5 * ((arrayWorkoutList[WORKOUT_LIST_WEIGHT].toDouble() /
                    (BRZYCKI_FORMULA_VALUE_ONE -
                    (BRZYCKI_FORMULA_VALUE_TWO * arrayWorkoutList[WORKOUT_LIST_REPS].toInt()))).toInt() / 5)

            // Stuff this newly calculated value into the array
            arrayWorkoutList[WORKOUT_LIST_RM] = rmValue.toString()

            // Finally, put the array back in the mutable map at the 'key' spot.
            workoutList[it.key] = arrayWorkoutList
        }

        return workoutList
    }

    fun setRMHasBeenCalculated(isCalculated : Boolean) { mHasRMBeenCalculated = isCalculated }
    fun getRMHasBeenCalculated() : Boolean { return mHasRMBeenCalculated }
    fun getIsCSVEmpty() : Boolean { return mWorkoutList.isEmpty() }

    /**
     * We do it this way for testing purposes
     */
    fun doCalculateRM() { mWorkoutList = calculateRM(mWorkoutList) }

    fun doGetExerciseMap() : MutableMap<String, MutableList<String>> {
        return getExerciseMap(mWorkoutList)
    }

    fun doGetGraphData(exerciseName : String) : MutableMap<String, Int> {
        return getGraphData(exerciseName, mWorkoutList)
    }

    /**
     * <p>A lazy way to only include one set of data. In this function we only want one of each exercise. This is
     * achieved using a hash map (MutableList). Since each exercise is a key and there can only be one of each key
     * simply traversing the list and "trying" to add a new key is the best and easiest approach. If a key already
     * exists when trying to add a new one, we simply check the RM value to see if it is greater (or equal to)
     * the one that already exists.</p>
     *
     * <p>Here is an example output:
     *      {Back Squat=[Back Squat, 310, 2],
     *       Barbell Bench Press=[Barbell Bench Press, 245, 1],
     *       Deadlift=[Deadlift, 360, 1]}</p>
     */
    fun getExerciseMap(injectedWorkoutList : MutableMap<Int, List<String>>) : MutableMap<String, MutableList<String>> {
        // This is pretty expensive so only do it once:
        if (mHasRMBeenCalculated) {

            mExerciseMap.clear()

            // The map where we try to add each exercise:
            var workoutList: List<String>
            var tempList: MutableList<String>

            // Walk the class level map (mWorkoutList) of exercises:
            injectedWorkoutList.forEach {

                // Copy the List from mWorkoutList (this is the value part of the key/value pair)
                workoutList = it.value

                // Build the list with only necessary fields (ie; Exercise Name, RM, and Number of Records)
                val myExerciseList: MutableList<String> = mutableListOf()
                myExerciseList.add(workoutList[WORKOUT_LIST_EXERCISE_NAME]) // Exercise Name
                myExerciseList.add(workoutList[WORKOUT_LIST_RM])            // RM
                myExerciseList.add("1")                                     // Number of Records

                // Does the key already exist?
                //      If TRUE, we need to check for a new RM record.
                //      If FALSE, simply add the new record and move on to the next mWorkoutList item.
                if (mExerciseMap.containsKey(workoutList[WORKOUT_LIST_EXERCISE_NAME])) {

                    // Make a copy of the exerciseMap value:
                    tempList = mExerciseMap[workoutList[WORKOUT_LIST_EXERCISE_NAME]] as MutableList<String>

                    // Is the new RM > than the old one? If so, make the new RM the record.
                    // For clarity: myExerciseList[1] = RM
                    //              myExerciseList[2] = Number of Records
                    if (myExerciseList[1].toInt() > tempList[1].toInt()) {
                        tempList[1] = myExerciseList[1] // New RM Record value
                        tempList[2] = "1"               // Reset the Number of Records value

                        // Insert the potentially modified MutableList into the MutableMap value:
                        mExerciseMap[workoutList[WORKOUT_LIST_EXERCISE_NAME]] = tempList

                    // Is the new RM = to the old one? If so, increment the Number of Records.
                    } else if (myExerciseList[1].toInt().equals(tempList[1].toInt())) {

                        tempList[2] = (tempList[2].toInt() + 1).toString()

                        // Insert the potentially modified MutableList into the MutableMap value:
                        mExerciseMap[workoutList[2]] = tempList
                    }

                // Key DOES NOT exist so add it and move on
                } else {
                    mExerciseMap.put(workoutList[WORKOUT_LIST_EXERCISE_NAME], myExerciseList)
                }
            }
        }

        // Yay! We're done
        return mExerciseMap
    }

    /**
     * The plotted points on the graph in ChartActivity. The returned value is a mutable map where
     * the 'key' is the Date and the 'value' is the RM weight. The returned map looks like this:
     *
     *      {Sep 15=270, Sep 21=275, Sep 28=280, Oct 4=280, Oct 11=280}
     */
    fun getGraphData(exercise: String, injectedWorkoutList : MutableMap<Int, List<String>>) : MutableMap<String, Int> {

        // A temporary map used to hold graph data
        val graphData : MutableMap<Long, Int> = mutableMapOf()

        // We need to make some changes to the WorkoutList class map so make a copy instead
        // of a pointer:
        val copyWorkoutList: MutableMap<Int, List<String>> = injectedWorkoutList.toMutableMap()

        // Remove all of the map entries that DON'T have the correct Exercise Name:
        copyWorkoutList.entries.removeAll { (_, value) -> value[WORKOUT_LIST_EXERCISE_NAME] != exercise }

        // Sort the remaining List items by date in descending order (ie; newer dates first)
        var sortedWorkoutList: Map<Int, List<String>> =
            copyWorkoutList.toList().sortedByDescending { (_, value) -> value[0] }.toMap()

        // Now we can cycle through the filtered and sorted map
        var idx : Int = 0

        sortedWorkoutList.forEach {
            val workoutList : List<String> = it.value
            val workoutListDateTime : Long = workoutList[WORKOUT_LIST_DATE_EPOCH].toLong()
            val workoutListRM : Int = workoutList[WORKOUT_LIST_RM].toInt()

            // Does the graph data item date already exist in the map? If so, see if the corresponding
            // RM is greater than the one already in the map. If that's true then replace the current
            // RM value with the current one.
            if(graphData.containsKey(workoutListDateTime)){

                val graphDataValue : Int = graphData[workoutListDateTime] as Int

                if(workoutListRM > graphDataValue) {
                    graphData[workoutListDateTime] = workoutListRM
                }

            // A new date (new map entry). Add it unless we already have 5.
            } else {

                // Break and Continue don't work with Kotlin for statements yet. So, if we already 5 graph
                // entries make sortedWorkoutList empty. In essence, this is like driving down the
                // highway at 55 mph and slamming the car into park. But, it's the only way to stop the loop
                // and save cycles/performance.
                if(idx >= 5) {

                    sortedWorkoutList = mapOf()

                // Simply add the new map entry
                } else {
                    graphData.put(workoutListDateTime, workoutListRM)
                    idx++
                }
            }
        }

        // Sort the graph data by date in ascending order (older dates first).
        val sortedGraphData: Map<Long, Int> = graphData.toList().sortedBy { (key, _) -> key }.toMap()

        // Initialize the graph map that will be returned.
        val returnGraphData: MutableMap<String, Int> = mutableMapOf()

        // Adjust the date into it's display-ready string
        sortedGraphData.forEach{
            val simpleDateFormat = SimpleDateFormat("MMM d")
            val displayDate : String = simpleDateFormat.format(Date(it.key))

            returnGraphData.put(displayDate, it.value)
        }

        // Yay! We're done
        return returnGraphData
    }
}
