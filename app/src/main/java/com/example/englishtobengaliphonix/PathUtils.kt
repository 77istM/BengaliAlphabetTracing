package com.example.englishtobengaliphonix

import androidx.compose.ui.graphics.Path
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