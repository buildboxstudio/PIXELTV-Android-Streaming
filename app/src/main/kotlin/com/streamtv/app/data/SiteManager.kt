package com.streamtv.app.data

/**
 * Multi-site support - daftar situs streaming yang bisa dipilih user
 */
object SiteManager {

    data class StreamingSite(
        val name: String,
        val url: String,
        val icon: String = "🎬"
    )

    val sites = listOf(
        StreamingSite("iDlix", "https://z1.idlixku.com", "🎬"),
        StreamingSite("LK21", "https://tv10.lk21official.cc", "🎥"),
        StreamingSite("Rebahin", "https://rebahinxxi3.beauty/", "📺")
    )

    fun getSiteByName(name: String): StreamingSite? {
        return sites.find { it.name == name }
    }
}
