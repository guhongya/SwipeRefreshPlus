/*
 * Copyright (C) 2013 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package me.guhy.swiperefresh

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.content.Context
import android.content.res.Resources
import android.graphics.*
import android.graphics.drawable.Drawable
import android.support.annotation.IntDef
import android.support.v4.view.animation.FastOutSlowInInterpolator
import android.view.View
import android.view.animation.Animation
import android.view.animation.Interpolator
import android.view.animation.LinearInterpolator
import java.lang.annotation.Retention
import java.lang.annotation.RetentionPolicy

/**
 * Created by Guhy on 2016/11/15.
 * copy from android support library
 */
class ProgressDrawable internal constructor(context: Context, private val mParent: View) : Drawable() {
    /** The list of animators operating on this drawable.  */
    //  private final ArrayList<Animation> mAnimators = new ArrayList<Animation>();

    /**
     * The indicator ring, used to manage animation state.
     */
    private val mRing: Ring

    /**
     * Canvas rotation in degrees.
     */
    internal var rotation: Float = 0f
         internal set(rotation) {
            field = rotation
            invalidateSelf()
        }

    private val mResources: Resources
    private var mAnimation: Animator? = null
    internal var mRotationCount: Float = 0.toFloat()
    private var mWidth: Double = 0.toDouble()
    private var mHeight: Double = 0.toDouble()
    internal var mFinishing: Boolean = false

    val isRunning: Boolean
        get() = mAnimation!!.isRunning


    private val mCallback = object : Drawable.Callback {
        override fun invalidateDrawable(d: Drawable) {
            invalidateSelf()
        }

        override fun scheduleDrawable(d: Drawable, what: Runnable, `when`: Long) {
            scheduleSelf(what, `when`)
        }

        override fun unscheduleDrawable(d: Drawable, what: Runnable) {
            unscheduleSelf(what)
        }
    }

    @Retention(RetentionPolicy.SOURCE)
    @IntDef(LARGE.toLong(), DEFAULT.toLong())
    annotation class ProgressDrawableSize

    init {
        mResources = context.resources

        mRing = ProgressDrawable.Ring(mCallback)
        mRing.setColors(COLORS)

        updateSizes(DEFAULT)
        setupAnimators()
    }

    private fun setSizeParameters(progressCircleWidth: Double, progressCircleHeight: Double,
                                  centerRadius: Double, strokeWidth: Double, arrowWidth: Float, arrowHeight: Float) {
        val ring = mRing
        val metrics = mResources.displayMetrics
        val screenDensity = metrics.density

        mWidth = progressCircleWidth * screenDensity
        mHeight = progressCircleHeight * screenDensity
        ring.strokeWidth = strokeWidth.toFloat() * screenDensity
        ring.centerRadius = centerRadius * screenDensity
        ring.setColorIndex(0)
        ring.setArrowDimensions(arrowWidth * screenDensity, arrowHeight * screenDensity)
        ring.setInsets(mWidth.toInt(), mHeight.toInt())
    }

    /**
     * Set the overall size for the progress spinner. This updates the radius
     * and stroke width of the ring.
     */
    fun updateSizes(@ProgressDrawableSize size: Int) {
        if (size == LARGE) {
            setSizeParameters(CIRCLE_DIAMETER_LARGE.toDouble(), CIRCLE_DIAMETER_LARGE.toDouble(), CENTER_RADIUS_LARGE.toDouble(),
                    STROKE_WIDTH_LARGE.toDouble(), ARROW_WIDTH_LARGE.toFloat(), ARROW_HEIGHT_LARGE.toFloat())
        } else {
            setSizeParameters(CIRCLE_DIAMETER.toDouble(), CIRCLE_DIAMETER.toDouble(), CENTER_RADIUS.toDouble(), STROKE_WIDTH.toDouble(),
                    ARROW_WIDTH.toFloat(), ARROW_HEIGHT.toFloat())
        }
    }

    /**
     * @param show Set to true to display the arrowhead on the progress spinner.
     */
    fun showArrow(show: Boolean) {
        mRing.setShowArrow(show)
    }

    /**
     * @param scale Set the scale of the arrowhead for the spinner.
     */
    fun setArrowScale(scale: Float) {
        mRing.setArrowScale(scale)
    }

    /**
     * Set the start and end trim for the progress spinner arc.
     *
     * @param startAngle start angle
     * @param endAngle   end angle
     */
    fun setStartEndTrim(startAngle: Float, endAngle: Float) {
        mRing.startTrim = startAngle
        mRing.endTrim = endAngle
    }

    /**
     * Set the amount of rotation to apply to the progress spinner.
     *
     * @param rotation Rotation is from [0..1]
     */
    fun setProgressRotation(rotation: Float) {
        mRing.rotation = rotation
    }

    /**
     * Update the background color of the circle image view.
     */
    fun setBackgroundColor(color: Int) {
        mRing.setBackgroundColor(color)
    }

    /**
     * Set the colors used in the progress animation from color resources.
     * The first color will also be the color of the bar that grows in response
     * to a user swipe gesture.
     *
     * @param colors 一组颜色值
     */
    fun setColorSchemeColors(vararg colors: Int) {
        mRing.setColors(colors)
        mRing.setColorIndex(0)
    }

    override fun getIntrinsicHeight(): Int {
        return mHeight.toInt()
    }

    override fun getIntrinsicWidth(): Int {
        return mWidth.toInt()
    }

    override fun draw(c: Canvas) {
        val bounds = bounds
        val saveCount = c.save()
        c.rotate(rotation, bounds.exactCenterX(), bounds.exactCenterY())
        mRing.draw(c, bounds)
        c.restoreToCount(saveCount)
    }

    override fun setAlpha(alpha: Int) {
        mRing.alpha = alpha
    }

    override fun getAlpha(): Int {
        return mRing.alpha
    }

    override fun setColorFilter(colorFilter: ColorFilter?) {
        mRing.setColorFilter(colorFilter)
    }

    override fun getOpacity(): Int {
        return PixelFormat.TRANSLUCENT
    }


    fun start() {
        mRing.storeOriginals()
        // Already showing some part of the ring
        if (mRing.endTrim != mRing.startTrim) {
            mFinishing = true
            mAnimation!!.duration = (ANIMATION_DURATION / 2).toLong()
            mAnimation!!.start()
        } else {
            mRing.setColorIndex(0)
            mRing.resetOriginals()
            mAnimation!!.duration = ANIMATION_DURATION.toLong()
            mAnimation!!.start()
        }
    }

    //@Override
    fun stop() {
        //mParent.clearAnimation();
        mAnimation!!.cancel()
        rotation = 0f
        mRing.setShowArrow(false)
        mRing.setColorIndex(0)
        mRing.resetOriginals()
    }

    internal fun getMinProgressArc(ring: Ring): Float {
        return Math.toRadians(
                ring.strokeWidth / (2.0 * Math.PI * ring.centerRadius)).toFloat()
    }

    // Adapted from ArgbEvaluator.java
    private fun evaluateColorChange(fraction: Float, startValue: Int, endValue: Int): Int {
        val startInt = startValue.toInt()
        val startA = startInt shr 24 and 0xff
        val startR = startInt shr 16 and 0xff
        val startG = startInt shr 8 and 0xff
        val startB = startInt and 0xff

        val endInt = endValue.toInt()
        val endA = endInt shr 24 and 0xff
        val endR = endInt shr 16 and 0xff
        val endG = endInt shr 8 and 0xff
        val endB = endInt and 0xff

        return ((startA + (fraction * (endA - startA)).toInt() shl 24).toInt()
                or (startR + (fraction * (endR - startR)).toInt() shl 16).toInt()
                or (startG + (fraction * (endG - startG)).toInt() shl 8).toInt()
                or (startB + (fraction * (endB - startB)).toInt()).toInt())
    }

    /**
     * Update the ring color if this is within the last 25% of the animation.
     * The new ring color will be a translation from the starting ring color to
     * the next color.
     */
    internal fun updateRingColor(interpolatedTime: Float, ring: Ring) {
        if (interpolatedTime > COLOR_START_DELAY_OFFSET) {
            // scale the interpolatedTime so that the full
            // transformation from 0 - 1 takes place in the
            // remaining time
            ring.setColor(evaluateColorChange((interpolatedTime - COLOR_START_DELAY_OFFSET) / (1.0f - COLOR_START_DELAY_OFFSET), ring.startingColor,
                    ring.nextColor))
        }
    }

    internal fun applyFinishTranslation(interpolatedTime: Float, ring: Ring) {
        // shrink back down and complete a full rotation before
        // starting other circles
        // Rotation goes between [0..1].
        updateRingColor(interpolatedTime, ring)
        val targetRotation = (Math.floor((ring.startingRotation / MAX_PROGRESS_ARC).toDouble()) + 1f).toFloat()
        val minProgressArc = getMinProgressArc(ring)
        val startTrim = ring.startingStartTrim + (ring.startingEndTrim - minProgressArc - ring.startingStartTrim) * interpolatedTime
        ring.startTrim = startTrim
        ring.endTrim = ring.startingEndTrim
        val rotation = ring.startingRotation + (targetRotation - ring.startingRotation) * interpolatedTime
        ring.rotation = rotation
    }

    private fun setupAnimators() {
        val ring = mRing
        val animator = ObjectAnimator.ofFloat(0f, 1f)
        animator.addUpdateListener { animation ->
            val interpolatedTime = java.lang.Float.valueOf(animation.animatedValue.toString())!!
            if (mFinishing) {
                applyFinishTranslation(interpolatedTime, ring)
            } else {
                // The minProgressArc is calculated from 0 to create an
                // angle that matches the stroke width.
                val minProgressArc = getMinProgressArc(ring)
                val startingEndTrim = ring.startingEndTrim
                val startingTrim = ring.startingStartTrim
                val startingRotation = ring.startingRotation

                updateRingColor(interpolatedTime, ring)

                // Moving the start trim only occurs in the first 50% of a
                // single ring animation
                if (interpolatedTime <= START_TRIM_DURATION_OFFSET) {
                    // scale the interpolatedTime so that the full
                    // transformation from 0 - 1 takes place in the
                    // remaining time
                    val scaledTime = interpolatedTime / (1.0f - START_TRIM_DURATION_OFFSET)
                    val startTrim = startingTrim + (MAX_PROGRESS_ARC - minProgressArc) * MATERIAL_INTERPOLATOR
                            .getInterpolation(scaledTime)
                    ring.startTrim = startTrim
                }

                // Moving the end trim starts after 50% of a single ring
                // animation completes
                if (interpolatedTime > END_TRIM_START_DELAY_OFFSET) {
                    // scale the interpolatedTime so that the full
                    // transformation from 0 - 1 takes place in the
                    // remaining time
                    val minArc = MAX_PROGRESS_ARC - minProgressArc
                    val scaledTime = (interpolatedTime - START_TRIM_DURATION_OFFSET) / (1.0f - START_TRIM_DURATION_OFFSET)
                    val endTrim = startingEndTrim + minArc * MATERIAL_INTERPOLATOR.getInterpolation(scaledTime)
                    ring.endTrim = endTrim
                }

                var rotation = startingRotation + 0.25f * interpolatedTime
                ring.rotation = rotation

                val groupRotation = FULL_ROTATION / NUM_POINTS * interpolatedTime + FULL_ROTATION * (mRotationCount / NUM_POINTS)
                rotation = groupRotation
            }
        }
        animator.repeatCount = Animation.INFINITE
        animator.repeatMode = ValueAnimator.RESTART
        animator.interpolator = LINEAR_INTERPOLATOR
        animator.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {

            }

            override fun onAnimationStart(animation: Animator) {
                mRotationCount = 0f
            }

            override fun onAnimationRepeat(animation: Animator) {
                ring.storeOriginals()
                ring.goToNextColor()
                ring.startTrim = ring.endTrim
                if (mFinishing) {
                    // finished closing the last ring from the swipe gesture; go
                    // into progress mode
                    mFinishing = false
                    animation.duration = ANIMATION_DURATION.toLong()
                    ring.setShowArrow(false)
                } else {
                    mRotationCount = (mRotationCount + 1) % NUM_POINTS
                }
            }
        })
        mAnimation = animator
    }

    internal class Ring internal constructor(private val mCallback: Drawable.Callback) {
        private val mTempBounds = RectF()
        private val mPaint = Paint()
        private val mArrowPaint = Paint()

        var startTrim = 0.0f
            set(startTrim) {
                field = startTrim
                invalidateSelf()
            }
        var endTrim = 0.0f
            set(endTrim) {
                field = endTrim
                invalidateSelf()
            }
        var rotation = 0.0f
            set(rotation) {
                field = rotation
                invalidateSelf()
            }
        /**
         * @param strokeWidth Set the stroke width of the progress spinner in pixels.
         */
        var strokeWidth = 5.0f
            set(strokeWidth) {
                field = strokeWidth
                mPaint.strokeWidth = strokeWidth
                invalidateSelf()
            }
        var insets = 2.5f
            private set

        private var mColors: IntArray? = null
        // mColorIndex represents the offset into the available mColors that the
        // progress circle should currently display. As the progress circle is
        // animating, the mColorIndex moves by one to the next available color.
        private var mColorIndex: Int = 0
        var startingStartTrim: Float = 0.toFloat()
            private set
        var startingEndTrim: Float = 0.toFloat()
            private set
        /**
         * @return The amount the progress spinner is currently rotated, between [0..1].
         */
        var startingRotation: Float = 0.toFloat()
            private set
        private var mShowArrow: Boolean = false
        private var mArrow: Path? = null
        private var mArrowScale: Float = 0.toFloat()
        /**
         * @param centerRadius Inner radius in px of the circle the progress
         * spinner arc traces.
         */
        var centerRadius: Double = 0.toDouble()
        private var mArrowWidth: Int = 0
        private var mArrowHeight: Int = 0
        /**
         * @return Current alpha of the progress spinner and arrowhead.
         */
        /**
         * @param alpha Set the alpha of the progress spinner and associated arrowhead.
         */
        var alpha: Int = 0
        private val mCirclePaint = Paint(Paint.ANTI_ALIAS_FLAG)
        private var mBackgroundColor: Int = 0
        private var mCurrentColor: Int = 0

        /**
         * @return int describing the next color the progress spinner should use when drawing.
         */
        val nextColor: Int
            get() = mColors!![nextColorIndex]

        private val nextColorIndex: Int
            get() = (mColorIndex + 1) % mColors!!.size

        val startingColor: Int
            get() = mColors!![mColorIndex]

        init {

            mPaint.strokeCap = Paint.Cap.SQUARE
            mPaint.isAntiAlias = true
            mPaint.style = Paint.Style.STROKE

            mArrowPaint.style = Paint.Style.FILL
            mArrowPaint.isAntiAlias = true
        }

        fun setBackgroundColor(color: Int) {
            mBackgroundColor = color
        }

        /**
         * Set the dimensions of the arrowhead.
         *
         * @param width  Width of the hypotenuse of the arrow head
         * @param height Height of the arrow point
         */
        fun setArrowDimensions(width: Float, height: Float) {
            mArrowWidth = width.toInt()
            mArrowHeight = height.toInt()
        }

        /**
         * Draw the progress spinner
         */
        fun draw(c: Canvas, bounds: Rect) {
            val arcBounds = mTempBounds
            arcBounds.set(bounds)
            arcBounds.inset(insets, insets)

            val startAngle = (startTrim + rotation) * 360
            val endAngle = (endTrim + rotation) * 360
            val sweepAngle = endAngle - startAngle

            mPaint.color = mCurrentColor
            c.drawArc(arcBounds, startAngle, sweepAngle, false, mPaint)

            drawTriangle(c, startAngle, sweepAngle, bounds)

            if (alpha < 255) {
                mCirclePaint.color = mBackgroundColor
                mCirclePaint.alpha = 255 - alpha
                c.drawCircle(bounds.exactCenterX(), bounds.exactCenterY(), (bounds.width() / 2).toFloat(),
                        mCirclePaint)
            }
        }

        private fun drawTriangle(c: Canvas, startAngle: Float, sweepAngle: Float, bounds: Rect) {
            if (mShowArrow) {
                if (mArrow == null) {
                    mArrow = android.graphics.Path()
                    mArrow!!.fillType = android.graphics.Path.FillType.EVEN_ODD
                } else {
                    mArrow!!.reset()
                }

                // Adjust the position of the triangle so that it is inset as
                // much as the arc, but also centered on the arc.
                val inset = insets.toInt() / 2 * mArrowScale
                val x = (centerRadius * Math.cos(0.0) + bounds.exactCenterX()).toFloat()
                val y = (centerRadius * Math.sin(0.0) + bounds.exactCenterY()).toFloat()

                // Update the path each time. This works around an issue in SKIA
                // where concatenating a rotation matrix to a scale matrix
                // ignored a starting negative rotation. This appears to have
                // been fixed as of API 21.
                mArrow!!.moveTo(0f, 0f)
                mArrow!!.lineTo(mArrowWidth * mArrowScale, 0f)
                mArrow!!.lineTo(mArrowWidth * mArrowScale / 2, mArrowHeight * mArrowScale)
                mArrow!!.offset(x - inset, y)
                mArrow!!.close()
                // draw a triangle
                mArrowPaint.color = mCurrentColor
                c.rotate(startAngle + sweepAngle - ARROW_OFFSET_ANGLE, bounds.exactCenterX(),
                        bounds.exactCenterY())
                c.drawPath(mArrow!!, mArrowPaint)
            }
        }

        /**
         * Set the colors the progress spinner alternates between.
         *
         * @param colors Array of integers describing the colors. Must be non-`null`.
         */
        fun setColors(colors: IntArray) {
            mColors = colors
            // if colors are reset, make sure to reset the color index as well
            setColorIndex(0)
        }

        /**
         * Set the absolute color of the progress spinner. This is should only
         * be used when animating between current and next color when the
         * spinner is rotating.
         *
         * @param color int describing the color.
         */
        fun setColor(color: Int) {
            mCurrentColor = color
        }

        /**
         * @param index Index into the color array of the color to display in
         * the progress spinner.
         */
        fun setColorIndex(index: Int) {
            mColorIndex = index
            mCurrentColor = mColors!![mColorIndex]
        }

        /**
         * Proceed to the next available ring color. This will automatically
         * wrap back to the beginning of colors.
         */
        fun goToNextColor() {
            setColorIndex(nextColorIndex)
        }

        fun setColorFilter(filter: ColorFilter?) {
            mPaint.colorFilter = filter
            invalidateSelf()
        }

        fun setInsets(width: Int, height: Int) {
            val minEdge = Math.min(width, height).toFloat()
            val insets: Float
            if (centerRadius <= 0 || minEdge < 0) {
                insets = Math.ceil((strokeWidth / 2.0f).toDouble()).toFloat()
            } else {
                insets = (minEdge / 2.0f - centerRadius).toFloat()
            }
            this.insets = insets
        }

        /**
         * @param show Set to true to show the arrow head on the progress spinner.
         */
        fun setShowArrow(show: Boolean) {
            if (mShowArrow != show) {
                mShowArrow = show
                invalidateSelf()
            }
        }

        /**
         * @param scale Set the scale of the arrowhead for the spinner.
         */
        fun setArrowScale(scale: Float) {
            if (scale != mArrowScale) {
                mArrowScale = scale
                invalidateSelf()
            }
        }

        /**
         * If the start / end trim are offset to begin with, store them so that
         * animation starts from that offset.
         */
        fun storeOriginals() {
            startingStartTrim = startTrim
            startingEndTrim = endTrim
            startingRotation = rotation
        }

        /**
         * Reset the progress spinner to default rotation, start and end angles.
         */
        fun resetOriginals() {
            startingStartTrim = 0f
            startingEndTrim = 0f
            startingRotation = 0f
            startTrim = 0f
            endTrim = 0f
            rotation = 0f
        }

        private fun invalidateSelf() {
            mCallback.invalidateDrawable(null!!)
        }
    }

    companion object {
        private val LINEAR_INTERPOLATOR = LinearInterpolator()
        internal val MATERIAL_INTERPOLATOR: Interpolator = FastOutSlowInInterpolator()

        private val FULL_ROTATION = 1080.0f

        // Maps to ProgressBar.Large style
        internal const val LARGE = 0
        // Maps to ProgressBar default style
        internal const val DEFAULT = 1

        // Maps to ProgressBar default style
        private val CIRCLE_DIAMETER = 40
        //should add up to 10 when + stroke_width
        private val CENTER_RADIUS = 8.75f
        private val STROKE_WIDTH = 2.5f

        // Maps to ProgressBar.Large style
        private val CIRCLE_DIAMETER_LARGE = 56
        private val CENTER_RADIUS_LARGE = 12.5f
        private val STROKE_WIDTH_LARGE = 3f

        private val COLORS = intArrayOf(Color.BLACK)

        /**
         * The value in the linear interpolator for animating the drawable at which
         * the color transition should start
         */
        private val COLOR_START_DELAY_OFFSET = 0.75f
        private val END_TRIM_START_DELAY_OFFSET = 0.5f
        private val START_TRIM_DURATION_OFFSET = 0.5f

        /**
         * The duration of a single progress spin in milliseconds.
         */
        private val ANIMATION_DURATION = 1332

        /**
         * The number of points in the progress "star".
         */
        private val NUM_POINTS = 5f

        /**
         * Layout info for the arrowhead in dp
         */
        private val ARROW_WIDTH = 10
        private val ARROW_HEIGHT = 5
        private val ARROW_OFFSET_ANGLE = 5f

        /**
         * Layout info for the arrowhead for the large spinner in dp
         */
        private val ARROW_WIDTH_LARGE = 12
        private val ARROW_HEIGHT_LARGE = 6
        private val MAX_PROGRESS_ARC = .8f
    }
}
