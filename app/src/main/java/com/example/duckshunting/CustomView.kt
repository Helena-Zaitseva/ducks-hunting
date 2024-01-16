package com.example.duckshunting

import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.animation.AccelerateInterpolator
import android.widget.Toast
import androidx.core.animation.doOnEnd
import androidx.core.content.ContextCompat
import androidx.core.graphics.withSave

class CustomView @JvmOverloads constructor(
    context: Context? = null,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
    defStyleRes: Int = 0,
) : CustomViewScaffold(context, attrs, defStyleAttr, defStyleRes) {

    private val duckRectGenerator = DuckRectGenerator(
        context = this.context,
        drawableDefaultSizeFraction = DRAWABLE_DEFAULT_SIZE_FRACTION,
        ducksQuantity = DUCKS_QUANTITY,
    )

    private val circleDragDelegate = CircleDragDelegate(
        circleRadius = { crosshairCircleRadius },
        circleX = { circleX },
        circleY = { circleY },
        horizontalBorders = { horBorders },
        verticalBorders = { vertBorders }
    ) {
        circleXFraction = it.x / width
        circleX = it.x

        circleYFraction = it.y / height
        circleY = it.y

        invalidate()
    }

    private val horBorders
        get() = (0f)..(width.toFloat())
    private val vertBorders
        get() = (0f)..(height.toFloat())

    private val crosshairCirclePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        strokeWidth = dpToPx(CROSSHAIR_LINE_SIZE_DP)
        color = ContextCompat.getColor(getContext(), R.color.primary)
        style = Paint.Style.STROKE
    }
    private val crosshairDotPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = ContextCompat.getColor(getContext(), R.color.red)
        style = Paint.Style.FILL
    }

    private val crosshairCircleRadius = dpToPx(CROSSHAIR_CIRCLE_RADIUS_DP)
    private val crosshairDotRadius = dpToPx(CROSSHAIR_DOT_RADIUS_DP)
    private val crosshairHairLength = dpToPx(CROSSHAIR_HAIR_LENGTH_DP)

    private var circleX = 0f
    private var circleY = 0f

    private var circleXFraction = 0.5f
    private var circleYFraction = 0.5f

    private var animatorDuckAppearing: ValueAnimator? = null
    private var animatorDuckDying: ValueAnimator? = null

    fun startAnimation() {
        animatorDuckAppearing = ValueAnimator.ofInt(0, 255).apply {
            duration = ANIM_DUCK_APPEARING_DURATION_MS
            interpolator = AccelerateInterpolator()
            addUpdateListener {
                val fraction = it.animatedValue as Int
                duckRectGenerator.duckDrawable.alpha = fraction
                invalidate()
            }
            start()
        }
    }

    fun stopAnimation() {
        animatorDuckAppearing?.cancel()
        animatorDuckDying?.cancel()
    }

    private fun startDuckKillingAnimation() {
        val startingAlpha = duckRectGenerator.duckDrawable.alpha
        animatorDuckDying = ValueAnimator.ofInt(startingAlpha, 0).apply {
            duration = ANIM_DUCK_KILLING_DURATION_MS
            interpolator = AccelerateInterpolator()
            addUpdateListener {
                val fraction = it.animatedValue as Int
                duckRectGenerator.dyingDuckDrawable.alpha = fraction
                invalidate()
            }
            doOnEnd {
                duckRectGenerator.dyingDuckDrawable.bounds.setEmpty()
            }
            start()
        }
    }

    fun showDucks() {
        duckRectGenerator.regenerateDuckRects(maxWidth = width, maxHeight = height)
        startAnimation()
        invalidate()
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent?): Boolean {
        if (event == null) {
            return super.onTouchEvent(null)
        }
        when (event.action) {
            MotionEvent.ACTION_UP -> tryToShootDuck(event)
        }
        return circleDragDelegate.handleTouchEvent(event)
    }

    private fun tryToShootDuck(event: MotionEvent) {
        val index = duckRectGenerator.indexOfDuckUnderCrosshairOrNull(event.x, event.y) ?: return
        duckRectGenerator.killDuck(index)
        startDuckKillingAnimation()
        Toast.makeText(context, "Duck has been hit", Toast.LENGTH_SHORT).show()
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        // Duck
        duckRectGenerator.regenerateDuckRects(maxWidth = w, maxHeight = h)

        // CrossHair
        circleX = w * circleXFraction.coerceIn(0f, 1f)
        circleY = h * circleYFraction.coerceIn(0f, 1f)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        //Duck
        duckRectGenerator.run {
            dyingDuckDrawable.draw(canvas)
            duckRects.forEach {
                duckDrawable.bounds = it
                duckDrawable.draw(canvas)
            }
        }

        //Crosshair
        canvas.withSave {
            canvas.translate(circleX, circleY)
            canvas.drawCircle(0f, 0f, crosshairCircleRadius, crosshairCirclePaint)
            canvas.drawCircle(0f, 0f, crosshairDotRadius, crosshairDotPaint)
            val startX = 0f
            val startY = 0f - crosshairCircleRadius - crosshairHairLength.div(2)
            repeat(4) {
                canvas.rotate(90f)
                canvas.drawLine(startX, startY, startX, startY + crosshairHairLength, crosshairCirclePaint)
            }
        }
    }

    companion object {

        private const val CROSSHAIR_CIRCLE_RADIUS_DP = 60f
        private const val CROSSHAIR_DOT_RADIUS_DP = 6f
        private const val CROSSHAIR_HAIR_LENGTH_DP = 25f
        private const val CROSSHAIR_LINE_SIZE_DP = 2f
        private const val DRAWABLE_DEFAULT_SIZE_FRACTION = 0.3f
        private const val ANIM_DUCK_APPEARING_DURATION_MS = 300L
        private const val ANIM_DUCK_KILLING_DURATION_MS = 300L

        // Game settings
        const val DUCKS_QUANTITY = 5
        const val DUCKS_DISAPPEARING_TIME_MS = 2_000L
    }
}