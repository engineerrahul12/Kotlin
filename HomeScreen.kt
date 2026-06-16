package com.rahulsah.studio.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.*
import com.rahulsah.studio.ui.components.*
import com.rahulsah.studio.ui.theme.*

@Composable
fun HomeScreen(
    onNavigateDownload: () -> Unit,
    onNavigateLibrary: () -> Unit,
    onNavigateEditor: () -> Unit,
    onNavigateBrowser: (String) -> Unit,
) {
    StudioBackground {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 100.dp)
        ) {
            // Header
            item {
                Column(Modifier.padding(horizontal = 20.dp, vertical = 24.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text(
                                "Studio",
                                style = MaterialTheme.typography.displayMedium.copy(
                                    brush = Brush.linearGradient(listOf(StudioVioletGlow, StudioCyan)),
                                    fontWeight = FontWeight.Black
                                )
                            )
                            Text(
                                "Download · Edit · Create",
                                style = MaterialTheme.typography.bodyMedium,
                                color = StudioTextHint
                            )
                        }
                        // App icon area
                        Box(
                            Modifier
                                .size(48.dp)
                                .background(
                                    Brush.radialGradient(listOf(StudioViolet, StudioDeepNavy)),
                                    RoundedCornerShape(14.dp)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Rounded.AutoAwesome, null, tint = Color.White, modifier = Modifier.size(24.dp))
                        }
                    }
                }
            }

            // Quick Action Grid
            item {
                Text(
                    "Quick Actions",
                    style = MaterialTheme.typography.titleMedium,
                    color = StudioTextSecondary,
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
                )
                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    QuickActionCard(
                        icon = Icons.Rounded.Download,
                        label = "Download",
                        subtitle = "Any site",
                        gradient = listOf(Color(0xFF7C3AED), Color(0xFF4C1D95)),
                        modifier = Modifier.weight(1f),
                        onClick = onNavigateDownload
                    )
                    QuickActionCard(
                        icon = Icons.Rounded.MovieCreation,
                        label = "Edit",
                        subtitle = "Pro tools",
                        gradient = listOf(Color(0xFF0891B2), Color(0xFF164E63)),
                        modifier = Modifier.weight(1f),
                        onClick = onNavigateEditor
                    )
                }
                Spacer(Modifier.height(12.dp))
                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    QuickActionCard(
                        icon = Icons.Rounded.PhotoLibrary,
                        label = "Library",
                        subtitle = "Your media",
                        gradient = listOf(Color(0xFFDB2777), Color(0xFF831843)),
                        modifier = Modifier.weight(1f),
                        onClick = onNavigateLibrary
                    )
                    QuickActionCard(
                        icon = Icons.Rounded.Language,
                        label = "Browser",
                        subtitle = "Built-in",
                        gradient = listOf(Color(0xFF059669), Color(0xFF064E3B)),
                        modifier = Modifier.weight(1f),
                        onClick = { onNavigateBrowser("https://www.google.com") }
                    )
                }
            }

            // Supported Platforms
            item {
                Spacer(Modifier.height(24.dp))
                Text(
                    "Supported Platforms",
                    style = MaterialTheme.typography.titleMedium,
                    color = StudioTextSecondary,
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
                )
            }
            item {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(supportedPlatformsUi) { platform ->
                        PlatformPill(platform)
                    }
                }
            }

            // Features
            item {
                Spacer(Modifier.height(24.dp))
                Text(
                    "Editor Features",
                    style = MaterialTheme.typography.titleMedium,
                    color = StudioTextSecondary,
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
                )
            }
            items(editorFeatures) { feature ->
                FeatureRow(feature, modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp))
            }

            // Footer
            item {
                Spacer(Modifier.height(32.dp))
                Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    Text(
                        "Made by Rahul Sah  ·  Studio v1.0",
                        style = MaterialTheme.typography.bodySmall,
                        color = StudioTextHint
                    )
                }
            }
        }
    }
}

@Composable
private fun QuickActionCard(
    icon: ImageVector,
    label: String,
    subtitle: String,
    gradient: List<Color>,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Box(
        modifier
            .height(110.dp)
            .background(Brush.linearGradient(gradient), RoundedCornerShape(18.dp))
            .clickable(onClick = onClick)
            .padding(16.dp)
    ) {
        Column {
            Icon(icon, null, Modifier.size(28.dp), tint = Color.White.copy(alpha = 0.9f))
            Spacer(Modifier.weight(1f))
            Text(label, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = Color.White)
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = Color.White.copy(alpha = 0.65f))
        }
    }
}

@Composable
private fun PlatformPill(platform: PlatformUi) {
    Row(
        Modifier
            .background(StudioCard, RoundedCornerShape(20.dp))
            .border(1.dp, StudioBorder, RoundedCornerShape(20.dp))
            .padding(horizontal = 14.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            Modifier
                .size(8.dp)
                .background(Color(platform.color), androidx.compose.foundation.shape.CircleShape)
        )
        Spacer(Modifier.width(8.dp))
        Text(platform.name, style = MaterialTheme.typography.labelMedium, color = StudioTextPrimary)
    }
}

@Composable
private fun FeatureRow(feature: EditorFeatureUi, modifier: Modifier = Modifier) {
    GlassCard(modifier = modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier
                    .size(40.dp)
                    .background(feature.color.copy(alpha = 0.15f), RoundedCornerShape(10.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(feature.icon, null, Modifier.size(20.dp), tint = feature.color)
            }
            Spacer(Modifier.width(12.dp))
            Column {
                Text(feature.title, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold, color = StudioTextPrimary))
                Text(feature.desc, style = MaterialTheme.typography.bodySmall, color = StudioTextHint)
            }
        }
    }
}

// ──────────────────────────────────────────────
// Static data for Home
// ──────────────────────────────────────────────
data class PlatformUi(val name: String, val color: Long)
data class EditorFeatureUi(val title: String, val desc: String, val icon: ImageVector, val color: Color)

val supportedPlatformsUi = listOf(
    PlatformUi("YouTube", 0xFFFF0000), PlatformUi("Instagram", 0xFFE1306C),
    PlatformUi("TikTok", 0xFF69C9D0), PlatformUi("Twitter/X", 0xFF1DA1F2),
    PlatformUi("Facebook", 0xFF1877F2), PlatformUi("Vimeo", 0xFF1AB7EA),
    PlatformUi("Reddit", 0xFFFF4500), PlatformUi("SoundCloud", 0xFFFF5500),
    PlatformUi("Any Site", 0xFF7C3AED),
)

val editorFeatures = listOf(
    EditorFeatureUi("Trim & Cut", "Precise frame-level trimming and splitting", Icons.Rounded.ContentCut, StudioViolet),
    EditorFeatureUi("Filters & LUTs", "10+ cinematic color grades", Icons.Rounded.AutoFixHigh, StudioCyan),
    EditorFeatureUi("Speed Control", "0.1× slow-mo to 4× fast forward", Icons.Rounded.Speed, StudioAmber),
    EditorFeatureUi("Text & Stickers", "Animated overlays with custom fonts", Icons.Rounded.TextFields, StudioPink),
    EditorFeatureUi("Audio Editing", "Volume, mute, voiceover, background music", Icons.Rounded.MusicNote, StudioGreen),
    EditorFeatureUi("Crop & Rotate", "All aspect ratios including 9:16 for Reels", Icons.Rounded.Crop, StudioVioletLight),
    EditorFeatureUi("Color Grading", "Brightness, contrast, saturation, exposure", Icons.Rounded.Palette, Color(0xFFFFAB00)),
    EditorFeatureUi("Export HD", "Up to 4K export powered by FFmpeg", Icons.Rounded.FileUpload, StudioGreen),
)
