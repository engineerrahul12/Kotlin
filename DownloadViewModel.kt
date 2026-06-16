package com.rahulsah.studio.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.rahulsah.studio.data.model.*
import com.rahulsah.studio.data.repository.DownloadRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class DownloadUiState(
    val urlInput: String = "",
    val isAnalyzing: Boolean = false,
    val mediaInfo: MediaInfo? = null,
    val selectedQuality: VideoQuality = VideoQuality.Q_720P,
    val errorMessage: String? = null,
    val showQualitySheet: Boolean = false,
    val activeTab: DownloadTab = DownloadTab.DOWNLOADER,
)

enum class DownloadTab { DOWNLOADER, BROWSER, QUEUE, HISTORY }

class DownloadViewModel(app: Application) : AndroidViewModel(app) {

    private val repo = DownloadRepository(app)

    private val _uiState = MutableStateFlow(DownloadUiState())
    val uiState: StateFlow<DownloadUiState> = _uiState.asStateFlow()

    val downloads: StateFlow<List<DownloadItem>> = repo.downloads

    fun onUrlChanged(url: String) {
        _uiState.update { it.copy(urlInput = url, errorMessage = null, mediaInfo = null) }
    }

    fun onPaste(url: String) {
        _uiState.update { it.copy(urlInput = url) }
        analyzeUrl(url)
    }

    fun analyzeUrl(url: String = _uiState.value.urlInput) {
        if (url.isBlank()) return
        viewModelScope.launch {
            _uiState.update { it.copy(isAnalyzing = true, errorMessage = null, mediaInfo = null) }
            repo.analyzeUrl(url).fold(
                onSuccess = { info ->
                    _uiState.update {
                        it.copy(
                            isAnalyzing = false,
                            mediaInfo = info,
                            showQualitySheet = true
                        )
                    }
                },
                onFailure = { e ->
                    _uiState.update {
                        it.copy(
                            isAnalyzing = false,
                            errorMessage = e.message ?: "Could not analyze URL"
                        )
                    }
                }
            )
        }
    }

    fun selectQuality(quality: VideoQuality) {
        _uiState.update { it.copy(selectedQuality = quality) }
    }

    fun startDownload() {
        val state = _uiState.value
        val info = state.mediaInfo ?: return
        viewModelScope.launch {
            repo.startDownload(info, state.selectedQuality)
            _uiState.update {
                it.copy(
                    showQualitySheet = false,
                    urlInput = "",
                    mediaInfo = null,
                    activeTab = DownloadTab.QUEUE
                )
            }
        }
    }

    fun dismissQualitySheet() {
        _uiState.update { it.copy(showQualitySheet = false) }
    }

    fun onTabSelected(tab: DownloadTab) {
        _uiState.update { it.copy(activeTab = tab) }
    }

    fun pauseDownload(id: String) = repo.pauseDownload(id)
    fun resumeDownload(id: String) = repo.resumeDownload(id)
    fun cancelDownload(id: String) = repo.cancelDownload(id)
    fun removeDownload(id: String) = repo.removeDownload(id)
    fun clearError() = _uiState.update { it.copy(errorMessage = null) }
}
