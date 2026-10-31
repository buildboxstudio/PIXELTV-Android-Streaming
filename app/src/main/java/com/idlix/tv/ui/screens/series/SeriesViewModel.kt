package com.idlix.tv.ui.screens.series

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.idlix.tv.data.model.MovieItem
import com.idlix.tv.data.model.SeriesItem
import com.idlix.tv.data.repository.IdlixRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class SeriesUiState(
    val isLoading: Boolean = true,
    val heroSeries: MovieItem? = null,
    val popularSeries: List<MovieItem> = emptyList(),
    val latestSeries: List<MovieItem> = emptyList(),
    val dramaSeries: List<MovieItem> = emptyList(),
    val actionSeries: List<MovieItem> = emptyList(),
    val comedySeries: List<MovieItem> = emptyList(),
    val error: String? = null
)

class SeriesViewModel(
    private val repository: IdlixRepository = IdlixRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(SeriesUiState())
    val uiState: StateFlow<SeriesUiState> = _uiState.asStateFlow()

    init {
        loadSeriesData()
    }

    fun loadSeriesData() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            try {
                coroutineScope {
                    val popularDef = async { repository.getPopularSeries().map { it.toMovieItem() } }
                    val latestDef = async { repository.getSeriesBySort("createdAt").map { it.toMovieItem() } }
                    val dramaDef = async { repository.getSeriesByGenre("drama").map { it.toMovieItem() } }
                    val actionDef = async { repository.getSeriesByGenre("action").map { it.toMovieItem() } }
                    val comedyDef = async { repository.getSeriesByGenre("comedy").map { it.toMovieItem() } }

                    val popular = popularDef.await()
                    val latestRaw = latestDef.await()
                    val latest = latestRaw.ifEmpty { popular }
                    val drama = dramaDef.await()
                    val action = actionDef.await()
                    val comedy = comedyDef.await()

                    _uiState.value = SeriesUiState(
                        isLoading = false,
                        heroSeries = popular.firstOrNull(),
                        popularSeries = popular,
                        latestSeries = latest,
                        dramaSeries = drama,
                        actionSeries = action,
                        comedySeries = comedy
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.localizedMessage ?: "Gagal memuat katalog serial TV"
                )
            }
        }
    }

    fun setHeroMovie(movie: MovieItem) {
        _uiState.value = _uiState.value.copy(heroSeries = movie)
    }
}
