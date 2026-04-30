package com.example.englishtobengaliphonix

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.asAndroidPath
import androidx.compose.ui.graphics.asComposePath
import androidx.core.graphics.PathParser
import kotlin.math.min

/**
 * Scales SVG path data to fit within a target canvas of [canvasSize] × [canvasSize] pixels,
 * with a [padding]-pixel margin on each side.
 *
 * When [canvasSize] is not supplied the legacy fixed value (800 px canvas + 100 px padding)
 * is used so existing call-sites continue to work unchanged.
 */
fun scalePath(
    svgData: String,
    canvasSize: Float = 800f,
    padding: Float = 100f
): Path {
    val rawPath = PathParser.createPathFromPathData(svgData)
    val bounds = android.graphics.RectF()
    rawPath.computeBounds(bounds, true)

    val availableSize = canvasSize - 2 * padding
    val scale = min(
        availableSize / (if (bounds.width() == 0f) 1f else bounds.width()),
        availableSize / (if (bounds.height() == 0f) 1f else bounds.height())
    )

    val matrix = android.graphics.Matrix()
    matrix.postTranslate(-bounds.left, -bounds.top)
    matrix.postScale(scale, scale)
    matrix.postTranslate(padding, padding)

    rawPath.transform(matrix)
    return rawPath.asComposePath()
}

/**
 * Splits SVG path data into individual stroke segments by cutting at each
 * absolute-move command (`M`).  Compound glyphs (e.g. আ, ঃ) are naturally
 * decomposed this way, giving independent guide paths for the Watch animation
 * and Hint feature.
 *
 * When the path data contains only one `M` (a single stroke) the original
 * string is returned as a one-element list.
 */
fun splitIntoStrokes(pathData: String): List<String> {
    if (pathData.isBlank()) return listOf(pathData)

    // Find every absolute-M command position.
    // The regex matches 'M' followed by a digit, minus, or period
    // (guards against false positives like the letter 'm' in SVG).
    val pattern = Regex("M(?=[0-9.\\-])")
    val matches = pattern.findAll(pathData).toList()

    if (matches.size <= 1) return listOf(pathData)

    return matches.mapIndexed { index, match ->
        val start = match.range.first
        val end = if (index + 1 < matches.size) matches[index + 1].range.first else pathData.length
        pathData.substring(start, end).trim()
    }
}

/**
 * Returns the portion of [sourcePath] from 0% to [progress] (0f–1f) using PathMeasure.
 * Used for the animated stroke-drawing cursor.
 */
fun extractPartialPath(sourcePath: Path, progress: Float): Path {
    val measure = android.graphics.PathMeasure(sourcePath.asAndroidPath(), false)
    val totalLength = measure.length
    val dst = android.graphics.Path()
    if (totalLength > 0f) {
        measure.getSegment(0f, totalLength * progress.coerceIn(0f, 1f), dst, true)
    }
    return dst.asComposePath()
}

/**
 * Returns the 2D canvas position at [progress] (0f–1f) along [sourcePath],
 * or null if the path is empty.
 */
fun getPathEndPosition(sourcePath: Path, progress: Float): Offset? {
    val measure = android.graphics.PathMeasure(sourcePath.asAndroidPath(), false)
    val totalLength = measure.length
    if (totalLength <= 0f) return null
    val pos = FloatArray(2)
    measure.getPosTan(totalLength * progress.coerceIn(0f, 1f), pos, null)
    return Offset(pos[0], pos[1])
}