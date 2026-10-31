package com.idlix.tv.ui.screens.history

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.idlix.tv.data.local.WatchHistoryStore
import com.idlix.tv.data.model.WatchHistoryItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class ContinueWatchingUiState(
    val items: List<WatchHistoryItem> = emptyList(),
    val isLoading: Boolean = false
)

class ContinueWatchingViewModel(
    application: Application
) : AndroidViewModel(application) {

    private val historyStore = WatchHistoryStore.getInstance(application.applicationContext)

    private val _uiState = MutableStateFlow(ContinueWatchingUiState())
    val uiState: StateFlow<ContinueWatchingUiState> = _uiState.asStateFlow()

    init {
        loadHistory()
        viewModelScope.launch {
            historyStore.historyFlow.collect {
                loadHistory()
            }
        }
    }

    fun loadHistory() {
        val unfinished = historyStore.getUnfinishedItems()
        _uiState.value = ContinueWatchingUiState(items = unfinished, isLoading = false)
    }

    fun removeItem(contentId: String) {
        historyStore.removeItem(contentId)
        loadHistory()
    }

    fun clearAll() {
        historyStore.clearAll()
        loadHistory()
    }
}
