package com.rahulsah.studio.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.*
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.*
import coil.compose.AsyncImage
import com.rahulsah.studio.data.model.*
import com.rahulsah.studio.ui.theme.*

// ──────────────────────────────────────────────
// Glowing violet gradient background
// ──────────────────────────────────────────────
@Composable
fun StudioBackground(content: @Composable () -> Unit) {
    Box(
        Modifier
            .fillMaxSize()
            .background(StudioBlack)
            .drawBehind {
                drawCircle(
                    brush = Brush.radialGradient(
                        listOf(StudioViolet.copy(alpha = 0.12f), Color.Transparent),
                        center = Offset(size.width * 0.8f, 0f),
                        radius = size.width * 0.7f
                    )
                )
                drawCircle(
                    brush = Brush.radialGradient(
                        listOf(StudioCyan.copy(alpha = 0.06f), Color.Transparent),
                        center = Offset(0f, size.height * 0.6f),
                        radius = size.width * 0.5f
                    )
                )
            }
    ) { content() }
}

// ──────────────────────────────────────────────
// Studio Top App Bar
// ──────────────────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudioTopBar(
    title: String,
    subtitle: String = "",
    showBack: Boolean = false,
    onBack: () -> Unit = {},
    actions: @Composable RowScope.() -> Unit = {}
) {
    TopAppBar(
        title = {
            Column {
                Text(
                    title,
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.ExtraBold,
                        brush = Brush.linearGradient(listOf(StudioVioletGlow, StudioCyan))
                    )
                )
                if (subtitle.isNotEmpty()) {
                    Text(subtitle, style = MaterialTheme.typography.labelSmall, color = StudioTextHint)
                }
            }
        },
        navigationIcon = {
            if (showBack) {
                IconButton(onClick = onBack) {
                    Icon(Icons.Rounded.ArrowBackIos, null, tint = StudioTextSecondary)
                }
            }
        },
        actions = actions,
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = Color.Transparent,
            titleContentColor = StudioTextPrimary
        )
    )
}

// ──────────────────────────────────────────────
// Neon Pill Button
// ──────────────────────────────────────────────
@Composable
fun NeonButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    icon: ImageVector? = null,
    loading: Boolean = false,
    color: Color = StudioViolet
) {
    Button(
        onClick = onClick,
        enabled = enabled && !loading,
        modifier = modifier.height(52.dp),
        shape = RoundedCornerShape(14.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = color,
            disabledContainerColor = color.copy(alpha = 0.3f)
        ),
        elevation = ButtonDefaults.buttonElevation(
            defaultElevation = 0.dp,
            pressedElevation = 0.dp
        )
    ) {
        Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
            if (loading) {
                CircularProgressIndicator(
                    Modifier.size(22.dp),
                    color = Color.White,
                    strokeWidth = 2.dp
                )
            } else {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.padding(horizontal = 8.dp)
                ) {
                    if (icon != null) {
                        Icon(icon, null, Modifier.size(18.dp), tint = Color.White)
                        Spacer(Modifier.width(8.dp))
                    }
                    Text(
                        text,
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        ),
                        color = Color.White
                    )
                }
            }
        }
    }
}

// ──────────────────────────────────────────────
// Glass Card
// ──────────────────────────────────────────────
@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    val base = Modifier
        .background(
            Brush.verticalGradient(listOf(StudioCard.copy(alpha = 0.9f), StudioElevated.copy(alpha = 0.7f))),
            RoundedCornerShape(16.dp)
        )
        .border(1.dp, StudioBorder.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
        .then(modifier)

    if (onClick != null) {
        Column(
            base.clickable(onClick = onClick).padding(16.dp),
            content = content
        )
    } else {
        Column(base.padding(16.dp), content = content)
    }
}

// ──────────────────────────────────────────────
// Download Item Card
// ──────────────────────────────────────────────
@Composable
fun DownloadItemCard(
    item: DownloadItem,
    onPause: () -> Unit,
    onResume: () -> Unit,
    onCancel: () -> Unit,
    onOpen: () -> Unit,
) {
    GlassCard(modifier = Modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            // Thumbnail / icon
            Box(
                Modifier
                    .size(56.dp)
                    .background(StudioElevated, RoundedCornerShape(10.dp))
                    .clip(RoundedCornerShape(10.dp)),
                contentAlignment = Alignment.Center
            ) {
                if (item.thumbnailUrl.isNotEmpty()) {
                    AsyncImage(item.thumbnailUrl, null, contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize())
                } else {
                    Icon(
                        when (item.mediaType) {
                            MediaType.AUDIO -> Icons.Rounded.MusicNote
                            MediaType.IMAGE -> Icons.Rounded.Image
                            else -> Icons.Rounded.VideoFile
                        },
                        null, tint = StudioVioletLight, modifier = Modifier.size(26.dp)
                    )
                }
                // Platform badge
                if (item.platform.isNotEmpty()) {
                    Box(
                        Modifier
                            .align(Alignment.BottomEnd)
                            .size(16.dp)
                            .background(StudioViolet, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(item.platform.first().uppercase(), style = MaterialTheme.typography.bodySmall.copy(fontSize = 8.sp), color = Color.White)
                    }
                }
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(item.title, maxLines = 2, overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.bodyMedium.copy(color = StudioTextPrimary, fontWeight = FontWeight.SemiBold))
                Spacer(Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    StatusChip(item.status)
                    if (item.quality != VideoQuality.AUDIO_ONLY) {
                        Spacer(Modifier.width(6.dp))
                        Text(item.quality.label, style = MaterialTheme.typography.labelSmall, color = StudioTextHint)
                    }
                }
                if (item.status == DownloadStatus.DOWNLOADING) {
                    Spacer(Modifier.height(6.dp))
                    LinearProgressIndicator(
                        progress = { item.progress },
                        modifier = Modifier.fillMaxWidth().height(3.dp).clip(RoundedCornerShape(2.dp)),
                        color = StudioViolet,
                        trackColor = StudioBorder
                    )
                }
            }
            Spacer(Modifier.width(8.dp))
            // Actions
            when (item.status) {
                DownloadStatus.DOWNLOADING -> IconButton(onClick = onPause) {
                    Icon(Icons.Rounded.Pause, null, tint = StudioAmber, modifier = Modifier.size(20.dp))
                }
                DownloadStatus.PAUSED -> IconButton(onClick = onResume) {
                    Icon(Icons.Rounded.PlayArrow, null, tint = StudioGreen, modifier = Modifier.size(20.dp))
                }
                DownloadStatus.COMPLETED -> IconButton(onClick = onOpen) {
                    Icon(Icons.Rounded.FolderOpen, null, tint = StudioCyan, modifier = Modifier.size(20.dp))
                }
                else -> IconButton(onClick = onCancel) {
                    Icon(Icons.Rounded.Close, null, tint = StudioPink, modifier = Modifier.size(20.dp))
                }
            }
        }
    }
}

@Composable
fun StatusChip(status: DownloadStatus) {
    val (label, color) = when (status) {
        DownloadStatus.QUEUED      -> "Queued" to StudioAmber
        DownloadStatus.DOWNLOADING -> "Downloading" to StudioCyan
        DownloadStatus.PAUSED      -> "Paused" to StudioAmber
        DownloadStatus.COMPLETED   -> "Done" to StudioGreen
        DownloadStatus.FAILED      -> "Failed" to StudioPink
    }
    Box(
        Modifier
            .background(color.copy(alpha = 0.15f), RoundedCornerShape(4.dp))
            .padding(horizontal = 6.dp, vertical = 2.dp)
    ) {
        Text(label, style = MaterialTheme.typography.labelSmall.copy(color = color))
    }
}

// ──────────────────────────────────────────────
// Adjustable Slider Row
// ──────────────────────────────────────────────
@Composable
fun AdjustSlider(
    label: String,
    value: Float,
    onValueChange: (Float) -> Unit,
    valueRange: ClosedFloatingPointRange<Float> = -1f..1f,
    icon: ImageVector,
    accentColor: Color = StudioViolet
) {
    Column(Modifier.fillMaxWidth()) {
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(icon, null, Modifier.size(16.dp), tint = accentColor)
                Spacer(Modifier.width(6.dp))
                Text(label, style = MaterialTheme.typography.labelMedium)
            }
            Text(
                "${(value * 100).toInt()}",
                style = MaterialTheme.typography.labelSmall.copy(color = accentColor)
            )
        }
        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = valueRange,
            modifier = Modifier.fillMaxWidth().height(32.dp),
            colors = SliderDefaults.colors(
                thumbColor = accentColor,
                activeTrackColor = accentColor,
                inactiveTrackColor = StudioBorder
            )
        )
    }
}

// ──────────────────────────────────────────────
// Filter Chip
// ──────────────────────────────────────────────
@Composable
fun FilterChip(
    filter: VideoFilter,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .width(72.dp)
            .clickable(onClick = onClick)
            .padding(4.dp)
    ) {
        Box(
            Modifier
                .size(56.dp)
                .background(Color(filter.previewColor), RoundedCornerShape(10.dp))
                .then(
                    if (isSelected) Modifier.border(2.dp, StudioViolet, RoundedCornerShape(10.dp))
                    else Modifier
                )
        )
        Spacer(Modifier.height(4.dp))
        Text(
            filter.name,
            style = MaterialTheme.typography.bodySmall.copy(
                color = if (isSelected) StudioViolet else StudioTextSecondary,
                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
            ),
            maxLines = 1
        )
    }
}

// ──────────────────────────────────────────────
// Bottom Nav Bar
// ──────────────────────────────────────────────
data class NavItem(val label: String, val icon: ImageVector, val route: String)

@Composable
fun StudioBottomNav(
    currentRoute: String,
    onNavigate: (String) -> Unit
) {
    val items = listOf(
        NavItem("Home", Icons.Rounded.Home, "home"),
        NavItem("Download", Icons.Rounded.Download, "downloader"),
        NavItem("Library", Icons.Rounded.PhotoLibrary, "library"),
        NavItem("Editor", Icons.Rounded.MovieCreation, "editor"),
    )
    NavigationBar(
        containerColor = StudioDeepNavy,
        tonalElevation = 0.dp,
        modifier = Modifier.border(
            width = 0.5.dp,
            color = StudioBorder,
            shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
        )
    ) {
        items.forEach { item ->
            val selected = currentRoute.startsWith(item.route)
            NavigationBarItem(
                selected = selected,
                onClick = { onNavigate(item.route) },
                icon = {
                    Icon(
                        item.icon,
                        null,
                        Modifier.size(22.dp),
                        tint = if (selected) StudioViolet else StudioTextHint
                    )
                },
                label = {
                    Text(
                        item.label,
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = if (selected) StudioViolet else StudioTextHint,
                            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal
                        )
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    indicatorColor = StudioViolet.copy(alpha = 0.15f)
                )
            )
        }
    }
}

// ──────────────────────────────────────────────
// Quality Selector Sheet Item
// ──────────────────────────────────────────────
@Composable
fun QualityOption(
    quality: VideoQuality,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Row(
        Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .background(
                if (isSelected) StudioViolet.copy(alpha = 0.1f) else Color.Transparent,
                RoundedCornerShape(10.dp)
            )
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            if (quality == VideoQuality.AUDIO_ONLY) Icons.Rounded.MusicNote else Icons.Rounded.Videocam,
            null, Modifier.size(20.dp),
            tint = if (isSelected) StudioViolet else StudioTextSecondary
        )
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(
                quality.label,
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = if (isSelected) StudioViolet else StudioTextPrimary,
                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
                )
            )
        }
        if (isSelected) {
            Icon(Icons.Rounded.CheckCircle, null, Modifier.size(20.dp), tint = StudioViolet)
        }
    }
}

// ──────────────────────────────────────────────
// Empty State
// ──────────────────────────────────────────────
@Composable
fun EmptyState(icon: ImageVector, title: String, subtitle: String) {
    Column(
        Modifier.fillMaxWidth().padding(48.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            icon, null,
            Modifier.size(64.dp),
            tint = StudioViolet.copy(alpha = 0.4f)
        )
        Spacer(Modifier.height(16.dp))
        Text(title, style = MaterialTheme.typography.titleMedium, color = StudioTextSecondary)
        Spacer(Modifier.height(6.dp))
        Text(subtitle, style = MaterialTheme.typography.bodySmall, color = StudioTextHint)
    }
}
