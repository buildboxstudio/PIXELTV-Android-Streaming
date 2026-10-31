package com.idlix.tv.data.repository

import com.idlix.tv.IdlixTvApp
import com.idlix.tv.data.api.ApiClient
import com.idlix.tv.data.api.ApiV1Service
import com.idlix.tv.data.api.IdlixApiService
import com.idlix.tv.data.api.MajorplayApiService
import com.idlix.tv.data.model.ClaimRequest
import com.idlix.tv.data.model.EpisodeResponse
import com.idlix.tv.data.model.Genre
import com.idlix.tv.data.model.MovieDetail
import com.idlix.tv.data.model.MovieItem
import com.idlix.tv.data.model.PlayInfoResponse
import com.idlix.tv.data.model.RedeemRequest
import com.idlix.tv.data.model.ResolvedPlayback
import com.idlix.tv.data.model.SearchResultItem
import com.idlix.tv.data.model.SeriesDetail
import com.idlix.tv.data.model.SeriesItem
import com.idlix.tv.data.model.V1Item
import com.idlix.tv.data.model.toMovieDetail
import com.idlix.tv.data.model.toMovieItem
import com.idlix.tv.data.model.toResolvedPlayback
import com.idlix.tv.data.model.toSearchResult
import com.idlix.tv.data.model.toSeriesDetail
import com.idlix.tv.data.model.toSeriesItem
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import java.util.concurrent.ConcurrentHashMap

class IdlixRepository(
    private val idlixApi: IdlixApiService = ApiClient.idlixApi,
    private val majorplayApi: MajorplayApiService = ApiClient.majorplayApi,
    private val v1Api: ApiV1Service = ApiClient.v1Api
) {

    private fun rememberSource(item: V1Item) {
        if (item.source.isBlank()) return
        if (item.id.isNotBlank()) sourceByKey[item.id] = item.source
        if (item.playId.isNotBlank()) sourceByKey[item.playId] = item.source
    }

    // Backend tanpa /api/v1 membalas 404. Setelah itu jangan tanya lagi supaya
    // tiap layar tidak menunggu satu request 404 sebelum jatuh ke route 4.x.
    private fun markV1IfMissing(e: Exception) {
        if (e is HttpException && e.code() == 404) v1Disabled = true
    }

    private suspend fun v1Catalog(
        type: String?,
        page: Int = 1,
        query: String? = null
    ): List<V1Item>? {
        if (v1Disabled) return null
        return try {
            val items = v1Api.catalog(type = type, page = page, query = query).items
            items.forEach(::rememberSource)
            items
        } catch (e: Exception) {
            markV1IfMissing(e)
            android.util.Log.d("IdlixRepository", "v1 catalog unavailable: ${e.message}")
            null
        }
    }

    suspend fun getTrendingMovies(): List<MovieItem> = withContext(Dispatchers.IO) {
        v1Catalog("movie")?.map { it.toMovieItem() }?.takeIf { it.isNotEmpty() }
            ?: try {
                idlixApi.getMovies(page = 1, limit = 20, sort = "popularity").data
            } catch (e: Exception) {
                emptyList()
            }
    }

    suspend fun getPopularMovies(): List<MovieItem> = withContext(Dispatchers.IO) {
        v1Catalog("movie")?.map { it.toMovieItem() }?.takeIf { it.isNotEmpty() }
            ?: try {
                idlixApi.getMovies(page = 1, limit = 20).data
            } catch (e: Exception) {
                emptyList()
            }
    }

    suspend fun getMoviesBySort(sort: String): List<MovieItem> = withContext(Dispatchers.IO) {
        try {
            idlixApi.getMovies(page = 1, limit = 20, sort = sort).data
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun getMoviesByGenre(genreSlug: String): List<MovieItem> = withContext(Dispatchers.IO) {
        try {
            idlixApi.getMovies(page = 1, limit = 20, genre = genreSlug).data
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun getPopularSeries(): List<SeriesItem> = withContext(Dispatchers.IO) {
        v1Catalog("series")?.map { it.toSeriesItem() }?.takeIf { it.isNotEmpty() }
            ?: try {
                idlixApi.getSeries(page = 1, limit = 20, sort = "popularity").data
            } catch (e: Exception) {
                emptyList()
            }
    }

    suspend fun getSeriesBySort(sort: String): List<SeriesItem> = withContext(Dispatchers.IO) {
        try {
            idlixApi.getSeries(page = 1, limit = 20, sort = sort).data
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun getSeriesByGenre(genreSlug: String): List<SeriesItem> = withContext(Dispatchers.IO) {
        try {
            idlixApi.getSeries(page = 1, limit = 20, genre = genreSlug).data
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun getMovieDetail(slug: String): MovieDetail? = withContext(Dispatchers.IO) {
        val source = sourceByKey[slug]
        if (source != null) {
            try {
                val detail = v1Api.detail(source, "movie", slug)
                if (detail.playId.isNotBlank()) sourceByKey[detail.playId] = source
                return@withContext detail.toMovieDetail()
            } catch (e: Exception) {
                android.util.Log.d("IdlixRepository", "v1 movie detail unavailable: ${e.message}")
            }
        }
        try {
            idlixApi.getMovieDetail(slug)
        } catch (e: Exception) {
            null
        }
    }

    suspend fun getSeriesDetail(slug: String): SeriesDetail? = withContext(Dispatchers.IO) {
        val source = sourceByKey[slug]
        if (source != null) {
            try {
                val detail = v1Api.detail(source, "series", slug)
                sourceByKey[detail.playId] = source
                detail.episodes.forEach { episode ->
                    if (episode.playId.isNotBlank()) sourceByKey[episode.playId] = source
                    if (episode.id.isNotBlank()) sourceByKey[episode.id] = source
                }
                return@withContext detail.toSeriesDetail()
            } catch (e: Exception) {
                android.util.Log.d("IdlixRepository", "v1 series detail unavailable: ${e.message}")
            }
        }
        try {
            idlixApi.getSeriesDetail(slug)
        } catch (e: Exception) {
            null
        }
    }

    suspend fun getEpisodeDetail(slug: String, season: Int, episode: Int): EpisodeResponse? = withContext(Dispatchers.IO) {
        try {
            idlixApi.getEpisodeDetail(slug, season, episode)
        } catch (e: Exception) {
            null
        }
    }

    suspend fun search(query: String): List<SearchResultItem> = withContext(Dispatchers.IO) {
        val trimmed = query.trim()
        if (trimmed.isBlank()) return@withContext emptyList()

        val v1Results = v1Catalog(null, page = 1, query = trimmed)?.map { it.toSearchResult() }
        if (!v1Results.isNullOrEmpty()) return@withContext v1Results

        try {
            val res = idlixApi.search(trimmed)
            res.results
        } catch (e: Exception) {
            android.util.Log.w("IdlixRepository", "Backend search error: ${e.message}")
            emptyList()
        }
    }

    private data class CachedPlayback(
        val playback: ResolvedPlayback,
        val timestamp: Long = System.currentTimeMillis()
    )

    companion object {
        private val playbackCache = ConcurrentHashMap<String, CachedPlayback>()
        private val activePrewarms = ConcurrentHashMap<String, Deferred<Result<ResolvedPlayback>>>()
        private val sourceByKey = ConcurrentHashMap<String, String>()
        @Volatile
        private var v1Disabled = false
        private const val CACHE_TTL_MS = 60 * 60 * 1000L // 60 minutes
        private val repoScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    }

    /**
     * Pre-warms the stream resolution in the background while the user is reading
     * details or looking at synopsis. When they click "Play", the stream is already unlocked!
     */
    fun prewarmStream(contentType: String, contentId: String, contentTitle: String) {
        val cacheKey = "$contentType:$contentId"
        val cached = playbackCache[cacheKey]
        if (cached != null && (System.currentTimeMillis() - cached.timestamp) < CACHE_TTL_MS) {
            return
        }

        if (activePrewarms.containsKey(cacheKey)) {
            return
        }

        val deferred = repoScope.async {
            try {
                val res = resolveStreamInternal(contentType, contentId, contentTitle, onCountdown = null)
                res.onSuccess { resolved ->
                    playbackCache[cacheKey] = CachedPlayback(resolved)
                }
                res
            } catch (e: Exception) {
                Result.failure(e)
            } finally {
                activePrewarms.remove(cacheKey)
            }
        }
        activePrewarms[cacheKey] = deferred
    }

    suspend fun resolveStream(
        contentType: String,
        contentId: String,
        contentTitle: String,
        onCountdown: ((Int) -> Unit)? = null
    ): Result<ResolvedPlayback> = withContext(Dispatchers.IO) {
        val cacheKey = "$contentType:$contentId"

        // 1. Instant hit: Already cached in memory
        val cached = playbackCache[cacheKey]
        if (cached != null && (System.currentTimeMillis() - cached.timestamp) < CACHE_TTL_MS) {
            onCountdown?.invoke(0)
            return@withContext Result.success(cached.playback)
        }

        // 2. Pre-warming hit: Already resolving in background
        val ongoing = activePrewarms[cacheKey]
        if (ongoing != null) {
            try {
                val res = ongoing.await()
                if (res.isSuccess) {
                    onCountdown?.invoke(0)
                    return@withContext res
                }
            } catch (e: Exception) {}
        }

        // 3. Fallback to standard resolution
        val res = resolveStreamInternal(contentType, contentId, contentTitle, onCountdown)
        res.onSuccess { resolved ->
            playbackCache[cacheKey] = CachedPlayback(resolved)
        }
        res
    }

    private suspend fun resolveStreamInternal(
        contentType: String,
        contentId: String,
        contentTitle: String,
        onCountdown: ((Int) -> Unit)? = null
    ): Result<ResolvedPlayback> = withContext(Dispatchers.IO) {
        try {
            var targetType = contentType
            var targetId = contentId

            // If a TV series ID is passed instead of an episode ID, retrieve episode 1 of season 1
            if (contentType == "tv_series") {
                val series = getSeriesDetail(contentId)
                val firstEp = series?.defaultSeason?.episodes?.firstOrNull()
                    ?: series?.seasons?.firstOrNull()?.let { s ->
                        getEpisodeDetail(series.slug, s.seasonNumber, 1)?.episode
                    }

                if (firstEp != null) {
                    targetType = "episode"
                    targetId = firstEp.id
                } else {
                    return@withContext Result.failure(Exception("Tidak ada episode yang tersedia untuk serial ini."))
                }
            }

            // Provider layer (v1): use the source recorded from catalog or detail
            val v1Source = sourceByKey[targetId]
            if (v1Source != null) {
                try {
                    val playType = if (targetType == "episode") "episode" else targetType
                    val play = v1Api.play(v1Source, playType, targetId)
                    if (play.hlsUrl.isNotBlank()) {
                        onCountdown?.invoke(0)
                        return@withContext Result.success(play.toResolvedPlayback())
                    }
                } catch (e: Exception) {
                    android.util.Log.d("IdlixRepository", "v1 play fallback: ${e.message}")
                }
            }

            // High-performance path: If no interactive countdown listener is needed, use backend one-shot resolver
            if (onCountdown == null) {
                try {
                    val resolved = idlixApi.resolvePlayback(
                        contentType = targetType,
                        contentId = targetId,
                        contentTitle = contentTitle
                    )
                    if (resolved.hlsMasterUrl.isNotBlank()) {
                        return@withContext Result.success(resolved)
                    }
                } catch (e: Exception) {
                    android.util.Log.d("IdlixRepository", "One-shot resolver fallback: ${e.message}")
                }
            }

            // Step 1: Get play-info (gateToken + unlockAt) from backend API
            val playInfo: PlayInfoResponse = if (targetType == "episode") {
                idlixApi.getEpisodePlayInfo(targetId)
            } else {
                idlixApi.getMoviePlayInfo(targetId)
            }

            if (playInfo.gateToken.isBlank()) {
                return@withContext Result.failure(Exception("Gagal memperoleh token pemutaran"))
            }

            // Step 2: Server-synchronized countdown
            val diffMs = if (playInfo.serverNow > 0 && playInfo.unlockAt > playInfo.serverNow) {
                playInfo.unlockAt - playInfo.serverNow
            } else {
                15000L
            }

            // Extract owner device ID from gateToken to construct valid session cookie
            val didMatch = try {
                val rawP0 = playInfo.gateToken.split(".")[0]
                val decodedBytes = try {
                    java.util.Base64.getUrlDecoder().decode(rawP0)
                } catch (e: Throwable) {
                    android.util.Base64.decode(rawP0, android.util.Base64.URL_SAFE)
                }
                val p0 = String(decodedBytes, Charsets.UTF_8)
                """"owner":"d:([^"]+)"""".toRegex().find(p0)?.groupValues?.get(1)
            } catch (e: Exception) {
                null
            }
            val didCookie = if (!didMatch.isNullOrBlank()) "did=$didMatch" else null

            var secondsLeft = (diffMs / 1000).toInt()
            while (secondsLeft > 1) {
                onCountdown?.invoke(secondsLeft)
                delay(1000)
                secondsLeft--
            }
            if (secondsLeft > 0) {
                onCountdown?.invoke(1)
            }

            // Step 3: Claim session with retry polling via Backend API
            var validClaimRes: com.idlix.tv.data.model.ClaimResponse? = null

            for (attempt in 1..15) {
                try {
                    val res = idlixApi.claimSession(
                        request = ClaimRequest(gateToken = playInfo.gateToken),
                        cookie = didCookie
                    )
                    if (res.kind == "pentos" && res.claim.isNotBlank()) {
                        validClaimRes = res
                        break
                    }
                } catch (_: Exception) {}
                delay(600)
            }

            onCountdown?.invoke(0)

            val claimRes = validClaimRes
            if (claimRes == null || claimRes.claim.isBlank()) {
                return@withContext Result.failure(Exception("Gagal mengklaim sesi pemutaran. Silakan coba lagi."))
            }

            // Step 4: Redeem HLS URL from majorplay CDN
            val redeemUrl = claimRes.redeemUrl.ifBlank { com.idlix.tv.data.api.IdlixSource.DEFAULT_REDEEM_URL }
            val redeemRes = try {
                majorplayApi.redeemPlay(
                    url = redeemUrl,
                    request = RedeemRequest(claim = claimRes.claim, mode = "browser")
                )
            } catch (e: Exception) {
                null
            }

            if (redeemRes == null || redeemRes.url.isBlank()) {
                return@withContext Result.failure(Exception("Gagal mengambil master playlist HLS."))
            }

            val finalSubtitles = redeemRes.subtitles.toMutableList()
            val hasIndoSub = finalSubtitles.any { it.lang == "id" }

            // SubDL Third-party Subtitle Fallback:
            // If the content has no Indonesian subtitle (or no subtitles at all), fetch from subdl.com
            if (!hasIndoSub) {
                try {
                    val subdlSub = SubdlRepository.fetchIndonesianSubtitle(
                        title = contentTitle,
                        type = if (targetType == "episode") "tv" else "movie",
                        context = IdlixTvApp.appContext
                    )
                    if (subdlSub != null) {
                        finalSubtitles.add(subdlSub)
                    }
                } catch (e: Exception) {
                    android.util.Log.w("IdlixRepository", "SubDL fallback failed: ${e.message}")
                }
            }

            Result.success(
                ResolvedPlayback(
                    title = claimRes.title.ifBlank { contentTitle },
                    hlsMasterUrl = redeemRes.url,
                    subtitles = finalSubtitles,
                    durationSec = claimRes.durationSec,
                    videoId = claimRes.videoId
                )
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
