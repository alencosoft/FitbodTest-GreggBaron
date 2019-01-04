package com.fitbod.my.test

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect

// The Header Array Values
private const val HEADER_EXERCISE_NAME : Int = 0
private const val HEADER_ONE_REP_MAX_WEIGHT : Int = 1
private const val HEADER_ONE_REP_MAX_COUNT : Int = 2
private const val HEADER_LBS_STRING_LITERAL : Int = 3

// The 'gutter' white space between the bottom labels (ie; the Textfields under the graph)
private const val MIN_SPACE_ALLOWED_BETWEEN_TEXTFIELDS : Int = 40

/**
 * The interface which only exposes certain public methods. Every object/class that uses this class
 * should be of type IChartCalc
 */
interface IChartCalc {
    fun drawLeftLabel(whichone : Int)
    fun drawHorizontalLine(whichone: Int)
    fun drawBottomLabel(whichone : Int)
    fun drawVerticalLine(whichone : Int)
    fun drawHeaderText()
    fun drawPlottedCirclesLines()
}

class ChartPositionCalculator constructor(
    context : Context,
    headerArrayValues : ArrayList<String>,
    canvas : Canvas) : IChartCalc {

    // The Header data (similar to the list in MainActivity)
    private val mHeaderArrayValues : ArrayList<String> = headerArrayValues

    // The canvas and context objects from ChartActivity
    private val mCanvas : Canvas = canvas
    private val mContext : Context = context

    // Component position arrays (Specs)
    private var mGraphSpecs : ComponentSpecs = ComponentSpecs(context)
    private var mGutterAboveGraphSpecs : ComponentSpecs = ComponentSpecs(context)
    private var mGutterBelowGraphSpecs : ComponentSpecs = ComponentSpecs(context)
    private var mGutterLeftGraphSpecs : ComponentSpecs = ComponentSpecs(context)
    private var mMarginSpecs : ComponentSpecs = ComponentSpecs(context)
    private var mCanvasSpecs : ComponentSpecs = ComponentSpecs(context)

    // The Header labels
    private var mHeaderLabels : MutableList<String> = mutableListOf()
    private var mHeaderLabelsSpecs : MutableList<ComponentSpecs> = mutableListOf()

    // The Graph labels (left and bottom of Graph)
    private var mGraphLabelsBottom : MutableList<String> = mutableListOf()
    private var mGraphLabelsBottomSpecs : MutableList<ComponentSpecs> = mutableListOf()
    private var mGraphLabelsLeft : MutableList<Int> = mutableListOf()
    private var mGraphLabelsLeftFinal : MutableList<String> = mutableListOf()
    private var mGraphLabelsLeftSpecs : MutableList<ComponentSpecs> = mutableListOf()

    // Is device orientation portrait or landscape?
    private var mIsDeviceOrientationPortrait : Boolean = true

    // Represents the 'weight' values that are evenly spaced for the Left Label strings
    private var mLeftLabelSpacingValue : Int = 0

    // The graph lines, plotted lines and plotted circles
    private var mGraphLineSpecs : ComponentSpecs = ComponentSpecs(context)
    private var mPlottedLineSpecs : ComponentSpecs = ComponentSpecs(context)
    private var mPlottedCircleSpecs : ComponentSpecs = ComponentSpecs(context)

    init {
        startCalculations()
    }

    /** The initial chart data comes from the DataSingleton class. It's raw form is a MutableMap:
     *
     *      Example: {Sep 15=270, Sep 21=275, Sep 28=280, Oct 4=280, Oct 11=285}
     *
     * This map is converted into 2 separate arrays with their raw form looking like this:
     *
     *           - Bottom Labels: [Sep 15, Sep 21, Sep 28, Oct 4, Oct 11]
     *           - Left Labels: [270, 275, 280, 280, 285]
     *
     * And, finally, the displayed 'Left' labels look like this:
     *
     *           - Final Left Labels: [270 lbs, 275 lbs, 280 lbs, 285 lbs]
     *
     * The layout process is:
     *
     * STEP 1: Set the various class objects/variables.
     * STEP 2: Set the specs for 'margins' and 'gutters' (both horizontal and vertical).
     * STEP 3: Set Left Label text and specs
     * STEP 4: Set Header label text and specs
     * STEP 5: Make the remaining space fit for the Bottom Labels (or adjust margins/gutters/text size
     *         to make it all fit).
     * STEP 6: Set initial heights of Header textFields and Graph.
     * STEP 7: Finally, make the vertical dimensions fit total height
     * STEP 8: Drink champagne as we're done! :)
     */
    private fun startCalculations() {

        // STEP 1: Set the various class objects/variables
        setGraphData(mHeaderArrayValues[HEADER_EXERCISE_NAME])
        setCanvasHeightAndWidth()
        setGraphLineSpecs()
        setPlottedLineAndCircleSpecs()

        // STEP 2: Set margin and gutter specs
        setMarginAndGutterSpecs()

        // STEP 3: Set Left Label text and specs
        setLeftLabelFinalText()
        setAllLabelInitSpecs()

        // STEP 4: Set Header label text and specs
        setHeaderLabelTextAndSpecs()

        // STEP 5: Make the remaining space fit for the Bottom Labels (or adjust margins/gutters/text size
        //         to make it all fit).
        setGraphHorizontalSize()
        makeHorizontalLayoutFit()

        // STEP 6: Calculate initial heights
        setGraphHeight()

        // STEP 7: Make the vertical dimensions fit total height
        makeVerticalLayoutFit()
    }

    /**
     *  Break down the mutable map of chart data into X and Y axis values
     *  An example of one item looks like this: 'Sep 15=270'
     */
    private fun setGraphData(exerciseName : String) {
        val graphData: MutableMap<String, Int> = DataSingleton.getInstance().doGetGraphData(exerciseName)

        graphData.forEach {
            mGraphLabelsBottom.add(it.key)
            mGraphLabelsLeft.add(it.value)
        }
    }

    /**
     * Easier to store these vales than to continue calling the framework for the device drawable size.
     */
    private fun setCanvasHeightAndWidth() {
        mCanvasSpecs.setAllPositions(0, 0, mCanvas.getHeight(), mCanvas.getWidth())

        // Since we have the height/width values let's set the device orientation now.
        // NOTE: The default orientation is set to 'true'
        if(mCanvasSpecs.getHeight() < mCanvasSpecs.getWidth()) {
            mIsDeviceOrientationPortrait = false
        }
    }

    /**
     * The Graph lines
     */
    private fun setGraphLineSpecs() {
        mGraphLineSpecs.setPaintObject(PAINT_GRAPH_GRID_LINES)
    }

    /**
     * The plotted lines and circles
     */
    private fun setPlottedLineAndCircleSpecs() {
        mPlottedLineSpecs.setPaintObject(PAINT_PLOTTED_LINES)
        mPlottedCircleSpecs.setPaintObject(PAINT_PLOTTED_CIRCLES)
    }

    /**
     * 4 margins, of course, and 3 gutters (above, left and below graph).
     * I came up with these values based on the provided mockup.
     */
    private fun setMarginAndGutterSpecs() {
        mMarginSpecs.setAllPositions(
            0,
            0,
            (.05 * mCanvasSpecs.getWidth()).toInt(),
            (.05 * mCanvasSpecs.getWidth()).toInt())

        mGutterAboveGraphSpecs.setAllPositions(
            0,
            0,
            (.05 * mCanvasSpecs.getWidth()).toInt(),
            (.05 * mCanvasSpecs.getWidth()).toInt())

        mGutterLeftGraphSpecs.setAllPositions(
            0,
            0,
            (.05 * mCanvasSpecs.getWidth()).toInt(),
            (.05 * mCanvasSpecs.getWidth()).toInt())

        mGutterBelowGraphSpecs.setAllPositions(
            0,
            0,
            (.05 * mCanvasSpecs.getWidth()).toInt(),
            (.05 * mCanvasSpecs.getWidth()).toInt())
    }

    /**
     * This is probably the most important part of calculating the chart layout. Each of the Left labels
     * (below graph labels) must fit in the space available with some 'gutter' space between the labels so they
     * don't just appear like one long string of text.
     */
    private fun setLeftLabelFinalText() {
        val maxWeightValue = mGraphLabelsLeft.max() as Int
        var minWeightValue = mGraphLabelsLeft.min() as Int

        // Set the Left Label text strings so that each one is equally spaced in value. The Left label
        // value difference between the max and min must be divisible by 3 and 5 (or 15)
        do {
            minWeightValue--
        } while ((maxWeightValue - minWeightValue).rem(15) != 0)

        // Now store the calculated Left label values (strings)
        mLeftLabelSpacingValue = ((maxWeightValue - minWeightValue) / 3).toInt()

        for (idx in 0..3) {
            mGraphLabelsLeftFinal.add(
                (mGraphLabelsLeft.max() as Int -
                (mLeftLabelSpacingValue * idx)).toString() +
                " " +
                mContext.getResources().getString(R.string.lbs_literal))
        }
    }

    /**
     * The initial specifications for Left Labels
     */
    private fun setAllLabelInitSpecs() {
        // Left labels
        for (idx in 0..3) {
            mGraphLabelsLeftSpecs.add(ComponentSpecs(mContext))
            mGraphLabelsLeftSpecs[idx].setPaintObject(PAINT_LABEL_TEXT)
            mGraphLabelsLeftSpecs[idx].setTextSize(52f)
            mGraphLabelsLeftSpecs[idx].setAllPositions(0, mMarginSpecs.getWidth(), 0, 0)
        }

        // Bottom Labels
        for (idx in 0..4) {
            mGraphLabelsBottomSpecs.add(ComponentSpecs(mContext))
            mGraphLabelsBottomSpecs[idx].setPaintObject(PAINT_LABEL_TEXT)
            mGraphLabelsBottomSpecs[idx].setTextSize(52f)
            mGraphLabelsBottomSpecs[idx].setAllPositions(0, 0, 0, 0)
        }
    }

    /**
     * Set the Header label positioning and strings
     */
    private fun setHeaderLabelTextAndSpecs() {
        // Set the 4 text strings
        mHeaderLabels.add(HEADER_EXERCISE_NAME, mHeaderArrayValues[HEADER_EXERCISE_NAME])
        mHeaderLabels.add(HEADER_ONE_REP_MAX_WEIGHT, mHeaderArrayValues[HEADER_ONE_REP_MAX_WEIGHT])

        // We need to make adjustments for the 'Number of RM Records' text
        var exerciseNumRecords: String = mHeaderArrayValues[HEADER_ONE_REP_MAX_COUNT] + " " +
                mContext.getResources().getString(R.string.rm_record)

        if (mHeaderArrayValues[HEADER_ONE_REP_MAX_COUNT].toInt() > 1) {
            exerciseNumRecords += mContext.getResources().getString(R.string.plural)
        }

        mHeaderLabels.add(HEADER_ONE_REP_MAX_COUNT, exerciseNumRecords)
        mHeaderLabels.add(HEADER_LBS_STRING_LITERAL, mContext.getResources().getString(R.string.lbs_literal))

        // Add all 4 textField specs
        for(idx in 0..3) { mHeaderLabelsSpecs.add(ComponentSpecs(mContext)) }

        // Initialize all 4 paint specs
        mHeaderLabelsSpecs[HEADER_EXERCISE_NAME].setPaintObject(PAINT_HEADER_TEXT_LARGE)
        mHeaderLabelsSpecs[HEADER_ONE_REP_MAX_WEIGHT].setPaintObject(PAINT_HEADER_TEXT_LARGE)
        mHeaderLabelsSpecs[HEADER_ONE_REP_MAX_COUNT].setPaintObject(PAINT_HEADER_TEXT_SMALL)
        mHeaderLabelsSpecs[HEADER_LBS_STRING_LITERAL].setPaintObject(PAINT_HEADER_TEXT_SMALL)

        setTextSizeForAllLabels(52f)

        // Initialize all 4 position specs
        // 1) Exercise Name
        var bounds = Rect()

        mHeaderLabelsSpecs[HEADER_EXERCISE_NAME].getPaintObject().getTextBounds(
            mHeaderLabels[HEADER_EXERCISE_NAME],
            0, (mHeaderLabels[HEADER_EXERCISE_NAME]).toString().length,
            bounds)

        var deviceOrientationAdjustmentHeight : Double = 1.0

        if(mIsDeviceOrientationPortrait) {
            deviceOrientationAdjustmentHeight = 1.5
        }

        mHeaderLabelsSpecs[HEADER_EXERCISE_NAME].setAllPositions(
            (mMarginSpecs.getHeight() * deviceOrientationAdjustmentHeight).toInt() + (bounds.height() / 2),
            mMarginSpecs.getWidth(),
            0,
            0)

        // 2) RM Max Weight
        bounds = Rect()

        mHeaderLabelsSpecs[HEADER_ONE_REP_MAX_WEIGHT].getPaintObject().getTextBounds(
            mHeaderLabels[HEADER_ONE_REP_MAX_WEIGHT],
            0, (mHeaderLabels[HEADER_ONE_REP_MAX_WEIGHT]).toString().length,
            bounds)

        mHeaderLabelsSpecs[HEADER_ONE_REP_MAX_WEIGHT].setAllPositions(
            (mMarginSpecs.getHeight() * deviceOrientationAdjustmentHeight).toInt() + (bounds.height() / 2),
            mCanvasSpecs.getWidth() - mMarginSpecs.getWidth() - bounds.width(),
            0,
            0)

        // 3) RM Count
        val boundsOfExerciseNameTextField = Rect()

        mHeaderLabelsSpecs[HEADER_EXERCISE_NAME].getPaintObject().getTextBounds(
            mHeaderLabels[HEADER_EXERCISE_NAME],
            0, (mHeaderLabels[HEADER_EXERCISE_NAME]).toString().length,
            boundsOfExerciseNameTextField)

        bounds = Rect()

        mHeaderLabelsSpecs[HEADER_ONE_REP_MAX_COUNT].getPaintObject().getTextBounds(
            mHeaderLabels[HEADER_ONE_REP_MAX_COUNT],
            0, (mHeaderLabels[HEADER_ONE_REP_MAX_COUNT]).toString().length,
            bounds)

        mHeaderLabelsSpecs[HEADER_ONE_REP_MAX_COUNT].setAllPositions(
            mHeaderLabelsSpecs[HEADER_EXERCISE_NAME].getTop() +
                    boundsOfExerciseNameTextField.height() +
                    (bounds.height() * .25).toInt(),
            mMarginSpecs.getWidth(),
            0,
            0)

        // 4) 'lbs' string literal
        bounds = Rect()

        mHeaderLabelsSpecs[HEADER_LBS_STRING_LITERAL].getPaintObject().getTextBounds(
            mHeaderLabels[HEADER_LBS_STRING_LITERAL],
            0, (mHeaderLabels[HEADER_LBS_STRING_LITERAL]).toString().length,
            bounds)

        mHeaderLabelsSpecs[HEADER_LBS_STRING_LITERAL].setAllPositions(
            mHeaderLabelsSpecs[HEADER_EXERCISE_NAME].getTop() +
                    boundsOfExerciseNameTextField.height() +
                    (bounds.height() * .25).toInt(),
            mCanvasSpecs.getWidth() - mMarginSpecs.getWidth() - bounds.width(),
            mHeaderLabelsSpecs[HEADER_EXERCISE_NAME].getTop() +
                    boundsOfExerciseNameTextField.height() +
                    (bounds.height() * 1.25).toInt(),
            0)
    }

    /**
     * Text size for labels has changed due to changes while making everything fit horizontally
     */
    private fun setTextSizeForAllLabels(textSize : Float) {
        for (idx in 0..3) {
            mGraphLabelsLeftSpecs[idx].setTextSize(textSize)
        }

        for (idx in 0..4) {
            mGraphLabelsBottomSpecs[idx].setTextSize(textSize)
        }

        var largeTextAdjustment : Double = 1.1
        var smallTextAdjustment : Double = 0.9

        if(mIsDeviceOrientationPortrait) {
            largeTextAdjustment = 1.25
            smallTextAdjustment = 1.0
        }

        mHeaderLabelsSpecs[HEADER_EXERCISE_NAME].setTextSize((textSize * largeTextAdjustment).toFloat())
        mHeaderLabelsSpecs[HEADER_ONE_REP_MAX_WEIGHT].setTextSize((textSize * largeTextAdjustment).toFloat())
        mHeaderLabelsSpecs[HEADER_ONE_REP_MAX_COUNT].setTextSize((textSize * smallTextAdjustment).toFloat())
        mHeaderLabelsSpecs[HEADER_LBS_STRING_LITERAL].setTextSize((textSize * smallTextAdjustment).toFloat())
    }

    /**
     * Helper method that gets the max width for all 4 Left labels
     */
    private fun getLeftLabelMaxWidth() : Int {
        var maxWidth = 0

        for(idx in 0..3) {
            val measuredTextWidth : Int =
                (mGraphLabelsLeftSpecs[idx].getPaintObject().measureText(mGraphLabelsLeftFinal[idx])).toInt()

            if(measuredTextWidth > maxWidth) {
                maxWidth = measuredTextWidth
            }
        }

        return maxWidth
    }

    /**
     * We should now have all the information we need to calculate the actual Graph 'box' horizontal dimensions.
     */
    private fun setGraphHorizontalSize() {
        mGraphSpecs.setLeft(
            mMarginSpecs.getWidth() +
            getLeftLabelMaxWidth() +
            mGutterLeftGraphSpecs.getWidth())

        mGraphSpecs.setRight(
            mCanvasSpecs.getWidth() -
            (mMarginSpecs.getWidth() * 1.5).toInt())
    }

    /**
     * One of the most complex methods here. We may need to adjust sizes of the vertical 'gutters' and
     * label text sizes to make sure everything fits on the screen (especially when the device is in
     * portrait view)
     */
    private fun makeHorizontalLayoutFit() {
        var widthOfText : Int
        var totalSpaceUsedByText : Int
        var numPassesThroughDoLoop : Int = 0
        var textSize : Float = mGraphLabelsBottomSpecs[0].getTextSize()

        do {
            numPassesThroughDoLoop += 1
            widthOfText = 0

            for (idx in 0..4) {
                widthOfText +=
                        mGraphLabelsBottomSpecs[idx].getPaintObject().measureText(mGraphLabelsBottom[idx]).toInt()
            }

            widthOfText += (MIN_SPACE_ALLOWED_BETWEEN_TEXTFIELDS * 4) + mMarginSpecs.getWidth()

            val xValueAtStartOfFirstText : Int =
                mGraphSpecs.getLeft() -
                (mGraphLabelsBottomSpecs[0].getPaintObject().measureText(mGraphLabelsBottom[0]) / 2).toInt()

            val xValueAtEndOfLastText : Int =
                mGraphSpecs.getRight() -
                (mGraphLabelsBottomSpecs[4].getPaintObject().measureText(mGraphLabelsBottom[4]) / 2).toInt()

            totalSpaceUsedByText = xValueAtEndOfLastText - xValueAtStartOfFirstText

            if (widthOfText > totalSpaceUsedByText) {
                when {

                    // Adjust the gutter sizes first
                    numPassesThroughDoLoop == 1 -> {
                        mGutterLeftGraphSpecs.setRight(mGutterLeftGraphSpecs.getRight() - 1)
                    }

                    // Adjust the text size second
                    numPassesThroughDoLoop == 2 -> {
                        textSize -= 1f
                        setTextSizeForAllLabels(textSize)
                        numPassesThroughDoLoop = 0
                    }
                }

                setGraphHorizontalSize()
            }
        } while(widthOfText > totalSpaceUsedByText)
    }

    /**
     * Takes into account all of the 'gutters', 'margins' and Header height.
     */
    private fun setGraphHeight() {
        // Top of Graph
        mGraphSpecs.setTop(mHeaderLabelsSpecs[HEADER_LBS_STRING_LITERAL].getBottom() +
                mGutterAboveGraphSpecs.getHeight())

        // Make each graph section a "square" if possible
        if(mIsDeviceOrientationPortrait) {
            mGraphSpecs.setBottom(mGraphSpecs.getTop() + ((mGraphSpecs.getWidth() / 4) * 3))

        // Set Graph height based on heights of other components
        } else {
            val bounds = Rect()

            mGraphLabelsBottomSpecs[0].getPaintObject().getTextBounds(mGraphLabelsBottom[0],
                0, (mGraphLabelsBottom[0]).toString().length,
                bounds)

            mGraphSpecs.setBottom(
                mCanvasSpecs.getHeight() -
               (mMarginSpecs.getHeight() / 2) -
                bounds.height() -
                mGutterBelowGraphSpecs.getHeight())
        }
    }

    /**
     * Another complex method that adjusts gutter and graph heights to make the layout fit. This
     * is really only necessary if the device is in landscape view.
     */
    private fun makeVerticalLayoutFit() {
        var totalHeight : Int
        var numPassesThroughDoLoop : Int = 0
        val bounds = Rect()

        mGraphLabelsBottomSpecs[0].getPaintObject().getTextBounds(mGraphLabelsBottom[0],
            0, (mGraphLabelsBottom[0]).toString().length,
            bounds)

        do {
            numPassesThroughDoLoop += 1
            totalHeight =
                    (mMarginSpecs.getHeight() * 1.5).toInt() +
                    (mHeaderLabelsSpecs[HEADER_LBS_STRING_LITERAL].getBottom() -
                            mHeaderLabelsSpecs[HEADER_EXERCISE_NAME].getTop()) +
                    mGutterAboveGraphSpecs.getHeight() +
                    mGraphSpecs.getHeight() +
                    mGutterBelowGraphSpecs.getHeight() +
                    bounds.height()

            if(totalHeight > mCanvasSpecs.getHeight()) {
                when {

                    // Adjust the gutter sizes first
                    numPassesThroughDoLoop < 4 -> {
                        mGraphSpecs.setBottom(mGraphSpecs.getBottom() - 1)
                    }

                    // Adjust the graph height second
                    numPassesThroughDoLoop == 4 -> {
                        mGutterAboveGraphSpecs.setBottom(mGutterAboveGraphSpecs.getBottom() - 1)
                        mGutterBelowGraphSpecs.setBottom(mGutterBelowGraphSpecs.getBottom() - 1)
                        numPassesThroughDoLoop = 0
                    }
                }
            }

        } while (totalHeight > mCanvasSpecs.getHeight())
    }

    /** #########################################################################
        ########################    PUBLIC ACCESSORS     ########################
        ######################################################################### */

    override fun drawLeftLabel(whichone : Int) {
        val bounds = Rect()
        val paint : Paint = mGraphLabelsLeftSpecs[whichone].getPaintObject()

        paint.getTextBounds(mGraphLabelsLeftFinal[whichone],
            0, (mGraphLabelsLeftFinal[whichone]).toString().length,
            bounds)

        val yValue =
            (mGraphSpecs.getTop() + ((mGraphSpecs.getHeight() / 3) * whichone) + (bounds.height() / 2)).toFloat()

        mCanvas.drawText(
            mGraphLabelsLeftFinal[whichone],
            mGraphLabelsLeftSpecs[whichone].getLeft().toFloat(),
            yValue,
            paint)
    }

    override fun drawHorizontalLine(whichone: Int) {
        val yValue = (mGraphSpecs.getTop() + ((mGraphSpecs.getHeight() / 3) * whichone)).toFloat()
        val xValueStart = mGraphSpecs.getLeft() - (mGutterLeftGraphSpecs.getWidth() / 2).toFloat()
        val xValueEnd = mCanvasSpecs.getWidth().toFloat() - mMarginSpecs.getWidth().toFloat()

        mCanvas.drawLine(
            xValueStart,
            yValue,
            xValueEnd,
            yValue,
            mGraphLineSpecs.getPaintObject())
    }

    override fun drawBottomLabel(whichone : Int) {
        val bounds = Rect()
        val paint : Paint = mGraphLabelsBottomSpecs[whichone].getPaintObject()

        paint.getTextBounds(mGraphLabelsBottom[0],
            0, (mGraphLabelsBottom[0]).toString().length,
            bounds)

        val horizontalSpacing = mGraphSpecs.getWidth() / 4

        var yValue = (mGraphSpecs.getBottom() +
                mGutterBelowGraphSpecs.getHeight() +
                bounds.height()).toFloat()

        val centeredXValue =
            ((horizontalSpacing * whichone).toFloat() +
                    mGraphSpecs.getLeft().toFloat() -
                    (bounds.width() / 2).toFloat()).toFloat()

        mCanvas.drawText(
            mGraphLabelsBottom[whichone],
            centeredXValue,
            yValue,
            paint)
    }

    override fun drawVerticalLine(whichone : Int) {
        val horizontalSpacing = mGraphSpecs.getWidth() / 4
        val xForGridLines = ((horizontalSpacing * whichone) + mGraphSpecs.getLeft()).toFloat()
        val bottomOfGridLines =
            (mGraphSpecs.getTop() +
            mGraphSpecs.getHeight() +
            (mGutterBelowGraphSpecs.getHeight() / 2)).toFloat()

        mCanvas.drawLine(
            xForGridLines,
            mGraphSpecs.getTop().toFloat(),
            xForGridLines,
            bottomOfGridLines,
            mGraphLineSpecs.getPaintObject())
    }

    override fun drawHeaderText() {

        mCanvas.apply {
            // Exercise Name
            drawText(
                mHeaderLabels[HEADER_EXERCISE_NAME],                          // The text
                mHeaderLabelsSpecs[HEADER_EXERCISE_NAME].getLeft().toFloat(), // X position
                mHeaderLabelsSpecs[HEADER_EXERCISE_NAME].getTop().toFloat(),  // Y Postion
                mHeaderLabelsSpecs[HEADER_EXERCISE_NAME].getPaintObject())    // Paint object

            // Exercise RM Value (weight)
            drawText(
                mHeaderLabels[HEADER_ONE_REP_MAX_WEIGHT],                           // The text
                mHeaderLabelsSpecs[HEADER_ONE_REP_MAX_WEIGHT].getLeft().toFloat(), // X position
                mHeaderLabelsSpecs[HEADER_ONE_REP_MAX_WEIGHT].getTop().toFloat(),  // Y position
                mHeaderLabelsSpecs[HEADER_ONE_REP_MAX_WEIGHT].getPaintObject())    // Paint object

            // Number of RM Records
            drawText(
                mHeaderLabels[HEADER_ONE_REP_MAX_COUNT],                          // The text
                mHeaderLabelsSpecs[HEADER_ONE_REP_MAX_COUNT].getLeft().toFloat(), // X position
                mHeaderLabelsSpecs[HEADER_ONE_REP_MAX_COUNT].getTop().toFloat(),  // Y position
                mHeaderLabelsSpecs[HEADER_ONE_REP_MAX_COUNT].getPaintObject())    // Paint object

            // String Literal 'lbs'
            drawText(
                mHeaderLabels[HEADER_LBS_STRING_LITERAL],                          // The text
                mHeaderLabelsSpecs[HEADER_LBS_STRING_LITERAL].getLeft().toFloat(), // X position
                mHeaderLabelsSpecs[HEADER_LBS_STRING_LITERAL].getTop().toFloat(),  // Y position
                mHeaderLabelsSpecs[HEADER_LBS_STRING_LITERAL].getPaintObject())    // Paint object
        }
    }

    override fun drawPlottedCirclesLines() {
        val graphMaxLabelValue = mGraphLabelsLeft.max() as Int
        val graphVerticalLineSpacing = mGraphSpecs.getWidth() / 4
        var previousXPlotPoint : Float = 0f
        var previousYPlotPoint : Float = 0f

        mCanvas.apply {
            for (idx in 0..4) {
                val xPlotPoint = ((graphVerticalLineSpacing * idx).toFloat() + mGraphSpecs.getLeft()).toFloat()

                val graphPointAsPercentageOfGraphMax = (
                        (graphMaxLabelValue.toFloat() - mGraphLabelsLeft[idx]).toFloat() /
                                (mLeftLabelSpacingValue * 3).toFloat())

                val yPlotPoint =
                    ((mGraphSpecs.getHeight().toFloat() *
                    graphPointAsPercentageOfGraphMax.toFloat()) +
                    mGraphSpecs.getTop().toFloat()).toFloat()

                drawCircle(
                    xPlotPoint,
                    yPlotPoint,
                    mContext.getResources().getDimension(R.dimen.graph_circle_radius),
                    mPlottedCircleSpecs.getPaintObject())

                if (idx > 0) {
                    drawLine(
                        xPlotPoint,
                        yPlotPoint,
                        previousXPlotPoint,
                        previousYPlotPoint,
                        mPlottedLineSpecs.getPaintObject())
                }

                previousXPlotPoint = xPlotPoint
                previousYPlotPoint = yPlotPoint
            }
        }
    }
}