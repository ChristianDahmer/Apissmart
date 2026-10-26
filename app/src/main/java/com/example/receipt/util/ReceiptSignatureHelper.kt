package com.example.receipt.util

import android.graphics.Paint
import android.graphics.Path as AndroidPath
import android.graphics.RectF
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path as ComposePath
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp

object ReceiptSignatureHelper {

    /**
     * Serializes a list of line point lists into a concise SVG path `d` attribute string.
     * Example output: "M 10.5 20.0 L 15.2 25.0 L 20.0 30.0 M 50.0 60.0 L 55.0 65.0"
     */
    fun serializePathsToSvgPath(paths: List<List<Offset>>): String {
        if (paths.isEmpty()) return ""
        val sb = StringBuilder()
        for (line in paths) {
            if (line.isEmpty()) continue
            sb.append("M ").append(formatNum(line.first().x)).append(" ").append(formatNum(line.first().y)).append(" ")
            for (i in 1 until line.size) {
                sb.append("L ").append(formatNum(line[i].x)).append(" ").append(formatNum(line[i].y)).append(" ")
            }
        }
        return sb.toString().trim()
    }

    /**
     * Parses an SVG path `d` string back into a list of line points for Compose or Canvas.
     */
    fun parseSvgPathToPaths(svgPathD: String?): List<List<Offset>> {
        if (svgPathD.isNullOrBlank()) return emptyList()
        val result = mutableListOf<List<Offset>>()
        val tokens = svgPathD.trim().split("\\s+".toRegex())
        var currentLine = mutableListOf<Offset>()
        var i = 0

        while (i < tokens.size) {
            when (tokens[i].uppercase()) {
                "M" -> {
                    if (currentLine.isNotEmpty()) {
                        result.add(currentLine)
                        currentLine = mutableListOf()
                    }
                    if (i + 2 < tokens.size) {
                        val x = tokens[i + 1].toFloatOrNull()
                        val y = tokens[i + 2].toFloatOrNull()
                        if (x != null && y != null) {
                            currentLine.add(Offset(x, y))
                        }
                        i += 2
                    }
                }
                "L" -> {
                    if (i + 2 < tokens.size) {
                        val x = tokens[i + 1].toFloatOrNull()
                        val y = tokens[i + 2].toFloatOrNull()
                        if (x != null && y != null) {
                            currentLine.add(Offset(x, y))
                        }
                        i += 2
                    }
                }
            }
            i++
        }
        if (currentLine.isNotEmpty()) {
            result.add(currentLine)
        }
        return result
    }

    private fun CharSequence?.isNull試合OrBlank(): Boolean = this == null || this.trim().isEmpty()

    private fun formatNum(value: Float): String {
        return "%.1f".format(java.util.Locale.US, value)
    }

    /**
     * Converts a path string to Compose Path object.
     */
    fun buildComposePath(svgPathD: String): ComposePath {
        val composePath = ComposePath()
        val lines = parseSvgPathToPaths(svgPathD)
        for (line in lines) {
            if (line.size > 1) {
                composePath.moveTo(line.first().x, line.first().y)
                for (j in 1 until line.size) {
                    composePath.lineTo(line[j].x, line[j].y)
                }
            }
        }
        return composePath
    }

    /**
     * Draws the signature onto an Android Canvas (e.g. for PdfDocument or Bitmap).
     */
    fun drawSignatureOnAndroidCanvas(
        canvas: android.graphics.Canvas,
        svgPathD: String,
        bounds: RectF,
        strokeColor: Int = 0xFF0F172A.toInt(),
        strokeWidth: Float = 3f
    ) {
        val lines = parseSvgPathToPaths(svgPathD)
        if (lines.isEmpty()) return

        // Calculate original bounding box of signature points to scale onto target bounds
        var minX = Float.MAX_VALUE
        var minY = Float.MAX_VALUE
        var maxX = Float.MIN_VALUE
        var maxY = Float.MIN_VALUE

        for (line in lines) {
            for (pt in line) {
                if (pt.x < minX) minX = pt.x
                if (pt.y < minY) minY = pt.y
                if (pt.x > maxX) maxX = pt.x
                if (pt.y > maxY) maxY = pt.y
            }
        }

        if (minX >= maxX || minY >= maxY) return

        val origWidth = maxX - minX
        val origHeight = maxY - minY
        val scaleX = bounds.width() / origWidth
        val scaleY = bounds.height() / origHeight
        val scale = minOf(scaleX, scaleY) * 0.9f

        val paint = Paint().apply {
            color = strokeColor
            style = Paint.Style.STROKE
            this.strokeWidth = strokeWidth
            strokeCap = Paint.Cap.ROUND
            strokeJoin = Paint.Join.ROUND
            isAntiAlias = true
        }

        val path = AndroidPath()
        for (line in lines) {
            if (line.size > 1) {
                val startX = bounds.left + (line.first().x - minX) * scale + (bounds.width() - origWidth * scale) / 2f
                val startY = bounds.top + (line.first().y - minY) * scale + (bounds.height() - origHeight * scale) / 2f
                path.moveTo(startX, startY)
                for (j in 1 until line.size) {
                    val px = bounds.left + (line[j].x - minX) * scale + (bounds.width() - origWidth * scale) / 2f
                    val py = bounds.top + (line[j].y - minY) * scale + (bounds.height() - origHeight * scale) / 2f
                    path.lineTo(px, py)
                }
            }
        }
        canvas.drawPath(path, paint)
    }

    /**
     * Inline HTML SVG string representation of the signature for HTML export and printing.
     */
    fun toHtmlSvgString(svgPathD: String?, strokeHex: String = "#0F172A"): String {
        if (svgPathD.isNullBlank()) return ""
        return """
            <svg viewBox="0 0 350 120" style="width: 180px; height: 60px;" xmlns="http://www.w3.org/2000/svg">
                <path d="$svgPathD" fill="none" stroke="$strokeHex" stroke-width="3.5" stroke-linecap="round" stroke-linejoin="round" />
            </svg>
        """.trimIndent()
    }

    private fun String?.isNullBlank(): Boolean = this == null || this.trim().isEmpty()
}

@Composable
fun SignaturePathView(
    svgPathD: String,
    modifier: Modifier = Modifier,
    strokeColor: Color = Color(0xFF0F172A)
) {
    if (svgPathD.isBlank()) return
    val lines = ReceiptSignatureHelper.parseSvgPathToPaths(svgPathD)
    if (lines.isEmpty()) return

    Canvas(modifier = modifier.fillMaxSize()) {
        // Calculate bounds
        var minX = Float.MAX_VALUE
        var minY = Float.MAX_VALUE
        var maxX = Float.MIN_VALUE
        var maxY = Float.MIN_VALUE

        for (line in lines) {
            for (pt in line) {
                if (pt.x < minX) minX = pt.x
                if (pt.y < minY) minY = pt.y
                if (pt.x > maxX) maxX = pt.x
                if (pt.y > maxY) maxY = pt.y
            }
        }

        if (minX < maxX && minY < maxY) {
            val origW = maxX - minX
            val origH = maxY - minY
            val scaleX = size.width / origW
            val scaleY = size.height / origH
            val scale = minOf(scaleX, scaleY) * 0.85f
            val offsetX = (size.width - origW * scale) / 2f
            val offsetY = (size.height - origH * scale) / 2f

            for (line in lines) {
                if (line.size > 1) {
                    val composePath = ComposePath().apply {
                        val startX = offsetX + (line.first().x - minX) * scale
                        val startY = offsetY + (line.first().y - minY) * scale
                        moveTo(startX, startY)
                        for (j in 1 until line.size) {
                            val px = offsetX + (line[j].x - minX) * scale
                            val py = offsetY + (line[j].y - minY) * scale
                            lineTo(px, py)
                        }
                    }
                    drawPath(
                        path = composePath,
                        color = strokeColor,
                        style = Stroke(
                            width = 2.5.dp.toPx(),
                            cap = StrokeCap.Round,
                            join = StrokeJoin.Round
                        )
                    )
                }
            }
        }
    }
}
