package com.example.chilidetectionapp.ui.scan.objectdetection

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import androidx.core.content.ContextCompat
import com.example.chilidetectionapp.R
import org.tensorflow.lite.task.vision.detector.Detection
import java.util.*
import kotlin.math.min

class OverlayView(context: Context?, attrs: AttributeSet?) : View(context, attrs) {

    private var results: List<Detection> = LinkedList()
    private var boxPaint = Paint()
    private var textBackgroundPaint = Paint()
    private var textPaint = Paint()
    private var bounds = Rect()

    private var scaleFactor: Float = 1f
    private var offsetX: Float = 0f
    private var offsetY: Float = 0f

    init {
        initPaints()
    }

    fun clear() {
        textPaint.reset()
        textBackgroundPaint.reset()
        boxPaint.reset()
        initPaints()
        invalidate()
    }

    private fun initPaints() {
        textBackgroundPaint.color = Color.BLACK
        textBackgroundPaint.style = Paint.Style.FILL
        textBackgroundPaint.textSize = 50f

        textPaint.color = Color.WHITE
        textPaint.style = Paint.Style.FILL
        textPaint.textSize = 50f

        boxPaint.color = ContextCompat.getColor(context!!, R.color.bounding_box_color)
        boxPaint.strokeWidth = 8F
        boxPaint.style = Paint.Style.STROKE
    }

    override fun draw(canvas: Canvas) {
        super.draw(canvas)

        for (result in results) {
            val box = result.boundingBox

            val left = box.left * scaleFactor + offsetX
            val top = box.top * scaleFactor + offsetY
            val right = box.right * scaleFactor + offsetX
            val bottom = box.bottom * scaleFactor + offsetY

            val drawableRect = RectF(left, top, right, bottom)
            canvas.drawRect(drawableRect, boxPaint)

            val category = result.categories.firstOrNull()
            if (category != null) {
                val label = category.label
                val score = category.score
                val confidenceText = "$label ${(score * 100).toInt()}%"

                textBackgroundPaint.getTextBounds(confidenceText, 0, confidenceText.length, bounds)
                val textWidth = bounds.width()
                val textHeight = bounds.height()

                canvas.drawRect(
                    left,
                    top - textHeight - BOUNDING_RECT_TEXT_PADDING,
                    left + textWidth + 2 * BOUNDING_RECT_TEXT_PADDING,
                    top,
                    textBackgroundPaint
                )

                canvas.drawText(confidenceText, left + BOUNDING_RECT_TEXT_PADDING, top - BOUNDING_RECT_TEXT_PADDING, textPaint)
            }
        }
    }

    fun setResults(
        detectionResults: MutableList<Detection>,
        imageHeight: Int,
        imageWidth: Int,
    ) {
        results = detectionResults

        // Gunakan min untuk menjaga aspek rasio
        val scaleX = width.toFloat() / imageWidth
        val scaleY = height.toFloat() / imageHeight
        scaleFactor = min(scaleX, scaleY)

        val scaledImageWidth = imageWidth * scaleFactor
        val scaledImageHeight = imageHeight * scaleFactor

        // Hitung offset agar berada di tengah (letterbox)
        offsetX = (width - scaledImageWidth) / 2f
        offsetY = (height - scaledImageHeight) / 2f

        invalidate()
    }

    companion object {
        private const val BOUNDING_RECT_TEXT_PADDING = 8
    }
}