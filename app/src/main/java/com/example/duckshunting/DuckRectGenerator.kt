package com.example.duckshunting

import android.content.Context
import android.graphics.Rect
import android.widget.Toast
import androidx.annotation.FloatRange
import androidx.core.content.ContextCompat
import kotlin.math.min
import kotlin.random.Random

class DuckRectGenerator(
    val context: Context,
    private val drawableDefaultSizeFraction: Float,
    var ducksQuantity: Int,
) {

    var duckRects = List(ducksQuantity) { Rect() }
    val duckDrawable = ContextCompat.getDrawable(context, R.drawable.duck)!!

    fun regenerateDuckRects(maxWidth: Int, maxHeight: Int) {
        duckRects.forEachIndexed { index, rect ->
            generateRandomRect(maxWidth, maxHeight, rect)
            val alreadyGeneratedDucks = duckRects.subList(0, index)
            // regenerate until does not intersect with previous ones
            while (isIntersectingWithPreviousRects(rect, alreadyGeneratedDucks)) {
                println("generating...")
                generateRandomRect(maxWidth, maxHeight, rect)
            }
        }
    }

    fun killDuck() {
        val newQuantity = ducksQuantity--
        duckRects = List(newQuantity) { Rect() }
        if (newQuantity == 0) {
            Toast.makeText(context, "Game Over", Toast.LENGTH_LONG).show()
        }
    }

    private fun isIntersectingWithPreviousRects(rect: Rect, previousRects: List<Rect>): Boolean =
        previousRects.any { Rect.intersects(rect, it) }

    private fun generateRandomRect(maxWidth: Int, maxHeight: Int, rect: Rect) {
        val (duckWidth, duckHeight) = calculateDuckSize(maxWidth, maxHeight)
        val layoutPadding = 0f

        @FloatRange(from = 0.0, to = 1.0)
        val randomXFraction = generateRandomCoordinate()

        @FloatRange(from = 0.0, to = 1.0)
        val randomYFraction = generateRandomCoordinate()

        val duckX: Float = maxWidth.minus(duckWidth) * randomXFraction
        val duckY: Float = maxHeight.minus(duckHeight) * randomYFraction

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