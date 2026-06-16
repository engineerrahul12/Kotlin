package com.rahulsah.studio.viewmodel

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.rahulsah.studio.data.model.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.UUID

class EditorViewModel(app: Application) : AndroidViewModel(app) {

    private val _state = MutableStateFlow(EditorState())
    val state: StateFlow<EditorState> = _state.asStateFlow()

    private val _exportResult = MutableSharedFlow<ExportResult>()
    val exportResult: SharedFlow<ExportResult> = _exportResult

    // ──────────────────────────────────────────────
    // Media Loading
    // ──────────────────────────────────────────────

    fun loadMedia(uri: Uri, type: MediaType = MediaType.VIDEO) {
        _state.update {
            it.copy(
                mediaUri = uri.toString(),
                mediaType = type,
                trimStartMs = 0,
                trimEndMs = 0,
                activeTool = null
            )
        }
    }

    fun setDuration(durationMs: Long) {
        _state.update {
            it.copy(
                totalDurationMs = durationMs,
                trimEndMs = durationMs
            )
        }
    }

    // ──────────────────────────────────────────────
    // Playback
    // ──────────────────────────────────────────────

    fun togglePlayback() = _state.update { it.copy(isPlaying = !it.isPlaying) }
    fun seekTo(posMs: Long) = _state.update { it.copy(playbackPositionMs = posMs) }
    fun setPlaying(playing: Boolean) = _state.update { it.copy(isPlaying = playing) }

    // ──────────────────────────────────────────────
    // Tools
    // ──────────────────────────────────────────────

    fun selectTool(tool: EditorTool?) = _state.update { it.copy(activeTool = tool) }

    // Trim
    fun setTrimStart(ms: Long) = _state.update {
        it.copy(trimStartMs = ms.coerceIn(0, it.trimEndMs - 500))
    }
    fun setTrimEnd(ms: Long) = _state.update {
        it.copy(trimEndMs = ms.coerceIn(it.trimStartMs + 500, it.totalDurationMs))
    }

    // Speed
    fun setSpeed(speed: Float) = _state.update { it.copy(speed = speed.coerceIn(0.1f, 4f)) }

    // Volume / Mute
    fun setVolume(v: Float) = _state.update { it.copy(volume = v.coerceIn(0f, 2f)) }
    fun toggleMute() = _state.update { it.copy(isMuted = !it.isMuted) }

    // Reverse
    fun toggleReverse() = _state.update { it.copy(isReversed = !it.isReversed) }

    // ──────────────────────────────────────────────
    // Color Adjustments
    // ──────────────────────────────────────────────

    fun setBrightness(v: Float) = _state.update { it.copy(brightness = v.coerceIn(-1f, 1f)) }
    fun setContrast(v: Float) = _state.update { it.copy(contrast = v.coerceIn(-1f, 1f)) }
    fun setSaturation(v: Float) = _state.update { it.copy(saturation = v.coerceIn(-1f, 1f)) }
    fun setExposure(v: Float) = _state.update { it.copy(exposure = v.coerceIn(-1f, 1f)) }
    fun setSharpness(v: Float) = _state.update { it.copy(sharpness = v.coerceIn(0f, 1f)) }
    fun setVignette(v: Float) = _state.update { it.copy(vignette = v.coerceIn(0f, 1f)) }

    fun resetAdjustments() = _state.update {
        it.copy(
            brightness = 0f, contrast = 0f, saturation = 0f,
            exposure = 0f, sharpness = 0f, vignette = 0f,
            selectedFilter = builtInFilters[0]
        )
    }

    // ──────────────────────────────────────────────
    // Filters
    // ──────────────────────────────────────────────

    fun applyFilter(filter: VideoFilter) = _state.update { it.copy(selectedFilter = filter) }

    // ──────────────────────────────────────────────
    // Text Overlays
    // ──────────────────────────────────────────────

    fun addTextOverlay(text: String = "Tap to edit") {
        val overlay = TextOverlay(
            id = UUID.randomUUID().toString(),
            text = text,
            startMs = _state.value.playbackPositionMs,
            endMs = (_state.value.playbackPositionMs + 3000).coerceAtMost(_state.value.totalDurationMs)
        )
        _state.update { it.copy(textOverlays = it.textOverlays + overlay) }
    }

    fun updateTextOverlay(updated: TextOverlay) {
        _state.update {
            it.copy(textOverlays = it.textOverlays.map { o -> if (o.id == updated.id) updated else o })
        }
    }

    fun removeTextOverlay(id: String) {
        _state.update { it.copy(textOverlays = it.textOverlays.filterNot { o -> o.id == id }) }
    }

    // ──────────────────────────────────────────────
    // Aspect Ratio
    // ──────────────────────────────────────────────

    fun setAspectRatio(ratio: AspectRatio) = _state.update { it.copy(aspectRatio = ratio) }

    // ──────────────────────────────────────────────
    // Export
    // ──────────────────────────────────────────────

    fun export(outputFormat: String = "mp4", quality: ExportQuality = ExportQuality.HIGH) {
        val current = _state.value
        if (current.mediaUri.isEmpty()) return

        viewModelScope.launch {
            _state.update { it.copy(isExporting = true, exportProgress = 0f) }

            // Build FFmpeg command based on state
            val ffmpegCmd = buildFfmpegCommand(current, outputFormat, quality)

            // Simulate progress (real: use FFmpeg progress callback)
            repeat(20) { i ->
                delay(150)
                _state.update { it.copy(exportProgress = (i + 1) / 20f) }
            }

            // In production: run FFmpeg.executeAsync(ffmpegCmd, ...) 
            // and handle callback for success/failure
            _state.update { it.copy(isExporting = false, exportProgress = 1f) }
            _exportResult.emit(ExportResult.Success("/storage/emulated/0/Downloads/Studio/export_${System.currentTimeMillis()}.$outputFormat"))
        }
    }

    private fun buildFfmpegCommand(state: EditorState, format: String, quality: ExportQuality): String {
        val sb = StringBuilder("-i \"${state.mediaUri}\"")

        // Trim
        if (state.trimStartMs > 0) sb.append(" -ss ${state.trimStartMs / 1000.0}")
        if (state.trimEndMs < state.totalDurationMs) sb.append(" -to ${state.trimEndMs / 1000.0}")

        // Speed
        if (state.speed != 1f) sb.append(" -vf \"setpts=${1f/state.speed}*PTS\" -af \"atempo=${state.speed}\"")

        // Filter chain
        val vFilters = mutableListOf<String>()
        if (state.selectedFilter.ffmpegFilter.isNotEmpty()) vFilters.add(state.selectedFilter.ffmpegFilter)
        val eq = buildEqualizerFilter(state)
        if (eq.isNotEmpty()) vFilters.add(eq)
        if (state.isReversed) vFilters.add("reverse")
        if (vFilters.isNotEmpty()) sb.append(" -vf \"${vFilters.joinToString(",")}\"")

        // Audio
        if (state.isMuted) sb.append(" -an")
        else if (state.volume != 1f) sb.append(" -af \"volume=${state.volume}\"")

        // Quality
        val crf = when (quality) {
            ExportQuality.LOW -> "28"
            ExportQuality.MEDIUM -> "23"
            ExportQuality.HIGH -> "18"
            ExportQuality.ULTRA -> "12"
        }
        sb.append(" -crf $crf -preset medium")

        val outputPath = "/storage/emulated/0/Downloads/Studio/export_${System.currentTimeMillis()}.$format"
        sb.append(" \"$outputPath\"")
        return sb.toString()
    }

    private fun buildEqualizerFilter(state: EditorState): String {
        val parts = mutableListOf<String>()
        if (state.brightness != 0f) parts.add("brightness=${state.brightness}")
        if (state.contrast != 0f) parts.add("contrast=${1f + state.contrast}")
        if (state.saturation != 0f) parts.add("saturation=${1f + state.saturation}")
        if (state.exposure != 0f) parts.add("gamma=${1f + state.exposure}")
        return if (parts.isNotEmpty()) "eq=${parts.joinToString(":")}" else ""
    }

    fun cancelExport() {
        // FFmpeg.cancel()
        _state.update { it.copy(isExporting = false, exportProgress = 0f) }
    }
}

enum class ExportQuality(val label: String) {
    LOW("Low (fast)"),
    MEDIUM("Medium"),
    HIGH("High"),
    ULTRA("Ultra (slow)")
}

sealed class ExportResult {
    data class Success(val outputPath: String) : ExportResult()
    data class Failure(val message: String) : ExportResult()
}
