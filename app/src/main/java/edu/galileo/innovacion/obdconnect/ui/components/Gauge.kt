
package edu.galileo.innovacion.obdconnect.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun Gauge(
    label: String,
    value: Float,
    minValue: Float,
    maxValue: Float,
    modifier: Modifier = Modifier,
    unit: String = ""
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Gauge display
        Box(
            modifier = Modifier
                .size(180.dp)
                .padding(8.dp),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val canvasWidth = size.width
                val canvasHeight = size.height
                val radius = canvasWidth / 2 * 0.8f
                val center = Offset(canvasWidth / 2, canvasHeight / 2 + radius * 0.3f)

                // Draw gauge arc background
                drawArc(
                    color = Color.LightGray,
                    startAngle = 180f,
                    sweepAngle = 180f,
                    useCenter = false,
                    topLeft = Offset(center.x - radius, center.y - radius),
                    size = Size(radius * 2, radius * 2),
                    style = Stroke(width = 20f, cap = StrokeCap.Round)
                )

                // Draw gauge arc (colored part)
                val normalizedValue = ((value - minValue) / (maxValue - minValue)).coerceIn(0f, 1f)
                val sweepAngle = normalizedValue * 180f

                drawArc(
                    color = Color(0xFF4CAF50),
                    startAngle = 180f,
                    sweepAngle = sweepAngle,
                    useCenter = false,
                    topLeft = Offset(center.x - radius, center.y - radius),
                    size = Size(radius * 2, radius * 2),
                    style = Stroke(width = 20f, cap = StrokeCap.Round)
                )

                // Draw needle
                val needleAngle = 180f + sweepAngle
                val needleLength = radius * 0.9f

                rotate(needleAngle, pivot = center) {
                    drawLine(
                        color = Color.Red,
                        start = center,
                        end = Offset(center.x + needleLength, center.y),
                        strokeWidth = 8f,
                        cap = StrokeCap.Round
                    )
                }

                // Draw center circle
                drawCircle(
                    color = Color.Red,
                    radius = 12f,
                    center = center
                )

                // Draw min and max labels
                val textPaint = android.graphics.Paint().apply {
                    color = android.graphics.Color.GRAY
                    textSize = 30f
                    textAlign = android.graphics.Paint.Align.CENTER
                }

                // Min value position (left side)
                val minAngleRad = Math.toRadians(180.0)
                val minLabelX = center.x + cos(minAngleRad).toFloat() * (radius * 0.75f)
                val minLabelY = center.y + sin(minAngleRad).toFloat() * (radius * 0.75f)

                drawContext.canvas.nativeCanvas.drawText(
                    minValue.toInt().toString(),
                    minLabelX,
                    minLabelY + 10f,
                    textPaint
                )

                // Max value position (right side)
                val maxAngleRad = Math.toRadians(0.0)
                val maxLabelX = center.x + cos(maxAngleRad).toFloat() * (radius * 0.75f)
                val maxLabelY = center.y + sin(maxAngleRad).toFloat() * (radius * 0.75f)

                drawContext.canvas.nativeCanvas.drawText(
                    maxValue.toInt().toString(),
                    maxLabelX,
                    maxLabelY + 10f,
                    textPaint
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Label
        Text(
            text = label,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )

        // Current value
        Text(
            text = "${value.toInt()} $unit",
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Bold
        )
    }
}
