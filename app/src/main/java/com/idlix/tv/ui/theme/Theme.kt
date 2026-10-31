package com.idlix.tv.ui.theme

import androidx.compose.runtime.Composable
import androidx.tv.material3.ExperimentalTvMaterial3Api
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.darkColorScheme

@OptIn(ExperimentalTvMaterial3Api::class)
private val DarkColorScheme = darkColorScheme(
    primary = NetflixRed,
    onPrimary = NetflixWhite,
    primaryContainer = NetflixRedDark,
    onPrimaryContainer = NetflixWhite,
    secondary = NetflixDarkGray,
    onSecondary = NetflixWhite,
    background = NetflixBlack,
    onBackground = NetflixWhite,
    surface = NetflixDarkCard,
    onSurface = NetflixWhite,
    surfaceVariant = NetflixDarkGray,
    onSurfaceVariant = NetflixLightGray
)

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun IdlixTvTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        content = content
    )
}
