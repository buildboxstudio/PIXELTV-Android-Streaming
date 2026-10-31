package com.idlix.tv.ui.screens.player

import android.app.Activity
import android.content.Context
import android.net.Uri
import android.os.Looper
import android.view.KeyEvent
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.annotation.OptIn
import androidx.compose.foundation.background
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.MimeTypes
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DefaultDataSource
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.exoplayer.DefaultRenderersFactory
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.Renderer
import androidx.media3.exoplayer.hls.HlsMediaSource
import androidx.media3.exoplayer.source.MergingMediaSource
import androidx.media3.exoplayer.source.SingleSampleMediaSource
import androidx.media3.exoplayer.text.TextOutput
import androidx.media3.exoplayer.text.TextRenderer
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import androidx.tv.material3.Button
import androidx.tv.material3.ButtonDefaults
import androidx.tv.material3.ExperimentalTvMaterial3Api
import androidx.tv.material3.Text
import com.idlix.tv.R
import com.idlix.tv.ui.components.TvLoadingSpinner
import com.idlix.tv.ui.theme.NetflixBlack
import com.idlix.tv.ui.theme.NetflixLightGray
import com.idlix.tv.ui.theme.NetflixRed
import com.idlix.tv.ui.theme.NetflixWhite
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.ArrayList

@OptIn(UnstableApi::class)
private class QueryPreservingDataSource(
    private val upstream: androidx.media3.datasource.DataSource,
    private val tokenQuery: String
) : androidx.media3.datasource.DataSource {

    override fun addTransferListener(transferListener: androidx.media3.datasource.TransferListener) {
        upstream.addTransferListener(transferListener)
    }

    override fun open(dataSpec: androidx.media3.datasource.DataSpec): Long {
        val uri = dataSpec.uri
        val uriStr = uri.toString()
        val finalUri = if (tokenQuery.isNotBlank() && !uriStr.contains("t=")) {
            val delimiter = if (uriStr.contains("?")) "&" else "?"
            Uri.parse("$uriStr$delimiter$tokenQuery")
        } else {
            uri
        }
        val finalSpec = if (finalUri != uri) {
            dataSpec.buildUpon().setUri(finalUri).build()
        } else {
            dataSpec
        }
        return upstream.open(finalSpec)
    }

    override fun read(buffer: ByteArray, offset: Int, length: Int): Int {
        return upstream.read(buffer, offset, length)
    }

    override fun getUri(): Uri? = upstream.uri

    override fun getResponseHeaders(): Map<String, List<String>> = upstream.responseHeaders

    override fun close() {
        upstream.close()
    }
}

@OptIn(UnstableApi::class)
private class QueryPreservingDataSourceFactory(
    private val upstreamFactory: androidx.media3.datasource.DataSource.Factory,
    private val tokenQuery: String
) : androidx.media3.datasource.DataSource.Factory {
    override fun createDataSource(): androidx.media3.datasource.DataSource {
        return QueryPreservingDataSource(upstreamFactory.createDataSource(), tokenQuery)
    }
}

@OptIn(UnstableApi::class)
private fun applyPlayerResizeMode(playerView: PlayerView, mode: Int) {
    playerView.resizeMode = mode
    val (targetScaleX, targetScaleY) = when (mode) {
        AspectRatioFrameLayout.RESIZE_MODE_ZOOM -> 1.333f to 1.333f
        AspectRatioFrameLayout.RESIZE_MODE_FILL -> 1.0f to 1.333f
        else -> 1.0f to 1.0f
    }
    playerView.videoSurfaceView?.let { surface ->
        val applyScale = {
            surface.pivotX = surface.width / 2f
            surface.pivotY = surface.height / 2f
            surface.scaleX = targetScaleX
            surface.scaleY = targetScaleY
        }
        if (surface.width > 0 && surface.height > 0) {
            applyScale()
        } else {
            surface.post(applyScale)
        }
    }
}

@OptIn(UnstableApi::class, ExperimentalTvMaterial3Api::class)
@Composable
fun PlayerScreen(
    contentType: String,
    contentId: String,
    title: String,
    posterUrl: String? = null,
    backdropUrl: String? = null,
    slug: String? = null,
    seasonNumber: Int? = null,
    episodeNumber: Int? = null,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: PlayerViewModel = viewModel()
) {
    val context = LocalContext.current
    val view = LocalView.current
    val uiState by viewModel.uiState.collectAsState()
    val scope = rememberCoroutineScope()

    var resizeMode by remember { mutableIntStateOf(AspectRatioFrameLayout.RESIZE_MODE_FIT) }
    val playerViewRef = remember { mutableStateOf<PlayerView?>(null) }

    DisposableEffect(Unit) {
        val window = (context as? Activity)?.window
        val controller = window?.let { WindowCompat.getInsetsController(it, view) }
        controller?.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        controller?.hide(WindowInsetsCompat.Type.systemBars())
        onDispose { controller?.show(WindowInsetsCompat.Type.systemBars()) }
    }

    var showControls by remember { mutableStateOf(true) }
    var currentPos by remember { mutableLongStateOf(0L) }
    var duration by remember { mutableLongStateOf(0L) }
    var isPlaying by remember { mutableStateOf(true) }
    var isScrubbing by remember { mutableStateOf(false) }
    var userInteractionCount by remember { mutableIntStateOf(0) }

    val remainingMs = if (duration > 0L) (duration - currentPos).coerceAtLeast(0L) else Long.MAX_VALUE
    val isNearEnd = duration > 120_000L && remainingMs in 1L..90_000L
    val isNextPromptVisible = isNearEnd && uiState.nextEpisode != null && !uiState.isNextEpisodeDismissed

    val rootFocusRequester = remember { FocusRequester() }
    val playButtonFocusRequester = remember { FocusRequester() }

    var activePlayer: ExoPlayer? by remember { mutableStateOf(null) }

    LaunchedEffect(contentType, contentId) {
        viewModel.loadStream(
            contentType = contentType,
            contentId = contentId,
            title = title,
            posterUrl = posterUrl,
            backdropUrl = backdropUrl,
            slug = slug,
            seasonNumber = seasonNumber,
            episodeNumber = episodeNumber
        )
    }

    LaunchedEffect(Unit) {
        rootFocusRequester.requestFocus()
    }

    // Auto-hide controls after 6 seconds of inactivity, paused during modal or scrubbing
    LaunchedEffect(showControls, uiState.isSubtitlePickerVisible, isScrubbing, userInteractionCount) {
        if (uiState.isSubtitlePickerVisible || isScrubbing) {
            // Keep controls open while user is selecting subtitles or scrubbing the seekbar
            return@LaunchedEffect
        }
        if (showControls) {
            delay(6000)
            showControls = false
        } else {
            try {
                rootFocusRequester.requestFocus()
            } catch (e: Exception) {}
        }
    }

    LaunchedEffect(showControls) {
        if (showControls) {
            delay(50)
            try {
                playButtonFocusRequester.requestFocus()
            } catch (e: Exception) {}
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(NetflixBlack)
            .focusRequester(rootFocusRequester)
            .focusable()
            .onKeyEvent { keyEvent ->
                if (keyEvent.nativeKeyEvent.action == KeyEvent.ACTION_DOWN) {
                    when (keyEvent.nativeKeyEvent.keyCode) {
                        KeyEvent.KEYCODE_BACK -> {
                            if (uiState.isSubtitlePickerVisible) {
                                viewModel.closeSubtitlePicker()
                                true
                            } else if (isNextPromptVisible) {
                                viewModel.dismissNextEpisode()
                                true
                            } else if (isScrubbing) {
                                isScrubbing = false
                                playButtonFocusRequester.requestFocus()
                                true
                            } else if (showControls) {
                                showControls = false
                                true
                            } else {
                                onBack()
                                true
                            }
                        }
                        KeyEvent.KEYCODE_DPAD_CENTER,
                        KeyEvent.KEYCODE_ENTER,
                        KeyEvent.KEYCODE_NUMPAD_ENTER -> {
                            if (!showControls) {
                                showControls = true
                                true
                            } else false
                        }
                        KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE,
                        KeyEvent.KEYCODE_MEDIA_PLAY,
                        KeyEvent.KEYCODE_MEDIA_PAUSE -> {
                            activePlayer?.let { p ->
                                if (p.isPlaying) p.pause() else p.play()
                            }
                            showControls = true
                            true
                        }
                        KeyEvent.KEYCODE_DPAD_LEFT -> {
                            if (!showControls) {
                                activePlayer?.let { p ->
                                    p.seekTo((p.currentPosition - 10000L).coerceAtLeast(0L))
                                }
                                showControls = true
                                true
                            } else false
                        }
                        KeyEvent.KEYCODE_DPAD_RIGHT -> {
                            if (!showControls) {
                                activePlayer?.let { p ->
                                    p.seekTo((p.currentPosition + 10000L).coerceAtMost(p.duration))
                                }
                                showControls = true
                                true
                            } else false
                        }
                        KeyEvent.KEYCODE_DPAD_UP,
                        KeyEvent.KEYCODE_DPAD_DOWN -> {
                            if (!showControls) {
                                showControls = true
                                true
                            } else false
                        }
                        else -> false
                    }
                } else false
            }
    ) {
        if (uiState.isLoading) {
            Column(
                modifier = Modifier.align(Alignment.Center),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                TvLoadingSpinner()
                Spacer(modifier = Modifier.height(16.dp))

                if (uiState.countdownSeconds > 0) {
                    Text(
                        text = stringResource(R.string.loading_secure_stream),
                        color = NetflixWhite,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = stringResource(R.string.starting_in_seconds, uiState.countdownSeconds),
                        color = NetflixLightGray,
                        fontSize = 13.sp
                    )
                } else {
                    Text(
                        text = stringResource(R.string.connecting_to_media),
                        color = NetflixLightGray,
                        fontSize = 14.sp
                    )
                }
            }
        } else if (uiState.error != null) {
            Column(
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = uiState.error ?: stringResource(R.string.failed_to_play),
                    color = NetflixWhite,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(18.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                    Button(
                        onClick = { viewModel.loadStream(contentType, contentId, title) },
                        colors = ButtonDefaults.colors(
                            containerColor = NetflixRed,
                            contentColor = NetflixWhite
                        )
                    ) {
                        Text(text = stringResource(R.string.try_again))
                    }

                    Button(
                        onClick = onBack,
                        colors = ButtonDefaults.colors(
                            containerColor = NetflixLightGray.copy(alpha = 0.3f),
                            contentColor = NetflixWhite
                        )
                    ) {
                        Text(text = stringResource(R.string.back))
                    }
                }
            }
        } else if (uiState.resolvedStream != null) {
            val resolved = uiState.resolvedStream!!

            val tokenQuery = remember(resolved.hlsMasterUrl) {
                try {
                    Uri.parse(resolved.hlsMasterUrl).query.orEmpty()
                } catch (_: Exception) {
                    ""
                }
            }

            val httpDataSourceFactory = remember(tokenQuery) {
                val baseHttp = DefaultHttpDataSource.Factory()
                    .setUserAgent("Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36")
                    .setDefaultRequestProperties(
                        mapOf(
                            "Referer" to com.idlix.tv.data.api.IdlixSource.REFERER,
                            "Origin" to com.idlix.tv.data.api.IdlixSource.ORIGIN
                        )
                    )
                QueryPreservingDataSourceFactory(baseHttp, tokenQuery)
            }

            fun buildMediaSource(
                masterUrl: String,
                sub: com.idlix.tv.data.model.SubtitleTrack?
            ): androidx.media3.exoplayer.source.MediaSource {
                val mediaItem = MediaItem.Builder()
                    .setUri(masterUrl)
                    .setMimeType(MimeTypes.APPLICATION_M3U8)
                    .build()

                val hlsSource = HlsMediaSource.Factory(httpDataSourceFactory)
                    .createMediaSource(mediaItem)
                if (sub == null) return hlsSource

                val mimeType = if (sub.path.endsWith(".srt", ignoreCase = true)) {
                    MimeTypes.APPLICATION_SUBRIP
                } else {
                    MimeTypes.TEXT_VTT
                }

                val subtitleConfig = MediaItem.SubtitleConfiguration.Builder(Uri.parse(sub.path))
                    .setMimeType(mimeType)
                    .setLanguage(sub.lang)
                    .setLabel(sub.label)
                    .setSelectionFlags(C.SELECTION_FLAG_DEFAULT or C.SELECTION_FLAG_FORCED)
                    .build()

                val subUri = Uri.parse(sub.path)
                val subDataSourceFactory = if (subUri.scheme == "file" || subUri.scheme == null) {
                    DefaultDataSource.Factory(context)
                } else {
                    httpDataSourceFactory
                }

                val subtitleSource = SingleSampleMediaSource.Factory(subDataSourceFactory)
                    .createMediaSource(subtitleConfig, C.TIME_UNSET)

                return MergingMediaSource(hlsSource, subtitleSource)
            }

            val exoPlayer = remember(context, resolved.hlsMasterUrl) {
                val renderersFactory = object : DefaultRenderersFactory(context) {
                    override fun buildTextRenderers(
                        context: Context,
                        output: TextOutput,
                        outputLooper: Looper,
                        extensionRendererMode: Int,
                        out: ArrayList<Renderer>
                    ) {
                        val renderer = TextRenderer(output, outputLooper)
                        renderer.experimentalSetLegacyDecodingEnabled(true)
                        out.add(renderer)
                    }
                }

                val loadControl = androidx.media3.exoplayer.DefaultLoadControl.Builder()
                    .setBufferDurationsMs(
                        15000,
                        50000,
                        2500,
                        5000
                    )
                    .setPrioritizeTimeOverSizeThresholds(true)
                    .build()

                ExoPlayer.Builder(context, renderersFactory)
                    .setLoadControl(loadControl)
                    .build().apply {
                    val activeSub = resolved.subtitles.firstOrNull { it.lang == "id" }
                        ?: resolved.subtitles.firstOrNull()

                    val finalMediaSource = buildMediaSource(resolved.hlsMasterUrl, activeSub)
                    setMediaSource(finalMediaSource)

                    trackSelectionParameters = trackSelectionParameters.buildUpon()
                        .setPreferredTextLanguage("id")
                        .setSelectUndeterminedTextLanguage(true)
                        .setTrackTypeDisabled(C.TRACK_TYPE_TEXT, false)
                        .build()

                    prepare()
                    if (uiState.initialPositionMs > 0) {
                        seekTo(uiState.initialPositionMs)
                    }
                    playWhenReady = true
                }
            }

            fun applySubtitleTrack(track: com.idlix.tv.data.model.SubtitleTrack?) {
                val currentPos = exoPlayer.currentPosition
                val wasPlaying = exoPlayer.isPlaying
                val newSource = buildMediaSource(resolved.hlsMasterUrl, track)
                exoPlayer.setMediaSource(newSource, currentPos)
                val selector = exoPlayer.trackSelectionParameters.buildUpon()
                if (track != null) {
                    selector
                        .setPreferredTextLanguage(track.lang)
                        .setSelectUndeterminedTextLanguage(true)
                        .setTrackTypeDisabled(C.TRACK_TYPE_TEXT, false)
                } else {
                    selector.setTrackTypeDisabled(C.TRACK_TYPE_TEXT, true)
                }
                exoPlayer.trackSelectionParameters = selector.build()
                if (wasPlaying) {
                    exoPlayer.play()
                }
            }

            val initialResizeMode = resizeMode

            LaunchedEffect(exoPlayer) {
                activePlayer = exoPlayer
            }

            // Sync playback position & throttle save progress to avoid disk I/O lag
            LaunchedEffect(exoPlayer) {
                var lastSavedPos = 0L
                while (true) {
                    currentPos = exoPlayer.currentPosition
                    duration = exoPlayer.duration.coerceAtLeast(0L)
                    isPlaying = exoPlayer.isPlaying
                    if (Math.abs(currentPos - lastSavedPos) >= 5000L && duration > 0) {
                        viewModel.saveProgress(currentPos, duration)
                        lastSavedPos = currentPos
                    }
                    delay(500)
                }
            }

            DisposableEffect(exoPlayer) {
                val playerListener = object : Player.Listener {
                    override fun onPlaybackStateChanged(playbackState: Int) {
                        if (playbackState == Player.STATE_ENDED) {
                            if (uiState.nextEpisode != null && !uiState.isNextEpisodeDismissed) {
                                viewModel.playNextEpisode()
                            }
                        }
                    }

                    override fun onVideoSizeChanged(videoSize: androidx.media3.common.VideoSize) {
                        playerViewRef.value?.let { applyPlayerResizeMode(it, resizeMode) }
                    }
                }
                exoPlayer.addListener(playerListener)

                onDispose {
                    try {
                        exoPlayer.removeListener(playerListener)
                        val finalPos = exoPlayer.currentPosition
                        val finalDur = exoPlayer.duration
                        if (finalDur > 0) {
                            viewModel.saveProgress(finalPos, finalDur)
                        }
                    } catch (_: Exception) {}
                    activePlayer = null
                    exoPlayer.release()
                }
            }

            // Video Surface with high-visibility Netflix-style subtitles
            AndroidView(
                factory = { ctx ->
                    PlayerView(ctx).apply {
                        layoutParams = FrameLayout.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.MATCH_PARENT
                        )
                        player = exoPlayer
                        useController = false
                        playerViewRef.value = this
                        subtitleView?.apply {
                            setUserDefaultStyle()
                            setUserDefaultTextSize()
                            setFractionalTextSize(0.053f) // Standard Netflix TV subtitle proportion
                        }
                        applyPlayerResizeMode(this, initialResizeMode)
                    }
                },
                update = { playerView -> applyPlayerResizeMode(playerView, resizeMode) },
                modifier = Modifier.fillMaxSize()
            )

            // D-Pad Player Controls
            TvPlayerControls(
                title = resolved.title,
                isVisible = showControls && !uiState.isSubtitlePickerVisible,
                isPlaying = isPlaying,
                currentPosMs = currentPos,
                durationMs = duration,
                isSubtitleEnabled = uiState.isSubtitleEnabled,
                selectedSubtitleLabel = uiState.selectedSubtitle?.label,
                resizeMode = resizeMode,
                onToggleResizeMode = {
                    val nextMode = when (resizeMode) {
                        AspectRatioFrameLayout.RESIZE_MODE_FIT -> AspectRatioFrameLayout.RESIZE_MODE_ZOOM
                        AspectRatioFrameLayout.RESIZE_MODE_ZOOM -> AspectRatioFrameLayout.RESIZE_MODE_FILL
                        else -> AspectRatioFrameLayout.RESIZE_MODE_FIT
                    }
                    resizeMode = nextMode
                    playerViewRef.value?.let { applyPlayerResizeMode(it, nextMode) }
                    showControls = true
                    userInteractionCount++
                },
                playButtonModifier = Modifier.focusRequester(playButtonFocusRequester),
                onPlayPauseClick = {
                    if (exoPlayer.isPlaying) {
                        exoPlayer.pause()
                    } else {
                        exoPlayer.play()
                    }
                    showControls = true
                    userInteractionCount++
                },
                onSeekBackward = {
                    exoPlayer.seekTo((exoPlayer.currentPosition - 10000L).coerceAtLeast(0L))
                    showControls = true
                    userInteractionCount++
                },
                onSeekForward = {
                    exoPlayer.seekTo((exoPlayer.currentPosition + 10000L).coerceAtMost(exoPlayer.duration))
                    showControls = true
                    userInteractionCount++
                },
                onSeekTo = { targetMs ->
                    exoPlayer.seekTo(targetMs)
                    showControls = true
                    userInteractionCount++
                },
                onScrubbingChanged = { scrubbing ->
                    isScrubbing = scrubbing
                },
                onUserInteraction = {
                    userInteractionCount++
                },
                onToggleSubtitle = {
                    val willBeEnabled = !uiState.isSubtitleEnabled
                    viewModel.toggleSubtitle()
                    val trackSelector = exoPlayer.trackSelectionParameters.buildUpon()
                    if (willBeEnabled) {
                        trackSelector
                            .setPreferredTextLanguage(uiState.selectedSubtitle?.lang ?: "id")
                            .setSelectUndeterminedTextLanguage(true)
                            .setTrackTypeDisabled(C.TRACK_TYPE_TEXT, false)
                    } else {
                        trackSelector.setTrackTypeDisabled(C.TRACK_TYPE_TEXT, true)
                    }
                    exoPlayer.trackSelectionParameters = trackSelector.build()
                    showControls = true
                    userInteractionCount++
                },
                onOpenSubtitlePicker = {
                    viewModel.openSubtitlePicker()
                    userInteractionCount++
                }
            )

            // Subtitle Picker Modal (IDLIX & SubDL.com third party)
            TvSubtitlePickerModal(
                isVisible = uiState.isSubtitlePickerVisible,
                availableSubtitles = resolved.subtitles,
                selectedSubtitle = uiState.selectedSubtitle,
                isSubtitleEnabled = uiState.isSubtitleEnabled,
                subdlSubtitles = uiState.subdlSubtitles,
                isSearchingSubdl = uiState.isSearchingSubdl,
                onSelectTrack = { track ->
                    viewModel.selectSubtitleTrack(track)
                    applySubtitleTrack(track)
                },
                onSelectSubdl = { subdl ->
                    viewModel.selectSubdlSubtitle(subdl) { newTrack ->
                        applySubtitleTrack(newTrack)
                    }
                },
                onSearchSubdlClick = {
                    viewModel.searchSubdlSubtitles()
                },
                onDismiss = {
                    viewModel.closeSubtitlePicker()
                }
            )
            // Next Episode Prompt for TV Series (When <= 90s remaining)
            if (uiState.nextEpisode != null) {
                val remainingSec = (remainingMs / 1000).toInt().coerceAtLeast(0)
                TvNextEpisodePrompt(
                    nextEpisode = uiState.nextEpisode!!,
                    remainingSeconds = remainingSec,
                    isVisible = isNextPromptVisible && !uiState.isSubtitlePickerVisible,
                    onPlayNext = {
                        viewModel.playNextEpisode()
                    },
                    onDismiss = {
                        viewModel.dismissNextEpisode()
                    },
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(
                            end = 36.dp,
                            bottom = if (showControls) 115.dp else 40.dp
                        )
                )
            }
        }
    }
}
