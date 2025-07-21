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
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
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
    // Utiliser remember pour éviter les recalculs des valeurs min/max
    val chartData = remember(data) {
        if (data.isEmpty()) return@remember null
        
        val minValue = data.minOfOrNull { it.value } ?: 0.0
        val maxValue = data.maxOfOrNull { it.value } ?: 1.0
        val valueRange = if (maxValue - minValue == 0.0) 1.0 else maxValue - minValue
        
        val minTime = data.minOfOrNull { it.timestamp } ?: 0L
        val maxTime = data.maxOfOrNull { it.timestamp } ?: 1L
        val timeRange = if (maxTime - minTime == 0L) 1L else maxTime - minTime
        
        ChartData(minValue, maxValue, valueRange, minTime, maxTime, timeRange)
    }
    
    Canvas(modifier = modifier) {
        if (data.isEmpty() || chartData == null) return@Canvas
        
        val width = size.width
        val height = size.height
        val padding = 40f
        
        // Dessiner les axes (simplifié)
        drawSimpleAxes(width, height, padding, Color.Gray)
        
        // Dessiner la grille (réduite)
        drawSimpleGrid(width, height, padding, Color.LightGray)
        
        // Préparer les points pour le graphique
        val points = data.map { point ->
            val x = padding + ((point.timestamp - chartData.minTime).toFloat() / chartData.timeRange.toFloat()) * (width - 2 * padding)
            val y = height - padding - ((point.value - chartData.minValue).toFloat() / chartData.valueRange.toFloat()) * (height - 2 * padding)
            Offset(x, y)
        }
        
        // Dessiner la ligne (optimisé)
        if (points.size > 1) {
            val path = Path()
            path.moveTo(points[0].x, points[0].y)
            
            for (i in 1 until points.size) {
                path.lineTo(points[i].x, points[i].y)
            }
            
            drawPath(
                path = path,
                color = color,
                style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round)
            )
        }
        
        // Dessiner moins de points pour améliorer les performances
        if (points.size <= 10) {
            points.forEach { point ->
                drawCircle(
                    color = color,
                    radius = 3.dp.toPx(),
                    center = point
                )
            }
        }
    }
}

data class ChartData(
    val minValue: Double,
    val maxValue: Double,
    val valueRange: Double,
    val minTime: Long,
    val maxTime: Long,
    val timeRange: Long
)

private fun DrawScope.drawSimpleAxes(
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
        strokeWidth = 1.dp.toPx()
    )
    
    // Axe Y (vertical)
    drawLine(
        color = color,
        start = Offset(padding, padding),
        end = Offset(padding, height - padding),
        strokeWidth = 1.dp.toPx()
    )
}

private fun DrawScope.drawSimpleGrid(
    width: Float,
    height: Float,
    padding: Float,
    color: Color
) {
    val gridLines = 3 // Réduit de 5 à 3 pour moins de calculs
    val stepX = (width - 2 * padding) / gridLines
    val stepY = (height - 2 * padding) / gridLines
    
    // Lignes verticales (réduites)
    for (i in 1 until gridLines) {
        val x = padding + i * stepX
        drawLine(
            color = color,
            start = Offset(x, padding),
            end = Offset(x, height - padding),
            strokeWidth = 0.5.dp.toPx()
        )
    }
    
    // Lignes horizontales (réduites)
    for (i in 1 until gridLines) {
        val y = padding + i * stepY
        drawLine(
            color = color,
            start = Offset(padding, y),
            end = Offset(width - padding, y),
            strokeWidth = 0.5.dp.toPx()
        )
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