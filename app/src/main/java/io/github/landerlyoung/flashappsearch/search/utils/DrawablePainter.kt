package io.github.landerlyoung.flashappsearch.search.utils

import android.graphics.drawable.Drawable
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.painter.Painter
import kotlin.math.roundToInt

/*
 * ```
 * Author: yangchao12
 * Date:   2021-10-04
 * Time:   00:40
 * Life with Passion, Code with Creativity.
 * ```
 */

// TODO: 2021/10/4 support animated drawable
class DrawablePainter(private val drawable: Drawable?) : Painter() {
    override val intrinsicSize: Size
        get() = Size.Unspecified

    override fun DrawScope.onDraw() {
        val dr = drawable ?: return

        drawIntoCanvas {
            dr.setBounds(
                0,
                0,
                this@onDraw.size.width.roundToInt(),
                this@onDraw.size.height.roundToInt()
            )
            dr.draw(it.nativeCanvas)
        }
    }
}