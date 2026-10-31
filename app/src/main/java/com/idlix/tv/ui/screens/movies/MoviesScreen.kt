package com.idlix.tv.ui.screens.movies

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.tv.material3.Text
import com.idlix.tv.R
import com.idlix.tv.ui.components.TvHeroBillboard
import com.idlix.tv.ui.components.TvLoadingSpinner
import com.idlix.tv.ui.components.TvSwimlaneRow
import com.idlix.tv.ui.theme.NetflixBlack
import com.idlix.tv.ui.theme.NetflixWhite

@Composable
fun MoviesScreen(
    onNavigateToDetail: (contentType: String, slug: String) -> Unit,
    onNavigateToPlayer: (contentType: String, contentId: String, title: String, poster: String?, backdrop: String?, slug: String?) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: MoviesViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

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
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(24.dp)
            )
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize()
            ) {
                // Hero Movie Billboard
                item {
                    TvHeroBillboard(
                        movie = uiState.heroMovie,
                        onPlayClick = {
                            uiState.heroMovie?.let {
                                onNavigateToPlayer(it.contentType, it.id, it.title, it.fullPosterUrl, it.fullBackdropUrl, it.slug)
                            }
                        },
                        onDetailsClick = {
                            uiState.heroMovie?.let {
                                onNavigateToDetail(it.contentType, it.slug)
                            }
                        }
                    )
                }

                // Swimlane: Film Terpopuler
                item {
                    TvSwimlaneRow(
                        title = stringResource(R.string.popular_movies_indonesia),
                        items = uiState.popularMovies,
                        onItemClick = { onNavigateToDetail(it.contentType, it.slug) }
                    )
                }

                // Swimlane: Film Terbaru
                item {
                    TvSwimlaneRow(
                        title = stringResource(R.string.latest_movies),
                        items = uiState.latestMovies,
                        onItemClick = { onNavigateToDetail(it.contentType, it.slug) }
                    )
                }

                // Swimlane: Film Aksi
                item {
                    TvSwimlaneRow(
                        title = stringResource(R.string.movies_action),
                        items = uiState.actionMovies,
                        onItemClick = { onNavigateToDetail(it.contentType, it.slug) }
                    )
                }

                // Swimlane: Film Komedi
                item {
                    TvSwimlaneRow(
                        title = stringResource(R.string.movies_comedy),
                        items = uiState.comedyMovies,
                        onItemClick = { onNavigateToDetail(it.contentType, it.slug) }
                    )
                }

                // Swimlane: Film Horor
                item {
                    TvSwimlaneRow(
                        title = stringResource(R.string.movies_horror),
                        items = uiState.horrorMovies,
                        onItemClick = { onNavigateToDetail(it.contentType, it.slug) }
                    )
                }

                // Swimlane: Film Fiksi Ilmiah (Sci-Fi)
                item {
                    TvSwimlaneRow(
                        title = stringResource(R.string.movies_scifi),
                        items = uiState.sciFiMovies,
                        onItemClick = { onNavigateToDetail(it.contentType, it.slug) }
                    )
                }
            }
        }
    }
}
