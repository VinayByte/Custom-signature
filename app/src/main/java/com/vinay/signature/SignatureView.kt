package com.vinay.signature

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.util.TypedValue
import android.view.MotionEvent
import android.view.View
import java.util.*
import kotlin.math.*

class SignatureView : View {

    private var maxStrokeWidth =
        TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 6f, resources.displayMetrics)
    private var minStrokeWidth =
        TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 1.5f, resources.displayMetrics)
    var smoothingRatio = 0.75f

    // points
    private var pointQueue = ArrayList<SignaturePoint>()
    private var pointRecycle = ArrayList<SignaturePoint>()

    // misc
    var density = 0f
    var bitmap: Bitmap? = null
    private var canvas: Canvas? = null
    private var paint: Paint? = null
    private var dirty: RectF? = null
    private var listeners = ArrayList<SignatureListner>()


    @JvmOverloads
    constructor(
        context: Context?,
        attrs: AttributeSet?,
        defStyleAttr: Int = 0
    ) : super(context, attrs, defStyleAttr) {
        init()
    }

    private fun init() {

        // init screen density
        val metrics = resources.displayMetrics
        density = (metrics.xdpi + metrics.ydpi) / 2f

        // init paint
        paint = Paint()
        paint!!.strokeCap = Paint.Cap.ROUND
        paint!!.isAntiAlias = true

        // apply default settings
        paint!!.color = STROKE_COLOR
        // init dirty rect
        dirty = RectF()
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        clear()
    }

    override fun onTouchEvent(e: MotionEvent): Boolean {
        val action = e.action
        // on down, initialize stroke point
        if (action == MotionEvent.ACTION_DOWN) {
            addPoint(getRecycledPoint(e.x, e.y, e.eventTime))

            // notify listeners of sign
            for (listener in listeners) {
                listener.onSignatureDraw()
            }
        } else if (action == MotionEvent.ACTION_MOVE) {
            if (!pointQueue[pointQueue.size - 1].equals(e.x, e.y)) {
                addPoint(getRecycledPoint(e.x, e.y, e.eventTime))
            }
        }

        // on up, draw remaining queue
        if (action == MotionEvent.ACTION_UP) {
            // draw final points
            if (pointQueue.size == 1) {
                draw(pointQueue[0])
            } else if (pointQueue.size == 2) {
                pointQueue[1].findControlPoints(pointQueue[0], null)
                draw(pointQueue[0], pointQueue[1])
            }

            // recycle remaining points
            pointRecycle.addAll(pointQueue)
            pointQueue.clear()
        }
        return true
    }

    override fun onDraw(canvas: Canvas) {
        // simply paint the bitmap on the canvas
        canvas.drawBitmap(bitmap!!, 0f, 0f, null)
        super.onDraw(canvas)
    }

    /**
     * Clears the view
     */
    fun clear() {
        // clean up existing bitmap
        if (bitmap != null) {
            bitmap!!.recycle()
        }

        // init bitmap cache
        val bitmaps = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        canvas = Canvas(bitmaps)
        bitmap = bitmaps

        // notify listeners
        for (listener in listeners) {
            listener.onSignatureClear()
        }
        invalidate()
    }

    fun getBitmaps(): Bitmap {
        // create new bitmap
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val bitmapCanvas = Canvas(bitmap)
        // draw bitmap
        bitmapCanvas.drawBitmap(this.bitmap!!, 0f, 0f, null)
        return bitmap
    }


    //--------------------------------------
    // Util
    //--------------------------------------
    private fun addPoint(p: SignaturePoint) {
        pointQueue.add(p)
        val queueSize = pointQueue.size
        when (queueSize) {
            1 -> {
                // compute starting velocity
                val recycleSize = pointRecycle.size
                p.velocity =
                    if (recycleSize > 0) pointRecycle[recycleSize - 1].velocityTo(p) / 2f else 0f

                // compute starting stroke width
                paint!!.strokeWidth = computeStrokeWidth(p.velocity)
            }
            2 -> {
                val p0 = pointQueue[0]

                // compute velocity for new point
                p.velocity = p0.velocityTo(p)

                // re-compute velocity for 1st point (predictive velocity)
                p0.velocity = p0.velocity + p.velocity / 2f

                // find control points for first point
                p0.findControlPoints(null, p)

                // update starting stroke width
                paint!!.strokeWidth = computeStrokeWidth(p0.velocity)
            }
            3 -> {
                val p0 = pointQueue[0]
                val p1 = pointQueue[1]

                // find control points for second point
                p1.findControlPoints(p0, p)

                // compute velocity for new point
                p.velocity = p1.velocityTo(p)

                // draw geometry between first 2 points
                draw(p0, p1)

                // recycle 1st point
                pointRecycle.add(pointQueue.removeAt(0))
            }
        }
    }

    private fun getRecycledPoint(x: Float, y: Float, time: Long): SignaturePoint {
        return if (pointRecycle.size == 0) {
            SignaturePoint(x, y, time)
        } else pointRecycle.removeAt(0).reset(x, y, time)
    }

    private fun computeStrokeWidth(velocity: Float): Float {
        return maxStrokeWidth - (maxStrokeWidth - minStrokeWidth) * Math.min(
            velocity / THRESHOLD_VELOCITY,
            1f
        )
    }

    private fun draw(p: SignaturePoint) {
        paint!!.style = Paint.Style.FILL

        // draw dot
        canvas!!.drawCircle(p.x, p.y, paint!!.strokeWidth / 2f, paint!!)
        invalidate()
    }

    private fun draw(p1: SignaturePoint, p2: SignaturePoint) {
        // init dirty rect
        dirty!!.left = Math.min(p1.x, p2.x)
        dirty!!.right = Math.max(p1.x, p2.x)
        dirty!!.top = Math.min(p1.y, p2.y)
        dirty!!.bottom = Math.max(p1.y, p2.y)
        paint!!.style = Paint.Style.STROKE

        // adjust low-pass ratio from changing acceleration
        // using comfortable range of 0.2 -> 0.3 approx.
        val acceleration =
            abs((p2.velocity - p1.velocity) / (p2.time - p1.time)) // in/s^2
        val filterRatio = min(
            FILTER_RATIO_MIN + FILTER_RATIO_ACCELERATION_MODIFIER * acceleration / THRESHOLD_ACCELERATION,
            1f
        )

        // compute new stroke width
        val desiredWidth = computeStrokeWidth(p2.velocity)
        val startWidth = paint!!.strokeWidth
        val endWidth = filterRatio * desiredWidth + (1f - filterRatio) * startWidth
        val deltaWidth = endWidth - startWidth

        // compute # of steps to interpolate in the bezier curve
        val steps = (sqrt(
            (p2.x - p1.x.toDouble()).pow(2.0) + (p2.y - p1.y.toDouble()).pow(2.0)
        ) / 5).toInt()

        // computational setup for differentials used to interpolate the bezier curve
        val u = 1f / (steps + 1)
        val uu = u * u
        val uuu = u * u * u
        val pre1 = 3f * u
        val pre2 = 3f * uu
        val pre3 = 6f * uu
        val pre4 = 6f * uuu
        val tmp1x = p1.x - p1.c2x * 2f + p2.c1x
        val tmp1y = p1.y - p1.c2y * 2f + p2.c1y
        val tmp2x = (p1.c2x - p2.c1x) * 3f - p1.x + p2.x
        val tmp2y = (p1.c2y - p2.c1y) * 3f - p1.y + p2.y
        var dx = (p1.c2x - p1.x) * pre1 + tmp1x * pre2 + tmp2x * uuu
        var dy = (p1.c2y - p1.y) * pre1 + tmp1y * pre2 + tmp2y * uuu
        var ddx = tmp1x * pre3 + tmp2x * pre4
        var ddy = tmp1y * pre3 + tmp2y * pre4
        val dddx = tmp2x * pre4
        val dddy = tmp2y * pre4
        var x1 = p1.x
        var y1 = p1.y
        var x2: Float
        var y2: Float

        // iterate over each step and draw the curve
        var i = 0
        while (i++ < steps) {
            x2 = x1 + dx
            y2 = y1 + dy
            paint!!.strokeWidth = startWidth + deltaWidth * i / steps
            canvas!!.drawLine(x1, y1, x2, y2, paint!!)
            x1 = x2
            y1 = y2
            dx += ddx
            dy += ddy
            ddx += dddx
            ddy += dddy

            // adjust dirty bounds to account for curve
            dirty!!.left = min(dirty!!.left, x1)
            dirty!!.right = max(dirty!!.right, x1)
            dirty!!.top = min(dirty!!.top, y1)
            dirty!!.bottom = max(dirty!!.bottom, y1)
        }
        paint!!.strokeWidth = endWidth
        canvas!!.drawLine(x1, y1, p2.x, p2.y, paint!!)
        invalidate(
            (dirty!!.left - maxStrokeWidth / 2).toInt(),
            (dirty!!.top - maxStrokeWidth / 2).toInt(),
            (dirty!!.right + maxStrokeWidth / 2).toInt(),
            (dirty!!.bottom + maxStrokeWidth / 2).toInt()
        )
    }

    /**
     * Listener for the Signature view to notify on actions
     */
    interface SignatureListner {

        fun onSignatureClear()

        fun onSignatureDraw()
    }

    //--------------------------------------
    // Util Classes
    //--------------------------------------
    inner class SignaturePoint(
        x: Float,
        y: Float,
        time: Long
    ) {
        var x = 0f
        var y = 0f
        var c1x = 0f
        var c1y = 0f
        var c2x = 0f
        var c2y = 0f
        var velocity = 0f
        var time: Long = 0
        fun reset(x: Float, y: Float, time: Long): SignaturePoint {
            this.x = x
            this.y = y
            this.time = time
            velocity = 0f
            c1x = x
            c1y = y
            c2x = x
            c2y = y
            return this
        }

        fun equals(x: Float, y: Float): Boolean {
            return this.x == x && this.y == y
        }

        private fun distanceTo(p: SignaturePoint): Float {
            val dx = p.x - x
            val dy = p.y - y
            return sqrt(dx * dx + dy * dy.toDouble()).toFloat()
        }

        fun velocityTo(p: SignaturePoint): Float {
            return 1000f * distanceTo(p) / (Math.abs(p.time - time) * density) // in/s
        }

        fun findControlPoints(prev: SignaturePoint?, next: SignaturePoint?) {
            if (prev == null && next == null) {
                return
            }
            var r = smoothingRatio

            // if start of a stroke, c2 control points half-way between this and next point
            if (prev == null) {
                c2x = x + r * (next!!.x - x) / 2f
                c2y = y + r * (next.y - y) / 2f
                return
            }

            // if end of a stroke, c1 control points half-way between this and prev point
            if (next == null) {
                c1x = x + r * (prev.x - x) / 2f
                c1y = y + r * (prev.y - y) / 2f
                return
            }

            // init control points
            c1x = (x + prev.x) / 2f
            c1y = (y + prev.y) / 2f
            c2x = (x + next.x) / 2f
            c2y = (y + next.y) / 2f

            // calculate control offsets
            val len1 = distanceTo(prev)
            val len2 = distanceTo(next)
            val k = len1 / (len1 + len2)
            val xM = c1x + (c2x - c1x) * k
            val yM = c1y + (c2y - c1y) * k
            val dx = x - xM
            val dy = y - yM

            // inverse smoothing ratio
            r = 1f - r

            // translate control points
            c1x += dx + r * (xM - c1x)
            c1y += dy + r * (yM - c1y)
            c2x += dx + r * (xM - c2x)
            c2y += dy + r * (yM - c2y)
        }

        init {
            reset(x, y, time)
        }
    }

    companion object {
        const val THRESHOLD_VELOCITY = 7f // in/s
        const val THRESHOLD_ACCELERATION = 3f // in/s^2
        const val FILTER_RATIO_MIN = 0.22f
        const val FILTER_RATIO_ACCELERATION_MODIFIER = 0.1f
        const val STROKE_COLOR = -0x1000000
    }
}