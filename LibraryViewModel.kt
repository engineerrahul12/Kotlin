package com.rahulsah.studio.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.rahulsah.studio.data.model.LibraryItem
import com.rahulsah.studio.data.model.MediaType
import com.rahulsah.studio.utils.MediaStoreHelper
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class LibraryUiState(
    val items: List<LibraryItem> = emptyList(),
    val isLoading: Boolean = false,
    val selectedTab: Int = 0,
    val searchQuery: String = "",
)

class LibraryViewModel(app: Application) : AndroidViewModel(app) {

    private val _state = MutableStateFlow(LibraryUiState())
    val state: StateFlow<LibraryUiState> = _state.asStateFlow()

    val filteredItems: StateFlow<List<LibraryItem>> = _state.map { s ->
        var list = s.items
        // Tab filter
        list = when (s.selectedTab) {
            1 -> list.filter { it.mediaType == MediaType.VIDEO }
            2 -> list.filter { it.mediaType == MediaType.IMAGE }
            3 -> list.filter { it.mediaType == MediaType.AUDIO }
            else -> list
        }
        // Search filter
        if (s.searchQuery.isNotBlank()) {
            list = list.filter { it.title.contains(s.searchQuery, ignoreCase = true) }
        }
        list
    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    init { loadMedia() }

    fun loadMedia() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            val items = MediaStoreHelper.loadStudioMedia(getApplication())
            _state.update { it.copy(items = items, isLoading = false) }
        }
    }

    fun selectTab(tab: Int) = _state.update { it.copy(selectedTab = tab) }
    fun search(query: String) = _state.update { it.copy(searchQuery = query) }
}
