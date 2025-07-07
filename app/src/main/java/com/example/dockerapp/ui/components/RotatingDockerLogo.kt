package com.example.dockerapp.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.dockerapp.R
import com.example.dockerapp.ui.theme.DockerBlue


@Composable
fun RotatingDockerLogo(
    size: Dp = 48.dp,
    color: androidx.compose.ui.graphics.Color = DockerBlue,
    padding: Dp = 0.dp,
    durationMillis: Int = 1500,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "docker_logo_rotation")

    // Animation de rotation
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = durationMillis,
                easing = LinearEasing
            )
        ),
        label = "rotation"
    )

    Box(modifier = Modifier.padding(padding)) {
        Image(
            painter = painterResource(id = R.mipmap.ic_launcher_foreground),
            contentDescription = "Logo DockPilot",
            modifier = modifier
                .size(size)
                .rotate(rotation),
            colorFilter = ColorFilter.tint(color)
        )
    }
}
