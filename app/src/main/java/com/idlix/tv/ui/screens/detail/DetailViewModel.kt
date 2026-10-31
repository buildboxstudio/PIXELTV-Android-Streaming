package com.idlix.tv.ui.screens.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.idlix.tv.data.model.EpisodeItem
import com.idlix.tv.data.model.MovieDetail
import com.idlix.tv.data.model.Season
import com.idlix.tv.data.model.SeriesDetail
import com.idlix.tv.data.repository.IdlixRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class DetailUiState(
    val isLoading: Boolean = true,
    val movie: MovieDetail? = null,
    val series: SeriesDetail? = null,
    val selectedSeason: Season? = null,
    val episodes: List<EpisodeItem> = emptyList(),
    val error: String? = null
)

class DetailViewModel(
    private val repository: IdlixRepository = IdlixRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(DetailUiState())
    val uiState: StateFlow<DetailUiState> = _uiState.asStateFlow()

    fun loadDetail(contentType: String, slug: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            try {
                if (contentType == "tv_series") {
                    val series = repository.getSeriesDetail(slug)
                    val firstSeason = series?.seasons?.firstOrNull()
                    val episodes = series?.defaultSeason?.episodes ?: emptyList()

                    _uiState.value = DetailUiState(
                        isLoading = false,
                        series = series,
                        selectedSeason = firstSeason,
                        episodes = episodes
                    )
                } else {
                    val movie = repository.getMovieDetail(slug)
                    _uiState.value = DetailUiState(
                        isLoading = false,
                        movie = movie
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.localizedMessage ?: "Gagal memuat detail"
                )
            }
        }
    }

    fun selectSeason(seriesSlug: String, season: Season) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(selectedSeason = season)
            try {
                // Fetch specific season episodes via episode 1 detail
                val epRes = repository.getEpisodeDetail(seriesSlug, season.seasonNumber, 1)
                val newEpisodes = epRes?.season?.episodes ?: emptyList()
                if (newEpisodes.isNotEmpty()) {
                    _uiState.value = _uiState.value.copy(episodes = newEpisodes)
                }
            } catch (e: Exception) {
                // Keep existing episodes if failure
            }
        }
    }
}
