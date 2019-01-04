package com.fitbod.my.test

import org.junit.Test
import org.junit.Assert.*
import org.junit.jupiter.api.TestInstance
import java.io.InputStream

/**
 * Test DataSingleton class
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class DataSingletonTest {

    @Test
    fun `Singleton is truly single` () {

        // Set up the test instances
        val firstInstance = DataSingleton
        val secondInstance = DataSingleton

        // Get the instance values for each
        val constructors = DataSingleton::class.java.declaredConstructors
        constructors.forEach {
            it.isAccessible = true
            return@forEach
        }

        // Compare them... They should be the same
        assertSame(firstInstance, secondInstance)
    }

    @Test
    fun `Inject VALID CSV and verify` () {
        val inputStream : InputStream = this.javaClass.getResourceAsStream("workoutData.txt")

        val workoutList : MutableMap<Int, List<String>> =
            DataSingleton.getInstance().createMutableMapFromCSV(inputStream)

        assertFalse(workoutList.isNullOrEmpty())
    }

    @Test
    fun `Inject INVALID CSV and verify` () {
        val inputStream : InputStream = this.javaClass.getResourceAsStream("failsValidityCheck.txt")

        val workoutList : MutableMap<Int, List<String>> =
            DataSingleton.getInstance().createMutableMapFromCSV(inputStream)

        assertTrue(workoutList.isNullOrEmpty())
    }

    @Test
    fun `Calculate One Rep Max - VALID` () {

        // Setup injected data
        val inputArray1 = listOf<String>("1507705200000", "Oct 11 2017", "Back Squat", "1", "10", "100", "0")
        val inputArray2 = listOf<String>("1507705200000", "Oct 11 2017", "Back Squat", "1", "10", "100", "0")
        val inputMap : MutableMap<Int, List<String>> = mutableMapOf()
        inputMap.put(1, inputArray1)
        inputMap.put(2, inputArray2)

        // What we expect to get returned
        val expectedString = "{1=[1507705200000, Oct 11 2017, Back Squat, 1, 10, 100, 130]," +
                             " 2=[1507705200000, Oct 11 2017, Back Squat, 1, 10, 100, 130]}"

        // Call the test method 'calculateRM()' and get the result
        val actualString : String = (DataSingleton.getInstance().calculateRM(inputMap)).toString()

        // Yay! They match! (Unless someone's piddled around with my code
        assertEquals(expectedString, actualString)
    }

    @Test
    fun `Calculate One Rep Max - INVALID` () {

        // Setup injected data
        val inputArray1 = listOf<String>("1507705200000", "Oct 11 2017", "Back Squat", "1", "10", "100", "0")
        val inputArray2 = listOf<String>("1507705200000", "Oct 11 2017", "Back Squat", "1", "10", "100", "0")
        val inputMap : MutableMap<Int, List<String>> = mutableMapOf()
        inputMap.put(1, inputArray1)
        inputMap.put(2, inputArray2)

        // What we expect to get returned
        val nonExpectedString = "{1=[1507705200000, Oct 11 2017, Back Squat, 1, 10, 100, 130]," +
                                " 2=[1507705200000, Oct 11 2017, Back Squat, 1, 10, 100, 50000000]}"

        // Call 'calculateRM()' and get the result
        val actualString : String = (DataSingleton.getInstance().calculateRM(inputMap)).toString()

        // Yay! They match! (Unless someone's piddled around with my code)
        assertNotEquals(nonExpectedString, actualString)
    }

    @Test
    fun `Integration Test 1 - Get VALID RM` () {
        // Inject a valid CSV
        val inputStream : InputStream = this.javaClass.getResourceAsStream("integrationTest.txt")

        val workoutList : MutableMap<Int, List<String>> =
            DataSingleton.getInstance().createMutableMapFromCSV(inputStream)

        assertFalse(workoutList.isNullOrEmpty())

        // Call the test method 'calculateRM()' and get the result
        val actualString : String = (DataSingleton.getInstance().calculateRM(workoutList)).toString()

        // What we expect to get returned
        val expectedString = "{1=[1507705200000, Oct 11 2017, Back Squat, 1, 10, 45, 60]," +
                " 2=[1507705200000, Oct 11 2017, Back Squat, 1, 10, 135, 180]," +
                " 3=[1507705200000, Oct 11 2017, Back Squat, 1, 3, 185, 195]," +
                " 4=[1507705200000, Oct 11 2017, Back Squat, 1, 6, 245, 280]," +
                " 5=[1507705200000, Oct 11 2017, Back Squat, 1, 6, 245, 280]," +
                " 6=[1507705200000, Oct 11 2017, Back Squat, 1, 6, 245, 280]," +
                " 7=[1507705200000, Oct 11 2017, Back Squat, 1, 6, 245, 280]," +
                " 8=[1507186800000, Oct 05 2017, Barbell Bench Press, 1, 4, 45, 45]," +
                " 9=[1507186800000, Oct 05 2017, Barbell Bench Press, 1, 4, 125, 135]," +
                " 10=[1507186800000, Oct 05 2017, Barbell Bench Press, 1, 2, 185, 190]," +
                " 11=[1507186800000, Oct 05 2017, Barbell Bench Press, 1, 2, 205, 210]," +
                " 12=[1507186800000, Oct 05 2017, Barbell Bench Press, 1, 2, 225, 230]," +
                " 13=[1507186800000, Oct 05 2017, Barbell Bench Press, 1, 2, 225, 230]," +
                " 14=[1507186800000, Oct 05 2017, Barbell Bench Press, 1, 2, 225, 230]," +
                " 15=[1507186800000, Oct 05 2017, Barbell Bench Press, 1, 2, 225, 230]," +
                " 16=[1507186800000, Oct 05 2017, Barbell Bench Press, 1, 2, 225, 230]}"

        // Yay! They match! (Unless someone's piddled around with my code)
        assertEquals(expectedString, actualString)
    }

    @Test
    fun `Integration Test 2 - Get VALID exercise map` () {
        // Inject a valid CSV
        val inputStream : InputStream = this.javaClass.getResourceAsStream("integrationTest.txt")

        val workoutList : MutableMap<Int, List<String>> =
            DataSingleton.getInstance().createMutableMapFromCSV(inputStream)

        assertFalse(workoutList.isNullOrEmpty())

        // Call the 'calculateRM()' and get the result
        val updatedWorkoutList : MutableMap<Int, List<String>> =
            DataSingleton.getInstance().calculateRM(workoutList)

        assertFalse(updatedWorkoutList.isNullOrEmpty())

        // Call 'getExerciseMap()' and get the result
        // First, set the bool that confirms the One Rep Max value has been set
        DataSingleton.getInstance().setRMHasBeenCalculated(true)
        val actualString : String = (DataSingleton.getInstance().getExerciseMap(updatedWorkoutList)).toString()

        // What we expect to get returned
        val expectedString = "{Back Squat=[Back Squat, 280, 4], " +
                              "Barbell Bench Press=[Barbell Bench Press, 230, 5]}"

        // Yay! They match! (Unless someone's piddled around with my code)
        assertEquals(expectedString, actualString)
    }

    @Test
    fun `Integration Test 3 - Get INVALID exercise map` () {
        // Inject an INVALID CSV
        val inputStream : InputStream = this.javaClass.getResourceAsStream("failsValidityCheck.txt")

        val workoutList : MutableMap<Int, List<String>> =
            DataSingleton.getInstance().createMutableMapFromCSV(inputStream)

        assertTrue(workoutList.isNullOrEmpty()) // Doesn't break the app

        // Call the test method 'calculateRM()' and get the result
        val updatedWorkoutList : MutableMap<Int, List<String>> =
            DataSingleton.getInstance().calculateRM(workoutList)

        assertTrue(updatedWorkoutList.isNullOrEmpty()) // Doesn't break the app

        // Call 'getExerciseMap()' and get the result
        // First, set the bool that confirms the One Rep Max value has been set
        DataSingleton.getInstance().setRMHasBeenCalculated(true)
        val exerciseMap : MutableMap<String, MutableList<String>> =
            DataSingleton.getInstance().getExerciseMap(updatedWorkoutList)

        assertTrue(exerciseMap.isNullOrEmpty()) // Doesn't break the app
    }

    @Test
    fun `Integration Test 4 - Get VALID graph data` () {
        // Inject a valid CSV
        val inputStream : InputStream = this.javaClass.getResourceAsStream("integrationTest2.txt")

        val workoutList : MutableMap<Int, List<String>> =
            DataSingleton.getInstance().createMutableMapFromCSV(inputStream)

        assertFalse(workoutList.isNullOrEmpty())

        // Call the test method 'calculateRM()' and get the result
        val updatedWorkoutList : MutableMap<Int, List<String>> =
            DataSingleton.getInstance().calculateRM(workoutList)

        assertFalse(updatedWorkoutList.isNullOrEmpty())

        // Call 'getGraphData()' and get the result
        val actualString : String =
            (DataSingleton.getInstance().getGraphData("Back Squat", updatedWorkoutList)).toString()

        // What we expect to get returned
        val expectedString = "{Oct 11=60, Oct 12=195, Oct 13=280, Oct 14=280}"

        // Yay! They match! (Unless someone's piddled around with my code)
        assertEquals(expectedString, actualString)
    }
}
