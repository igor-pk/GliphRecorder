package org.paykey.gliphrecorder

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View

class PathView @JvmOverloads constructor(context: Context?, attrs: AttributeSet? = null, defStyleAttr: Int = 0, defStyleRes: Int = 0) : View(context, attrs, defStyleAttr, defStyleRes) {

    private var path = Path()
    private val paint: Paint = Paint()

    init {
        paint.apply {
            strokeWidth = 3f
            isAntiAlias = true
            isDither = true
            color = Color.BLACK
            style = Paint.Style.STROKE
            strokeJoin = Paint.Join.ROUND
            strokeCap = Paint.Cap.ROUND
        }
    }

    fun init(actions: List<Action>) {
        actions.forEach {
            when (it) {
                is Action.Move -> path.moveTo(it.x, it.y)
                is Action.Quad -> path.quadTo(it.x1, it.y1, it.x2, it.y2)
            }
        }
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)

        val matrix = Matrix().apply {
            setScale(0.1f, 0.1f)
        }
        path.transform(matrix)
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        canvas?.drawPath(path, paint)
    }
}