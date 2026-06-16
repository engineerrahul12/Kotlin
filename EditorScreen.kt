package com.rahulsah.studio.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.*
import com.rahulsah.studio.data.model.*
import com.rahulsah.studio.ui.components.*
import com.rahulsah.studio.ui.theme.*
import com.rahulsah.studio.viewmodel.EditorViewModel
import com.rahulsah.studio.viewmodel.ExportQuality
import com.rahulsah.studio.viewmodel.ExportResult

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditorScreen(
    viewModel: EditorViewModel,
    onBack: () -> Unit,
) {
    val state by viewModel.state.collectAsState()
    var showExportSheet by remember { mutableStateOf(false) }
    var exportQuality by remember { mutableStateOf(ExportQuality.HIGH) }

    val mediaPicker = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { viewModel.loadMedia(it, MediaType.VIDEO) }
    }

    LaunchedEffect(Unit) {
        viewModel.exportResult.collect { result ->
            when (result) {
                is ExportResult.Success -> showExportSheet = false
                is ExportResult.Failure -> { /* show snackbar */ }
            }
        }
    }

    StudioBackground {
        if (state.mediaUri.isEmpty()) {
            // No media loaded — show pick screen
            EditorPickScreen(
                onPickVideo = { mediaPicker.launch("video/*") },
                onPickImage = { mediaPicker.launch("image/*") },
                onPickAudio = { mediaPicker.launch("audio/*") },
                onBack = onBack
            )
        } else {
            // Full editor
            Column(Modifier.fillMaxSize()) {
                // Top bar
                EditorTopBar(
                    title = "Editor",
                    onBack = onBack,
                    isExporting = state.isExporting,
                    onExport = { showExportSheet = true }
                )

                // Video preview area
                VideoPreviewArea(
                    state = state,
                    onTogglePlay = viewModel::togglePlayback,
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                )

                // Timeline / scrubber
                TimelineBar(
                    positionMs = state.playbackPositionMs,
                    durationMs = state.totalDurationMs,
                    trimStartMs = state.trimStartMs,
                    trimEndMs = state.trimEndMs,
                    onSeek = viewModel::seekTo,
                    onTrimStart = viewModel::setTrimStart,
                    onTrimEnd = viewModel::setTrimEnd,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )

                Spacer(Modifier.height(8.dp))

                // Tool panel
                EditorToolPanel(
                    state = state,
                    onSelectTool = viewModel::selectTool,
                    onBrightness = viewModel::setBrightness,
                    onContrast = viewModel::setContrast,
                    onSaturation = viewModel::setSaturation,
                    onExposure = viewModel::setExposure,
                    onSharpness = viewModel::setSharpness,
                    onVignette = viewModel::setVignette,
                    onSelectFilter = viewModel::applyFilter,
                    onSpeed = viewModel::setSpeed,
                    onVolume = viewModel::setVolume,
                    onMute = viewModel::toggleMute,
                    onReverse = viewModel::toggleReverse,
                    onAspectRatio = viewModel::setAspectRatio,
                    onAddText = { viewModel.addTextOverlay() },
                    onResetAdjust = viewModel::resetAdjustments,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp)
                )
            }
        }
    }

    // Export sheet
    if (showExportSheet) {
        ExportBottomSheet(
            isExporting = state.isExporting,
            progress = state.exportProgress,
            selectedQuality = exportQuality,
            onQualitySelect = { exportQuality = it },
            onExport = { viewModel.export(quality = exportQuality) },
            onCancel = {
                if (state.isExporting) viewModel.cancelExport()
                else showExportSheet = false
            }
        )
    }
}

@Composable
private fun EditorPickScreen(
    onPickVideo: () -> Unit,
    onPickImage: () -> Unit,
    onPickAudio: () -> Unit,
    onBack: () -> Unit
) {
    Column(Modifier.fillMaxSize()) {
        StudioTopBar("Editor", showBack = true, onBack = onBack)
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    Icons.Rounded.MovieCreation, null,
                    Modifier.size(72.dp), tint = StudioViolet.copy(alpha = 0.4f)
                )
                Spacer(Modifier.height(24.dp))
                Text("Open Media to Edit", style = MaterialTheme.typography.headlineMedium, color = StudioTextPrimary)
                Text("Import video, photo or audio", style = MaterialTheme.typography.bodyMedium, color = StudioTextHint)
                Spacer(Modifier.height(32.dp))

                Column(
                    Modifier.padding(horizontal = 40.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    NeonButton("Import Video", onPickVideo, Modifier.fillMaxWidth(), icon = Icons.Rounded.VideoFile)
                    NeonButton("Import Photo", onPickImage, Modifier.fillMaxWidth(), icon = Icons.Rounded.Image, color = StudioCyan.copy(0.8f))
                    NeonButton("Import Audio", onPickAudio, Modifier.fillMaxWidth(), icon = Icons.Rounded.MusicNote, color = StudioPink.copy(0.8f))
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EditorTopBar(
    title: String,
    onBack: () -> Unit,
    isExporting: Boolean,
    onExport: () -> Unit
) {
    TopAppBar(
        title = {
            Text(title, style = MaterialTheme.typography.titleLarge.copy(
                fontWeight = FontWeight.ExtraBold,
                brush = Brush.linearGradient(listOf(StudioVioletGlow, StudioCyan))
            ))
        },
        navigationIcon = {
            IconButton(onClick = onBack) {
                Icon(Icons.Rounded.ArrowBackIos, null, tint = StudioTextSecondary)
            }
        },
        actions = {
            if (!isExporting) {
                TextButton(
                    onClick = onExport,
                    colors = ButtonDefaults.textButtonColors(contentColor = StudioViolet)
                ) {
                    Icon(Icons.Rounded.FileUpload, null, Modifier.size(18.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Export", style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold))
                }
            } else {
                CircularProgressIndicator(Modifier.size(24.dp).padding(end = 16.dp), color = StudioViolet, strokeWidth = 2.dp)
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
    )
}

@Composable
private fun VideoPreviewArea(
    state: EditorState,
    onTogglePlay: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier
            .background(StudioCard, RoundedCornerShape(16.dp))
            .clip(RoundedCornerShape(16.dp)),
        contentAlignment = Alignment.Center
    ) {
        // In production: embed AndroidView with ExoPlayer PlayerView here
        Box(
            Modifier
                .fillMaxSize()
                .background(
                    Brush.radialGradient(
                        listOf(StudioElevated, StudioBlack),
                        center = androidx.compose.ui.geometry.Offset.Unspecified
                    )
                )
        )
        // Play / Pause overlay
        IconButton(
            onClick = onTogglePlay,
            modifier = Modifier
                .size(56.dp)
                .background(StudioViolet.copy(alpha = 0.85f), CircleShape)
        ) {
            Icon(
                if (state.isPlaying) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
                null, Modifier.size(30.dp), tint = Color.White
            )
        }

        // Speed badge
        if (state.speed != 1f) {
            Box(
                Modifier
                    .align(Alignment.TopEnd)
                    .padding(10.dp)
                    .background(StudioAmber.copy(alpha = 0.85f), RoundedCornerShape(6.dp))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text("${state.speed}×", style = MaterialTheme.typography.labelSmall, color = Color.Black, fontWeight = FontWeight.Bold)
            }
        }

        // Mute indicator
        if (state.isMuted) {
            Box(
                Modifier
                    .align(Alignment.TopStart)
                    .padding(10.dp)
                    .background(StudioPink.copy(alpha = 0.85f), CircleShape)
                    .padding(6.dp)
            ) {
                Icon(Icons.Rounded.VolumeOff, null, Modifier.size(14.dp), tint = Color.White)
            }
        }
    }
}

@Composable
private fun TimelineBar(
    positionMs: Long,
    durationMs: Long,
    trimStartMs: Long,
    trimEndMs: Long,
    onSeek: (Long) -> Unit,
    onTrimStart: (Long) -> Unit,
    onTrimEnd: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val safeDuration = if (durationMs > 0) durationMs else 1L

    Column(modifier) {
        // Time labels
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(formatMs(positionMs), style = MaterialTheme.typography.labelSmall, color = StudioTextHint)
            Text(formatMs(durationMs), style = MaterialTheme.typography.labelSmall, color = StudioTextHint)
        }
        Spacer(Modifier.height(4.dp))

        // Scrubber
        Slider(
            value = positionMs.toFloat(),
            onValueChange = { onSeek(it.toLong()) },
            valueRange = 0f..safeDuration.toFloat(),
            modifier = Modifier.fillMaxWidth(),
            colors = SliderDefaults.colors(
                thumbColor = StudioViolet,
                activeTrackColor = StudioViolet,
                inactiveTrackColor = StudioBorder
            )
        )

        // Trim handles
        Spacer(Modifier.height(4.dp))
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text("Trim", style = MaterialTheme.typography.labelSmall, color = StudioTextHint)
            Spacer(Modifier.width(8.dp))
            Text(
                "${formatMs(trimStartMs)} – ${formatMs(trimEndMs)}",
                style = MaterialTheme.typography.labelSmall,
                color = StudioVioletLight
            )
        }
        // Trim range slider (dual-handle via two overlapping sliders)
        Box(Modifier.fillMaxWidth().height(32.dp)) {
            Slider(
                value = trimStartMs.toFloat(),
                onValueChange = { onTrimStart(it.toLong()) },
                valueRange = 0f..safeDuration.toFloat(),
                modifier = Modifier.fillMaxWidth(),
                colors = SliderDefaults.colors(
                    thumbColor = StudioCyan,
                    activeTrackColor = Color.Transparent,
                    inactiveTrackColor = Color.Transparent
                )
            )
            Slider(
                value = trimEndMs.toFloat(),
                onValueChange = { onTrimEnd(it.toLong()) },
                valueRange = 0f..safeDuration.toFloat(),
                modifier = Modifier.fillMaxWidth(),
                colors = SliderDefaults.colors(
                    thumbColor = StudioCyan,
                    activeTrackColor = StudioViolet.copy(alpha = 0.35f),
                    inactiveTrackColor = Color.Transparent
                )
            )
        }
    }
}

@Composable
private fun EditorToolPanel(
    state: EditorState,
    onSelectTool: (EditorTool?) -> Unit,
    onBrightness: (Float) -> Unit,
    onContrast: (Float) -> Unit,
    onSaturation: (Float) -> Unit,
    onExposure: (Float) -> Unit,
    onSharpness: (Float) -> Unit,
    onVignette: (Float) -> Unit,
    onSelectFilter: (VideoFilter) -> Unit,
    onSpeed: (Float) -> Unit,
    onVolume: (Float) -> Unit,
    onMute: () -> Unit,
    onReverse: () -> Unit,
    onAspectRatio: (AspectRatio) -> Unit,
    onAddText: () -> Unit,
    onResetAdjust: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier) {
        // Active tool panel (collapsible)
        AnimatedVisibility(visible = state.activeTool != null) {
            Box(
                Modifier
                    .fillMaxWidth()
                    .background(StudioDeepNavy)
                    .padding(16.dp)
            ) {
                when (state.activeTool) {
                    EditorTool.FILTER -> FilterPanel(state.selectedFilter, onSelectFilter)
                    EditorTool.BRIGHTNESS, EditorTool.CONTRAST, EditorTool.SATURATION,
                    EditorTool.EXPOSURE -> ColorAdjustPanel(
                        state, onBrightness, onContrast, onSaturation, onExposure,
                        onSharpness, onVignette, onResetAdjust
                    )
                    EditorTool.SPEED -> SpeedPanel(state.speed, onSpeed)
                    EditorTool.AUDIO -> AudioPanel(state.volume, state.isMuted, onVolume, onMute)
                    EditorTool.REVERSE -> {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("Reverse video", style = MaterialTheme.typography.bodyMedium, color = StudioTextPrimary)
                            Spacer(Modifier.weight(1f))
                            Switch(
                                checked = state.isReversed, onCheckedChange = { onReverse() },
                                colors = SwitchDefaults.colors(checkedThumbColor = StudioViolet, checkedTrackColor = StudioViolet.copy(0.3f))
                            )
                        }
                    }
                    EditorTool.CROP -> AspectRatioPanel(state.aspectRatio, onAspectRatio)
                    EditorTool.TEXT -> {
                        NeonButton("+ Add Text Overlay", onAddText, Modifier.fillMaxWidth(), icon = Icons.Rounded.TextFields)
                    }
                    else -> {}
                }
            }
        }

        HorizontalDivider(color = StudioBorder)

        // Tool strip
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(toolStrip) { (tool, label, icon) ->
                val isActive = state.activeTool == tool
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.clickable {
                        onSelectTool(if (isActive) null else tool)
                    }
                ) {
                    Box(
                        Modifier
                            .size(44.dp)
                            .background(
                                if (isActive) StudioViolet else StudioCard,
                                RoundedCornerShape(12.dp)
                            )
                            .border(
                                1.dp,
                                if (isActive) StudioViolet else StudioBorder,
                                RoundedCornerShape(12.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(icon, null, Modifier.size(20.dp),
                            tint = if (isActive) Color.White else StudioTextSecondary)
                    }
                    Spacer(Modifier.height(4.dp))
                    Text(label, style = MaterialTheme.typography.labelSmall.copy(
                        color = if (isActive) StudioViolet else StudioTextHint
                    ))
                }
            }
        }
    }
}

@Composable
private fun FilterPanel(selected: VideoFilter, onSelect: (VideoFilter) -> Unit) {
    Column {
        Text("Filters", style = MaterialTheme.typography.labelMedium, color = StudioTextHint)
        Spacer(Modifier.height(8.dp))
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(builtInFilters) { filter ->
                FilterChip(filter, filter.id == selected.id) { onSelect(filter) }
            }
        }
    }
}

@Composable
private fun ColorAdjustPanel(
    state: EditorState,
    onBrightness: (Float) -> Unit,
    onContrast: (Float) -> Unit,
    onSaturation: (Float) -> Unit,
    onExposure: (Float) -> Unit,
    onSharpness: (Float) -> Unit,
    onVignette: (Float) -> Unit,
    onReset: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text("Adjustments", style = MaterialTheme.typography.labelMedium, color = StudioTextHint)
            TextButton(onClick = onReset, colors = ButtonDefaults.textButtonColors(contentColor = StudioPink)) {
                Text("Reset", style = MaterialTheme.typography.labelSmall)
            }
        }
        AdjustSlider("Brightness", state.brightness, onBrightness, icon = Icons.Rounded.LightMode, accentColor = StudioAmber)
        AdjustSlider("Contrast", state.contrast, onContrast, icon = Icons.Rounded.Contrast, accentColor = StudioTextPrimary)
        AdjustSlider("Saturation", state.saturation, onSaturation, icon = Icons.Rounded.Palette, accentColor = StudioPink)
        AdjustSlider("Exposure", state.exposure, onExposure, icon = Icons.Rounded.WbSunny, accentColor = StudioAmber)
        AdjustSlider("Sharpness", state.sharpness, onSharpness, valueRange = 0f..1f, icon = Icons.Rounded.CenterFocusStrong, accentColor = StudioCyan)
        AdjustSlider("Vignette", state.vignette, onVignette, valueRange = 0f..1f, icon = Icons.Rounded.Vignette, accentColor = StudioVioletLight)
    }
}

@Composable
private fun SpeedPanel(speed: Float, onSpeed: (Float) -> Unit) {
    Column {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("Playback Speed", style = MaterialTheme.typography.labelMedium, color = StudioTextHint)
            Text("${speed}×", style = MaterialTheme.typography.labelMedium, color = StudioAmber)
        }
        Spacer(Modifier.height(8.dp))
        val speeds = listOf(0.25f, 0.5f, 0.75f, 1f, 1.25f, 1.5f, 2f, 3f, 4f)
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(speeds) { s ->
                val isSelected = speed == s
                Box(
                    Modifier
                        .background(
                            if (isSelected) StudioAmber else StudioCard,
                            RoundedCornerShape(8.dp)
                        )
                        .clickable { onSpeed(s) }
                        .padding(horizontal = 14.dp, vertical = 8.dp)
                ) {
                    Text("${s}×",
                        style = MaterialTheme.typography.labelMedium.copy(
                            color = if (isSelected) Color.Black else StudioTextSecondary,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                        )
                    )
                }
            }
        }
    }
}

@Composable
private fun AudioPanel(volume: Float, isMuted: Boolean, onVolume: (Float) -> Unit, onMute: () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text("Audio", style = MaterialTheme.typography.labelMedium, color = StudioTextHint)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(if (isMuted) Icons.Rounded.VolumeOff else Icons.Rounded.VolumeUp, null, Modifier.size(16.dp), tint = StudioGreen)
                Spacer(Modifier.width(4.dp))
                Switch(
                    checked = !isMuted, onCheckedChange = { onMute() },
                    colors = SwitchDefaults.colors(checkedThumbColor = StudioGreen, checkedTrackColor = StudioGreen.copy(0.3f))
                )
            }
        }
        AdjustSlider("Volume", volume, onVolume, valueRange = 0f..2f, icon = Icons.Rounded.VolumeUp, accentColor = StudioGreen)
    }
}

@Composable
private fun AspectRatioPanel(current: AspectRatio, onSelect: (AspectRatio) -> Unit) {
    Column {
        Text("Aspect Ratio", style = MaterialTheme.typography.labelMedium, color = StudioTextHint)
        Spacer(Modifier.height(8.dp))
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(AspectRatio.entries) { ratio ->
                val isSelected = current == ratio
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.clickable { onSelect(ratio) }
                ) {
                    val w = if (ratio.w > 0) ratio.w * 3 else 40
                    val h = if (ratio.h > 0) ratio.h * 3 else 40
                    Box(
                        Modifier
                            .width(w.coerceIn(24, 54).dp)
                            .height(h.coerceIn(24, 54).dp)
                            .background(
                                if (isSelected) StudioViolet else StudioCard,
                                RoundedCornerShape(4.dp)
                            )
                            .border(1.dp, if (isSelected) StudioViolet else StudioBorder, RoundedCornerShape(4.dp))
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(ratio.label, style = MaterialTheme.typography.bodySmall.copy(
                        color = if (isSelected) StudioViolet else StudioTextHint
                    ))
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ExportBottomSheet(
    isExporting: Boolean,
    progress: Float,
    selectedQuality: ExportQuality,
    onQualitySelect: (ExportQuality) -> Unit,
    onExport: () -> Unit,
    onCancel: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = { if (!isExporting) onCancel() },
        containerColor = StudioDeepNavy,
    ) {
        Column(Modifier.padding(16.dp).padding(bottom = 32.dp)) {
            Text("Export Video", style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold), color = StudioTextPrimary)
            Spacer(Modifier.height(16.dp))

            if (isExporting) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                    CircularProgressIndicator(
                        progress = { progress },
                        modifier = Modifier.size(72.dp),
                        color = StudioViolet,
                        strokeWidth = 6.dp
                    )
                    Spacer(Modifier.height(12.dp))
                    Text("Exporting... ${(progress * 100).toInt()}%", style = MaterialTheme.typography.bodyMedium, color = StudioTextPrimary)
                    Text("Powered by FFmpeg", style = MaterialTheme.typography.bodySmall, color = StudioTextHint)
                    Spacer(Modifier.height(20.dp))
                    OutlinedButton(onClick = onCancel, border = BorderStroke(1.dp, StudioPink)) {
                        Text("Cancel", color = StudioPink)
                    }
                }
            } else {
                Text("Quality", style = MaterialTheme.typography.labelMedium, color = StudioTextHint)
                Spacer(Modifier.height(8.dp))
                ExportQuality.entries.forEach { q ->
                    val isSelected = selectedQuality == q
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .clickable { onQualitySelect(q) }
                            .background(
                                if (isSelected) StudioViolet.copy(0.1f) else Color.Transparent,
                                RoundedCornerShape(10.dp)
                            )
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(selected = isSelected, onClick = { onQualitySelect(q) },
                            colors = RadioButtonDefaults.colors(selectedColor = StudioViolet))
                        Spacer(Modifier.width(8.dp))
                        Text(q.label, style = MaterialTheme.typography.bodyMedium.copy(
                            color = if (isSelected) StudioViolet else StudioTextPrimary
                        ))
                    }
                }
                Spacer(Modifier.height(16.dp))
                NeonButton("Export Now", onExport, Modifier.fillMaxWidth(), icon = Icons.Rounded.FileUpload)
            }
        }
    }
}

// ──────────────────────────────────────────────
// Tool strip data
// ──────────────────────────────────────────────
private val toolStrip = listOf(
    Triple(EditorTool.TRIM,       "Trim",    Icons.Rounded.ContentCut),
    Triple(EditorTool.FILTER,     "Filter",  Icons.Rounded.AutoFixHigh),
    Triple(EditorTool.BRIGHTNESS, "Adjust",  Icons.Rounded.Tune),
    Triple(EditorTool.SPEED,      "Speed",   Icons.Rounded.Speed),
    Triple(EditorTool.TEXT,       "Text",    Icons.Rounded.TextFields),
    Triple(EditorTool.AUDIO,      "Audio",   Icons.Rounded.MusicNote),
    Triple(EditorTool.REVERSE,    "Reverse", Icons.Rounded.SwapHoriz),
    Triple(EditorTool.CROP,       "Ratio",   Icons.Rounded.Crop),
    Triple(EditorTool.VIGNETTE,   "Vignette",Icons.Rounded.Vignette),
    Triple(EditorTool.EXPORT,     "Export",  Icons.Rounded.FileUpload),
)

private fun formatMs(ms: Long): String {
    val total = ms / 1000
    val m = total / 60
    val s = total % 60
    return "%d:%02d".format(m, s)
}
