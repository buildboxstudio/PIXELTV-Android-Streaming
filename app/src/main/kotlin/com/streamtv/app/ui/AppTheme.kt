package com.streamtv.app.ui

import android.graphics.Color

/**
 * PIXELTV Theme - Red / Black / Grey accent
 * Centralized color palette for consistent UI across all screens.
 */
object AppTheme {
    // Base backgrounds (black tones)
    val BG_DARKEST = Color.parseColor("#0A0A0A")   // near-black main bg
    val BG_DARK = Color.parseColor("#141414")      // card/panel bg
    val BG_ELEVATED = Color.parseColor("#1E1E1E")  // elevated surface
    val BG_PANEL = Color.parseColor("#181818")     // menu panel

    // Red accent
    val ACCENT = Color.parseColor("#E50914")       // primary red (Netflix-ish)
    val ACCENT_DARK = Color.parseColor("#B00710")  // darker red
    val ACCENT_LIGHT = Color.parseColor("#FF2D3A") // brighter red

    // Grey tones
    val GREY = Color.parseColor("#888888")
    val GREY_LIGHT = Color.parseColor("#B3B3B3")
    val GREY_DARK = Color.parseColor("#404040")

    // Text
    val TEXT_PRIMARY = Color.parseColor("#FFFFFF")
    val TEXT_SECONDARY = Color.parseColor("#B3B3B3")
    val TEXT_MUTED = Color.parseColor("#666666")

    // Overlays / strokes
    val STROKE = Color.parseColor("#2A2A2A")
    val STROKE_FOCUS = ACCENT
    val DIVIDER = Color.parseColor("#222222")

    // Translucent variants (for hex string usage)
    const val HEX_ACCENT = "#E50914"
    const val HEX_ACCENT_DARK = "#B00710"
    const val HEX_PANEL_TRANSLUCENT = "#E6141414"
    const val HEX_ITEM = "#1AFFFFFF"
    const val HEX_ITEM_FOCUS = "#33E50914"
}
