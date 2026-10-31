package com.idlix.tv.ui.screens.history

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.tv.foundation.lazy.grid.TvGridCells
import androidx.tv.foundation.lazy.grid.TvLazyVerticalGrid
import androidx.tv.foundation.lazy.grid.items
import androidx.tv.material3.Border
import androidx.tv.material3.Button
import androidx.tv.material3.ButtonDefaults
import androidx.tv.material3.ExperimentalTvMaterial3Api
import androidx.tv.material3.Icon
import androidx.tv.material3.Text
import com.idlix.tv.R
import com.idlix.tv.ui.components.TvContinueWatchingCard
import com.idlix.tv.ui.theme.NetflixBlack
import com.idlix.tv.ui.theme.NetflixDarkCard
import com.idlix.tv.ui.theme.NetflixLightGray
import com.idlix.tv.ui.theme.NetflixRed
import com.idlix.tv.ui.theme.NetflixWhite

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun ContinueWatchingScreen(
    onNavigateToPlayer: (contentType: String, contentId: String, title: String, poster: String?, backdrop: String?, slug: String?, season: Int?, episode: Int?) -> Unit,
    onNavigateToHome: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ContinueWatchingViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadHistory()
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(NetflixBlack)
            .padding(horizontal = 28.dp, vertical = 20.dp)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = stringResource(R.string.continue_watching),
                        color = NetflixWhite,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = if (uiState.items.isNotEmpty()) {
                            stringResource(R.string.continue_watching_count, uiState.items.size)
                        } else {
                            stringResource(R.string.continue_watching_subtitle)
                        },
                        color = NetflixLightGray,
                        fontSize = 12.sp
                    )
                }

                if (uiState.items.isNotEmpty()) {
                    Button(
                        onClick = { viewModel.clearAll() },
                        colors = ButtonDefaults.colors(
                            containerColor = NetflixDarkCard,
                            contentColor = NetflixLightGray,
                            focusedContainerColor = NetflixRed,
                            focusedContentColor = NetflixWhite
                        ),
                        border = ButtonDefaults.border(
                            focusedBorder = Border(
                                border = BorderStroke(2.dp, NetflixWhite)
                            )
                        ),
                        shape = ButtonDefaults.shape(shape = RoundedCornerShape(6.dp))
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = stringResource(R.string.clear_all),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = stringResource(R.string.clear_all), fontSize = 12.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            if (uiState.items.isEmpty()) {
                // Empty state
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.History,
                            contentDescription = null,
                            tint = NetflixLightGray.copy(alpha = 0.5f),
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(modifier = Modifier.height(14.dp))
                        Text(
                            text = stringResource(R.string.no_history_title),
                            color = NetflixWhite,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = stringResource(R.string.no_history_desc),
                            color = NetflixLightGray,
                            fontSize = 13.sp
                        )
                        Spacer(modifier = Modifier.height(18.dp))
                        Button(
                            onClick = onNavigateToHome,
                            colors = ButtonDefaults.colors(
                                containerColor = NetflixRed,
                                contentColor = NetflixWhite
                            ),
                            scale = ButtonDefaults.scale(focusedScale = 1.08f),
                            border = ButtonDefaults.border(
                                focusedBorder = Border(BorderStroke(2.dp, NetflixWhite))
                            ),
                            shape = ButtonDefaults.shape(shape = RoundedCornerShape(6.dp))
                        ) {
                            Icon(imageVector = Icons.Default.Home, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = stringResource(R.string.explore_home))
                        }
                    }
                }
            } else {
                TvLazyVerticalGrid(
                    columns = TvGridCells.Adaptive(230.dp),
                    contentPadding = PaddingValues(bottom = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f)
                ) {
                    items(uiState.items, key = { it.contentId }) { item ->
                        Column {
                            TvContinueWatchingCard(
                                item = item,
                                onClick = {
                                    onNavigateToPlayer(
                                        item.contentType,
                                        item.contentId,
                                        item.title,
                                        item.posterUrl,
                                        item.backdropUrl,
                                        item.slug,
                                        item.seasonNumber,
                                        item.episodeNumber
                                    )
                                },
                                width = 230,
                                height = 140
                            )

                            Spacer(modifier = Modifier.height(6.dp))

                            Row(
                                modifier = Modifier.width(230.dp),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Button(
                                    onClick = {
                                        onNavigateToPlayer(
                                            item.contentType,
                                            item.contentId,
                                            item.title,
                                            item.posterUrl,
                                            item.backdropUrl,
                                            item.slug,
                                            item.seasonNumber,
                                            item.episodeNumber
                                        )
                                    },
                                    colors = ButtonDefaults.colors(
                                        containerColor = NetflixRed,
                                        contentColor = NetflixWhite
                                    ),
                                    scale = ButtonDefaults.scale(focusedScale = 1.05f),
                                    border = ButtonDefaults.border(
                                        focusedBorder = Border(BorderStroke(1.5.dp, NetflixWhite))
                                    ),
                                    shape = ButtonDefaults.shape(shape = RoundedCornerShape(4.dp)),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.PlayArrow,
                                        contentDescription = stringResource(R.string.play),
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(text = stringResource(R.string.play), fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                                }

                                Button(
                                    onClick = { viewModel.removeItem(item.contentId) },
                                    colors = ButtonDefaults.colors(
                                        containerColor = NetflixDarkCard,
                                        contentColor = NetflixLightGray,
                                        focusedContainerColor = NetflixRed.copy(alpha = 0.8f),
                                        focusedContentColor = NetflixWhite
                                    ),
                                    scale = ButtonDefaults.scale(focusedScale = 1.05f),
                                    border = ButtonDefaults.border(
                                        focusedBorder = Border(BorderStroke(1.5.dp, NetflixWhite))
                                    ),
                                    shape = ButtonDefaults.shape(shape = RoundedCornerShape(4.dp))
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = stringResource(R.string.delete),
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
