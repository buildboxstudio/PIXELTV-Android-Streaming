package com.idlix.tv.data.model

import kotlinx.serialization.Serializable

const val TMDB_POSTER_PREFIX = "https://image.tmdb.org/t/p/w342"
const val TMDB_BACKDROP_PREFIX = "https://image.tmdb.org/t/p/w1280"
const val TMDB_STILL_PREFIX = "https://image.tmdb.org/t/p/w500"

@Serializable
data class V1CatalogResponse(
    val items: List<V1Item> = emptyList(),
    val warnings: List<V1Warning> = emptyList()
)

@Serializable
data class V1Warning(
    val source: String = "",
    val message: String = ""
)

@Serializable
data class V1Item(
    val id: String = "",
    val playId: String = "",
    val type: String = "movie",
    val title: String = "",
    val year: Int? = null,
    val poster: String? = null,
    val backdrop: String? = null,
    val rating: Double? = null,
    val synopsis: String? = null,
    val source: String = ""
) {
    val isSeries: Boolean get() = type == "series"
}

@Serializable
data class V1Episode(
    val id: String = "",
    val playId: String = "",
    val season: Int = 1,
    val episode: Int = 1,
    val title: String? = null,
    val still: String? = null
)

@Serializable
data class V1Detail(
    val id: String = "",
    val playId: String = "",
    val type: String = "movie",
    val title: String = "",
    val year: Int? = null,
    val poster: String? = null,
    val backdrop: String? = null,
    val rating: Double? = null,
    val synopsis: String? = null,
    val genres: List<String> = emptyList(),
    val runtimeMinutes: Int? = null,
    val episodes: List<V1Episode> = emptyList(),
    val source: String = ""
)

@Serializable
data class V1Subtitle(
    val lang: String = "id",
    val label: String = "Indonesian",
    val url: String = ""
)

@Serializable
data class V1Play(
    val title: String = "",
    val hlsUrl: String = "",
    val subtitles: List<V1Subtitle> = emptyList(),
    val durationSec: Long = 0L,
    val source: String = ""
)

fun V1Item.toMovieItem(): MovieItem = MovieItem(
    id = playId.ifBlank { id },
    title = title,
    slug = id,
    posterPath = poster,
    backdropPath = backdrop,
    releaseDate = year?.toString(),
    voteAverage = rating?.toString(),
    contentType = "movie"
)

fun V1Item.toSeriesItem(): SeriesItem = SeriesItem(
    id = playId.ifBlank { id },
    title = title,
    slug = id,
    posterPath = poster,
    backdropPath = backdrop,
    firstAirDate = year?.toString(),
    voteAverage = rating?.toString(),
    contentType = "tv_series"
)

fun V1Item.toSearchResult(): SearchResultItem = SearchResultItem(
    id = playId.ifBlank { id },
    contentType = type,
    title = title,
    overview = synopsis,
    posterPath = poster,
    backdropPath = backdrop,
    slug = id,
    releaseDate = if (isSeries) null else year?.toString(),
    firstAirDate = if (isSeries) year?.toString() else null,
    voteAverage = rating?.toString()
)

fun V1Detail.toMovieDetail(): MovieDetail = MovieDetail(
    id = playId.ifBlank { id },
    title = title,
    slug = id,
    overview = synopsis,
    posterPath = poster,
    backdropPath = backdrop,
    releaseDate = year?.toString(),
    runtime = runtimeMinutes,
    duration = runtimeMinutes,
    voteAverage = rating?.toString(),
    genres = genres.map { Genre(name = it) }
)

fun V1Detail.toSeriesDetail(): SeriesDetail {
    val seasons = episodes.map { it.season }.distinct().sorted()
    val seasonDetails = seasons.map { number ->
        SeasonDetail(
            seasonNumber = number,
            name = "Season $number",
            episodes = episodes
                .filter { it.season == number }
                .map { episode ->
                    EpisodeItem(
                        id = episode.playId.ifBlank { episode.id },
                        tvSeriesId = playId,
                        episodeNumber = episode.episode,
                        name = episode.title ?: "Episode ${episode.episode}",
                        stillPath = episode.still
                    )
                }
        )
    }

    return SeriesDetail(
        id = playId.ifBlank { id },
        title = title,
        slug = id,
        overview = synopsis,
        posterPath = poster,
        backdropPath = backdrop,
        firstAirDate = year?.toString(),
        numberOfSeasons = seasons.size.coerceAtLeast(1),
        numberOfEpisodes = episodes.size.coerceAtLeast(1),
        voteAverage = rating?.toString(),
        genres = genres.map { Genre(name = it) },
        seasons = seasons.map { number ->
            Season(
                seasonNumber = number,
                name = "Season $number",
                episodeCount = episodes.count { it.season == number }
            )
        },
        defaultSeason = seasonDetails.firstOrNull()
    )
}

fun V1Play.toResolvedPlayback(): ResolvedPlayback = ResolvedPlayback(
    title = title,
    hlsMasterUrl = hlsUrl,
    subtitles = subtitles.map { SubtitleTrack(lang = it.lang, label = it.label, path = it.url) },
    durationSec = durationSec
)
