package com.idlix.tv.data.api

import com.idlix.tv.data.model.ClaimRequest
import com.idlix.tv.data.model.ClaimResponse
import com.idlix.tv.data.model.EpisodeResponse
import com.idlix.tv.data.model.GenreListResponse
import com.idlix.tv.data.model.MovieDetail
import com.idlix.tv.data.model.MovieListResponse
import com.idlix.tv.data.model.PlayInfoResponse
import com.idlix.tv.data.model.SearchResponse
import com.idlix.tv.data.model.SeriesDetail
import com.idlix.tv.data.model.SeriesListResponse
import com.idlix.tv.data.model.ResolvedPlayback
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface IdlixApiService {

    @GET("api/movies")
    suspend fun getMovies(
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 20,
        @Query("sort") sort: String? = null,
        @Query("genre") genre: String? = null
    ): MovieListResponse

    @GET("api/series")
    suspend fun getSeries(
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 20,
        @Query("sort") sort: String? = null,
        @Query("genre") genre: String? = null
    ): SeriesListResponse

    @GET("api/movies/{slug}")
    suspend fun getMovieDetail(
        @Path("slug") slug: String
    ): MovieDetail

    @GET("api/series/{slug}")
    suspend fun getSeriesDetail(
        @Path("slug") slug: String
    ): SeriesDetail

    @GET("api/series/{slug}/season/{season}/episode/{episode}")
    suspend fun getEpisodeDetail(
        @Path("slug") slug: String,
        @Path("season") season: Int,
        @Path("episode") episode: Int
    ): EpisodeResponse

    @GET("api/genres")
    suspend fun getGenres(): GenreListResponse

    @GET("api/search")
    suspend fun search(
        @Query("q") query: String
    ): SearchResponse

    @GET("api/watch/play-info/movie/{id}")
    suspend fun getMoviePlayInfo(
        @Path("id") movieId: String
    ): PlayInfoResponse

    @GET("api/watch/play-info/episode/{id}")
    suspend fun getEpisodePlayInfo(
        @Path("id") episodeId: String
    ): PlayInfoResponse

    @POST("api/watch/session/claim")
    suspend fun claimSession(
        @Body request: ClaimRequest,
        @Header("Cookie") cookie: String? = null,
        @Header("Origin") origin: String = IdlixSource.ORIGIN
    ): ClaimResponse

    @GET("api/playback/resolve")
    suspend fun resolvePlayback(
        @Query("contentType") contentType: String,
        @Query("contentId") contentId: String,
        @Query("contentTitle") contentTitle: String? = null
    ): ResolvedPlayback
}
