package com.example.englishtobengaliphonix

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.asAndroidPath
import androidx.compose.ui.graphics.asComposePath
import androidx.core.graphics.PathParser
import kotlin.math.min

fun scalePath(svgData: String): Path {
    val rawPath = PathParser.createPathFromPathData(svgData)
    val bounds = android.graphics.RectF()
    rawPath.computeBounds(bounds, true)

    val targetSize = 800f
    val scale = min(
        targetSize / (if (bounds.width() == 0f) 1f else bounds.width()),
        targetSize / (if (bounds.height() == 0f) 1f else bounds.height())
    )

    val matrix = android.graphics.Matrix()
    matrix.postTranslate(-bounds.left, -bounds.top)
    matrix.postScale(scale, scale)
    matrix.postTranslate(100f, 100f)

    rawPath.transform(matrix)
    return rawPath.asComposePath()
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