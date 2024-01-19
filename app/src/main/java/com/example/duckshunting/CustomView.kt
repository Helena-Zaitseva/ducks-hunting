package com.example.duckshunting

import android.animation.ValueAnimator
import android.animation.ValueAnimator.*
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.animation.AccelerateInterpolator
import androidx.core.animation.doOnEnd
import androidx.core.content.ContextCompat
import androidx.core.graphics.withSave

class CustomView @JvmOverloads constructor(
    context: Context? = null,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
    defStyleRes: Int = 0,
) : CustomViewScaffold(context, attrs, defStyleAttr, defStyleRes) {

    var showDialogListener: ShowDialogListener? = null

    var soundListener: SoundListener? = null

    private var bulletRectGenerator = BulletRectGenerator(
        context = this.context,
        drawableDefaultSize = BULLET_DRAWABLE_DEFAULT_SIZE_FRACTION,
        bulletQuantity = BULLET_QUANTITY
    )

    private var duckRectGenerator = DuckRectGenerator(
        context = this.context,
        drawableDefaultSizeFraction = DRAWABLE_DEFAULT_SIZE_FRACTION,
        horizontalBorders = { horDuckBorders },
        verticalBorders = { vertDuckBorders },
        ducksQuantity = DUCKS_QUANTITY,
    )

    private val circleDragDelegate = CircleDragDelegate(
        circleRadius = { crosshairCircleRadius },
        circleX = { circleX },
        circleY = { circleY },
        horizontalBorders = { horCircleBorders },
        verticalBorders = { vertCircleBorders }
    ) {
        circleXFraction = it.x / width
        circleX = it.x

        circleYFraction = it.y / height
        circleY = it.y

        invalidate()
    }

    private val horCircleBorders
        get() = (0f)..(width.toFloat())
    private val vertCircleBorders
        get() = (bulletRectGenerator.drawableHeight)..(height.toFloat())
    private val horDuckBorders
        get() = (0f)..(width.toFloat())
    private val vertDuckBorders
        get() = (bulletRectGenerator.drawableHeight)..(height.toFloat() - bulletRectGenerator.drawableHeight)

    private val crosshairCirclePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        strokeWidth = dpToPx(CROSSHAIR_LINE_SIZE_DP)
        color = ContextCompat.getColor(getContext(), R.color.primary)
        style = Paint.Style.STROKE
    }
    private val crosshairDotPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = ContextCompat.getColor(getContext(), R.color.red)
        style = Paint.Style.FILL
    }

    private var crosshairCircleRadius = dpToPx(CROSSHAIR_CIRCLE_RADIUS_DP)
    private val crosshairDotRadius = dpToPx(CROSSHAIR_DOT_RADIUS_DP)
    private val crosshairHairLength = dpToPx(CROSSHAIR_HAIR_LENGTH_DP)

    private var circleX = 0f
    private var circleY = 0f

    private var circleXFraction = 0.5f
    private var circleYFraction = 0.5f

    private var animatorDuckAppearing: ValueAnimator? = null
    private var animatorDuckDying: ValueAnimator? = null
    private var crosshairValueAnimator: ValueAnimator? = null

    fun startAnimation() {
        animatorDuckAppearing = ofInt(0, 255).apply {
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
        crosshairValueAnimator?.cancel()
    }

    private fun startDuckKillingAnimation() {
        val startingAlpha = duckRectGenerator.duckDrawable.alpha
        animatorDuckDying = ofInt(startingAlpha, 0).apply {
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

    private fun crosshairAnimation() {
        crosshairValueAnimator = ofFloat(130f, 160f).apply {
            duration = ANIM_CROSSHAIR_DURATION_MS
            interpolator = AccelerateInterpolator()
            repeatCount = RESTART
            repeatMode = REVERSE
            addUpdateListener {
                crosshairCircleRadius = it.animatedValue as Float
                invalidate()
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
            MotionEvent.ACTION_UP -> {
                crosshairAnimation()
                soundListener?.playShotSound()
                bulletRectGenerator.removeBullet()
                tryToShootDuck(event)
                if (bulletRectGenerator.bulletRects.isEmpty() && duckRectGenerator.duckRects.isNotEmpty()) {
                    showDialogListener?.showRetryGameDialog()
                }
                if (duckRectGenerator.duckRects.isEmpty()) {
                    showDialogListener?.showWinDialog()
                }
            }
        }
        return circleDragDelegate.handleTouchEvent(event)
    }

    private fun tryToShootDuck(event: MotionEvent) {
        val index = duckRectGenerator.indexOfDuckUnderCrosshairOrNull(event.x, event.y) ?: return
        duckRectGenerator.killDuck(index)
        startDuckKillingAnimation()
        soundListener?.playDuckSound()
        soundListener?.vibration()
    }

    fun reset() {
        duckRectGenerator = DuckRectGenerator(
            context = this.context,
            drawableDefaultSizeFraction = DRAWABLE_DEFAULT_SIZE_FRACTION,
            horizontalBorders = { horDuckBorders },
            verticalBorders = { vertDuckBorders },
            ducksQuantity = DUCKS_QUANTITY,
        )
        showDucks()
        bulletRectGenerator = BulletRectGenerator(
            context = this.context,
            drawableDefaultSize = BULLET_DRAWABLE_DEFAULT_SIZE_FRACTION,
            bulletQuantity = BULLET_QUANTITY
        )
        bulletRectGenerator.generateBulletRect(width, height)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        // Duck
        duckRectGenerator.regenerateDuckRects(maxWidth = w, maxHeight = h)

        // CrossHair
        circleX = w * circleXFraction.coerceIn(0f, 1f)
        circleY = h * circleYFraction.coerceIn(0f, 1f)

        //Bullets
        bulletRectGenerator.generateBulletRect(w, h)
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

        //Bullets
        bulletRectGenerator.run {
            bulletRects.forEach {
                bulletDrawable.bounds = it
                bulletDrawable.draw(canvas)
            }
        }
    }

    companion object {

        private const val CROSSHAIR_CIRCLE_RADIUS_DP = 55f
        private const val CROSSHAIR_DOT_RADIUS_DP = 6f
        private const val CROSSHAIR_HAIR_LENGTH_DP = 22f
        private const val CROSSHAIR_LINE_SIZE_DP = 2f
        private const val DRAWABLE_DEFAULT_SIZE_FRACTION = 0.3f
        private const val BULLET_DRAWABLE_DEFAULT_SIZE_FRACTION = 0.3f
        private const val ANIM_DUCK_APPEARING_DURATION_MS = 300L
        private const val ANIM_DUCK_KILLING_DURATION_MS = 300L
        private const val ANIM_CROSSHAIR_DURATION_MS = 200L

        // Game settings
        const val DUCKS_QUANTITY = 5
        const val BULLET_QUANTITY = 5
        const val DUCKS_DISAPPEARING_TIME_MS = 1_500L
    }
}