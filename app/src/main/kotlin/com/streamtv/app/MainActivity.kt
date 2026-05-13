package com.streamtv.app

import android.annotation.SuppressLint
import android.app.Activity
import android.content.SharedPreferences
import android.content.pm.ActivityInfo
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.Gravity
import android.view.KeyEvent
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.webkit.*
import android.widget.FrameLayout
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
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

    private var customView: View? = null
    private var customViewCallback: WebChromeClient.CustomViewCallback? = null
    private var currentTitle: String = ""
    private var currentUrl: String = ""
    private var menuButton: FrameLayout? = null
    private var isMenuButtonVisible = true
    private val hideHandler = Handler(Looper.getMainLooper())

    companion object {
        private val HOME_URL = SiteManager.sites[0].url

        private val AD_BLOCK_LIST = listOf(
            "doubleclick.net",
            "googlesyndication.com",
            "adservice.google",
            "popads.net",
            "popcash.net",
            "propellerads.com",
            "adnxs.com",
            "exoclick.com",
            "juicyads.com",
            "trafficjunky.com",
            "ad.plus",
            "adsterra.com",
            "hilltopads.net",
            "onclickmax.com",
            "notifpush.com",
            "clickadu.com",
            "a-ads.com",
            "ad-maven.com",
            "admaven.com",
            "bidvertiser.com",
            "revcontent.com",
            "mgid.com",
            "taboola.com",
            "outbrain.com",
            "zedo.com",
            "yllix.com",
            "clicksor.com",
            "pushnotifications",
            "push-notifications",
            "onesignal.com",
            "popunder",
            "popmyads",
            "poperblocker",
            "adf.ly",
            "shorte.st",
            "sh.st",
            "bc.vc",
            "linkshrink"
        )
    }

    @SuppressLint("SetJavaScriptEnabled", "ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        prefs = getSharedPreferences("pixeltv_prefs", MODE_PRIVATE)

        // Fullscreen immersive
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
                ViewGroup.LayoutParams.MATCH_PARENT,
                6
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

        rootLayout.addView(webView)
        rootLayout.addView(progressBar)
        rootLayout.addView(fullscreenContainer)

        menuButton = createDraggableMenuButton()
        rootLayout.addView(menuButton)

        setContentView(rootLayout)

        // Restore saved button position
        rootLayout.post {
            restoreButtonPosition()
        }

        scheduleHideMenuButton()
        webView.loadUrl(HOME_URL)
    }

    /**
     * Draggable floating menu button — user bisa geser ke mana aja
     */
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

        // Gradient background
        val bg = GradientDrawable().apply {
            shape = GradientDrawable.OVAL
            colors = intArrayOf(
                Color.parseColor("#7C4DFF"),
                Color.parseColor("#6C3FC7")
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

        // Drag logic
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
                    val moveX = Math.abs(event.rawX - startX)
                    val moveY = Math.abs(event.rawY - startY)
                    if (moveX > 10 || moveY > 10) {
                        isDragging = true
                    }
                    if (isDragging) {
                        val newX = (event.rawX + dX).coerceIn(0f, (webView.width - view.width).toFloat())
                        val newY = (event.rawY + dY).coerceIn(0f, (webView.height - view.height).toFloat())
                        view.x = newX
                        view.y = newY
                    }
                    true
                }
                MotionEvent.ACTION_UP -> {
                    if (!isDragging) {
                        // Tap — open menu
                        overlayMenu.showMainMenu()
                    } else {
                        // Save position after drag
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
        prefs.edit()
            .putFloat("btn_x", x)
            .putFloat("btn_y", y)
            .apply()
    }

    private fun restoreButtonPosition() {
        val density = resources.displayMetrics.density
        val defaultX = 16 * density
        val defaultY = 16 * density

        val x = prefs.getFloat("btn_x", defaultX)
        val y = prefs.getFloat("btn_y", defaultY)

        menuButton?.x = x
        menuButton?.y = y
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

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        if (event?.action == MotionEvent.ACTION_DOWN) {
            if (!isMenuButtonVisible) scheduleHideMenuButton()
        }
        return super.onTouchEvent(event)
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

            override fun shouldOverrideUrlLoading(
                view: WebView?,
                request: WebResourceRequest?
            ): Boolean {
                val url = request?.url?.toString() ?: return false
                if (isAdUrl(url)) return true
                return false
            }

            override fun shouldInterceptRequest(
                view: WebView?,
                request: WebResourceRequest?
            ): WebResourceResponse? {
                val url = request?.url?.toString() ?: return null
                if (isAdUrl(url)) {
                    return WebResourceResponse("text/plain", "UTF-8", "".byteInputStream())
                }
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

            override fun onReceivedError(
                view: WebView?,
                request: WebResourceRequest?,
                error: WebResourceError?
            ) {
                if (request?.isForMainFrame == true) {
                    Toast.makeText(
                        this@MainActivity,
                        "Gagal memuat halaman. Periksa koneksi internet.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }

        webView.webChromeClient = object : WebChromeClient() {

            override fun onProgressChanged(view: WebView?, newProgress: Int) {
                progressBar.progress = newProgress
                if (newProgress == 100) progressBar.visibility = View.GONE
            }

            override fun onCreateWindow(
                view: WebView?,
                isDialog: Boolean,
                isUserGesture: Boolean,
                resultMsg: android.os.Message?
            ): Boolean = false

            override fun onShowCustomView(view: View?, callback: CustomViewCallback?) {
                if (customView != null) {
                    callback?.onCustomViewHidden()
                    return
                }

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
        val js = """
            (function() {
                var adSelectors = [
                    '[class*="ads"]', '[id*="ads"]',
                    '[class*="popup"]', '[id*="popup"]',
                    'iframe[src*="ad"]', '.adsbygoogle',
                    '[class*="iklan"]', '[id*="iklan"]'
                ];
                adSelectors.forEach(function(sel) {
                    try {
                        document.querySelectorAll(sel).forEach(function(el) {
                            if (!el.querySelector('video') && !el.closest('.player') && !el.closest('[class*="play"]')) {
                                el.style.display = 'none';
                            }
                        });
                    } catch(e) {}
                });

                window.open = function() { return null; };

                document.querySelectorAll('meta[http-equiv="refresh"]').forEach(function(m) { m.remove(); });

                try {
                    Object.defineProperty(window.location, 'reload', {
                        value: function() {},
                        writable: false,
                        configurable: false
                    });
                } catch(e) {}

                var style = document.getElementById('pixeltv-css');
                if (!style) {
                    style = document.createElement('style');
                    style.id = 'pixeltv-css';
                    style.innerHTML = `
                        a:focus, button:focus, [onclick]:focus, [role="button"]:focus,
                        input:focus, [tabindex]:focus {
                            outline: 3px solid #FFD700 !important;
                            outline-offset: 2px !important;
                            box-shadow: 0 0 12px rgba(255, 215, 0, 0.6) !important;
                        }
                    `;
                    document.head.appendChild(style);
                }

                document.querySelectorAll('a, button, [onclick], [role="button"]').forEach(function(el) {
                    if (!el.getAttribute('tabindex')) {
                        el.setAttribute('tabindex', '0');
                    }
                });
            })();
        """.trimIndent()
        webView.evaluateJavascript(js, null)
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
            }

            KeyEvent.KEYCODE_MENU, KeyEvent.KEYCODE_BOOKMARK -> {
                overlayMenu.showMainMenu()
                return true
            }

            KeyEvent.KEYCODE_INFO, KeyEvent.KEYCODE_GUIDE -> {
                bookmarkCurrentPage()
                return true
            }

            KeyEvent.KEYCODE_CHANNEL_UP -> {
                switchSite(1)
                return true
            }
            KeyEvent.KEYCODE_CHANNEL_DOWN -> {
                switchSite(-1)
                return true
            }

            KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE -> {
                toggleVideo()
                return true
            }
        }
        return super.onKeyDown(keyCode, event)
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
