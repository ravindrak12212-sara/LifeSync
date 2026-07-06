package com.example.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun GlassmorphicCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    borderColor: Color = Color.Transparent,
    content: @Composable ColumnScope.() -> Unit
) {
    val isLight = MaterialTheme.colorScheme.background == com.example.ui.theme.LightBG
    
    val finalBgColor = if (isLight) {
        Color.White.copy(alpha = 0.72f) // Beautiful transparent white frost
    } else {
        Color(0xFF1E293B).copy(alpha = 0.62f) // Beautiful transparent deep slate frost
    }

    val finalBorderColor = if (borderColor != Color.Transparent) {
        borderColor
    } else if (isLight) {
        Color(0xFFE2E8F0).copy(alpha = 0.6f) // Ultra-delicate slate border
    } else {
        Color(0xFF334155).copy(alpha = 0.6f) // Ultra-delicate dark slate border
    }

    val cardColors = CardDefaults.cardColors(
        containerColor = finalBgColor,
        contentColor = MaterialTheme.colorScheme.onSurface
    )

    val cardShape = RoundedCornerShape(24.dp) // Soft modern curves (rounded-3xl equivalent)

    if (onClick != null) {
        Card(
            onClick = onClick,
            modifier = modifier
                .border(1.dp, finalBorderColor, cardShape),
            shape = cardShape,
            colors = cardColors,
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
            content = content
        )
    } else {
        Card(
            modifier = modifier
                .border(1.dp, finalBorderColor, cardShape),
            shape = cardShape,
            colors = cardColors,
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
            content = content
        )
    }
}

@Composable
fun AnimatedProgressRing(
    progress: Float, // 0f to 1f
    modifier: Modifier = Modifier,
    strokeWidth: Dp = 10.dp,
    primaryColor: Color = MaterialTheme.colorScheme.primary,
    secondaryColor: Color = MaterialTheme.colorScheme.secondary,
    backgroundColor: Color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
    centerContent: @Composable () -> Unit = {}
) {
    val animatedProgress by animateFloatAsState(
        targetValue = progress.coerceIn(0f, 1f),
        animationSpec = tween(1000),
        label = "RingProgress"
    )

    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val sizeMin = size.minDimension
            val strokeWidthPx = strokeWidth.toPx()
            val radius = (sizeMin - strokeWidthPx) / 2f

            // 1. Draw Background Ring
            drawCircle(
                color = backgroundColor,
                radius = radius,
                style = Stroke(width = strokeWidthPx)
            )

            // 2. Draw Progress Arc
            drawArc(
                brush = Brush.sweepGradient(listOf(primaryColor, secondaryColor, primaryColor)),
                startAngle = -90f,
                sweepAngle = animatedProgress * 360f,
                useCenter = false,
                style = Stroke(width = strokeWidthPx, cap = StrokeCap.Round)
            )
        }
        centerContent()
    }
}

@Composable
fun CustomLineChart(
    dataPoints: List<Float>,
    labels: List<String>,
    modifier: Modifier = Modifier,
    lineColor: Color = MaterialTheme.colorScheme.primary,
    gradientColor: Color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.2f)
) {
    if (dataPoints.isEmpty()) {
        Box(modifier = modifier, contentAlignment = Alignment.Center) {
            Text("No log data available", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        return
    }

    val maxVal = (dataPoints.maxOrNull() ?: 1f).coerceAtLeast(1f)

    Column(modifier = modifier) {
        Canvas(modifier = Modifier.weight(1f).fillMaxWidth()) {
            val width = size.width
            val height = size.height
            val spacingX = width / (dataPoints.size - 1).coerceAtLeast(1)

            val path = Path()
            val fillPath = Path()

            dataPoints.forEachIndexed { i, value ->
                val x = i * spacingX
                val y = height - (value / maxVal) * (height - 20f) - 10f

                if (i == 0) {
                    path.moveTo(x, y)
                    fillPath.moveTo(x, height)
                    fillPath.lineTo(x, y)
                } else {
                    path.lineTo(x, y)
                    fillPath.lineTo(x, y)
                }

                if (i == dataPoints.size - 1) {
                    fillPath.lineTo(x, height)
                    fillPath.close()
                }
            }

            // Draw Area Gradient
            drawPath(
                path = fillPath,
                brush = Brush.verticalGradient(
                    colors = listOf(gradientColor, Color.Transparent),
                    startY = 0f,
                    endY = height
                )
            )

            // Draw Line
            drawPath(
                path = path,
                color = lineColor,
                style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
            )

            // Draw Data Dots
            dataPoints.forEachIndexed { i, value ->
                val x = i * spacingX
                val y = height - (value / maxVal) * (height - 20f) - 10f
                drawCircle(
                    color = lineColor,
                    radius = 4.dp.toPx(),
                    center = androidx.compose.ui.geometry.Offset(x, y)
                )
            }
        }

        // Labels Row
        Row(
            modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            labels.forEach { label ->
                Text(
                    text = label,
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun HeatmapChart(
    completions: List<Boolean>, // List of 28 days completion status (e.g., last 4 weeks)
    modifier: Modifier = Modifier,
    completedColor: Color = MaterialTheme.colorScheme.primary,
    emptyColor: Color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
) {
    val totalDays = 28
    val paddedCompletions = completions.take(totalDays) + List((totalDays - completions.size).coerceAtLeast(0)) { false }

    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = "Activity Grid (Last 28 Days)",
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        Row(
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Render 4 weeks, each with 7 days
            for (w in 0 until 4) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    for (d in 0 until 7) {
                        val index = w * 7 + d
                        val isDone = paddedCompletions.getOrNull(index) ?: false
                        Box(
                            modifier = Modifier
                                .size(16.dp)
                                .clip(RoundedCornerShape(3.dp))
                                .background(if (isDone) completedColor else emptyColor)
                        )
                    }
                }
            }
        }
    }
}
