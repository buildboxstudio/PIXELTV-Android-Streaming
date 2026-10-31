package com.idlix.tv.ui.screens.player

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.idlix.tv.data.local.WatchHistoryStore
import com.idlix.tv.data.model.EpisodeItem
import com.idlix.tv.data.model.ResolvedPlayback
import com.idlix.tv.data.model.Season
import com.idlix.tv.data.model.SubdlSubtitle
import com.idlix.tv.data.model.SubtitleTrack
import com.idlix.tv.data.repository.IdlixRepository
import com.idlix.tv.data.repository.SubdlRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class NextEpisodeInfo(
    val episode: EpisodeItem,
    val seasonNumber: Int,
    val seriesTitle: String,
    val seriesSlug: String,
    val posterUrl: String?,
    val backdropUrl: String?
)

data class PlayerUiState(
    val isLoading: Boolean = true,
    val countdownSeconds: Int = 0,
    val resolvedStream: ResolvedPlayback? = null,
    val selectedSubtitle: SubtitleTrack? = null,
    val isSubtitleEnabled: Boolean = true,
    val initialPositionMs: Long = 0L,
    val error: String? = null,
    val isSubtitlePickerVisible: Boolean = false,
    val subdlSubtitles: List<SubdlSubtitle> = emptyList(),
    val isSearchingSubdl: Boolean = false,
    val nextEpisode: NextEpisodeInfo? = null,
    val isNextEpisodeDismissed: Boolean = false
)

class PlayerViewModel(
    application: Application
) : AndroidViewModel(application) {

    private val repository = IdlixRepository()
    private val historyStore = WatchHistoryStore.getInstance(application.applicationContext)

    private val _uiState = MutableStateFlow(PlayerUiState())
    val uiState: StateFlow<PlayerUiState> = _uiState.asStateFlow()

    private var currentContentId: String = ""
    private var currentContentType: String = ""
    private var currentContentTitle: String = ""
    private var currentPosterUrl: String? = null
    private var currentBackdropUrl: String? = null
    private var currentSlug: String? = null
    private var currentSeasonNumber: Int? = null
    private var currentEpisodeNumber: Int? = null

    fun loadStream(
        contentType: String,
        contentId: String,
        title: String,
        posterUrl: String? = null,
        backdropUrl: String? = null,
        slug: String? = null,
        seasonNumber: Int? = null,
        episodeNumber: Int? = null
    ) {
        currentContentId = contentId
        currentContentType = contentType
        currentContentTitle = title
        currentPosterUrl = posterUrl
        currentBackdropUrl = backdropUrl
        currentSlug = slug
        currentSeasonNumber = seasonNumber
        currentEpisodeNumber = episodeNumber

        val savedPos = historyStore.getSavedPosition(contentId)

        // Immediately record in history so it tracks the title and images
        historyStore.recordStartPlayback(
            contentId = contentId,
            contentType = contentType,
            title = title,
            posterUrl = posterUrl,
            backdropUrl = backdropUrl,
            slug = slug,
            seasonNumber = seasonNumber,
            episodeNumber = episodeNumber
        )

        viewModelScope.launch {
            _uiState.value = PlayerUiState(
                isLoading = true,
                initialPositionMs = savedPos,
                nextEpisode = null,
                isNextEpisodeDismissed = false
            )

            // Asynchronously resolve the next episode in background for TV Series
            if (contentType == "episode" && !slug.isNullOrBlank()) {
                resolveNextEpisode(
                    seriesSlug = slug,
                    currentEpisodeId = contentId,
                    seasonNumber = seasonNumber,
                    episodeNumber = episodeNumber,
                    fallbackTitle = title,
                    posterUrl = posterUrl,
                    backdropUrl = backdropUrl
                )
            }

            val result = repository.resolveStream(
                contentType = contentType,
                contentId = contentId,
                contentTitle = title,
                onCountdown = { sec ->
                    _uiState.value = _uiState.value.copy(countdownSeconds = sec)
                }
            )

            result.onSuccess { resolved ->
                val defaultSub = resolved.subtitles.firstOrNull { it.lang == "id" }
                    ?: resolved.subtitles.firstOrNull()

                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    resolvedStream = resolved,
                    selectedSubtitle = defaultSub,
                    isSubtitleEnabled = defaultSub != null
                )
            }.onFailure { err ->
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = err.localizedMessage ?: "Failed to play video"
                )
            }
        }
    }

    private fun resolveNextEpisode(
        seriesSlug: String,
        currentEpisodeId: String,
        seasonNumber: Int?,
        episodeNumber: Int?,
        fallbackTitle: String,
        posterUrl: String?,
        backdropUrl: String?
    ) {
        viewModelScope.launch {
            try {
                val series = repository.getSeriesDetail(seriesSlug) ?: return@launch
                val seriesTitle = series.title.ifBlank { fallbackTitle.substringBefore(" -") }
                val allSeasons = series.seasons.ifEmpty {
                    if (series.defaultSeason != null) {
                        listOf(Season(seasonNumber = series.defaultSeason.seasonNumber, name = series.defaultSeason.name))
                    } else emptyList()
                }

                var currentSeasonNum = seasonNumber
                val currentEpNum = episodeNumber
                var seasonEpisodes: List<EpisodeItem> = emptyList()

                // If season number was passed or we can use defaultSeason
                if (currentSeasonNum != null) {
                    if (series.defaultSeason?.seasonNumber == currentSeasonNum && !series.defaultSeason.episodes.isNullOrEmpty()) {
                        seasonEpisodes = series.defaultSeason.episodes
                    } else {
                        val epDetail = repository.getEpisodeDetail(seriesSlug, currentSeasonNum, currentEpNum ?: 1)
                        seasonEpisodes = epDetail?.season?.episodes ?: emptyList()
                    }
                } else {
                    // Try defaultSeason first
                    if (series.defaultSeason?.episodes?.any { it.id == currentEpisodeId } == true) {
                        currentSeasonNum = series.defaultSeason.seasonNumber
                        seasonEpisodes = series.defaultSeason.episodes
                    } else {
                        // Scan seasons to find which season contains this episode ID
                        for (s in allSeasons) {
                            val epDetail = repository.getEpisodeDetail(seriesSlug, s.seasonNumber, 1)
                            val eps = epDetail?.season?.episodes ?: emptyList()
                            if (eps.any { it.id == currentEpisodeId }) {
                                currentSeasonNum = s.seasonNumber
                                seasonEpisodes = eps
                                break
                            }
                        }
                    }
                }

                if (seasonEpisodes.isEmpty()) return@launch

                val currentIndex = seasonEpisodes.indexOfFirst {
                    it.id == currentEpisodeId || (currentEpNum != null && it.episodeNumber == currentEpNum)
                }

                if (currentIndex != -1 && currentIndex + 1 < seasonEpisodes.size) {
                    // Next episode is in the same season!
                    val nextEp = seasonEpisodes[currentIndex + 1]
                    _uiState.value = _uiState.value.copy(
                        nextEpisode = NextEpisodeInfo(
                            episode = nextEp,
                            seasonNumber = currentSeasonNum ?: 1,
                            seriesTitle = seriesTitle,
                            seriesSlug = seriesSlug,
                            posterUrl = nextEp.fullStillUrl.ifBlank { posterUrl },
                            backdropUrl = backdropUrl ?: series.fullBackdropUrl
                        )
                    )
                } else if (currentSeasonNum != null) {
                    // Check if there is a next season
                    val currentSeasonIndex = allSeasons.indexOfFirst { it.seasonNumber == currentSeasonNum }
                    val nextSeason = if (currentSeasonIndex != -1 && currentSeasonIndex + 1 < allSeasons.size) {
                        allSeasons[currentSeasonIndex + 1]
                    } else {
                        allSeasons.firstOrNull { it.seasonNumber == currentSeasonNum + 1 }
                    }

                    if (nextSeason != null) {
                        // Load episode 1 of next season
                        val nextSeasonDetail = repository.getEpisodeDetail(seriesSlug, nextSeason.seasonNumber, 1)
                        val nextEp = nextSeasonDetail?.episode
                            ?: nextSeasonDetail?.season?.episodes?.firstOrNull()
                        if (nextEp != null) {
                            _uiState.value = _uiState.value.copy(
                                nextEpisode = NextEpisodeInfo(
                                    episode = nextEp,
                                    seasonNumber = nextSeason.seasonNumber,
                                    seriesTitle = seriesTitle,
                                    seriesSlug = seriesSlug,
                                    posterUrl = nextEp.fullStillUrl.ifBlank { posterUrl },
                                    backdropUrl = backdropUrl ?: series.fullBackdropUrl
                                )
                            )
                        } else {
                            _uiState.value = _uiState.value.copy(nextEpisode = null)
                        }
                    } else {
                        // Final episode of final season: stop completely
                        _uiState.value = _uiState.value.copy(nextEpisode = null)
                    }
                }
            } catch (e: Exception) {
                android.util.Log.w("PlayerViewModel", "Failed to resolve next episode: ${e.message}")
            }
        }
    }

    fun dismissNextEpisode() {
        _uiState.value = _uiState.value.copy(isNextEpisodeDismissed = true)
    }

    fun playNextEpisode(
        onReadyToPlay: ((NextEpisodeInfo) -> Unit)? = null
    ) {
        val next = _uiState.value.nextEpisode ?: return
        val nextTitle = "${next.seriesTitle} - ${next.episode.name}"
        loadStream(
            contentType = "episode",
            contentId = next.episode.id,
            title = nextTitle,
            posterUrl = next.posterUrl,
            backdropUrl = next.backdropUrl,
            slug = next.seriesSlug,
            seasonNumber = next.seasonNumber,
            episodeNumber = next.episode.episodeNumber
        )
        onReadyToPlay?.invoke(next)
    }

    fun openSubtitlePicker() {
        _uiState.value = _uiState.value.copy(isSubtitlePickerVisible = true)
        if (_uiState.value.subdlSubtitles.isEmpty()) {
            searchSubdlSubtitles()
        }
    }

    fun closeSubtitlePicker() {
        _uiState.value = _uiState.value.copy(isSubtitlePickerVisible = false)
    }

    fun searchSubdlSubtitles() {
        val title = currentContentTitle
        if (title.isBlank()) return

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSearchingSubdl = true)
            val results = SubdlRepository.searchAllSubtitles(
                title = title,
                type = if (currentContentType == "episode") "tv" else "movie"
            )
            _uiState.value = _uiState.value.copy(
                isSearchingSubdl = false,
                subdlSubtitles = results
            )
        }
    }

    fun selectSubtitleTrack(track: SubtitleTrack?) {
        _uiState.value = _uiState.value.copy(
            selectedSubtitle = track,
            isSubtitleEnabled = track != null,
            isSubtitlePickerVisible = false
        )
    }

    fun selectSubdlSubtitle(
        sub: SubdlSubtitle,
        onSubtitleReady: (SubtitleTrack) -> Unit
    ) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSearchingSubdl = true)
            val downloaded = SubdlRepository.downloadSubtitle(
                sub = sub,
                context = getApplication<Application>().applicationContext
            )
            _uiState.value = _uiState.value.copy(isSearchingSubdl = false)

            if (downloaded != null) {
                val currentStream = _uiState.value.resolvedStream
                if (currentStream != null) {
                    val updatedSubs = currentStream.subtitles.toMutableList()
                    updatedSubs.removeAll { it.path == downloaded.path }
                    updatedSubs.add(0, downloaded)
                    _uiState.value = _uiState.value.copy(
                        resolvedStream = currentStream.copy(subtitles = updatedSubs),
                        selectedSubtitle = downloaded,
                        isSubtitleEnabled = true,
                        isSubtitlePickerVisible = false
                    )
                }
                onSubtitleReady(downloaded)
            }
        }
    }

    fun toggleSubtitle() {
        val current = _uiState.value.isSubtitleEnabled
        _uiState.value = _uiState.value.copy(isSubtitleEnabled = !current)
    }

    fun saveProgress(currentPosMs: Long, durationMs: Long) {
        if (currentContentId.isNotBlank() && durationMs > 0) {
            historyStore.updateProgress(currentContentId, currentPosMs, durationMs)
        }
    }
}
