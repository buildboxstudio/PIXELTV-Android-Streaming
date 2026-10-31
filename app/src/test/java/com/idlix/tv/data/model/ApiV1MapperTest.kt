package com.idlix.tv.data.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ApiV1MapperTest {

    @Test
    fun `catalog item maps to movie item with absolute poster`() {
        val item = V1Item(
            id = "the-dog-stars-2026",
            playId = "8f866f15",
            type = "movie",
            title = "The Dog Stars",
            year = 2026,
            poster = "https://image.tmdb.org/t/p/w342/abc.jpg",
            rating = 6.6,
            source = "legacy"
        )

        val movie = item.toMovieItem()

        assertEquals("8f866f15", movie.id)
        assertEquals("the-dog-stars-2026", movie.slug)
        assertEquals("2026", movie.releaseDate)
        assertEquals("6.6", movie.voteAverage)
        assertEquals("https://image.tmdb.org/t/p/w342/abc.jpg", movie.fullPosterUrl)
        assertTrue(movie.fullBackdropUrl.isEmpty())
    }

    @Test
    fun `series detail builds seasons and default season`() {
        val detail = V1Detail(
            id = "some-series",
            playId = "series-1",
            type = "series",
            title = "Some Series",
            year = 2025,
            genres = listOf("Drama"),
            episodes = listOf(
                V1Episode(id = "ep-1", playId = "ep-1", season = 1, episode = 1, title = "Pilot"),
                V1Episode(id = "ep-2", playId = "ep-2", season = 1, episode = 2)
            )
        )

        val series = detail.toSeriesDetail()

        assertEquals("series-1", series.id)
        assertEquals(1, series.numberOfSeasons)
        assertEquals(2, series.numberOfEpisodes)
        assertEquals(listOf("Drama"), series.genres.map { it.name })
        assertEquals(2, series.defaultSeason?.episodes?.size)
        assertEquals("ep-2", series.defaultSeason?.episodes?.get(1)?.id)
    }

    @Test
    fun `play response maps subtitles and hls url`() {
        val play = V1Play(
            title = "Some Movie",
            hlsUrl = "https://cdn.example/master.m3u8?t=1",
            subtitles = listOf(V1Subtitle(lang = "id", label = "Indonesian", url = "https://cdn.example/id.vtt")),
            durationSec = 5400
        )

        val resolved = play.toResolvedPlayback()

        assertEquals("https://cdn.example/master.m3u8?t=1", resolved.hlsMasterUrl)
        assertEquals(1, resolved.subtitles.size)
        assertEquals("https://cdn.example/id.vtt", resolved.subtitles.first().path)
        assertEquals(5400L, resolved.durationSec)
    }

    @Test
    fun `relative poster path still resolves against tmdb`() {
        val movie = MovieItem(title = "Old", posterPath = "/cover.jpg")
        assertEquals("https://image.tmdb.org/t/p/w342/cover.jpg", movie.fullPosterUrl)
    }
}
