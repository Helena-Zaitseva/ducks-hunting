package com.example.duckshunting

import android.view.MotionEvent

object MotionEventExtensions {

    fun MotionEvent.distanceTo(x: Float, y: Float): Float {
        return MathUtils.pointsDistance(this.x, this.y, x, y)
    }
}