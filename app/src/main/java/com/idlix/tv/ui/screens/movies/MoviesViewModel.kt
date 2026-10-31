package com.idlix.tv.ui.screens.movies

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.idlix.tv.data.model.MovieItem
import com.idlix.tv.data.repository.IdlixRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class MoviesUiState(
    val isLoading: Boolean = true,
    val heroMovie: MovieItem? = null,
    val popularMovies: List<MovieItem> = emptyList(),
    val latestMovies: List<MovieItem> = emptyList(),
    val actionMovies: List<MovieItem> = emptyList(),
    val comedyMovies: List<MovieItem> = emptyList(),
    val horrorMovies: List<MovieItem> = emptyList(),
    val sciFiMovies: List<MovieItem> = emptyList(),
    val error: String? = null
)

class MoviesViewModel(
    private val repository: IdlixRepository = IdlixRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(MoviesUiState())
    val uiState: StateFlow<MoviesUiState> = _uiState.asStateFlow()

    init {
        loadMoviesData()
    }

    fun loadMoviesData() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            try {
                coroutineScope {
                    val popularDef = async { repository.getPopularMovies() }
                    val latestDef = async { repository.getMoviesBySort("createdAt") }
                    val actionDef = async { repository.getMoviesByGenre("action") }
                    val comedyDef = async { repository.getMoviesByGenre("comedy") }
                    val horrorDef = async { repository.getMoviesByGenre("horror") }
                    val sciFiDef = async { repository.getMoviesByGenre("science-fiction") }

                    val popular = popularDef.await()
                    val latest = latestDef.await()
                    val action = actionDef.await()
                    val comedy = comedyDef.await()
                    val horror = horrorDef.await()
                    val sciFi = sciFiDef.await()

                    _uiState.value = MoviesUiState(
                        isLoading = false,
                        heroMovie = popular.firstOrNull(),
                        popularMovies = popular,
                        latestMovies = latest,
                        actionMovies = action,
                        comedyMovies = comedy,
                        horrorMovies = horror,
                        sciFiMovies = sciFi
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.localizedMessage ?: "Gagal memuat katalog film"
                )
            }
        }
    }

    fun setHeroMovie(movie: MovieItem) {
        _uiState.value = _uiState.value.copy(heroMovie = movie)
    }
}
