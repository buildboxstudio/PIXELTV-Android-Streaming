package com.streamtv.app.ui

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.GradientDrawable
import android.net.Uri
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.view.WindowManager
import android.view.animation.DecelerateInterpolator
import android.widget.*
import com.streamtv.app.data.BookmarkManager
import com.streamtv.app.data.HistoryManager
import com.streamtv.app.data.SiteManager

/**
 * Modern overlay menu with glassmorphism-style design
 */
class OverlayMenu(
    private val context: Context,
    private val bookmarkManager: BookmarkManager,
    private val historyManager: HistoryManager,
    private val onNavigate: (String) -> Unit,
    private val onBookmarkCurrent: () -> Unit
) {

    companion object {
        const val APP_VERSION = "1.5.1"
        const val CREDIT_URL = "https://www.tiktok.com/@buildbox.studio"
    }

    private fun createMenuBackground(): GradientDrawable {
        return GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            cornerRadius = 28f
            colors = intArrayOf(
                Color.parseColor("#E6181828"),
                Color.parseColor("#E6101020")
            )
            gradientType = GradientDrawable.LINEAR_GRADIENT
            setStroke(2, Color.parseColor("#33FFFFFF"))
        }
    }

    private fun createMenuItemBackground(): GradientDrawable {
        return GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            cornerRadius = 16f
            setColor(Color.parseColor("#1AFFFFFF"))
        }
    }

    private fun createMenuItem(icon: String, label: String, onClick: () -> Unit): LinearLayout {
        val density = context.resources.displayMetrics.density

        return LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(
                (20 * density).toInt(),
                (14 * density).toInt(),
                (20 * density).toInt(),
                (14 * density).toInt()
            )
            background = createMenuItemBackground()

            val params = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            params.bottomMargin = (8 * density).toInt()
            layoutParams = params

            // Icon
            addView(TextView(context).apply {
                text = icon
                textSize = 20f
                layoutParams = LinearLayout.LayoutParams(
                    (36 * density).toInt(),
                    ViewGroup.LayoutParams.WRAP_CONTENT
                )
            })

            // Label
            addView(TextView(context).apply {
                text = label
                textSize = 15f
                setTextColor(Color.WHITE)
                typeface = Typeface.create("sans-serif-medium", Typeface.NORMAL)
                layoutParams = LinearLayout.LayoutParams(
                    0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f
                )
            })

            // Arrow
            addView(TextView(context).apply {
                text = "›"
                textSize = 20f
                setTextColor(Color.parseColor("#66FFFFFF"))
            })

            setOnClickListener { onClick() }

            // Focus highlight for TV remote
            isFocusable = true
            setOnFocusChangeListener { v, hasFocus ->
                if (hasFocus) {
                    (v.background as? GradientDrawable)?.setColor(Color.parseColor("#33FFFFFF"))
                } else {
                    (v.background as? GradientDrawable)?.setColor(Color.parseColor("#1AFFFFFF"))
                }
            }
        }
    }

    fun showMainMenu() {
        val dialog = Dialog(context)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.window?.apply {
            setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            setDimAmount(0.7f)
            setLayout(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT
            )
            setGravity(Gravity.CENTER)
        }

        val density = context.resources.displayMetrics.density
        val maxWidth = (340 * density).toInt()
        val maxHeight = (context.resources.displayMetrics.heightPixels * 0.85).toInt()

        // ScrollView as root so entire menu is scrollable
        val scrollView = ScrollView(context).apply {
            layoutParams = ViewGroup.LayoutParams(maxWidth, ViewGroup.LayoutParams.WRAP_CONTENT)
            isVerticalScrollBarEnabled = true
            isFillViewport = true
        }

        val rootLayout = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            background = createMenuBackground()
            setPadding(
                (24 * density).toInt(),
                (28 * density).toInt(),
                (24 * density).toInt(),
                (20 * density).toInt()
            )
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        }

        // Title
        rootLayout.addView(TextView(context).apply {
            text = "PIXELTV"
            textSize = 22f
            setTextColor(Color.WHITE)
            typeface = Typeface.create("sans-serif-black", Typeface.BOLD)
            gravity = Gravity.CENTER
            setPadding(0, 0, 0, (4 * density).toInt())
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        })

        // Subtitle
        rootLayout.addView(TextView(context).apply {
            text = "Streaming Menu"
            textSize = 12f
            setTextColor(Color.parseColor("#88FFFFFF"))
            gravity = Gravity.CENTER
            setPadding(0, 0, 0, (20 * density).toInt())
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        })

        // Divider
        rootLayout.addView(createDivider(density))

        // Menu items
        val menuItems = listOf(
            Triple("🏠", "Beranda") { dialog.dismiss(); onNavigate(SiteManager.sites[0].url) },
            Triple("🌐", "Pilih Situs") { dialog.dismiss(); showSiteSelector() },
            Triple("⭐", "Bookmark") { dialog.dismiss(); showBookmarks() },
            Triple("📋", "Riwayat") { dialog.dismiss(); showHistory() },
            Triple("➕", "Bookmark Halaman Ini") { dialog.dismiss(); onBookmarkCurrent() },
            Triple("🗑️", "Hapus Riwayat") { dialog.dismiss(); confirmClearHistory() }
        )

        menuItems.forEach { (icon, label, action) ->
            rootLayout.addView(createMenuItem(icon, label, action))
        }

        // Divider
        rootLayout.addView(createDivider(density))

        // Credit
        rootLayout.addView(TextView(context).apply {
            text = "Build by Buildbox Studio"
            textSize = 12f
            setTextColor(Color.parseColor("#7C4DFF"))
            gravity = Gravity.CENTER
            setPadding(0, (12 * density).toInt(), 0, (4 * density).toInt())
            paintFlags = paintFlags or android.graphics.Paint.UNDERLINE_TEXT_FLAG
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            setOnClickListener {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(CREDIT_URL))
                context.startActivity(intent)
            }
        })

        // Version
        rootLayout.addView(TextView(context).apply {
            text = "v$APP_VERSION"
            textSize = 10f
            setTextColor(Color.parseColor("#55FFFFFF"))
            gravity = Gravity.CENTER
            setPadding(0, (2 * density).toInt(), 0, 0)
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        })

        scrollView.addView(rootLayout)

        dialog.setContentView(scrollView)
        dialog.window?.setLayout(maxWidth, maxHeight.coerceAtMost(ViewGroup.LayoutParams.WRAP_CONTENT))
        dialog.show()

        // Animate in
        rootLayout.alpha = 0f
        rootLayout.scaleX = 0.9f
        rootLayout.scaleY = 0.9f
        rootLayout.animate()
            .alpha(1f)
            .scaleX(1f)
            .scaleY(1f)
            .setDuration(200)
            .setInterpolator(DecelerateInterpolator())
            .start()
    }

    fun showSiteSelector() {
        val dialog = Dialog(context)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.window?.apply {
            setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            setDimAmount(0.7f)
            setGravity(Gravity.CENTER)
        }

        val density = context.resources.displayMetrics.density
        val maxWidth = (320 * density).toInt()

        val rootLayout = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            background = createMenuBackground()
            setPadding(
                (24 * density).toInt(),
                (24 * density).toInt(),
                (24 * density).toInt(),
                (24 * density).toInt()
            )
            layoutParams = ViewGroup.LayoutParams(maxWidth, ViewGroup.LayoutParams.WRAP_CONTENT)
        }

        rootLayout.addView(TextView(context).apply {
            text = "🌐  Pilih Situs"
            textSize = 18f
            setTextColor(Color.WHITE)
            typeface = Typeface.create("sans-serif-medium", Typeface.NORMAL)
            setPadding(0, 0, 0, (16 * density).toInt())
        })

        SiteManager.sites.forEach { site ->
            rootLayout.addView(createMenuItem(site.icon, site.name) {
                dialog.dismiss()
                onNavigate(site.url)
            })
        }

        dialog.setContentView(rootLayout)
        dialog.show()

        rootLayout.alpha = 0f
        rootLayout.animate().alpha(1f).setDuration(150).start()
    }

    fun showBookmarks() {
        val bookmarks = bookmarkManager.getBookmarks()

        if (bookmarks.isEmpty()) {
            Toast.makeText(context, "Belum ada bookmark", Toast.LENGTH_SHORT).show()
            return
        }

        val dialog = Dialog(context)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.window?.apply {
            setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            setDimAmount(0.7f)
            setGravity(Gravity.CENTER)
        }

        val density = context.resources.displayMetrics.density
        val maxWidth = (360 * density).toInt()

        val rootLayout = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            background = createMenuBackground()
            setPadding(
                (24 * density).toInt(),
                (24 * density).toInt(),
                (24 * density).toInt(),
                (24 * density).toInt()
            )
            layoutParams = ViewGroup.LayoutParams(maxWidth, ViewGroup.LayoutParams.WRAP_CONTENT)
        }

        rootLayout.addView(TextView(context).apply {
            text = "⭐  Bookmark (${bookmarks.size})"
            textSize = 18f
            setTextColor(Color.WHITE)
            typeface = Typeface.create("sans-serif-medium", Typeface.NORMAL)
            setPadding(0, 0, 0, (16 * density).toInt())
        })

        // Scrollable list
        val scrollView = ScrollView(context).apply {
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                (300 * density).toInt().coerceAtMost(bookmarks.size * (60 * density).toInt())
            )
        }

        val listLayout = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
        }

        bookmarks.forEach { bookmark ->
            val title = if (bookmark.title.length > 35) bookmark.title.take(35) + "…" else bookmark.title
            listLayout.addView(createMenuItem("⭐", title) {
                dialog.dismiss()
                onNavigate(bookmark.url)
            })
        }

        scrollView.addView(listLayout)
        rootLayout.addView(scrollView)

        // Clear all button
        rootLayout.addView(createMenuItem("🗑️", "Hapus Semua Bookmark") {
            dialog.dismiss()
            bookmarkManager.clearAll()
            Toast.makeText(context, "Semua bookmark dihapus", Toast.LENGTH_SHORT).show()
        })

        dialog.setContentView(rootLayout)
        dialog.show()

        rootLayout.alpha = 0f
        rootLayout.animate().alpha(1f).setDuration(150).start()
    }

    fun showHistory() {
        val history = historyManager.getHistory()

        if (history.isEmpty()) {
            Toast.makeText(context, "Belum ada riwayat", Toast.LENGTH_SHORT).show()
            return
        }

        val dialog = Dialog(context)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.window?.apply {
            setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            setDimAmount(0.7f)
            setGravity(Gravity.CENTER)
        }

        val density = context.resources.displayMetrics.density
        val maxWidth = (360 * density).toInt()

        val rootLayout = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            background = createMenuBackground()
            setPadding(
                (24 * density).toInt(),
                (24 * density).toInt(),
                (24 * density).toInt(),
                (24 * density).toInt()
            )
            layoutParams = ViewGroup.LayoutParams(maxWidth, ViewGroup.LayoutParams.WRAP_CONTENT)
        }

        rootLayout.addView(TextView(context).apply {
            text = "📋  Riwayat (${history.size})"
            textSize = 18f
            setTextColor(Color.WHITE)
            typeface = Typeface.create("sans-serif-medium", Typeface.NORMAL)
            setPadding(0, 0, 0, (16 * density).toInt())
        })

        val scrollView = ScrollView(context).apply {
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                (300 * density).toInt().coerceAtMost(history.size * (60 * density).toInt())
            )
        }

        val listLayout = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
        }

        history.take(20).forEach { item ->
            val title = if (item.title.length > 35) item.title.take(35) + "…" else item.title
            listLayout.addView(createMenuItem("📄", title) {
                dialog.dismiss()
                onNavigate(item.url)
            })
        }

        scrollView.addView(listLayout)
        rootLayout.addView(scrollView)

        rootLayout.addView(createMenuItem("🗑️", "Hapus Semua Riwayat") {
            dialog.dismiss()
            historyManager.clearAll()
            Toast.makeText(context, "Riwayat dihapus", Toast.LENGTH_SHORT).show()
        })

        dialog.setContentView(rootLayout)
        dialog.show()

        rootLayout.alpha = 0f
        rootLayout.animate().alpha(1f).setDuration(150).start()
    }

    private fun confirmClearHistory() {
        val dialog = Dialog(context)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.window?.apply {
            setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            setDimAmount(0.7f)
            setGravity(Gravity.CENTER)
        }

        val density = context.resources.displayMetrics.density
        val maxWidth = (300 * density).toInt()

        val rootLayout = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            background = createMenuBackground()
            setPadding(
                (24 * density).toInt(),
                (24 * density).toInt(),
                (24 * density).toInt(),
                (24 * density).toInt()
            )
            layoutParams = ViewGroup.LayoutParams(maxWidth, ViewGroup.LayoutParams.WRAP_CONTENT)
            gravity = Gravity.CENTER
        }

        rootLayout.addView(TextView(context).apply {
            text = "🗑️"
            textSize = 36f
            gravity = Gravity.CENTER
            setPadding(0, 0, 0, (12 * density).toInt())
        })

        rootLayout.addView(TextView(context).apply {
            text = "Hapus Riwayat?"
            textSize = 18f
            setTextColor(Color.WHITE)
            typeface = Typeface.create("sans-serif-medium", Typeface.NORMAL)
            gravity = Gravity.CENTER
            setPadding(0, 0, 0, (8 * density).toInt())
        })

        rootLayout.addView(TextView(context).apply {
            text = "Semua riwayat tontonan akan dihapus."
            textSize = 13f
            setTextColor(Color.parseColor("#88FFFFFF"))
            gravity = Gravity.CENTER
            setPadding(0, 0, 0, (20 * density).toInt())
        })

        // Buttons row
        val buttonRow = LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        }

        // Cancel button
        buttonRow.addView(TextView(context).apply {
            text = "Batal"
            textSize = 14f
            setTextColor(Color.WHITE)
            gravity = Gravity.CENTER
            setPadding(
                (24 * density).toInt(),
                (12 * density).toInt(),
                (24 * density).toInt(),
                (12 * density).toInt()
            )
            background = GradientDrawable().apply {
                cornerRadius = 12f
                setColor(Color.parseColor("#33FFFFFF"))
            }
            layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f).apply {
                marginEnd = (8 * density).toInt()
            }
            setOnClickListener { dialog.dismiss() }
            isFocusable = true
        })

        // Delete button
        buttonRow.addView(TextView(context).apply {
            text = "Hapus"
            textSize = 14f
            setTextColor(Color.WHITE)
            gravity = Gravity.CENTER
            setPadding(
                (24 * density).toInt(),
                (12 * density).toInt(),
                (24 * density).toInt(),
                (12 * density).toInt()
            )
            background = GradientDrawable().apply {
                cornerRadius = 12f
                setColor(Color.parseColor("#CC3333"))
            }
            layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f).apply {
                marginStart = (8 * density).toInt()
            }
            setOnClickListener {
                dialog.dismiss()
                historyManager.clearAll()
                Toast.makeText(context, "Riwayat dihapus", Toast.LENGTH_SHORT).show()
            }
            isFocusable = true
        })

        rootLayout.addView(buttonRow)

        dialog.setContentView(rootLayout)
        dialog.show()
    }

    private fun createDivider(density: Float): View {
        return View(context).apply {
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                1
            ).apply {
                topMargin = (12 * density).toInt()
                bottomMargin = (12 * density).toInt()
            }
            setBackgroundColor(Color.parseColor("#22FFFFFF"))
        }
    }
}
