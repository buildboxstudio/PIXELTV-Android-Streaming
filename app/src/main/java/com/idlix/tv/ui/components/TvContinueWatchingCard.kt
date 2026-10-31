package com.idlix.tv.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.tv.material3.Border
import androidx.tv.material3.Card
import androidx.tv.material3.CardDefaults
import androidx.tv.material3.ExperimentalTvMaterial3Api
import androidx.tv.material3.Glow
import androidx.tv.material3.Icon
import androidx.tv.material3.Text
import coil.compose.AsyncImage
import coil.request.ImageRequest
import coil.size.Precision
import com.idlix.tv.data.model.WatchHistoryItem
import com.idlix.tv.ui.theme.NetflixBlack
import com.idlix.tv.ui.theme.NetflixDarkCard
import com.idlix.tv.ui.theme.NetflixLightGray
import com.idlix.tv.ui.theme.NetflixRed
import com.idlix.tv.ui.theme.NetflixWhite

private val ContinueCardShape = RoundedCornerShape(8.dp)
private val ContinueScrimBrush = Brush.verticalGradient(
    colors = listOf(Color.Transparent, Color(0xCC141414), NetflixBlack)
)

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun TvContinueWatchingCard(
    item: WatchHistoryItem,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    width: Int = 220,
    height: Int = 135
) {
    var isFocused by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val imageUrl = item.backdropUrl ?: item.posterUrl
    val imageRequest = remember(imageUrl) {
        ImageRequest.Builder(context)
            .data(imageUrl)
            .size(440, 270)
            .precision(Precision.INEXACT)
            .allowRgb565(true)
            .crossfade(false)
            .build()
    }

    Card(
        onClick = onClick,
        modifier = modifier
            .width(width.dp)
            .height(height.dp)
            .onFocusChanged { isFocused = it.isFocused },
        scale = CardDefaults.scale(focusedScale = 1.0f),
        glow = CardDefaults.glow(focusedGlow = Glow.None),
        shape = CardDefaults.shape(shape = ContinueCardShape),
        border = CardDefaults.border(
            focusedBorder = Border(
                border = BorderStroke(2.dp, NetflixWhite)
            )
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(NetflixDarkCard)
        ) {
            // Backdrop Image
            AsyncImage(
                model = imageRequest,
                contentDescription = item.title,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )

            // Gradient scrim for text readability
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(ContinueScrimBrush)
            )

            // Center play button overlay on focus
            if (isFocused) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .background(NetflixRed, CircleShape)
                        .align(Alignment.Center)
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = "Play",
                        tint = NetflixWhite,
                        modifier = Modifier
                            .size(22.dp)
                            .align(Alignment.Center)
                    )
                }
            }

            // Bottom title & progress bar
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomStart)
                    .padding(horizontal = 10.dp, vertical = 6.dp)
            ) {
                Text(
                    text = item.title,
                    color = NetflixWhite,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                // Subtitle: Episode code or remaining time
                val subtitle = if (!item.episodeTitle.isNullOrBlank()) {
                    item.episodeTitle
                } else if (item.remainingMinutes > 0) {
                    "${item.remainingMinutes} min"
                } else ""
                if (subtitle.isNotBlank()) {
                    Text(
                        text = subtitle,
                        color = NetflixLightGray,
                        fontSize = 10.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Progress Bar (Netflix Red)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(3.dp)
                        .background(Color.White.copy(alpha = 0.3f), RoundedCornerShape(1.5.dp))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(fraction = item.progressFraction.coerceIn(0f, 1f))
                            .fillMaxHeight()
                            .background(NetflixRed, RoundedCornerShape(1.5.dp))
                    )
                }
            }
        }
    }
}
