package com.udacity

import android.animation.AnimatorSet
import android.animation.ValueAnimator
import android.content.Context
import android.content.res.TypedArray
import android.graphics.*
import android.text.TextPaint
import android.util.AttributeSet
import android.view.View
import android.view.animation.LinearInterpolator
import androidx.core.animation.addListener
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.core.content.withStyledAttributes
import androidx.interpolator.view.animation.LinearOutSlowInInterpolator
import java.util.concurrent.TimeUnit
import kotlin.math.min
import kotlin.properties.Delegates


class LoadingButton @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {
    private var widthSize = 0
    private var heightSize = 0

    private var loadingText: CharSequence = ""
    private var loadingTextColor = 0
    private var loadingBackgroundColor = 0
    private var text = ""
    private var defaultText: CharSequence = ""
    private var defaultBackgroundColor = 0

    private var loadingBackgroundEndPositionValue = 0f

    private var progressCircleSize = 0f
    private val progressCircleRect = RectF()
    private var progressCircleBackgroundColor = 0
    private var progressCircleEndAngleValue = 0f

    private lateinit var textBounds: Rect
    private lateinit var backgroundAnimator: ValueAnimator

    companion object {
        private const val TEXT_OFFSET = 20f
        private const val ANIMATION_TIME = 2L
        private const val TEXT_SIZE = 55f
    }

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }

    private val textPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        textAlign = Paint.Align.CENTER
        textSize = TEXT_SIZE
        typeface = Typeface.DEFAULT
    }

    private val animatorSet: AnimatorSet = AnimatorSet().apply {
        duration = TimeUnit.SECONDS.toMillis(ANIMATION_TIME)
    }

    private val progressAnimation = ValueAnimator.ofFloat(0f, 360f).apply {
        repeatMode = ValueAnimator.RESTART
        repeatCount = ValueAnimator.INFINITE
        interpolator = LinearInterpolator()
        addUpdateListener {
            progressCircleEndAngleValue = it.animatedValue as Float
            invalidate()
        }
    }


    private var buttonState: ButtonState by Delegates.observable<ButtonState>(ButtonState.Completed) { _, _, new ->
        when (new) {
            ButtonState.Loading -> {
                text = loadingText.toString()
                if (!::textBounds.isInitialized) {
                    getTextBounds()
                    setProgressCircle()
                }
                animatorSet.start()
            }
            else -> {
                text = defaultText.toString()
                new.takeIf { it == ButtonState.Completed }?.run { animatorSet.cancel() }
            }
        }
    }

    private fun getTextBounds() {
        textBounds = Rect()
        textPaint.getTextBounds(text, 0, text.length, textBounds)
    }

    private fun setProgressCircle() {
        val horizontalCenter =
            (textBounds.right + textBounds.width() + 24f)
        val verticalCenter = (heightSize / 2f)

        progressCircleRect.set(
            horizontalCenter - progressCircleSize,
            verticalCenter - progressCircleSize,
            horizontalCenter + progressCircleSize,
            verticalCenter + progressCircleSize
        )
    }


    init {
        context.withStyledAttributes(attrs, R.styleable.LoadingButton) {
            setStyleValues()
        }
        text = defaultText.toString()
        progressCircleBackgroundColor = ContextCompat.getColor(context, R.color.colorAccent)
    }

    private fun TypedArray.setStyleValues() {
        defaultBackgroundColor =
            getColor(R.styleable.LoadingButton_defaultBackgroundColor, 0)
        loadingBackgroundColor =
            getColor(R.styleable.LoadingButton_loadingBackgroundColor, 0)
        defaultText =
            getText(R.styleable.LoadingButton_defaultText)
        loadingTextColor =
            getColor(R.styleable.LoadingButton_loadingTextColor, 0)
        loadingText =
            getText(R.styleable.LoadingButton_loadingText)
    }


    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        canvas?.let {
            drawBackgroundColor(it)
            drawText(it)
        }
        if (buttonState == ButtonState.Loading) {
            drawProgressCircle(canvas)
        }
    }

    private fun drawBackgroundColor(canvas: Canvas?) {
        when (buttonState) {
            ButtonState.Loading -> {
                drawLoadingBackgroundColor(canvas)
                drawDefaultBackgroundColor(canvas)
            }
            else -> canvas?.drawColor(defaultBackgroundColor)
        }
    }

    private fun drawLoadingBackgroundColor(canvas: Canvas?) {
        paint.apply {
            color = loadingBackgroundColor
        }
        canvas?.drawRect(
            0f,
            0f,
            loadingBackgroundEndPositionValue,
            heightSize.toFloat(),
            paint
        )
    }

    private fun drawDefaultBackgroundColor(canvas: Canvas?) {
        paint.apply {
            color = defaultBackgroundColor
        }
        canvas?.drawRect(
            loadingBackgroundEndPositionValue,
            0f,
            widthSize.toFloat(),
            heightSize.toFloat(),
            paint
        )
    }

    private fun drawText(canvas: Canvas?) {
        textPaint.color = loadingTextColor
        canvas?.drawText(
            text,
            (widthSize / 2f),
            (heightSize / 2f) + TEXT_OFFSET,
            textPaint
        )
    }

    private fun drawProgressCircle(canvas: Canvas?) {
        paint.color = progressCircleBackgroundColor
        canvas?.drawArc(
            progressCircleRect,
            0f,
            progressCircleEndAngleValue,
            true,
            paint
        )
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        progressCircleSize = (min(w, h) / 2f) * 0.5f
        setBackgroundAnimator()
    }

    override fun performClick(): Boolean {
        super.performClick()
        if (buttonState == ButtonState.Completed) {
            buttonState = ButtonState.Clicked
            invalidate()
        }
        return true
    }

    private fun setBackgroundAnimator() {
        backgroundAnimator =  ValueAnimator.ofFloat(0f, widthSize.toFloat()).apply {
            repeatMode = ValueAnimator.RESTART
            repeatCount = ValueAnimator.INFINITE
            interpolator = LinearInterpolator()
            addUpdateListener {
                loadingBackgroundEndPositionValue = it.animatedValue as Float
                invalidate()
            }
        }
        animatorSet.playTogether(progressAnimation, backgroundAnimator)
    }

    fun changeButtonState(state: ButtonState) {
        if (state != buttonState) {
            buttonState = state
            invalidate()
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val minw: Int = paddingLeft + paddingRight + suggestedMinimumWidth
        val w: Int = resolveSizeAndState(minw, widthMeasureSpec, 1)
        val h: Int = resolveSizeAndState(
            MeasureSpec.getSize(w),
            heightMeasureSpec,
            0
        )
        widthSize = w
        heightSize = h
        setMeasuredDimension(w, h)
    }

}