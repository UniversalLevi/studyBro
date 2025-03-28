package com.example.studymate.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp

/**
 * A simple bar chart element
 */
@Composable
fun SimpleBarChart(value: Float, maxValue: Float, color: Color) {
    val progress = if (maxValue > 0) value / maxValue else 0f
    
    Box(
        modifier = Modifier
            .height(120.dp)
            .width(24.dp)
            .clip(RoundedCornerShape(4.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Box(
            modifier = Modifier
                .height(120.dp * progress)
                .width(24.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(color)
        )
    }
}

/**
 * A simple progress ring
 */
@Composable
fun SimpleProgressRing(
    progress: Float,
    color: Color,
    backgroundColor: Color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
    strokeWidth: Float = 8f
) {
    Canvas(
        modifier = Modifier
            .size(100.dp)
            .padding(8.dp)
    ) {
        // Background circle
        drawCircle(
            color = backgroundColor,
            radius = size.minDimension / 2,
            style = Stroke(width = strokeWidth)
        )
        
        // Progress arc
        drawArc(
            color = color,
            startAngle = -90f,
            sweepAngle = 360f * progress,
            useCenter = false,
            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
        )
    }
} 