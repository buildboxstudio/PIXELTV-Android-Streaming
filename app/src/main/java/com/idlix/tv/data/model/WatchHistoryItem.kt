package com.idlix.tv.data.model

import kotlinx.serialization.Serializable

@Serializable
data class WatchHistoryItem(
    val contentId: String,
    val contentType: String = "movie", // "movie", "episode", or "tv_series"
    val title: String,
    val posterUrl: String? = null,
    val backdropUrl: String? = null,
    val slug: String? = null,
    val episodeTitle: String? = null,
    val seasonNumber: Int? = null,
    val episodeNumber: Int? = null,
    val positionMs: Long = 0L,
    val durationMs: Long = 0L,
    val lastWatchedTime: Long = System.currentTimeMillis()
) {
    val progressFraction: Float
        get() = if (durationMs > 0L) {
            (positionMs.toFloat() / durationMs.toFloat()).coerceIn(0f, 1f)
        } else 0f

    val progressPercent: Int
        get() = (progressFraction * 100).toInt()

    val remainingMinutes: Long
        get() = if (durationMs > positionMs) {
            ((durationMs - positionMs) / 60000L).coerceAtLeast(1L)
        } else 0L

    val resolvedThumbnail: String
        get() = when {
            !backdropUrl.isNullOrBlank() -> if (backdropUrl.startsWith("http")) backdropUrl else "https://image.tmdb.org/t/p/w780$backdropUrl"
            !posterUrl.isNullOrBlank() -> if (posterUrl.startsWith("http")) posterUrl else "https://image.tmdb.org/t/p/w342$posterUrl"
            else -> ""
        }

    // Has user started watching (>= 10 sec) and not yet finished (< 95% of duration)?
    val isUnfinished: Boolean
        get() = positionMs >= 10_000L && (durationMs <= 0L || positionMs < (durationMs * 0.95))
}
