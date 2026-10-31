package com.idlix.tv.ui.screens.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Dns
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Subtitles
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.tv.material3.Button
import androidx.tv.material3.ButtonDefaults
import androidx.tv.material3.ExperimentalTvMaterial3Api
import androidx.tv.material3.Icon
import androidx.tv.material3.Text
import com.idlix.tv.BuildConfig
import com.idlix.tv.R
import com.idlix.tv.data.api.ApiClient
import com.idlix.tv.data.model.SubdlResponse
import com.idlix.tv.data.repository.SubdlRepository
import com.idlix.tv.ui.theme.NetflixBlack
import com.idlix.tv.ui.theme.NetflixDarkCard
import com.idlix.tv.ui.theme.NetflixLightGray
import com.idlix.tv.ui.theme.NetflixRed
import com.idlix.tv.ui.theme.NetflixWhite
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.Request

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun SettingsScreen(
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val keyboardController = LocalSoftwareKeyboardController.current
    val scrollState = rememberScrollState()

    // --- Backend API State ---
    var currentActiveUrl by remember { mutableStateOf(ApiClient.getBaseUrl()) }
    var inputUrl by remember { mutableStateOf(currentActiveUrl) }
    var isUrlInputFocused by remember { mutableStateOf(false) }

    var backendTestStatus by remember { mutableStateOf<String?>(null) }
    var isTestingBackend by remember { mutableStateOf(false) }
    var isBackendTestSuccess by remember { mutableStateOf(false) }

    // --- SubDL API Key State ---
    var currentActiveApiKey by remember { mutableStateOf(SubdlRepository.getApiKey()) }
    var inputApiKey by remember { mutableStateOf(currentActiveApiKey) }
    var isApiKeyInputFocused by remember { mutableStateOf(false) }

    var subdlTestStatus by remember { mutableStateOf<String?>(null) }
    var isTestingSubdl by remember { mutableStateOf(false) }
    var isSubdlTestSuccess by remember { mutableStateOf(false) }

    // --- Global Save Notification ---
    var saveMessage by remember { mutableStateOf<String?>(null) }

    // Test Backend API
    fun runTestBackend(targetUrl: String) {
        scope.launch {
            isTestingBackend = true
            backendTestStatus = context.getString(R.string.backend_testing)
            saveMessage = null

            val formatted = if (targetUrl.endsWith("/")) targetUrl else "$targetUrl/"
            val testEndpoint = "${formatted}api/movies?page=1&limit=1"

            val result = withContext(Dispatchers.IO) {
                val start = System.currentTimeMillis()
                try {
                    val req = Request.Builder()
                        .url(testEndpoint)
                        .get()
                        .build()
                    ApiClient.okHttpClient.newCall(req).execute().use { resp ->
                        val duration = System.currentTimeMillis() - start
                        if (resp.isSuccessful) {
                            Pair(true, context.getString(R.string.backend_connected, resp.code, duration))
                        } else {
                            Pair(false, context.getString(R.string.backend_failed_http, resp.code))
                        }
                    }
                } catch (e: Exception) {
                    Pair(false, context.getString(R.string.backend_failed_error, e.localizedMessage ?: "Timeout"))
                }
            }

            isTestingBackend = false
            isBackendTestSuccess = result.first
            backendTestStatus = result.second
        }
    }

    // Test SubDL API Key
    fun runTestSubdl(targetKey: String) {
        scope.launch {
            isTestingSubdl = true
            subdlTestStatus = context.getString(R.string.subdl_testing)
            saveMessage = null

            val testEndpoint = "https://api.subdl.com/api/v1/subtitles?api_key=${targetKey.trim()}&film_name=Avatar"

            val result = withContext(Dispatchers.IO) {
                try {
                    val req = Request.Builder()
                        .url(testEndpoint)
                        .header("User-Agent", "Mozilla/5.0 (Android TV; Idlix)")
                        .get()
                        .build()
                    ApiClient.okHttpClient.newCall(req).execute().use { resp ->
                        if (resp.isSuccessful) {
                            val body = resp.body?.string().orEmpty()
                            val parsed = try {
                                ApiClient.json.decodeFromString<SubdlResponse>(body)
                            } catch (_: Exception) { null }

                            if (parsed?.status == true) {
                                Pair(true, context.getString(R.string.subdl_valid))
                            } else {
                                Pair(false, context.getString(R.string.subdl_quota_error))
                            }
                        } else {
                            Pair(false, context.getString(R.string.subdl_invalid_http, resp.code))
                        }
                    }
                } catch (e: Exception) {
                    Pair(false, context.getString(R.string.subdl_failed_error, e.localizedMessage ?: ""))
                }
            }

            isTestingSubdl = false
            isSubdlTestSuccess = result.first
            subdlTestStatus = result.second
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(NetflixBlack)
            .verticalScroll(scrollState)
            .padding(horizontal = 36.dp, vertical = 24.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // Page Title Header
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Default.Settings,
                contentDescription = null,
                tint = NetflixRed,
                modifier = Modifier.size(28.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = stringResource(R.string.settings),
                    color = NetflixWhite,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = stringResource(R.string.settings_desc),
                    color = NetflixLightGray,
                    fontSize = 13.sp
                )
            }
        }

        // ==========================================
        // SECTION 1: BACKEND API CONFIGURATION
        // ==========================================
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(NetflixDarkCard, RoundedCornerShape(10.dp))
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Dns,
                    contentDescription = null,
                    tint = NetflixWhite,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = stringResource(R.string.backend_api_header),
                    color = NetflixWhite,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            // Current URL Info
            Text(
                text = stringResource(R.string.active_backend_url, currentActiveUrl),
                color = NetflixLightGray,
                fontSize = 12.sp
            )

            // Input Field URL
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .background(NetflixBlack, RoundedCornerShape(6.dp))
                    .border(
                        width = if (isUrlInputFocused) 2.dp else 1.dp,
                        color = if (isUrlInputFocused) NetflixWhite else NetflixDarkCard,
                        shape = RoundedCornerShape(6.dp)
                    )
                    .padding(horizontal = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                BasicTextField(
                    value = inputUrl,
                    onValueChange = {
                        inputUrl = it
                        saveMessage = null
                        backendTestStatus = null
                    },
                    textStyle = TextStyle(
                        color = NetflixWhite,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    ),
                    cursorBrush = SolidColor(NetflixRed),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = { keyboardController?.hide() }),
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .onFocusChanged { isUrlInputFocused = it.isFocused },
                    decorationBox = { inner ->
                        if (inputUrl.isEmpty()) {
                            Text(
                                text = "http://192.168.x.x:5376/",
                                color = NetflixLightGray,
                                fontSize = 13.sp
                            )
                        }
                        inner()
                    }
                )
            }

            // Backend Actions
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Button(
                    onClick = { runTestBackend(inputUrl) },
                    colors = ButtonDefaults.colors(
                        containerColor = NetflixBlack,
                        focusedContainerColor = NetflixWhite,
                        contentColor = NetflixWhite,
                        focusedContentColor = NetflixBlack
                    ),
                    shape = ButtonDefaults.shape(RoundedCornerShape(6.dp)),
                    modifier = Modifier.height(38.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isTestingBackend) stringResource(R.string.testing) else stringResource(R.string.test_backend_connection),
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }
                }

                Button(
                    onClick = {
                        inputUrl = BuildConfig.BACKEND_API_URL
                        backendTestStatus = null
                        saveMessage = null
                    },
                    colors = ButtonDefaults.colors(
                        containerColor = NetflixBlack.copy(alpha = 0.5f),
                        focusedContainerColor = NetflixWhite,
                        contentColor = NetflixLightGray,
                        focusedContentColor = NetflixBlack
                    ),
                    shape = ButtonDefaults.shape(RoundedCornerShape(6.dp)),
                    modifier = Modifier.height(38.dp)
                ) {
                    Text(text = stringResource(R.string.reset_default_url), fontSize = 12.sp)
                }
            }

            if (backendTestStatus != null) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = if (isBackendTestSuccess) Icons.Default.CheckCircle else Icons.Default.Error,
                        contentDescription = null,
                        tint = if (isBackendTestSuccess) Color(0xFF4CAF50) else NetflixRed,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = backendTestStatus ?: "",
                        color = if (isBackendTestSuccess) Color(0xFF4CAF50) else NetflixRed,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }

        // ==========================================
        // SECTION 2: THIRD-PARTY SUBTITLE (SUBDL)
        // ==========================================
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(NetflixDarkCard, RoundedCornerShape(10.dp))
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Subtitles,
                    contentDescription = null,
                    tint = NetflixWhite,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = stringResource(R.string.subdl_api_header),
                    color = NetflixWhite,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Text(
                text = stringResource(R.string.subdl_api_desc),
                color = NetflixLightGray,
                fontSize = 12.sp
            )

            // Input Field API Key
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .background(NetflixBlack, RoundedCornerShape(6.dp))
                    .border(
                        width = if (isApiKeyInputFocused) 2.dp else 1.dp,
                        color = if (isApiKeyInputFocused) NetflixWhite else NetflixDarkCard,
                        shape = RoundedCornerShape(6.dp)
                    )
                    .padding(horizontal = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Key,
                    contentDescription = null,
                    tint = if (isApiKeyInputFocused) NetflixWhite else NetflixLightGray,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                BasicTextField(
                    value = inputApiKey,
                    onValueChange = {
                        inputApiKey = it
                        saveMessage = null
                        subdlTestStatus = null
                    },
                    textStyle = TextStyle(
                        color = NetflixWhite,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    ),
                    cursorBrush = SolidColor(NetflixRed),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = { keyboardController?.hide() }),
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .onFocusChanged { isApiKeyInputFocused = it.isFocused },
                    decorationBox = { inner ->
                        if (inputApiKey.isEmpty()) {
                            Text(
                                text = "subdl__your_api_key_here",
                                color = NetflixLightGray,
                                fontSize = 13.sp
                            )
                        }
                        inner()
                    }
                )
            }

            // SubDL Actions
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Button(
                    onClick = { runTestSubdl(inputApiKey) },
                    colors = ButtonDefaults.colors(
                        containerColor = NetflixBlack,
                        focusedContainerColor = NetflixWhite,
                        contentColor = NetflixWhite,
                        focusedContentColor = NetflixBlack
                    ),
                    shape = ButtonDefaults.shape(RoundedCornerShape(6.dp)),
                    modifier = Modifier.height(38.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isTestingSubdl) stringResource(R.string.testing) else stringResource(R.string.test_subdl_key),
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }
                }

                Button(
                    onClick = {
                        inputApiKey = SubdlRepository.DEFAULT_API_KEY
                        subdlTestStatus = null
                        saveMessage = null
                    },
                    colors = ButtonDefaults.colors(
                        containerColor = NetflixBlack.copy(alpha = 0.5f),
                        focusedContainerColor = NetflixWhite,
                        contentColor = NetflixLightGray,
                        focusedContentColor = NetflixBlack
                    ),
                    shape = ButtonDefaults.shape(RoundedCornerShape(6.dp)),
                    modifier = Modifier.height(38.dp)
                ) {
                    Text(text = stringResource(R.string.reset_default_key), fontSize = 12.sp)
                }
            }

            if (subdlTestStatus != null) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = if (isSubdlTestSuccess) Icons.Default.CheckCircle else Icons.Default.Error,
                        contentDescription = null,
                        tint = if (isSubdlTestSuccess) Color(0xFF4CAF50) else NetflixRed,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = subdlTestStatus ?: "",
                        color = if (isSubdlTestSuccess) Color(0xFF4CAF50) else NetflixRed,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }

        // ==========================================
        // SECTION 3: SAVE ALL SETTINGS ACTION
        // ==========================================
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.padding(top = 4.dp, bottom = 20.dp)
        ) {
            Button(
                onClick = {
                    // Save Backend URL
                    val formattedUrl = if (inputUrl.endsWith("/")) inputUrl else "$inputUrl/"
                    ApiClient.setBaseUrl(formattedUrl)
                    currentActiveUrl = ApiClient.getBaseUrl()
                    inputUrl = currentActiveUrl

                    // Save SubDL Key
                    SubdlRepository.setApiKey(inputApiKey)
                    currentActiveApiKey = SubdlRepository.getApiKey()
                    inputApiKey = currentActiveApiKey

                    saveMessage = context.getString(R.string.settings_saved_success)
                },
                colors = ButtonDefaults.colors(
                    containerColor = NetflixRed,
                    focusedContainerColor = Color(0xFFE50914).copy(alpha = 0.8f),
                    contentColor = NetflixWhite,
                    focusedContentColor = NetflixWhite
                ),
                shape = ButtonDefaults.shape(RoundedCornerShape(6.dp)),
                modifier = Modifier.height(46.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Save,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = stringResource(R.string.save_all_settings),
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }
            }

            if (saveMessage != null) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = Color(0xFF4CAF50),
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = saveMessage ?: "",
                        color = Color(0xFF4CAF50),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
