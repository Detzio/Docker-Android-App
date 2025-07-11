package com.example.dockerapp.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.dockerapp.ui.theme.DockerBlue
import com.example.dockerapp.ui.viewmodel.MetricPoint

@Composable
fun MetricChart(
    title: String,
    data: List<MetricPoint>,
    unit: String = "",
    modifier: Modifier = Modifier,
    color: Color = DockerBlue
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            if (data.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Aucune donnée disponible",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Gray
                    )
                }
            } else {
                val currentValue = data.lastOrNull()?.value ?: 0.0
                Text(
                    text = "Valeur actuelle: ${String.format("%.2f", currentValue)} $unit",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Black
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                LineChart(
                    data = data,
                    color = color,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                )
            }
        }
    }
}

@Composable
private fun LineChart(
    data: List<MetricPoint>,
    color: Color,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        if (data.isEmpty()) return@Canvas
        
        val width = size.width
        val height = size.height
        val padding = 40f
        
        // Calculer les valeurs min/max
        val minValue = data.minOfOrNull { it.value } ?: 0.0
        val maxValue = data.maxOfOrNull { it.value } ?: 1.0
        val valueRange = maxValue - minValue
        
        val minTime = data.minOfOrNull { it.timestamp } ?: 0L
        val maxTime = data.maxOfOrNull { it.timestamp } ?: 1L
        val timeRange = maxTime - minTime
        
        // Dessiner les axes
        drawAxes(
            width = width,
            height = height,
            padding = padding,
            color = Color.Gray
        )
        
        // Dessiner les lignes de grille
        drawGrid(
            width = width,
            height = height,
            padding = padding,
            color = Color.LightGray
        )
        
        // Préparer les points pour le graphique
        val points = data.map { point ->
            val x = padding + ((point.timestamp - minTime).toFloat() / timeRange.toFloat()) * (width - 2 * padding)
            val y = height - padding - ((point.value - minValue).toFloat() / valueRange.toFloat()) * (height - 2 * padding)
            Offset(x, y)
        }
        
        // Dessiner la ligne
        if (points.size > 1) {
            val path = Path()
            path.moveTo(points[0].x, points[0].y)
            
            for (i in 1 until points.size) {
                path.lineTo(points[i].x, points[i].y)
            }
            
            drawPath(
                path = path,
                color = color,
                style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
            )
        }
        
        // Dessiner les points
        points.forEach { point ->
            drawCircle(
                color = color,
                radius = 4.dp.toPx(),
                center = point
            )
        }
        
        // Dessiner les labels des valeurs
        drawValueLabels(
            minValue = minValue,
            maxValue = maxValue,
            height = height,
            padding = padding
        )
    }
}

private fun DrawScope.drawAxes(
    width: Float,
    height: Float,
    padding: Float,
    color: Color
) {
    // Axe X (horizontal)
    drawLine(
        color = color,
        start = Offset(padding, height - padding),
        end = Offset(width - padding, height - padding),
        strokeWidth = 2.dp.toPx()
    )
    
    // Axe Y (vertical)
    drawLine(
        color = color,
        start = Offset(padding, padding),
        end = Offset(padding, height - padding),
        strokeWidth = 2.dp.toPx()
    )
}

private fun DrawScope.drawGrid(
    width: Float,
    height: Float,
    padding: Float,
    color: Color
) {
    val gridLines = 5
    val stepX = (width - 2 * padding) / gridLines
    val stepY = (height - 2 * padding) / gridLines
    
    // Lignes verticales
    for (i in 1 until gridLines) {
        val x = padding + i * stepX
        drawLine(
            color = color,
            start = Offset(x, padding),
            end = Offset(x, height - padding),
            strokeWidth = 1.dp.toPx(),
            pathEffect = PathEffect.dashPathEffect(floatArrayOf(5f, 5f))
        )
    }
    
    // Lignes horizontales
    for (i in 1 until gridLines) {
        val y = padding + i * stepY
        drawLine(
            color = color,
            start = Offset(padding, y),
            end = Offset(width - padding, y),
            strokeWidth = 1.dp.toPx(),
            pathEffect = PathEffect.dashPathEffect(floatArrayOf(5f, 5f))
        )
    }
}

private fun drawValueLabels(
    minValue: Double,
    maxValue: Double,
    height: Float,
    padding: Float
) {
    val valueRange = maxValue - minValue
    val steps = 5
    
    for (i in 0..steps) {
        val value = minValue + (valueRange * i / steps)
        height - padding - ((value - minValue).toFloat() / valueRange.toFloat()) * (height - 2 * padding)
    
    }
}

@Composable
fun MetricSummaryCard(
    title: String,
    value: String,
    subtitle: String? = null,
    icon: @Composable (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                icon?.invoke()
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    color = Color.Gray
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = value,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )
            
            subtitle?.let {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
            }
        }
    }
}