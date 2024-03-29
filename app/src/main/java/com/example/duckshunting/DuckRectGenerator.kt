package com.example.duckshunting

import android.content.Context
import android.graphics.Rect
import android.widget.Toast
import androidx.annotation.FloatRange
import androidx.core.content.ContextCompat
import kotlin.math.min
import kotlin.random.Random

class DuckRectGenerator(
    private val context: Context,
    private val drawableDefaultSizeFraction: Float,
    private val horizontalBorders: () -> ClosedFloatingPointRange<Float>,
    private val verticalBorders: () -> ClosedFloatingPointRange<Float>,
    val ducksQuantity: Int,

) {

    var duckRects = List(ducksQuantity) { Rect() }
        private set

    val duckDrawable = ContextCompat.getDrawable(context, R.drawable.duck)!!
    val dyingDuckDrawable = duckDrawable.constantState?.newDrawable(context.resources)?.mutate()!!

    fun regenerateDuckRects(maxWidth: Int, maxHeight: Int) {
        duckRects.forEachIndexed { index, rect ->
            generateRandomRect(maxWidth, maxHeight, rect)
            val alreadyGeneratedDucks = duckRects.subList(0, index)
            // regenerate until does not intersect with previous ones
            while (isIntersectingWithPreviousRects(rect, alreadyGeneratedDucks)) {
                generateRandomRect(maxWidth, maxHeight, rect)
            }
        }
    }

    fun killDuck(duckIndex: Int) {
        duckRects = duckRects.toMutableList().apply {
            val rect = removeAt(duckIndex)
            dyingDuckDrawable.bounds = rect
        }
    }

    fun indexOfDuckUnderCrosshairOrNull(x: Float, y: Float): Int? =
        duckRects.indexOfFirst { it.contains(x.toInt(), y.toInt()) }.takeIf { it >= 0 }

    private fun isIntersectingWithPreviousRects(rect: Rect, previousRects: List<Rect>): Boolean =
        previousRects.any { Rect.intersects(rect, it) }

    private fun generateRandomRect(maxWidth: Int, maxHeight: Int, rect: Rect) {
        val (duckWidth, duckHeight) = calculateDuckSize(maxWidth, maxHeight)
        val layoutPadding = 0f

        @FloatRange(from = 0.0, to = 1.0)
        val randomXFraction = generateRandomCoordinate()

        @FloatRange(from = 0.0, to = 1.0)
        val randomYFraction = generateRandomCoordinate()

        val duckX: Float = maxWidth
            .minus(duckWidth) * randomXFraction
            .coerceIn(horizontalBorders())
        val duckY: Float = maxHeight
            .minus(duckHeight)
            .times(randomYFraction)
            .coerceIn(verticalBorders())

        val left = 0f + layoutPadding + duckX
        val top = 0f + layoutPadding + duckY
        val right = left + duckWidth
        val bottom = top + duckHeight
        rect.set(left.toInt(), top.toInt(), right.toInt(), bottom.toInt())
    }

    private fun generateRandomCoordinate(): Float = Random.nextFloat()

    private fun calculateDuckSize(maxWidth: Int, maxHeight: Int): Pair<Float, Float> {
        val drawableMaxSize = min(maxWidth, maxHeight) * drawableDefaultSizeFraction
        val drawableIntrinsicRatio = 1f * duckDrawable.intrinsicWidth / duckDrawable.intrinsicHeight

        val drawableWidth =
            if (drawableIntrinsicRatio > 1) drawableMaxSize else drawableMaxSize * drawableIntrinsicRatio
        val drawableHeight =
            if (drawableIntrinsicRatio > 1) drawableMaxSize / drawableIntrinsicRatio else drawableMaxSize

        return drawableWidth to drawableHeight
    }
}