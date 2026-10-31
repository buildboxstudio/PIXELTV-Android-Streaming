package com.idlix.tv.data.model

import kotlinx.serialization.Serializable

@Serializable
data class SeriesListResponse(
    val data: List<SeriesItem> = emptyList(),
    val pagination: Pagination? = null
)

@Serializable
data class SeriesItem(
    val id: String = "",
    val title: String = "",
    val slug: String = "",
    val posterPath: String? = null,
    val backdropPath: String? = null,
    val firstAirDate: String? = null,
    @Serializable(with = FlexibleStringSerializer::class)
    val voteAverage: String? = null,
    val viewCount: Long = 0,
    val numberOfSeasons: Int? = null,
    val country: String? = null,
    val contentType: String = "tv_series",
    val genres: List<Genre> = emptyList(),
    val hasVideo: Boolean = true
) {
    val fullPosterUrl: String
        get() = posterPath?.let { if (it.startsWith("http")) it else "https://image.tmdb.org/t/p/w342$it" } ?: ""

    val fullBackdropUrl: String
        get() = backdropPath?.let { if (it.startsWith("http")) it else "https://image.tmdb.org/t/p/w1280$it" } ?: ""

    val formattedRating: String
        get() = voteAverage?.toDoubleOrNull()?.let { String.format("%.1f", it) } ?: "N/A"

    fun toMovieItem(): MovieItem = MovieItem(
        id = id,
        title = title,
        slug = slug,
        posterPath = posterPath,
        backdropPath = backdropPath,
        releaseDate = firstAirDate,
        voteAverage = voteAverage,
        viewCount = viewCount,
        contentType = "tv_series"
    )
}

@Serializable
data class SeriesDetail(
    val id: String = "",
    val tmdbId: Long? = null,
    val imdbId: String? = null,
    val title: String = "",
    val slug: String = "",
    val originalTitle: String? = null,
    val overview: String? = null,
    val tagline: String? = null,
    val posterPath: String? = null,
    val backdropPath: String? = null,
    val logoPath: String? = null,
    val backdrops: List<String> = emptyList(),
    val firstAirDate: String? = null,
    val numberOfSeasons: Int = 1,
    val numberOfEpisodes: Int = 1,
    @Serializable(with = FlexibleStringSerializer::class)
    val voteAverage: String? = null,
    val viewCount: Long = 0,
    val country: String? = null,
    val status: String? = null,
    val genres: List<Genre> = emptyList(),
    val seasons: List<Season> = emptyList(),
    val cast: List<CastMember> = emptyList(),
    val defaultSeason: SeasonDetail? = null
) {
    val fullPosterUrl: String
        get() = posterPath?.let { if (it.startsWith("http")) it else "https://image.tmdb.org/t/p/w342$it" } ?: ""

    val fullBackdropUrl: String
        get() = backdropPath?.let { if (it.startsWith("http")) it else "https://image.tmdb.org/t/p/w1280$it" } ?: ""
}

@Serializable
data class Season(
    val id: String = "",
    val seasonNumber: Int = 1,
    val name: String = "",
    val overview: String? = null,
    val posterPath: String? = null,
    val airDate: String? = null,
    val episodeCount: Int = 0
) {
    val fullPosterUrl: String
        get() = posterPath?.let { if (it.startsWith("http")) it else "https://image.tmdb.org/t/p/w342$it" } ?: ""
}

@Serializable
data class SeasonDetail(
    val id: String = "",
    val seasonNumber: Int = 1,
    val name: String = "",
    val overview: String? = null,
    val posterPath: String? = null,
    val episodes: List<EpisodeItem> = emptyList()
)

@Serializable
data class EpisodeItem(
    val id: String = "",
    val seasonId: String = "",
    val tvSeriesId: String = "",
    val episodeNumber: Int = 1,
    val name: String = "",
    val overview: String? = null,
    val stillPath: String? = null,
    val airDate: String? = null,
    val runtime: Int? = null,
    @Serializable(with = FlexibleStringSerializer::class)
    val voteAverage: String? = null,
    val hasVideo: Boolean = true
) {
    val fullStillUrl: String
        get() = stillPath?.let { if (it.startsWith("http")) it else "https://image.tmdb.org/t/p/w500$it" } ?: ""
}

@Serializable
data class EpisodeResponse(
    val series: SeriesDetail? = null,
    val season: SeasonDetail? = null,
    val episode: EpisodeItem? = null,
    val allSeasons: List<Season> = emptyList()
)

@Serializable
data class SearchResponse(
    val results: List<SearchResultItem> = emptyList(),
    val total: Long = 0,
    @Serializable(with = FlexibleStringSerializer::class)
    val order: String? = null
)

@Serializable
data class SearchResultItem(
    val id: String = "",
    val contentType: String = "movie",
    val title: String = "",
    val originalTitle: String? = null,
    val overview: String? = null,
    val genres: List<String> = emptyList(),
    val releaseDate: String? = null,
    val firstAirDate: String? = null,
    val originalLanguage: String? = null,
    @Serializable(with = FlexibleStringSerializer::class)
    val voteAverage: String? = null,
    val viewCount: Long = 0,
    val posterPath: String? = null,
    val backdropPath: String? = null,
    val quality: String? = null,
    val slug: String = "",
    val numberOfSeasons: Int? = null,
    val commentCount: Int? = null
) {
    val fullPosterUrl: String
        get() = posterPath?.let { if (it.startsWith("http")) it else "https://image.tmdb.org/t/p/w342$it" } ?: ""

    val fullBackdropUrl: String
        get() = backdropPath?.let { if (it.startsWith("http")) it else "https://image.tmdb.org/t/p/w1280$it" } ?: ""

    val formattedRating: String
        get() = voteAverage?.toDoubleOrNull()?.let { String.format("%.1f", it) } ?: (voteAverage ?: "N/A")
}
