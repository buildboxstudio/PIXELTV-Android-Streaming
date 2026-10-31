package com.idlix.tv.ui.screens.home

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.idlix.tv.data.local.WatchHistoryStore
import com.idlix.tv.data.model.MovieItem
import com.idlix.tv.data.model.SeriesItem
import com.idlix.tv.data.model.WatchHistoryItem
import com.idlix.tv.data.repository.IdlixRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class HomeUiState(
    val isLoading: Boolean = true,
    val heroMovie: MovieItem? = null,
    val continueWatching: List<WatchHistoryItem> = emptyList(),
    val trending: List<MovieItem> = emptyList(),
    val popularMovies: List<MovieItem> = emptyList(),
    val popularSeries: List<SeriesItem> = emptyList(),
    val actionMovies: List<MovieItem> = emptyList(),
    val error: String? = null
)

class HomeViewModel(
    application: Application
) : AndroidViewModel(application) {

    private val repository = IdlixRepository()
    private val historyStore = WatchHistoryStore.getInstance(application.applicationContext)

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        // Collect unfinished watch history items reactively
        viewModelScope.launch {
            historyStore.historyFlow.collect { allItems ->
                val unfinished = allItems.filter { it.isUnfinished }
                _uiState.value = _uiState.value.copy(continueWatching = unfinished)
            }
        }
        loadHomeData()
    }

    fun loadHomeData() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            try {
                coroutineScope {
                    val trendingDeferred = async { repository.getTrendingMovies() }
                    val popularDeferred = async { repository.getPopularMovies() }
                    val seriesDeferred = async { repository.getPopularSeries() }
                    val actionDeferred = async { repository.getMoviesByGenre("action") }

                    val trending = trendingDeferred.await()
                    val popular = popularDeferred.await()
                    val series = seriesDeferred.await()
                    val action = actionDeferred.await()
                    val unfinished = historyStore.getUnfinishedItems()

                    val hero = trending.firstOrNull() ?: popular.firstOrNull()

                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        heroMovie = hero,
                        continueWatching = unfinished,
                        trending = trending,
                        popularMovies = popular,
                        popularSeries = series,
                        actionMovies = action
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.localizedMessage ?: "Terjadi kesalahan saat memuat data"
                )
            }
        }
    }

    fun refreshContinueWatching() {
        val unfinished = historyStore.getUnfinishedItems()
        _uiState.value = _uiState.value.copy(continueWatching = unfinished)
    }

    fun removeFromHistory(contentId: String) {
        historyStore.removeItem(contentId)
        refreshContinueWatching()
    }

    fun setHeroMovie(movie: MovieItem) {
        _uiState.value = _uiState.value.copy(heroMovie = movie)
    }
}
