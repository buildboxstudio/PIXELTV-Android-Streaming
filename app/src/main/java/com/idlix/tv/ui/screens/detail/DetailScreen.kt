package com.idlix.tv.ui.screens.detail

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Star
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.delay
import androidx.tv.foundation.lazy.list.TvLazyColumn
import androidx.tv.foundation.lazy.list.TvLazyRow
import androidx.tv.foundation.lazy.list.items
import androidx.tv.material3.Border
import androidx.tv.material3.Button
import androidx.tv.material3.ButtonDefaults
import androidx.tv.material3.Card
import androidx.tv.material3.CardDefaults
import androidx.tv.material3.ExperimentalTvMaterial3Api
import androidx.tv.material3.Icon
import androidx.tv.material3.Text
import coil.compose.AsyncImage
import com.idlix.tv.R
import com.idlix.tv.data.model.CastMember
import com.idlix.tv.data.model.EpisodeItem
import com.idlix.tv.ui.components.TvLoadingSpinner
import com.idlix.tv.ui.theme.NetflixBlack
import com.idlix.tv.ui.theme.NetflixDarkCard
import com.idlix.tv.ui.theme.NetflixGold
import com.idlix.tv.ui.theme.NetflixLightGray
import com.idlix.tv.ui.theme.NetflixRed
import com.idlix.tv.ui.theme.NetflixWhite

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun DetailScreen(
    contentType: String,
    slug: String,
    onNavigateToPlayer: (contentType: String, contentId: String, title: String, poster: String?, backdrop: String?, slug: String?, season: Int?, episode: Int?) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: DetailViewModel = viewModel()
) {
    LaunchedEffect(contentType, slug) {
        viewModel.loadDetail(contentType, slug)
    }

    val uiState by viewModel.uiState.collectAsState()
    val playButtonFocusRequester = remember { FocusRequester() }

    // Place default focus on "Putar Sekarang" / "Putar Episode 1" as soon as media details load
    LaunchedEffect(uiState.isLoading, uiState.movie, uiState.series) {
        if (!uiState.isLoading && (uiState.movie != null || uiState.series != null)) {
            delay(120)
            try {
                playButtonFocusRequester.requestFocus()
            } catch (e: Exception) {}
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(NetflixBlack)
    ) {
        if (uiState.isLoading) {
            TvLoadingSpinner(
                modifier = Modifier.align(Alignment.Center)
            )
        } else if (uiState.error != null) {
            Text(
                text = uiState.error ?: "",
                color = NetflixWhite,
                modifier = Modifier.align(Alignment.Center)
            )
        } else {
            val title = uiState.movie?.title ?: uiState.series?.title ?: ""
            val overview = uiState.movie?.overview ?: uiState.series?.overview ?: ""
            val backdropUrl = uiState.movie?.fullBackdropUrl ?: uiState.series?.fullBackdropUrl ?: ""
            val rating = uiState.movie?.voteAverage ?: uiState.series?.voteAverage ?: ""
            val year = (uiState.movie?.releaseDate ?: uiState.series?.firstAirDate)?.take(4) ?: ""
            val genres = (uiState.movie?.genres ?: uiState.series?.genres ?: emptyList()).joinToString(", ") { it.name }
            val castList = uiState.movie?.cast ?: uiState.series?.cast ?: emptyList()

            TvLazyColumn(
                modifier = Modifier.fillMaxSize()
            ) {
                // Header & Info
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(380.dp)
                    ) {
                        AsyncImage(
                            model = backdropUrl,
                            contentDescription = title,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )

                        // Gradient fade
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    Brush.verticalGradient(
                                        colors = listOf(
                                            Color.Transparent,
                                            NetflixBlack.copy(alpha = 0.6f),
                                            NetflixBlack
                                        ),
                                        startY = 120f
                                    )
                                )
                        )

                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 28.dp, vertical = 20.dp),
                            verticalArrangement = Arrangement.Bottom
                        ) {
                            Text(
                                text = title,
                                color = NetflixWhite,
                                fontSize = 32.sp,
                                fontWeight = FontWeight.Black
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                if (rating.isNotBlank()) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.Star,
                                            contentDescription = null,
                                            tint = NetflixGold,
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Spacer(modifier = Modifier.width(3.dp))
                                        Text(
                                            text = "$rating / 10",
                                            color = NetflixWhite,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }

                                if (year.isNotBlank()) {
                                    Text(text = year, color = NetflixLightGray, fontSize = 12.sp)
                                }

                                if (genres.isNotBlank()) {
                                    Text(text = genres, color = NetflixLightGray, fontSize = 12.sp)
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            Text(
                                text = overview,
                                color = NetflixLightGray,
                                fontSize = 13.sp,
                                maxLines = 4,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.width(600.dp)
                            )

                            Spacer(modifier = Modifier.height(14.dp))

                            // Play Button with default focus and clear white outline ring
                            Button(
                                onClick = {
                                    if (contentType == "movie") {
                                        uiState.movie?.let {
                                            onNavigateToPlayer("movie", it.id, it.title, it.fullPosterUrl, it.fullBackdropUrl, it.slug, null, null)
                                        }
                                    } else {
                                        val firstEp = uiState.episodes.firstOrNull()
                                            ?: uiState.series?.defaultSeason?.episodes?.firstOrNull()
                                        if (firstEp != null) {
                                            onNavigateToPlayer(
                                                "episode",
                                                firstEp.id,
                                                "${uiState.series?.title} - ${firstEp.name}",
                                                firstEp.fullStillUrl,
                                                uiState.series?.fullBackdropUrl,
                                                uiState.series?.slug,
                                                uiState.selectedSeason?.seasonNumber ?: 1,
                                                firstEp.episodeNumber
                                            )
                                        }
                                    }
                                },
                                colors = ButtonDefaults.colors(
                                    containerColor = NetflixRed,
                                    contentColor = NetflixWhite,
                                    focusedContainerColor = NetflixRed,
                                    focusedContentColor = NetflixWhite
                                ),
                                scale = ButtonDefaults.scale(focusedScale = 1.08f),
                                border = ButtonDefaults.border(
                                    focusedBorder = Border(
                                        border = BorderStroke(2.dp, NetflixWhite)
                                    )
                                ),
                                shape = ButtonDefaults.shape(shape = RoundedCornerShape(6.dp)),
                                modifier = Modifier.focusRequester(playButtonFocusRequester)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.PlayArrow,
                                    contentDescription = stringResource(R.string.play)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (contentType == "movie") {
                                        stringResource(R.string.play_now)
                                    } else {
                                        stringResource(R.string.play_episode_1)
                                    },
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }

                // If Series: Season selector & Episode list
                if (contentType == "tv_series" && uiState.series != null) {
                    item {
                        Column(modifier = Modifier.padding(horizontal = 28.dp, vertical = 12.dp)) {
                            Text(
                                text = stringResource(R.string.select_season_episode),
                                color = NetflixWhite,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            // Seasons Row
                            TvLazyRow(
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                items(uiState.series?.seasons ?: emptyList()) { season ->
                                    val isSelected = season.id == uiState.selectedSeason?.id
                                    Button(
                                        onClick = { viewModel.selectSeason(slug, season) },
                                        colors = ButtonDefaults.colors(
                                            containerColor = if (isSelected) NetflixRed else NetflixDarkCard,
                                            contentColor = NetflixWhite
                                        ),
                                        shape = ButtonDefaults.shape(shape = RoundedCornerShape(6.dp))
                                    ) {
                                        Text(text = season.name)
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            // Episodes List
                            TvLazyRow(
                                horizontalArrangement = Arrangement.spacedBy(14.dp)
                            ) {
                                items(uiState.episodes, key = { it.id }) { ep ->
                                    TvEpisodeCard(
                                        episode = ep,
                                        onClick = {
                                            onNavigateToPlayer(
                                                "episode",
                                                ep.id,
                                                "${uiState.series?.title} - ${ep.name}",
                                                ep.fullStillUrl,
                                                uiState.series?.fullBackdropUrl,
                                                uiState.series?.slug,
                                                uiState.selectedSeason?.seasonNumber ?: 1,
                                                ep.episodeNumber
                                            )
                                        }
                                    )
                                }
                            }
                        }
                    }
                }

                // Cast list
                if (castList.isNotEmpty()) {
                    item {
                        Column(modifier = Modifier.padding(start = 28.dp, top = 16.dp, bottom = 32.dp)) {
                            Text(
                                text = stringResource(R.string.cast),
                                color = NetflixWhite,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(bottom = 10.dp)
                            )

                            TvLazyRow(
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                items(castList.take(12), key = { it.id }) { cast ->
                                    TvCastCard(cast = cast)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun TvEpisodeCard(
    episode: EpisodeItem,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier
            .width(220.dp)
            .height(180.dp),
        shape = CardDefaults.shape(shape = RoundedCornerShape(8.dp)),
        colors = CardDefaults.colors(
            containerColor = NetflixDarkCard
        ),
        scale = CardDefaults.scale(focusedScale = 1.05f),
        border = CardDefaults.border(
            focusedBorder = Border(
                border = BorderStroke(2.dp, NetflixWhite)
            )
        )
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(115.dp)
            ) {
                AsyncImage(
                    model = episode.fullStillUrl,
                    contentDescription = episode.name,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )

                Box(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(6.dp)
                        .background(Color(0xBB000000), RoundedCornerShape(3.dp))
                        .padding(horizontal = 4.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = stringResource(R.string.episode_badge, episode.episodeNumber),
                        color = NetflixWhite,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Column(modifier = Modifier.padding(8.dp)) {
                Text(
                    text = "${episode.episodeNumber}. ${episode.name}",
                    color = NetflixWhite,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = episode.overview ?: "",
                    color = NetflixLightGray,
                    fontSize = 9.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun TvCastCard(cast: CastMember) {
    Card(
        onClick = {},
        modifier = Modifier
            .width(105.dp)
            .padding(vertical = 4.dp),
        shape = CardDefaults.shape(shape = RoundedCornerShape(8.dp)),
        colors = CardDefaults.colors(
            containerColor = Color.Transparent,
            focusedContainerColor = NetflixDarkCard
        ),
        scale = CardDefaults.scale(focusedScale = 1.08f),
        border = CardDefaults.border(
            focusedBorder = Border(
                border = BorderStroke(2.dp, NetflixWhite)
            )
        )
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp, horizontal = 4.dp)
        ) {
            AsyncImage(
                model = cast.fullProfileUrl,
                contentDescription = cast.name,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(NetflixDarkCard)
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = cast.name,
                color = NetflixWhite,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = cast.character ?: "",
                color = NetflixLightGray,
                fontSize = 9.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
