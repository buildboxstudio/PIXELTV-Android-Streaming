package com.streamtv.app.data

import android.content.Context
import android.content.SharedPreferences
import org.json.JSONArray
import org.json.JSONObject

/**
 * History manager - simpan riwayat halaman yang dikunjungi
 */
class HistoryManager(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("streamtv_history", Context.MODE_PRIVATE)

    companion object {
        private const val MAX_HISTORY = 100
    }

    data class HistoryItem(
        val title: String,
        val url: String,
        val timestamp: Long = System.currentTimeMillis()
    )

    fun getHistory(): List<HistoryItem> {
        val json = prefs.getString("history", "[]") ?: "[]"
        val array = JSONArray(json)
        val list = mutableListOf<HistoryItem>()
        for (i in 0 until array.length()) {
            val obj = array.getJSONObject(i)
            list.add(
                HistoryItem(
                    title = obj.getString("title"),
                    url = obj.getString("url"),
                    timestamp = obj.optLong("timestamp", 0)
                )
            )
        }
        return list.sortedByDescending { it.timestamp }
    }

    fun addToHistory(title: String, url: String) {
        val history = getHistory().toMutableList()

        // Hapus duplikat URL (akan ditambah ulang di atas)
        history.removeAll { it.url == url }

        // Tambah di awal
        history.add(0, HistoryItem(title, url))

        // Limit jumlah history
        val trimmed = history.take(MAX_HISTORY)
        saveHistory(trimmed)
    }

    fun clearAll() {
        prefs.edit().putString("history", "[]").apply()
    }

    fun getRecentHistory(limit: Int = 10): List<HistoryItem> {
        return getHistory().take(limit)
    }

    private fun saveHistory(history: List<HistoryItem>) {
        val array = JSONArray()
        history.forEach { item ->
            val obj = JSONObject().apply {
                put("title", item.title)
                put("url", item.url)
                put("timestamp", item.timestamp)
            }
            array.put(obj)
        }
        prefs.edit().putString("history", array.toString()).apply()
    }
}
