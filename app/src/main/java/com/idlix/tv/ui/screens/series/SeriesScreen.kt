package com.idlix.tv.ui.screens.series

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
fun SeriesScreen(
    onNavigateToDetail: (contentType: String, slug: String) -> Unit,
    onNavigateToPlayer: (contentType: String, contentId: String, title: String, poster: String?, backdrop: String?, slug: String?) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: SeriesViewModel = viewModel()
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
                // Hero TV Series Billboard
                item {
                    TvHeroBillboard(
                        movie = uiState.heroSeries,
                        onPlayClick = {
                            uiState.heroSeries?.let {
                                onNavigateToDetail(it.contentType, it.slug)
                            }
                        },
                        onDetailsClick = {
                            uiState.heroSeries?.let {
                                onNavigateToDetail(it.contentType, it.slug)
                            }
                        }
                    )
                }

                // Swimlane: Serial TV Terpopuler
                item {
                    TvSwimlaneRow(
                        title = stringResource(R.string.popular_series),
                        items = uiState.popularSeries,
                        onItemClick = { onNavigateToDetail(it.contentType, it.slug) }
                    )
                }

                // Swimlane: Serial TV Terbaru
                item {
                    TvSwimlaneRow(
                        title = stringResource(R.string.on_the_air_series),
                        items = uiState.latestSeries,
                        onItemClick = { onNavigateToDetail(it.contentType, it.slug) }
                    )
                }

                // Swimlane: Serial Drama
                item {
                    TvSwimlaneRow(
                        title = stringResource(R.string.series_drama),
                        items = uiState.dramaSeries,
                        onItemClick = { onNavigateToDetail(it.contentType, it.slug) }
                    )
                }

                // Swimlane: Serial Aksi & Petualangan
                item {
                    TvSwimlaneRow(
                        title = stringResource(R.string.series_action),
                        items = uiState.actionSeries,
                        onItemClick = { onNavigateToDetail(it.contentType, it.slug) }
                    )
                }

                // Swimlane: Serial Komedi
                item {
                    TvSwimlaneRow(
                        title = stringResource(R.string.series_comedy),
                        items = uiState.comedySeries,
                        onItemClick = { onNavigateToDetail(it.contentType, it.slug) }
                    )
                }
            }
        }
    }
}
