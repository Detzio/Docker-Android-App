package com.example.dockerapp.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.dockerapp.R
import com.example.dockerapp.ui.theme.DockerBlue

@Composable
fun DockerLogo(
    size: Dp = 32.dp,
    color: androidx.compose.ui.graphics.Color = DockerBlue,
    modifier: Modifier = Modifier
) {
    Image(
        painter = painterResource(id = R.mipmap.ic_launcher_foreground),
        contentDescription = "Logo",
        modifier = modifier.size(size),
        colorFilter = ColorFilter.tint(color)
    )
}
