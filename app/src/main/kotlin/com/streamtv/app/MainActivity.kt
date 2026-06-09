package com.streamtv.app

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.content.SharedPreferences
import android.content.pm.ActivityInfo
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.GestureDetector
import android.view.Gravity
import android.view.KeyEvent
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.view.inputmethod.EditorInfo
import android.webkit.*
import android.widget.*
import com.streamtv.app.data.BookmarkManager
import com.streamtv.app.data.HistoryManager
import com.streamtv.app.data.SiteManager
import com.streamtv.app.ui.OverlayMenu

class MainActivity : Activity() {

    private lateinit var webView: WebView
    private lateinit var progressBar: ProgressBar
    private lateinit var fullscreenContainer: FrameLayout
    private lateinit var bookmarkManager: BookmarkManager
    private lateinit var historyManager: HistoryManager
    private lateinit var overlayMenu: OverlayMenu
    private lateinit var prefs: SharedPreferences
    private lateinit var gestureDetector: GestureDetector

    private var customView: View? = null
    private var customViewCallback: WebChromeClient.CustomViewCallback? = null
    private var currentTitle: String = ""
    private var currentUrl: String = ""
    private var menuButton: FrameLayout? = null
    private var isMenuButtonVisible = true
    private val hideHandler = Handler(Looper.getMainLooper())
    private var backPressedOnce = false

    companion object {
        private val HOME_URL = SiteManager.sites[0].url

        private val AD_BLOCK_LIST = listOf(
            "doubleclick.net", "googlesyndication.com", "adservice.google",
            "popads.net", "popcash.net", "propellerads.com", "adnxs.com",
            "exoclick.com", "juicyads.com", "trafficjunky.com", "ad.plus",
            "adsterra.com", "hilltopads.net", "onclickmax.com", "notifpush.com",
            "clickadu.com", "a-ads.com", "ad-maven.com", "admaven.com",
            "bidvertiser.com", "revcontent.com", "mgid.com", "taboola.com",
            "outbrain.com", "zedo.com", "yllix.com", "clicksor.com",
            "pushnotifications", "push-notifications", "onesignal.com",
            "popunder", "popmyads", "poperblocker", "adf.ly",
            "shorte.st", "sh.st", "bc.vc", "linkshrink"
        )
    }

    @SuppressLint("SetJavaScriptEnabled", "ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        prefs = getSharedPreferences("pixeltv_prefs", MODE_PRIVATE)

        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )
        window.decorView.systemUiVisibility = (
            View.SYSTEM_UI_FLAG_FULLSCREEN
                or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
        )
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        // Initialize managers
        bookmarkManager = BookmarkManager(this)
        historyManager = HistoryManager(this)
        overlayMenu = OverlayMenu(
            context = this,
            bookmarkManager = bookmarkManager,
            historyManager = historyManager,
            onNavigate = { url -> webView.loadUrl(url) },
            onBookmarkCurrent = { bookmarkCurrentPage() }
        )

        // Gesture detector for swipe — high threshold to avoid accidental triggers
        gestureDetector = GestureDetector(this, object : GestureDetector.SimpleOnGestureListener() {
            // Threshold dinaikkan jauh: butuh swipe panjang & cepat
            private val SWIPE_THRESHOLD = 250        // jarak minimal (px) — sebelumnya 100
            private val SWIPE_VELOCITY = 1200        // kecepatan minimal — sebelumnya 100
            private val DIRECTION_RATIO = 2.5f       // arah harus dominan (horizontal vs vertical)

            override fun onFling(
                e1: MotionEvent?,
                e2: MotionEvent,
                velocityX: Float,
                velocityY: Float
            ): Boolean {
                if (e1 == null) return false

                val diffX = e2.x - e1.x
                val diffY = e2.y - e1.y
                val absX = Math.abs(diffX)
                val absY = Math.abs(diffY)

                // Horizontal swipe — harus benar-benar dominan horizontal
                if (absX > absY * DIRECTION_RATIO) {
                    if (absX > SWIPE_THRESHOLD && Math.abs(velocityX) > SWIPE_VELOCITY) {
                        if (diffX > 0) {
                            // Swipe right — go back
                            if (webView.canGoBack()) webView.goBack()
                        } else {
                            // Swipe left — switch site
                            switchSite(1)
                        }
                        return true
                    }
                }
                // Swipe down — harus benar-benar dominan vertikal & dari atas
                else if (absY > absX * DIRECTION_RATIO) {
                    if (diffY > SWIPE_THRESHOLD && Math.abs(velocityY) > SWIPE_VELOCITY) {
                        // Hanya refresh kalau swipe dimulai dari area atas (pull-to-refresh)
                        if (e1.y < webView.height * 0.25f) {
                            webView.reload()
                            Toast.makeText(this@MainActivity, "🔄 Refresh", Toast.LENGTH_SHORT).show()
                            return true
                        }
                    }
                }
                return false
            }
        })

        // Setup layout
        val rootLayout = FrameLayout(this).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            setBackgroundColor(Color.BLACK)
        }

        fullscreenContainer = FrameLayout(this).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            visibility = View.GONE
            setBackgroundColor(Color.BLACK)
        }

        progressBar = ProgressBar(this, null, android.R.attr.progressBarStyleHorizontal).apply {
            layoutParams = FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, 6
            )
            isIndeterminate = false
            max = 100
        }

        webView = WebView(this).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            isFocusable = true
            isFocusableInTouchMode = true
            requestFocus()
        }

        setupWebView()

        // Intercept touch for gestures (but still pass to WebView)
        webView.setOnTouchListener { _, event ->
            gestureDetector.onTouchEvent(event)
            false // let WebView handle it too
        }

        rootLayout.addView(webView)
        rootLayout.addView(progressBar)
        rootLayout.addView(fullscreenContainer)

        menuButton = createDraggableMenuButton()
        rootLayout.addView(menuButton)

        setContentView(rootLayout)

        rootLayout.post { restoreButtonPosition() }

        scheduleHideMenuButton()

        // Load URL from intent or default
        val url = intent.getStringExtra("url") ?: HOME_URL
        webView.loadUrl(url)
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun createDraggableMenuButton(): FrameLayout {
        val density = resources.displayMetrics.density
        val sizePx = (48 * density).toInt()

        val container = FrameLayout(this).apply {
            layoutParams = FrameLayout.LayoutParams(sizePx, sizePx)
            alpha = 0.85f
            elevation = 998f
            isFocusable = false
        }

        val bg = GradientDrawable().apply {
            shape = GradientDrawable.OVAL
            colors = intArrayOf(
                Color.parseColor("#E50914"),
                Color.parseColor("#B00710")
            )
            gradientType = GradientDrawable.LINEAR_GRADIENT
        }
        container.background = bg

        val icon = TextView(this).apply {
            layoutParams = FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            text = "▶"
            textSize = 18f
            setTextColor(Color.WHITE)
            gravity = Gravity.CENTER
            isFocusable = false
        }
        container.addView(icon)

        var dX = 0f
        var dY = 0f
        var startX = 0f
        var startY = 0f
        var isDragging = false

        container.setOnTouchListener { view, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    dX = view.x - event.rawX
                    dY = view.y - event.rawY
                    startX = event.rawX
                    startY = event.rawY
                    isDragging = false
                    scheduleHideMenuButton()
                    true
                }
                MotionEvent.ACTION_MOVE -> {
                    if (Math.abs(event.rawX - startX) > 10 || Math.abs(event.rawY - startY) > 10) {
                        isDragging = true
                    }
                    if (isDragging) {
                        view.x = (event.rawX + dX).coerceIn(0f, (webView.width - view.width).toFloat())
                        view.y = (event.rawY + dY).coerceIn(0f, (webView.height - view.height).toFloat())
                    }
                    true
                }
                MotionEvent.ACTION_UP -> {
                    if (!isDragging) {
                        overlayMenu.showMainMenu()
                    } else {
                        saveButtonPosition(view.x, view.y)
                    }
                    scheduleHideMenuButton()
                    true
                }
                else -> false
            }
        }

        container.setOnLongClickListener {
            bookmarkCurrentPage()
            true
        }

        return container
    }

    private fun saveButtonPosition(x: Float, y: Float) {
        prefs.edit().putFloat("btn_x", x).putFloat("btn_y", y).apply()
    }

    private fun restoreButtonPosition() {
        val density = resources.displayMetrics.density
        menuButton?.x = prefs.getFloat("btn_x", 16 * density)
        menuButton?.y = prefs.getFloat("btn_y", 16 * density)
    }

    private fun scheduleHideMenuButton() {
        hideHandler.removeCallbacksAndMessages(null)
        menuButton?.animate()?.alpha(0.85f)?.setDuration(200)?.start()
        isMenuButtonVisible = true
        hideHandler.postDelayed({
            menuButton?.animate()?.alpha(0.2f)?.setDuration(600)?.start()
            isMenuButtonVisible = false
        }, 5000)
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun setupWebView() {
        webView.settings.apply {
            javaScriptEnabled = true
            domStorageEnabled = true
            databaseEnabled = true
            mediaPlaybackRequiresUserGesture = false
            useWideViewPort = true
            loadWithOverviewMode = true
            setSupportZoom(true)
            builtInZoomControls = true
            displayZoomControls = false
            allowFileAccess = true
            allowContentAccess = true
            mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
            cacheMode = WebSettings.LOAD_DEFAULT
            setSupportMultipleWindows(false)
            javaScriptCanOpenWindowsAutomatically = false
            userAgentString = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36"
        }

        webView.setLayerType(View.LAYER_TYPE_HARDWARE, null)

        webView.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
                val url = request?.url?.toString() ?: return false
                if (isAdUrl(url)) return true
                return false
            }

            override fun shouldInterceptRequest(view: WebView?, request: WebResourceRequest?): WebResourceResponse? {
                val url = request?.url?.toString() ?: return null
                if (isAdUrl(url)) return WebResourceResponse("text/plain", "UTF-8", "".byteInputStream())
                return null
            }

            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                super.onPageStarted(view, url, favicon)
                progressBar.visibility = View.VISIBLE
                currentUrl = url ?: ""
            }

            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                progressBar.visibility = View.GONE
                currentTitle = view?.title ?: url ?: "Untitled"
                currentUrl = url ?: ""
                if (!url.isNullOrBlank() && !isAdUrl(url)) {
                    historyManager.addToHistory(currentTitle, currentUrl)
                }
                injectHelpers()
            }

            override fun onReceivedError(view: WebView?, request: WebResourceRequest?, error: WebResourceError?) {
                if (request?.isForMainFrame == true) {
                    showSnackbar("Gagal memuat halaman. Periksa koneksi internet.", "Retry") {
                        webView.reload()
                    }
                }
            }
        }

        webView.webChromeClient = object : WebChromeClient() {
            override fun onProgressChanged(view: WebView?, newProgress: Int) {
                progressBar.progress = newProgress
                if (newProgress == 100) progressBar.visibility = View.GONE
            }

            override fun onCreateWindow(view: WebView?, isDialog: Boolean, isUserGesture: Boolean, resultMsg: android.os.Message?): Boolean = false

            override fun onShowCustomView(view: View?, callback: CustomViewCallback?) {
                if (customView != null) { callback?.onCustomViewHidden(); return }
                customView = view
                customViewCallback = callback
                webView.visibility = View.GONE
                menuButton?.visibility = View.GONE
                fullscreenContainer.visibility = View.VISIBLE
                fullscreenContainer.addView(view)
                requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
            }

            override fun onHideCustomView() {
                if (customView == null) return
                fullscreenContainer.removeView(customView)
                fullscreenContainer.visibility = View.GONE
                webView.visibility = View.VISIBLE
                menuButton?.visibility = View.VISIBLE
                customView = null
                customViewCallback?.onCustomViewHidden()
                customViewCallback = null
            }
        }
    }

    private fun injectHelpers() {
        // Load TV navigation JS from assets
        try {
            val inputStream = assets.open("tv_nav.js")
            val js = inputStream.bufferedReader().use { it.readText() }
            webView.evaluateJavascript(js, null)
        } catch (e: Exception) {
            // Fallback minimal injection
            val fallbackJs = """
                (function() {
                    window.open = function() { return null; };
                    document.querySelectorAll('meta[http-equiv="refresh"]').forEach(function(m) { m.remove(); });
                })();
            """.trimIndent()
            webView.evaluateJavascript(fallbackJs, null)
        }
    }

    private fun isAdUrl(url: String): Boolean {
        val lowerUrl = url.lowercase()
        return AD_BLOCK_LIST.any { lowerUrl.contains(it) }
    }

    private fun bookmarkCurrentPage() {
        if (currentUrl.isBlank()) {
            Toast.makeText(this, "Tidak ada halaman untuk di-bookmark", Toast.LENGTH_SHORT).show()
            return
        }
        if (bookmarkManager.isBookmarked(currentUrl)) {
            bookmarkManager.removeBookmark(currentUrl)
            Toast.makeText(this, "⭐ Bookmark dihapus", Toast.LENGTH_SHORT).show()
        } else {
            bookmarkManager.addBookmark(currentTitle, currentUrl)
            Toast.makeText(this, "⭐ Bookmark disimpan", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * Snackbar-style notification with action button
     */
    private fun showSnackbar(message: String, actionText: String, action: () -> Unit) {
        val density = resources.displayMetrics.density
        val root = window.decorView.findViewById<FrameLayout>(android.R.id.content)

        val snackbar = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            background = GradientDrawable().apply {
                cornerRadius = 12f
                setColor(Color.parseColor("#DD222222"))
            }
            setPadding((16 * density).toInt(), (12 * density).toInt(), (12 * density).toInt(), (12 * density).toInt())
            layoutParams = FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply {
                gravity = Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL
                bottomMargin = (32 * density).toInt()
            }
            elevation = 8f
        }

        snackbar.addView(TextView(this).apply {
            text = message
            textSize = 13f
            setTextColor(Color.WHITE)
            layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f)
        })

        snackbar.addView(TextView(this).apply {
            text = actionText
            textSize = 13f
            setTextColor(Color.parseColor("#E50914"))
            typeface = Typeface.DEFAULT_BOLD
            setPadding((16 * density).toInt(), 0, 0, 0)
            setOnClickListener {
                action()
                root.removeView(snackbar)
            }
        })

        root.addView(snackbar)

        // Auto dismiss after 5s
        Handler(Looper.getMainLooper()).postDelayed({
            root.removeView(snackbar)
        }, 5000)
    }

    // ==================== KEY HANDLING ====================

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        when (keyCode) {
            KeyEvent.KEYCODE_BACK -> {
                if (customView != null) {
                    webView.webChromeClient?.onHideCustomView()
                    return true
                }
                if (webView.canGoBack()) {
                    webView.goBack()
                    return true
                }
                showExitDialog()
                return true
            }

            // D-pad navigation → forward to JS navigation layer
            KeyEvent.KEYCODE_DPAD_UP -> {
                webView.evaluateJavascript("window.__pixeltv_navigate && window.__pixeltv_navigate('up');", null)
                return true
            }
            KeyEvent.KEYCODE_DPAD_DOWN -> {
                webView.evaluateJavascript("window.__pixeltv_navigate && window.__pixeltv_navigate('down');", null)
                return true
            }
            KeyEvent.KEYCODE_DPAD_LEFT -> {
                webView.evaluateJavascript("window.__pixeltv_navigate && window.__pixeltv_navigate('left');", null)
                return true
            }
            KeyEvent.KEYCODE_DPAD_RIGHT -> {
                webView.evaluateJavascript("window.__pixeltv_navigate && window.__pixeltv_navigate('right');", null)
                return true
            }

            // OK/Enter → click focused element
            KeyEvent.KEYCODE_DPAD_CENTER, KeyEvent.KEYCODE_ENTER -> {
                webView.evaluateJavascript("window.__pixeltv_click && window.__pixeltv_click();", null)
                return true
            }

            KeyEvent.KEYCODE_MENU, KeyEvent.KEYCODE_BOOKMARK -> {
                overlayMenu.showMainMenu()
                return true
            }

            KeyEvent.KEYCODE_INFO, KeyEvent.KEYCODE_GUIDE -> {
                bookmarkCurrentPage()
                return true
            }

            KeyEvent.KEYCODE_CHANNEL_UP -> { switchSite(1); return true }
            KeyEvent.KEYCODE_CHANNEL_DOWN -> { switchSite(-1); return true }

            KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE -> { toggleVideo(); return true }

            KeyEvent.KEYCODE_SEARCH -> { showSearchDialog(); return true }
        }
        return super.onKeyDown(keyCode, event)
    }

    private fun showExitDialog() {
        val density = resources.displayMetrics.density

        AlertDialog.Builder(this, android.R.style.Theme_DeviceDefault_Dialog)
            .setTitle("Keluar dari PIXELTV?")
            .setMessage("Yakin mau keluar dari aplikasi?")
            .setPositiveButton("Keluar") { _, _ -> finish() }
            .setNegativeButton("Batal", null)
            .setNeutralButton("🏠 Home") { _, _ ->
                startActivity(android.content.Intent(this, HomeActivity::class.java))
                finish()
            }
            .show()
    }

    fun showSearchDialog() {
        val density = resources.displayMetrics.density

        val input = EditText(this).apply {
            hint = "Cari judul film..."
            setTextColor(Color.WHITE)
            setHintTextColor(Color.parseColor("#66FFFFFF"))
            setPadding((16 * density).toInt(), (12 * density).toInt(), (16 * density).toInt(), (12 * density).toInt())
            setBackgroundColor(Color.parseColor("#22FFFFFF"))
            imeOptions = EditorInfo.IME_ACTION_SEARCH
            inputType = android.text.InputType.TYPE_CLASS_TEXT
        }

        AlertDialog.Builder(this, android.R.style.Theme_DeviceDefault_Dialog)
            .setTitle("🔍 Cari Film")
            .setView(input)
            .setPositiveButton("Cari") { _, _ ->
                val query = input.text.toString().trim()
                if (query.isNotEmpty()) {
                    // Search on current site
                    val searchUrl = when {
                        currentUrl.contains("idlix") -> "https://z1.idlixku.com/?s=$query"
                        currentUrl.contains("lk21") -> "https://tv10.lk21official.cc/?s=$query"
                        currentUrl.contains("rebahin") -> "https://rebahinxxi3.beauty/?s=$query"
                        else -> "https://z1.idlixku.com/?s=$query"
                    }
                    webView.loadUrl(searchUrl)
                }
            }
            .setNegativeButton("Batal", null)
            .show()
    }

    private fun toggleVideo() {
        webView.evaluateJavascript(
            "(function(){var v=document.querySelector('video');if(v){if(v.paused)v.play();else v.pause();}})();",
            null
        )
    }

    private var currentSiteIndex = 0

    private fun switchSite(direction: Int) {
        val sites = SiteManager.sites
        currentSiteIndex = (currentSiteIndex + direction + sites.size) % sites.size
        val site = sites[currentSiteIndex]
        webView.loadUrl(site.url)
        Toast.makeText(this, "${site.icon} ${site.name}", Toast.LENGTH_SHORT).show()
    }

    override fun onResume() {
        super.onResume()
        webView.onResume()
        webView.requestFocus()
        window.decorView.systemUiVisibility = (
            View.SYSTEM_UI_FLAG_FULLSCREEN
                or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
        )
    }

    override fun onPause() {
        super.onPause()
        webView.onPause()
    }

    override fun onDestroy() {
        hideHandler.removeCallbacksAndMessages(null)
        webView.destroy()
        super.onDestroy()
    }
}
