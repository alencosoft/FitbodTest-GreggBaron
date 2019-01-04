package com.fitbod.my.test

import android.content.Context
import android.graphics.Paint
import android.support.v4.content.ContextCompat

// The Color objects
private const val COLOR_PRIMARY : Int = 0
private const val COLOR_SECONDARY : Int = 1
private const val COLOR_GRAPH_LINES : Int = 2
private const val COLOR_GRAPH_PLOTS : Int = 3

// The Paint objects
const val PAINT_LABEL_TEXT : Int = 0
const val PAINT_HEADER_TEXT_LARGE : Int = 1
const val PAINT_HEADER_TEXT_SMALL : Int = 2
const val PAINT_GRAPH_GRID_LINES : Int = 3
const val PAINT_PLOTTED_CIRCLES : Int = 4
const val PAINT_PLOTTED_LINES : Int = 5

class ComponentSpecs constructor(context : Context) {

    // The Color and Paint Objects
    private var mColorsList : MutableList<Int> = mutableListOf()
    private var mPaintObjectList : MutableList<Paint> = mutableListOf()

    // Positions
    private var mTop : Int = -1
    private var mLeft : Int = -1
    private var mBottom : Int = -1
    private var mRight : Int = -1

    // Paint object
    private var mPaint : Paint = Paint()

    init {
        setColorAndPaintObjects(context)
    }

    /**
     * All of the Color and Paint objects needed to draw the screen. All of these array locations (indices)
     * are declared as constants to help ensure type safety. The companion getter is getPaintObject(index).
     */
    private fun setColorAndPaintObjects(context : Context) {
        mColorsList.add(COLOR_PRIMARY, ContextCompat.getColor(context, R.color.colorPrimary))
        mColorsList.add(COLOR_SECONDARY, ContextCompat.getColor(context, R.color.colorSecondary))
        mColorsList.add(COLOR_GRAPH_LINES, ContextCompat.getColor(context, R.color.colorListDivider))
        mColorsList.add(COLOR_GRAPH_PLOTS, ContextCompat.getColor(context, R.color.colorToolbarBackground))

        var paint : Paint = Paint()
        paint.color = mColorsList[COLOR_PRIMARY]
        paint.textAlign = Paint.Align.LEFT
        mPaintObjectList.add(PAINT_LABEL_TEXT, paint)

        paint = Paint()
        paint.color = mColorsList[COLOR_PRIMARY]
        paint.textAlign = Paint.Align.LEFT
        mPaintObjectList.add(PAINT_HEADER_TEXT_LARGE, paint)

        paint = Paint()
        paint.color = mColorsList[COLOR_SECONDARY]
        paint.textAlign = Paint.Align.LEFT
        mPaintObjectList.add(PAINT_HEADER_TEXT_SMALL, paint)

        paint = Paint()
        paint.color = mColorsList[COLOR_GRAPH_LINES]
        paint.strokeWidth = context.getResources().getDimension(R.dimen.line_stroke_width)
        mPaintObjectList.add(PAINT_GRAPH_GRID_LINES, paint)

        paint = Paint()
        paint.color = mColorsList[COLOR_GRAPH_PLOTS]
        paint.style = Paint.Style.FILL
        mPaintObjectList.add(PAINT_PLOTTED_CIRCLES, paint)

        paint = Paint()
        paint.color = mColorsList[COLOR_GRAPH_PLOTS]
        paint.strokeWidth = context.getResources().getDimension(R.dimen.line_stroke_width)
        mPaintObjectList.add(PAINT_PLOTTED_LINES, paint)
    }

    /** #########################################################################
        ########################    PUBLIC ACCESSORS     ########################
        ######################################################################### */

    fun setAllPositions(top : Int, left : Int, bottom : Int, right : Int) {
        mTop = top
        mLeft = left
        mBottom = bottom
        mRight = right
    }

    fun setTop(top : Int) { mTop = top }
    fun getTop() : Int { return mTop }

    fun setLeft(left : Int) { mLeft = left }
    fun getLeft() : Int { return mLeft }

    fun setBottom(bottom : Int) { mBottom = bottom }
    fun getBottom() : Int { return mBottom }

    fun setRight(right : Int) { mRight = right }
    fun getRight() : Int { return mRight }

    fun getHeight() : Int {
        if(mTop == -1 || mBottom == -1) { return -1 }
        return mBottom - mTop
    }

    fun getWidth() : Int {
        if(mLeft == -1 || mRight == -1) { return -1 }
        return mRight - mLeft
    }

    fun setPaintObject(whichOne : Int) { mPaint = mPaintObjectList.get(whichOne) }
    fun getPaintObject() : Paint { return mPaint }

    fun setTextSize(textSize : Float) { mPaint.setTextSize(textSize) }
    fun getTextSize() : Float { return mPaint.getTextSize() }
}