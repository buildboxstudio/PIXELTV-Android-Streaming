package com.streamtv.app.data

import android.content.Context
import android.content.SharedPreferences
import org.json.JSONArray
import org.json.JSONObject

/**
 * Bookmark manager - simpan & load bookmark dari SharedPreferences
 */
class BookmarkManager(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("streamtv_bookmarks", Context.MODE_PRIVATE)

    data class Bookmark(
        val title: String,
        val url: String,
        val timestamp: Long = System.currentTimeMillis()
    )

    fun getBookmarks(): List<Bookmark> {
        val json = prefs.getString("bookmarks", "[]") ?: "[]"
        val array = JSONArray(json)
        val list = mutableListOf<Bookmark>()
        for (i in 0 until array.length()) {
            val obj = array.getJSONObject(i)
            list.add(
                Bookmark(
                    title = obj.getString("title"),
                    url = obj.getString("url"),
                    timestamp = obj.optLong("timestamp", 0)
                )
            )
        }
        return list.sortedByDescending { it.timestamp }
    }

    fun addBookmark(title: String, url: String): Boolean {
        val bookmarks = getBookmarks().toMutableList()

        // Cek duplikat
        if (bookmarks.any { it.url == url }) return false

        bookmarks.add(Bookmark(title, url))
        saveBookmarks(bookmarks)
        return true
    }

    fun removeBookmark(url: String) {
        val bookmarks = getBookmarks().filter { it.url != url }
        saveBookmarks(bookmarks)
    }

    fun isBookmarked(url: String): Boolean {
        return getBookmarks().any { it.url == url }
    }

    fun clearAll() {
        prefs.edit().putString("bookmarks", "[]").apply()
    }

    private fun saveBookmarks(bookmarks: List<Bookmark>) {
        val array = JSONArray()
        bookmarks.forEach { bookmark ->
            val obj = JSONObject().apply {
                put("title", bookmark.title)
                put("url", bookmark.url)
                put("timestamp", bookmark.timestamp)
            }
            array.put(obj)
        }
        prefs.edit().putString("bookmarks", array.toString()).apply()
    }
}
