package com.example.duckshunting

import android.content.Context
import android.graphics.Rect
import androidx.core.content.ContextCompat
import kotlin.math.min

class BulletRectGenerator(
    private val context: Context,
    private val drawableDefaultSize: Float,
    var bulletQuantity: Int,
) {

    var bulletRects = List(bulletQuantity) { Rect() }
        private set

    val bulletDrawable = ContextCompat.getDrawable(context, R.drawable.bullet)!!

    var drawableHeight = 0f
    private var drawableWidth = 0f

    fun generateBulletRect(maxWidth: Int, maxHeight: Int) {
        val (bulletWidth, bulletHeight) = calculateBulletSize(maxWidth, maxHeight)
        bulletRects.forEachIndexed { index, rect ->
            val xOffset = bulletWidth * index / 3
            rect.set(
                /* left = */ xOffset.toInt(),
                /* top = */ 0,
                /* right = */ (xOffset + bulletWidth).toInt(),
                /* bottom = */ bulletHeight.toInt()
            )
        }
    }

    fun removeBullet() {
        bulletRects = bulletRects.toMutableList().dropLast(1)
    }

    private fun calculateBulletSize(maxWidth: Int, maxHeight: Int): Pair<Float, Float> {
        val drawableMaxSize = min(maxWidth, maxHeight) * drawableDefaultSize / 2
        val drawableIntrinsicRatio = 1f * bulletDrawable.intrinsicWidth / bulletDrawable.intrinsicHeight

        drawableWidth = if (drawableIntrinsicRatio > 1) drawableMaxSize
        else drawableMaxSize * drawableIntrinsicRatio

        drawableHeight = if (drawableIntrinsicRatio > 1)
            drawableMaxSize / drawableIntrinsicRatio else drawableMaxSize

        return drawableWidth to drawableHeight
    }
}