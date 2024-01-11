package com.example.duckshunting

import android.graphics.PointF
import android.view.MotionEvent
import kotlin.math.pow
import kotlin.math.sqrt

class CircleDragDelegate(
    private val circleRadius: () -> Float,
    private val circleX: () -> Float,
    private val circleY: () -> Float,
    private val horizontalBorders: () -> ClosedFloatingPointRange<Float>,
    private val verticalBorders: () -> ClosedFloatingPointRange<Float>,
    private val onLocationChanged: (PointF) -> Unit
) {

    private var isBeingDragged = false
    private var lastMotionEventX = 0f
    private var lastMotionEventY = 0f

    private var lastX = 0f
    private var lastY = 0f

    fun handleTouchEvent(event: MotionEvent): Boolean {
        val insideCircle = distanceFromCircleCenter(event.x, event.y) <= circleRadius()
        return when {
            insideCircle && event.action == MotionEvent.ACTION_DOWN -> {
                isBeingDragged = true
                lastMotionEventX = event.x
                lastMotionEventY = event.y
                true
            }

            isBeingDragged && event.action == MotionEvent.ACTION_MOVE -> {
                dragCircle(event)
                lastMotionEventX = event.x
                lastMotionEventY = event.y
                true
            }

            else -> {
                isBeingDragged = false
                false
            }
        }
    }

    private fun distanceFromCircleCenter(x: Float, y: Float): Float {
        val xDistance = circleX() - x
        val yDistance = circleY() - y
        return sqrt(xDistance.pow(2) + yDistance.pow(2))
    }

    private fun dragCircle(event: MotionEvent) {
        val dragDistanceX = event.x - lastMotionEventX
        val dragDistanceY = event.y - lastMotionEventY
        val newCircleX = circleX() + dragDistanceX
        val newCircleY = circleY() + dragDistanceY
        lastX = newCircleX.coerceIn(horizontalBorders())
        lastY = newCircleY.coerceIn(verticalBorders())
        onLocationChanged(PointF(lastX, lastY))
    }
}