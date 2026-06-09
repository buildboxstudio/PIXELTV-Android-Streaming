package com.streamtv.app

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.*
import com.streamtv.app.data.HistoryManager
import com.streamtv.app.data.SiteManager

/**
 * Home screen — user picks a site or continues watching
 */
class HomeActivity : Activity() {

    private lateinit var historyManager: HistoryManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )
        window.decorView.systemUiVisibility = (
            View.SYSTEM_UI_FLAG_FULLSCREEN
                or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
        )

        historyManager = HistoryManager(this)

        val density = resources.displayMetrics.density

        val scrollView = ScrollView(this).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            setBackgroundColor(Color.parseColor("#0A0A0A"))
        }

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(
                (32 * density).toInt(),
                (40 * density).toInt(),
                (32 * density).toInt(),
                (32 * density).toInt()
            )
        }

        // Header
        root.addView(TextView(this).apply {
            text = "PIXELTV"
            textSize = 28f
            setTextColor(Color.WHITE)
            typeface = Typeface.create("sans-serif-black", Typeface.BOLD)
            setPadding(0, 0, 0, (4 * density).toInt())
        })

        root.addView(TextView(this).apply {
            text = "Pilih situs untuk mulai streaming"
            textSize = 14f
            setTextColor(Color.parseColor("#B3B3B3"))
            setPadding(0, 0, 0, (28 * density).toInt())
        })

        // Red accent bar under header
        root.addView(View(this).apply {
            layoutParams = LinearLayout.LayoutParams((48 * density).toInt(), (3 * density).toInt()).apply {
                bottomMargin = (24 * density).toInt()
            }
            setBackgroundColor(Color.parseColor("#E50914"))
        })

        // Site cards
        root.addView(TextView(this).apply {
            text = "🌐  Situs Streaming"
            textSize = 16f
            setTextColor(Color.WHITE)
            typeface = Typeface.create("sans-serif-medium", Typeface.NORMAL)
            setPadding(0, 0, 0, (12 * density).toInt())
        })

        val siteGrid = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            setPadding(0, 0, 0, (28 * density).toInt())
        }

        SiteManager.sites.forEach { site ->
            siteGrid.addView(createSiteCard(site, density))
        }

        root.addView(siteGrid)

        // Continue watching section
        val recentHistory = historyManager.getRecentHistory(5)
        if (recentHistory.isNotEmpty()) {
            root.addView(TextView(this).apply {
                text = "📋  Terakhir Ditonton"
                textSize = 16f
                setTextColor(Color.WHITE)
                typeface = Typeface.create("sans-serif-medium", Typeface.NORMAL)
                setPadding(0, 0, 0, (12 * density).toInt())
            })

            recentHistory.forEach { item ->
                root.addView(createHistoryItem(item, density))
            }
        }

        // Tips section
        root.addView(View(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, 1
            ).apply { topMargin = (20 * density).toInt(); bottomMargin = (20 * density).toInt() }
            setBackgroundColor(Color.parseColor("#222222"))
        })

        root.addView(TextView(this).apply {
            text = "💡 Tips: Gunakan tombol Menu di remote untuk akses cepat ke bookmark dan ganti situs"
            textSize = 12f
            setTextColor(Color.parseColor("#666666"))
            setPadding(0, 0, 0, (8 * density).toInt())
        })

        root.addView(TextView(this).apply {
            text = "v2.2.0 • Build by Buildbox Studio"
            textSize = 11f
            setTextColor(Color.parseColor("#444444"))
            setPadding(0, (16 * density).toInt(), 0, 0)
        })

        scrollView.addView(root)
        setContentView(scrollView)
    }

    private fun createSiteCard(site: SiteManager.StreamingSite, density: Float): FrameLayout {
        val card = FrameLayout(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                0, (120 * density).toInt(), 1f
            ).apply {
                marginEnd = (12 * density).toInt()
            }
            background = GradientDrawable().apply {
                cornerRadius = 16f
                colors = intArrayOf(
                    Color.parseColor("#1E1E1E"),
                    Color.parseColor("#141414")
                )
                gradientType = GradientDrawable.LINEAR_GRADIENT
                setStroke(1, Color.parseColor("#2A2A2A"))
            }
            setPadding(
                (16 * density).toInt(),
                (16 * density).toInt(),
                (16 * density).toInt(),
                (16 * density).toInt()
            )
            isFocusable = true
            isClickable = true

            setOnFocusChangeListener { v, hasFocus ->
                val bg = v.background as? GradientDrawable
                if (hasFocus) {
                    bg?.setStroke((2 * density).toInt(), Color.parseColor("#E50914"))
                    v.scaleX = 1.05f
                    v.scaleY = 1.05f
                } else {
                    bg?.setStroke(1, Color.parseColor("#2A2A2A"))
                    v.scaleX = 1f
                    v.scaleY = 1f
                }
            }

            setOnClickListener {
                openSite(site.url)
            }
        }

        val content = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            layoutParams = FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
        }

        content.addView(TextView(this).apply {
            text = site.icon
            textSize = 32f
            gravity = Gravity.CENTER
            setPadding(0, 0, 0, (8 * density).toInt())
        })

        content.addView(TextView(this).apply {
            text = site.name
            textSize = 14f
            setTextColor(Color.WHITE)
            gravity = Gravity.CENTER
            typeface = Typeface.create("sans-serif-medium", Typeface.NORMAL)
        })

        card.addView(content)
        return card
    }

    private fun createHistoryItem(item: HistoryManager.HistoryItem, density: Float): LinearLayout {
        return LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(
                (16 * density).toInt(),
                (12 * density).toInt(),
                (16 * density).toInt(),
                (12 * density).toInt()
            )
            background = GradientDrawable().apply {
                cornerRadius = 12f
                setColor(Color.parseColor("#1A1A1A"))
            }
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply { bottomMargin = (8 * density).toInt() }

            isFocusable = true
            isClickable = true

            setOnFocusChangeListener { v, hasFocus ->
                val bg = v.background as? GradientDrawable
                if (hasFocus) {
                    bg?.setColor(Color.parseColor("#2A1416"))
                    bg?.setStroke((2 * density).toInt(), Color.parseColor("#E50914"))
                } else {
                    bg?.setColor(Color.parseColor("#1A1A1A"))
                    bg?.setStroke(0, Color.TRANSPARENT)
                }
            }

            setOnClickListener { openSite(item.url) }

            addView(TextView(this@HomeActivity).apply {
                text = "📄"
                textSize = 18f
                setPadding(0, 0, (12 * density).toInt(), 0)
            })

            val title = if (item.title.length > 40) item.title.take(40) + "…" else item.title
            addView(TextView(this@HomeActivity).apply {
                text = title
                textSize = 13f
                setTextColor(Color.WHITE)
                layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f)
            })

            addView(TextView(this@HomeActivity).apply {
                text = "›"
                textSize = 18f
                setTextColor(Color.parseColor("#888888"))
            })
        }
    }

    private fun openSite(url: String) {
        val intent = Intent(this, MainActivity::class.java)
        intent.putExtra("url", url)
        startActivity(intent)
    }
}
