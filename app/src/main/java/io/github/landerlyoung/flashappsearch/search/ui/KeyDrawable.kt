package io.github.landerlyoung.flashappsearch.search.ui

import android.graphics.*
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
    var input: Input? = _input
        set(value) {
            field = value
            if (value != null && value.keys.isNotEmpty()) {
                firstLevel = value.keys[0].toString()
                if (value.keys.size > 1) {
//                    secondLevel = String(value.keys, 1, value.keys.size - 1)
                } else {
                    secondLevel = null
                }
                updateTextSize()
            } else {
                firstLevel = null
                secondLevel = null
            }
        }

    private val paint = Paint().apply {
        flags = Paint.ANTI_ALIAS_FLAG
    }

    private var firstLevel: String? = null
    private var secondLevel: String? = null
    private var firstLevelSize = 0
    private var secondLevelSize = 0

    private var firstLevelColor = Color.BLACK
    private val secondLevelColor = 0xAA000000

    fun updateTextSize() {
        val bounds = bounds
        val size = min(bounds.width(), bounds.height())
        if (secondLevel != null) {

        }
        if (secondLevel != null) {
            // 2, 6
            firstLevelSize = size * 6 / 10
            secondLevelSize = size * 2 / 10
        } else {
            firstLevelSize = size * 8 / 10
        }
    }

    override fun onBoundsChange(bounds: Rect?) {
        super.onBoundsChange(bounds)
        updateTextSize()
    }

    override fun draw(canvas: Canvas) {
        val width = bounds.width()
        val height = bounds.height()

        if (secondLevel != null) {

        } else {
            val textWidth = paint.measureText(firstLevel)
            val x = (width - textWidth) / 2
//            val y = (height)
//            canvas.drawText(firstLevel, )
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