package io.github.landerlyoung.flashappsearch.search.ui

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.ColorFilter
import android.graphics.Paint
import android.graphics.PixelFormat
import android.graphics.Rect
import android.graphics.Typeface
import android.graphics.drawable.Drawable
import io.github.landerlyoung.flashappsearch.search.model.Input
import kotlin.math.min

/**
 * <pre>
 * Author: taylorcyang@tencent.com
 * Date:   2018-06-25
 * Time:   13:00
 * Life with Passion, Code with Creativity.
 * </pre>
 */
class KeyDrawable(_input: Input? = null) : Drawable() {
    var input: Input? = null
        set(value) {
            field = value
            if (value != null && value.keys.isNotEmpty()) {
                firstLevel = value.keys[0].toString().toUpperCase()
                secondLevel = if (value.keys.size > 1) {
                    String(value.keys.subList(1, value.keys.size).toCharArray()).toUpperCase()
                } else {
                    null
                }
                updateTextSize()
            } else {
                firstLevel = null
                secondLevel = null
            }
        }

    init {
        input = _input
    }

    companion object {
        private const val drawBaseLine = false
    }

    private val paint = Paint().apply {
        flags = Paint.ANTI_ALIAS_FLAG
        typeface = Typeface.MONOSPACE
    }

    private var firstLevel: String? = null
    private var secondLevel: String? = null
    private var firstLevelSize = 0f
    private var secondLevelSize = 0f

    var firstLevelColor: Int = Color.BLACK
    val secondLevelColor: Int = 0xAA000000.toInt()

    private fun updateTextSize() {
        val bounds = bounds
        val size = min(bounds.width() * 1.7f, bounds.height().toFloat())
        // 2, 5
        firstLevelSize = size * 5 / 10
        secondLevelSize = size * 2 / 10
    }

    override fun onBoundsChange(bounds: Rect?) {
        super.onBoundsChange(bounds)
        updateTextSize()
    }

    override fun draw(canvas: Canvas) {
        val width = bounds.width()
        val height = bounds.height()

        if (firstLevel != null) {
            val spacing = height.toFloat() / 10

            paint.color = firstLevelColor
            paint.textSize = firstLevelSize
            val fTextWidth = paint.measureText(firstLevel)
            val fX = (width - fTextWidth) / 2
            val fY = spacing + (firstLevelSize - paint.ascent()) / 2
            canvas.drawText(firstLevel!!, fX, fY, paint)
            if (drawBaseLine) {
                canvas.drawLine(0f, fY, width.toFloat(), fY, paint)
            }

            if (secondLevel != null) {
                paint.color = secondLevelColor
                paint.textSize = secondLevelSize
                val sTextWidth = paint.measureText(secondLevel)
                val sX = (width - sTextWidth) / 2
                val sY = spacing + firstLevelSize + spacing + (secondLevelSize - paint.ascent()) / 2
                canvas.drawText(secondLevel!!, sX, sY, paint)
                if (drawBaseLine) {
                    canvas.drawLine(0f, sY, width.toFloat(), sY, paint)
                }
            }
        }
    }

    override fun setAlpha(alpha: Int) {
        paint.alpha = alpha
    }

    override fun getOpacity() = PixelFormat.OPAQUE

    override fun setColorFilter(colorFilter: ColorFilter?) {
        paint.colorFilter = colorFilter
    }
}