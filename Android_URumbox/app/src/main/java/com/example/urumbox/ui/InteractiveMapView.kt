package com.example.urumbox.ui

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.Path
import android.graphics.PointF
import android.graphics.drawable.TransitionDrawable
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.view.animation.DecelerateInterpolator
import androidx.appcompat.widget.AppCompatImageView
import androidx.core.content.ContextCompat
import com.example.urumbox.R
import com.example.urumbox.data.model.Coordenada
import com.example.urumbox.data.model.PasoNav

class InteractiveMapView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : AppCompatImageView(context, attrs, defStyleAttr), ScaleGestureDetector.OnScaleGestureListener {

    private var coordenadas: List<Coordenada> = emptyList()
    private var pasoActual: PasoNav? = null
    private var currentPiso: Int = 1

    private var density = 1f

    // Bounding box of the active map layout content mapped to screen density
    private var contentLeft = 0f
    private var contentTop = 0f
    private var contentRight = 0f
    private var contentBottom = 0f
    private var contentWidth = 0f
    private var contentHeight = 0f

    // Interactive Transformations
    private val mapMatrix = Matrix()
    private val savedMatrix = Matrix()

    // Drag / Pan state
    private val startPoint = PointF()
    private var mode = NONE

    private val scaleDetector = ScaleGestureDetector(context, this)

    // Double tap detection
    private var lastTouchTime: Long = 0
    private val DOUBLE_TAP_TIMEOUT = 300 // ms

    // Min and Max scale thresholds
    private var minScale = 1.0f
    private var maxScale = 5.0f
    private var baseScale = 1.0f
    private var baseTx = 0f
    private var baseTy = 0f

    private var pendingCenter: PointF? = null
    private var matrixAnimator: ValueAnimator? = null
    private var arrowX = -1f
    private var arrowY = -1f
    private var arrowAngle = 0f
    private var arrowAnimator: ValueAnimator? = null
    private var rotationAnimator: ValueAnimator? = null

    // Paints
    private val routePaint = Paint().apply {
        color = Color.parseColor("#0ea5e9") // Cyan/blue primary route color
        style = Paint.Style.STROKE
        strokeWidth = 14f
        strokeCap = Paint.Cap.ROUND
        strokeJoin = Paint.Join.ROUND
        isAntiAlias = true
    }

    private val pinOuterPaint = Paint().apply {
        color = Color.parseColor("#331A3A5C") // Semi-transparent pulse background in Dark Blue
        style = Paint.Style.FILL
        isAntiAlias = true
    }

    private val pinInnerPaint = Paint().apply {
        color = Color.parseColor("#1A3A5C") // Solid dark blue center
        style = Paint.Style.FILL
        isAntiAlias = true
    }

    private val pinBorderPaint = Paint().apply {
        color = Color.WHITE
        style = Paint.Style.STROKE
        strokeWidth = 4f
        isAntiAlias = true
    }

    companion object {
        private const val NONE = 0
        private const val DRAG = 1
        private const val ZOOM = 2
    }

    init {
        density = context.resources.displayMetrics.density
        
        contentLeft = 0f
        contentTop = 0f
        contentRight = 1000f * density
        contentBottom = 1000f * density
        contentWidth = contentRight - contentLeft
        contentHeight = contentBottom - contentTop

        // Use matrix scale type for custom translation/zooming control
        scaleType = ScaleType.MATRIX
        setImageResource(R.drawable.mapa_claustro_p1)
    }

    private fun getAngleAtCoordinateIndex(index: Int): Float {
        var angleDegrees = arrowAngle
        var foundDirection = false
        if (index + 1 < coordenadas.size) {
            val nextCoord = coordenadas[index + 1]
            if (nextCoord.piso == currentPiso) {
                val dx = nextCoord.x - coordenadas[index].x
                val dy = nextCoord.y - coordenadas[index].y
                if (Math.hypot(dx.toDouble(), dy.toDouble()) > 1.0) {
                    angleDegrees = Math.toDegrees(Math.atan2(dy.toDouble(), dx.toDouble())).toFloat()
                    foundDirection = true
                }
            }
        }
        if (!foundDirection && index > 0) {
            val prevCoord = coordenadas[index - 1]
            if (prevCoord.piso == currentPiso) {
                val dx = coordenadas[index].x - prevCoord.x
                val dy = coordenadas[index].y - prevCoord.y
                if (Math.hypot(dx.toDouble(), dy.toDouble()) > 1.0) {
                    angleDegrees = Math.toDegrees(Math.atan2(dy.toDouble(), dx.toDouble())).toFloat()
                    foundDirection = true
                }
            }
        }
        return angleDegrees
    }

    fun setRouteData(coordenadas: List<Coordenada>, pasoActual: PasoNav?) {
        this.coordenadas = coordenadas
        val oldPaso = this.pasoActual
        this.pasoActual = pasoActual
        
        pasoActual?.let { targetPaso ->
            val floorChanged = setFloor(targetPaso.piso)
            
            // Find coordinate index for targetPaso
            var targetIndex = -1
            var minDistance = Float.MAX_VALUE
            for (i in coordenadas.indices) {
                val coord = coordenadas[i]
                if (coord.piso == targetPaso.piso) {
                    val dist = Math.hypot((coord.x - targetPaso.x).toDouble(), (coord.y - targetPaso.y).toDouble()).toFloat()
                    if (dist < minDistance) {
                        minDistance = dist
                        targetIndex = i
                    }
                }
            }
            
            val finalTargetAngle = if (targetIndex != -1) {
                getAngleAtCoordinateIndex(targetIndex)
            } else {
                0f
            }
            
            if (arrowX == -1f || arrowY == -1f || floorChanged) {
                arrowAnimator?.cancel()
                rotationAnimator?.cancel()
                arrowX = targetPaso.x
                arrowY = targetPaso.y
                arrowAngle = finalTargetAngle
                invalidate()
            } else {
                arrowAnimator?.cancel()
                rotationAnimator?.cancel()
                
                val startX = arrowX
                val startY = arrowY
                val endX = targetPaso.x
                val endY = targetPaso.y
                
                val dx = endX - startX
                val dy = endY - startY
                val translationAngle = if (Math.hypot(dx.toDouble(), dy.toDouble()) > 0.1) {
                    Math.toDegrees(Math.atan2(dy.toDouble(), dx.toDouble())).toFloat()
                } else {
                    arrowAngle
                }
                
                // Phase 1: Translate the arrow to the target position
                arrowAnimator = ValueAnimator.ofFloat(0f, 1f).apply {
                    duration = 320 // Muy ligeramente más rápido que el desplazamiento del mapa (400ms)
                    addUpdateListener { animator ->
                        val fraction = animator.animatedValue as Float
                        arrowX = startX + fraction * (endX - startX)
                        arrowY = startY + fraction * (endY - startY)
                        arrowAngle = translationAngle // Keep translation angle during movement
                        invalidate()
                    }
                    
                    addListener(object : AnimatorListenerAdapter() {
                        override fun onAnimationEnd(animation: Animator) {
                            // Phase 2: Rotation to target path angle
                            // Normalize angle difference for shortest rotation path
                            var diff = finalTargetAngle - translationAngle
                            while (diff < -180f) diff += 360f
                            while (diff > 180f) diff -= 360f
                            val shortestEndAngle = translationAngle + diff
                            
                            rotationAnimator = ValueAnimator.ofFloat(0f, 1f).apply {
                                duration = 200 // smooth and rapid rotation at the end
                                addUpdateListener { rotAnimator ->
                                    val rotFraction = rotAnimator.animatedValue as Float
                                    arrowAngle = translationAngle + rotFraction * (shortestEndAngle - translationAngle)
                                    invalidate()
                                }
                                start()
                            }
                        }
                    })
                    
                    start()
                }
            }
            
            centerOnCoordinate(targetPaso.x, targetPaso.y, animate = !floorChanged)
        } ?: run {
            arrowAnimator?.cancel()
            rotationAnimator?.cancel()
            arrowX = -1f
            arrowY = -1f
            arrowAngle = 0f
            resetMapTransform()
        }
        
        invalidate()
    }

    fun setFloor(piso: Int): Boolean {
        if (this.currentPiso != piso) {
            this.currentPiso = piso
            
            var oldDrawable = drawable
            if (oldDrawable is TransitionDrawable && oldDrawable.numberOfLayers > 1) {
                oldDrawable = oldDrawable.getDrawable(1)
            }
            
            val newDrawable = if (piso == 2) {
                ContextCompat.getDrawable(context, R.drawable.mapa_claustro_p2)
            } else {
                ContextCompat.getDrawable(context, R.drawable.mapa_claustro_p1)
            }
            
            if (oldDrawable != null && newDrawable != null) {
                val transitionDrawable = TransitionDrawable(arrayOf(oldDrawable, newDrawable))
                transitionDrawable.isCrossFadeEnabled = true
                setImageDrawable(transitionDrawable)
                transitionDrawable.startTransition(300) // 300ms seamless crossfade
            } else {
                if (piso == 2) {
                    setImageResource(R.drawable.mapa_claustro_p2)
                } else {
                    setImageResource(R.drawable.mapa_claustro_p1)
                }
            }
            return true
        }
        return false
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        resetMapTransform()
        pendingCenter?.let {
            centerOnCoordinate(it.x, it.y, animate = false)
            pendingCenter = null
        }
    }

    fun resetMapTransform() {
        val viewWidth = width.toFloat()
        val viewHeight = height.toFloat()
        if (viewWidth == 0f || viewHeight == 0f) return

        // Scale to fit content bounds inside the view dimensions
        val scaleX = viewWidth / contentWidth
        val scaleY = viewHeight / contentHeight
        baseScale = minOf(scaleX, scaleY)

        // Center map content in the view
        val cx = (contentLeft + contentRight) / 2f
        val cy = (contentTop + contentBottom) / 2f
        
        baseTx = (viewWidth / 2f) - (cx * baseScale)
        baseTy = (viewHeight / 2f) - (cy * baseScale)

        mapMatrix.reset()
        mapMatrix.postScale(baseScale, baseScale)
        mapMatrix.postTranslate(baseTx, baseTy)
        
        imageMatrix = mapMatrix
        
        minScale = baseScale
        maxScale = baseScale * 6f
        
        invalidate()
    }

    private fun centerOnCoordinate(cx: Float, cy: Float, animate: Boolean = true) {
        val viewWidth = width.toFloat()
        val viewHeight = height.toFloat()
        if (viewWidth == 0f || viewHeight == 0f) {
            pendingCenter = PointF(cx, cy)
            return
        }

        val values = FloatArray(9)
        mapMatrix.getValues(values)
        var currentScale = values[Matrix.MSCALE_X]
        
        // If current scale is base scale, zoom in slightly for better focus (gentler zoom level)
        if (currentScale <= baseScale * 1.1f) {
            currentScale = baseScale * 1.5f
        }
        
        // Use density scaled coordinates
        val cxDensity = cx * density
        val cyDensity = cy * density
        
        val tx = (viewWidth / 2f) - (cxDensity * currentScale)
        val ty = (viewHeight / 2f) - (cyDensity * currentScale)
        
        val targetMatrix = Matrix()
        targetMatrix.postScale(currentScale, currentScale)
        targetMatrix.postTranslate(tx, ty)
        
        // Temporarily set mapMatrix to target to apply constraints, then restore
        val tempMatrix = Matrix(mapMatrix)
        mapMatrix.set(targetMatrix)
        limitPanBounds()
        val limitedTargetMatrix = Matrix(mapMatrix)
        mapMatrix.set(tempMatrix)

        if (animate) {
            animateToMatrix(limitedTargetMatrix)
        } else {
            mapMatrix.set(limitedTargetMatrix)
            imageMatrix = mapMatrix
            invalidate()
        }
    }

    private fun animateToMatrix(targetMatrix: Matrix) {
        matrixAnimator?.cancel()

        val startValues = FloatArray(9)
        mapMatrix.getValues(startValues)

        val endValues = FloatArray(9)
        targetMatrix.getValues(endValues)

        matrixAnimator = ValueAnimator.ofFloat(0f, 1f).apply {
            duration = 400
            interpolator = DecelerateInterpolator()
            addUpdateListener { animator ->
                val fraction = animator.animatedValue as Float
                val currentValues = FloatArray(9)
                for (i in 0..8) {
                    currentValues[i] = startValues[i] + fraction * (endValues[i] - startValues[i])
                }
                mapMatrix.setValues(currentValues)
                imageMatrix = mapMatrix
                invalidate()
            }
            start()
        }
    }

    private fun limitPanBounds() {
        val values = FloatArray(9)
        mapMatrix.getValues(values)
        val s = values[Matrix.MSCALE_X]
        var tx = values[Matrix.MTRANS_X]
        var ty = values[Matrix.MTRANS_Y]
        
        val viewWidth = width.toFloat()
        val viewHeight = height.toFloat()
        
        val scaledContentWidth = (contentRight - contentLeft) * s
        if (scaledContentWidth > viewWidth) {
            val minTx = viewWidth - contentRight * s
            val maxTx = -contentLeft * s
            if (tx < minTx) tx = minTx
            if (tx > maxTx) tx = maxTx
        } else {
            tx = (viewWidth / 2f) - ((contentLeft + contentRight) / 2f * s)
        }
        
        val scaledContentHeight = (contentBottom - contentTop) * s
        if (scaledContentHeight > viewHeight) {
            val minTy = viewHeight - contentBottom * s
            val maxTy = -contentTop * s
            if (ty < minTy) ty = minTy
            if (ty > maxTy) ty = maxTy
        } else {
            ty = (viewHeight / 2f) - ((contentTop + contentBottom) / 2f * s)
        }
        
        values[Matrix.MTRANS_X] = tx
        values[Matrix.MTRANS_Y] = ty
        mapMatrix.setValues(values)
    }

    override fun performClick(): Boolean {
        super.performClick()
        return true
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        scaleDetector.onTouchEvent(event)
        
        when (event.action and MotionEvent.ACTION_MASK) {
            MotionEvent.ACTION_DOWN -> {
                performClick()
                savedMatrix.set(mapMatrix)
                startPoint.set(event.x, event.y)
                mode = DRAG
                
                val clickTime = System.currentTimeMillis()
                if (clickTime - lastTouchTime < DOUBLE_TAP_TIMEOUT) {
                    onDoubleTap(event.x, event.y)
                }
                lastTouchTime = clickTime
            }
            MotionEvent.ACTION_POINTER_DOWN -> {
                mode = ZOOM
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_POINTER_UP -> {
                mode = NONE
            }
            MotionEvent.ACTION_MOVE -> {
                if (mode == DRAG) {
                    val dx = event.x - startPoint.x
                    val dy = event.y - startPoint.y
                    mapMatrix.set(savedMatrix)
                    mapMatrix.postTranslate(dx, dy)
                    limitPanBounds()
                    imageMatrix = mapMatrix
                    invalidate()
                }
            }
        }
        return true
    }

    private fun onDoubleTap(x: Float, y: Float) {
        val values = FloatArray(9)
        mapMatrix.getValues(values)
        val currentScale = values[Matrix.MSCALE_X]
        
        if (currentScale > baseScale * 1.5f) {
            resetMapTransform()
        } else {
            val targetScale = baseScale * 2.5f
            val factor = targetScale / currentScale
            mapMatrix.postScale(factor, factor, x, y)
            limitPanBounds()
            imageMatrix = mapMatrix
            invalidate()
        }
    }

    override fun onScale(detector: ScaleGestureDetector): Boolean {
        val scaleFactor = detector.scaleFactor
        val values = FloatArray(9)
        mapMatrix.getValues(values)
        val currentScale = values[Matrix.MSCALE_X]
        
        var targetScale = currentScale * scaleFactor
        
        if (targetScale < minScale) {
            targetScale = minScale
        } else if (targetScale > maxScale) {
            targetScale = maxScale
        }
        
        val factor = targetScale / currentScale
        mapMatrix.postScale(factor, factor, detector.focusX, detector.focusY)
        limitPanBounds()
        imageMatrix = mapMatrix
        invalidate()
        return true
    }

    override fun onScaleBegin(detector: ScaleGestureDetector): Boolean {
        mode = ZOOM
        return true
    }

    override fun onScaleEnd(detector: ScaleGestureDetector) {
        mode = NONE
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val viewWidth = width.toFloat()
        val viewHeight = height.toFloat()
        if (viewWidth == 0f || viewHeight == 0f) return

        val routePath = Path()
        var pathStarted = false
        val pts = FloatArray(2)

        // Find the index of coordinates list corresponding to the current step.
        var startIndex = 0
        pasoActual?.let { paso ->
            var minDistance = Float.MAX_VALUE
            var bestIndex = -1
            for (i in coordenadas.indices) {
                val coord = coordenadas[i]
                if (coord.piso == paso.piso) {
                    val dist = Math.hypot((coord.x - paso.x).toDouble(), (coord.y - paso.y).toDouble()).toFloat()
                    if (dist < minDistance) {
                        minDistance = dist
                        bestIndex = i
                    }
                }
            }
            if (bestIndex != -1 && minDistance < 15f) {
                startIndex = bestIndex
            }
        }

        // Draw path starting from arrow's current position to the target step and subsequent coordinates
        if (pasoActual != null && pasoActual!!.piso == currentPiso && arrowX != -1f && arrowY != -1f) {
            pts[0] = arrowX * density
            pts[1] = arrowY * density
            imageMatrix.mapPoints(pts)
            routePath.moveTo(pts[0], pts[1])
            pathStarted = true

            // Add the remaining coordinates starting from the current target step (startIndex)
            for (i in startIndex until coordenadas.size) {
                val coord = coordenadas[i]
                if (coord.piso == currentPiso) {
                    pts[0] = coord.x * density
                    pts[1] = coord.y * density
                    imageMatrix.mapPoints(pts)
                    routePath.lineTo(pts[0], pts[1])
                }
            }
        } else {
            // Fallback: draw from startIndex onwards if arrow is not active
            for (i in startIndex until coordenadas.size) {
                val coord = coordenadas[i]
                if (coord.piso == currentPiso) {
                    pts[0] = coord.x * density
                    pts[1] = coord.y * density
                    imageMatrix.mapPoints(pts)
                    val px = pts[0]
                    val py = pts[1]

                    if (!pathStarted) {
                        routePath.moveTo(px, py)
                        pathStarted = true
                    } else {
                        routePath.lineTo(px, py)
                    }
                }
            }
        }

        if (pathStarted) {
            canvas.drawPath(routePath, routePaint)
        }

        pasoActual?.let { paso ->
            if (paso.piso == currentPiso && arrowX != -1f && arrowY != -1f) {
                pts[0] = arrowX * density
                pts[1] = arrowY * density
                imageMatrix.mapPoints(pts)
                val px = pts[0]
                val py = pts[1]

                // Draw semi-transparent background pulse (slightly larger)
                canvas.drawCircle(px, py, 42f, pinOuterPaint)

                // Use the updated arrowAngle
                val hasPath = coordenadas.size > 1 && (startIndex < coordenadas.size)
                if (hasPath) {
                    canvas.save()
                    canvas.translate(px, py)
                    canvas.rotate(arrowAngle)

                    // Stylized GPS navigation arrow, slightly larger
                    val arrowPath = Path().apply {
                        moveTo(24f * density, 0f)
                        lineTo(-18f * density, -15f * density)
                        lineTo(-9f * density, 0f)
                        lineTo(-18f * density, 15f * density)
                        close()
                    }

                    val arrowPaint = Paint().apply {
                        color = Color.parseColor("#0A7EBF") // Secondary blue
                        style = Paint.Style.FILL
                        isAntiAlias = true
                    }

                    val arrowBorderPaint = Paint().apply {
                        color = Color.WHITE
                        style = Paint.Style.STROKE
                        strokeWidth = 5f
                        strokeCap = Paint.Cap.ROUND
                        strokeJoin = Paint.Join.ROUND
                        isAntiAlias = true
                    }

                    canvas.drawPath(arrowPath, arrowPaint)
                    canvas.drawPath(arrowPath, arrowBorderPaint)
                    canvas.restore()
                } else {
                    // Fallback to solid circle if at the very end of the route
                    canvas.drawCircle(px, py, 18f, pinInnerPaint)
                    canvas.drawCircle(px, py, 18f, pinBorderPaint)
                }
            }
        }
    }
}
