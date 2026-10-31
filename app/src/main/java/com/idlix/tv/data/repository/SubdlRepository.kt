package com.idlix.tv.data.repository

import android.content.Context
import android.net.Uri
import android.util.Log
import com.idlix.tv.IdlixTvApp
import com.idlix.tv.data.api.ApiClient
import com.idlix.tv.data.model.SubdlResponse
import com.idlix.tv.data.model.SubdlSubtitle
import com.idlix.tv.data.model.SubtitleTrack
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.Request
import java.io.File
import java.io.FileOutputStream
import java.net.URLEncoder
import java.util.zip.ZipInputStream

object SubdlRepository {
    private const val TAG = "SubdlRepository"
    private const val PREF_KEY_SUBDL_API_KEY = "custom_subdl_api_key"
    const val DEFAULT_API_KEY = "subdl__VuGBV3ExlH8tW68l4wVEIsk6Mfnaxq1_FiONFN8Yg0"
    private const val BASE_URL = "https://api.subdl.com/api/v1/subtitles"

    @Volatile
    private var currentApiKey: String? = null

    fun getApiKey(): String {
        currentApiKey?.let { return it }
        val saved = try {
            val prefs = IdlixTvApp.appContext.getSharedPreferences("idlix_config", Context.MODE_PRIVATE)
            prefs.getString(PREF_KEY_SUBDL_API_KEY, null)
        } catch (_: Exception) {
            null
        }
        val key = if (!saved.isNullOrBlank()) saved else DEFAULT_API_KEY
        currentApiKey = key
        return key
    }

    fun setApiKey(newKey: String) {
        val trimmed = newKey.trim()
        currentApiKey = trimmed
        try {
            val prefs = IdlixTvApp.appContext.getSharedPreferences("idlix_config", Context.MODE_PRIVATE)
            prefs.edit().putString(PREF_KEY_SUBDL_API_KEY, trimmed).apply()
        } catch (_: Exception) {}
    }

    suspend fun searchAllSubtitles(
        title: String,
        type: String = "movie",
        tmdbId: Long? = null,
        imdbId: String? = null,
        season: Int? = null,
        episode: Int? = null,
        languages: String = "ID,EN"
    ): List<SubdlSubtitle> = withContext(Dispatchers.IO) {
        try {
            val apiKey = getApiKey()
            val queryParams = StringBuilder("api_key=$apiKey&languages=$languages")
            if (tmdbId != null && tmdbId > 0) {
                queryParams.append("&tmdb_id=$tmdbId")
            } else if (!imdbId.isNullOrBlank()) {
                queryParams.append("&imdb_id=$imdbId")
            } else {
                val cleanTitle = title.replace(Regex("\\(\\d{4}\\)"), "").trim()
                val encoded = URLEncoder.encode(cleanTitle, "UTF-8")
                queryParams.append("&film_name=$encoded")
            }

            if (type == "tv" || season != null) {
                queryParams.append("&type=tv")
                if (season != null) queryParams.append("&season_number=$season")
                if (episode != null) queryParams.append("&episode_number=$episode")
            } else {
                queryParams.append("&type=movie")
            }

            val requestUrl = "$BASE_URL?$queryParams"
            val request = Request.Builder()
                .url(requestUrl)
                .header("User-Agent", "Mozilla/5.0 (Android TV; Idlix)")
                .build()

            val response = ApiClient.okHttpClient.newCall(request).execute()
            if (!response.isSuccessful) {
                Log.w(TAG, "SubDL request failed with code: ${response.code}")
                return@withContext emptyList()
            }

            val body = response.body?.string() ?: return@withContext emptyList()
            val subdlRes = ApiClient.json.decodeFromString<SubdlResponse>(body)
            if (!subdlRes.status || subdlRes.subtitles.isEmpty()) {
                Log.d(TAG, "No SubDL subtitles found for $title")
                return@withContext emptyList()
            }

            subdlRes.subtitles
        } catch (e: Exception) {
            Log.e(TAG, "Failed to search SubDL subtitles: ${e.message}")
            emptyList()
        }
    }

    suspend fun downloadSubtitle(
        sub: SubdlSubtitle,
        context: Context
    ): SubtitleTrack? = withContext(Dispatchers.IO) {
        try {
            val zipUrl = sub.fullDownloadUrl
            val prefix = "subdl_${sub.language ?: "id"}_${System.currentTimeMillis()}"
            val extractedFile = downloadAndExtractSrt(zipUrl, context, prefix) ?: return@withContext null

            val isIndo = sub.language.equals("ID", ignoreCase = true) || sub.lang.equals("indonesian", ignoreCase = true)
            val langCode = if (isIndo) "id" else (sub.language?.lowercase() ?: "en")
            val releaseTag = sub.author?.let { " ($it)" } ?: ""
            val label = if (isIndo) "Indonesian [SubDL]$releaseTag" else "English [SubDL]$releaseTag"

            SubtitleTrack(
                lang = langCode,
                label = label,
                path = Uri.fromFile(extractedFile).toString()
            )
        } catch (e: Exception) {
            Log.e(TAG, "Failed to download SubDL subtitle: ${e.message}")
            null
        }
    }

    suspend fun fetchIndonesianSubtitle(
        title: String,
        type: String = "movie",
        tmdbId: Long? = null,
        imdbId: String? = null,
        season: Int? = null,
        episode: Int? = null,
        context: Context
    ): SubtitleTrack? = withContext(Dispatchers.IO) {
        val allSubs = searchAllSubtitles(title, type, tmdbId, imdbId, season, episode, languages = "ID")
        val chosen = allSubs.firstOrNull {
            it.language.equals("ID", ignoreCase = true) || it.lang.equals("indonesian", ignoreCase = true)
        } ?: allSubs.firstOrNull() ?: return@withContext null

        downloadSubtitle(chosen, context)
    }

    private fun downloadAndExtractSrt(zipUrl: String, context: Context, filePrefix: String): File? {
        return try {
            val req = Request.Builder()
                .url(zipUrl)
                .header("User-Agent", "Mozilla/5.0 (Android TV; Idlix)")
                .build()

            val res = ApiClient.okHttpClient.newCall(req).execute()
            if (!res.isSuccessful) return null

            val inputStream = res.body?.byteStream() ?: return null
            val zipStream = ZipInputStream(inputStream)
            val subDir = File(context.cacheDir, "subtitles").apply { mkdirs() }

            var entry = zipStream.nextEntry
            var targetFile: File? = null

            while (entry != null) {
                val name = entry.name.lowercase()
                if (name.endsWith(".srt") || name.endsWith(".vtt")) {
                    val ext = if (name.endsWith(".vtt")) ".vtt" else ".srt"
                    val outFile = File(subDir, "$filePrefix$ext")
                    FileOutputStream(outFile).use { fos ->
                        zipStream.copyTo(fos)
                    }
                    targetFile = outFile
                    break
                }
                zipStream.closeEntry()
                entry = zipStream.nextEntry
            }
            zipStream.close()
            targetFile
        } catch (e: Exception) {
            Log.e(TAG, "Error extracting zip from $zipUrl: ${e.message}")
            null
        }
    }
}
