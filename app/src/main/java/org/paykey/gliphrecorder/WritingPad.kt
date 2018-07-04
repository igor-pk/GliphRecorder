package org.paykey.gliphrecorder

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View

class WritingPad : View {

    private var path = Path()
    private val paint: Paint = Paint()

    private var pX: Float = 0f
    private var pY: Float = 0f

    private val TOUCH_TOLERANCE = 2

    private val pathList = mutableListOf<Path>()
    private val actionList: ArrayList<Action> = ArrayList()
    val strokeList: ArrayList<Stroke> = ArrayList()

    @JvmOverloads
    constructor(context: Context?, attrs: AttributeSet? = null, defStyleAttr: Int = 0, defStyleRes: Int = 0) : super(context, attrs, defStyleAttr, defStyleRes) {
        paint.apply {
            strokeWidth = 8f
            isAntiAlias = true
            isDither = true
            color = Color.BLACK
            style = Paint.Style.STROKE
            strokeJoin = Paint.Join.ROUND
            strokeCap = Paint.Cap.ROUND
        }

    }


    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        pathList.forEach { canvas.drawPath(it, paint) }
        canvas.drawPath(path, paint)
    }

    private val rectF1: RectF = RectF()
    private var downTime: Long = -1
    private var upTime: Long = 0

    override fun onTouchEvent(event: MotionEvent): Boolean {

        val x = event.x
        val y = event.y

        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                downTime = System.currentTimeMillis()
                path.reset()
                pX = x
                pY = y
                path.moveTo(x, y)
                path.lineTo(x,y)
                actionList.add(Action.Move(x, y))
                invalidate()

            }
            MotionEvent.ACTION_MOVE -> {
                for (i in 0 until event.historySize) {
                    doMove(event.getHistoricalX(i), event.getHistoricalY(i))
                }
                doMove(x, y)
                invalidate()
            }
            MotionEvent.ACTION_UP -> {
                val now = System.currentTimeMillis()
                val sinceLastStroke = if (upTime != 0L) downTime - upTime else -1
                val deltaT = now - downTime
                upTime = now
                path.lineTo(x, y)
                actionList.add(Action.Move(x, y))
                path.computeBounds(rectF1, true)
                pathList.add(path)
                path = Path()
                val stroke = Stroke(sinceLastStroke ,deltaT, RectF(rectF1), width to height, actionList.toList())
                strokeList.add(stroke)
            }
        }


        return true
    }

    private fun doMove(x: Float, y: Float) {
        val dx = Math.abs(x - pX)
        val dy = Math.abs(y - pY)
        if (dx >= TOUCH_TOLERANCE || dy >= TOUCH_TOLERANCE) {
            path.quadTo(pX, pY, (x + pX) / 2, (y + pY) / 2)
            actionList.add(Action.Quad(pX, pY, (x + pX) / 2, (y + pY) / 2))
            pX = x
            pY = y
        }
    }

    fun clear() {
        path.reset()
        actionList.clear()
        pathList.clear()
        strokeList.clear()
        invalidate()
    }
}

sealed class Action(val action: Char) {
    data class Move(val x: Float, val y: Float) : Action('M')
    data class Quad(val x1: Float, val y1: Float, val x2: Float, val y2: Float): Action('Q')
}

data class Stroke(val sinceLastStroke: Long ,val duration: Long, val pathBounds: RectF, val viewSize: Pair<Int, Int>, val actions: List<Action>)
