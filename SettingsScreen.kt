package com.rahulsah.studio.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.*
import com.rahulsah.studio.ui.components.*
import com.rahulsah.studio.ui.theme.*

@Composable
fun SettingsScreen(onBack: () -> Unit) {
    var defaultQuality by remember { mutableStateOf(1) } // 0=720p 1=1080p 2=4K
    var saveToGallery  by remember { mutableStateOf(true) }
    var darkMode       by remember { mutableStateOf(true) }
    var autoAnalyze    by remember { mutableStateOf(true) }
    var wifiOnly       by remember { mutableStateOf(false) }

    StudioBackground {
        Column(Modifier.fillMaxSize()) {
            StudioTopBar("Settings", showBack = true, onBack = onBack)

            LazyColumn(
                Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Download settings
                item {
                    SettingsSection("Download") {
                        SettingsToggle(
                            icon = Icons.Rounded.Wifi,
                            title = "Wi-Fi only",
                            subtitle = "Download only on Wi-Fi",
                            checked = wifiOnly,
                            onToggle = { wifiOnly = it }
                        )
                        HorizontalDivider(color = StudioBorder)
                        SettingsToggle(
                            icon = Icons.Rounded.PhotoLibrary,
                            title = "Save to Gallery",
                            subtitle = "Add downloads to device gallery",
                            checked = saveToGallery,
                            onToggle = { saveToGallery = it }
                        )
                        HorizontalDivider(color = StudioBorder)
                        SettingsToggle(
                            icon = Icons.Rounded.AutoMode,
                            title = "Auto-Analyze",
                            subtitle = "Analyze URL when clipboard changes",
                            checked = autoAnalyze,
                            onToggle = { autoAnalyze = it }
                        )
                        HorizontalDivider(color = StudioBorder)
                        SettingsPicker(
                            icon = Icons.Rounded.HighQuality,
                            title = "Default Quality",
                            options = listOf("720p", "1080p", "4K"),
                            selected = defaultQuality,
                            onSelect = { defaultQuality = it }
                        )
                    }
                }

                // Editor settings
                item {
                    SettingsSection("Editor") {
                        SettingsToggle(
                            icon = Icons.Rounded.DarkMode,
                            title = "Dark Theme",
                            subtitle = "Always use dark editor theme",
                            checked = darkMode,
                            onToggle = { darkMode = it }
                        )
                    }
                }

                // About
                item {
                    SettingsSection("About") {
                        SettingsInfoRow(Icons.Rounded.Info, "Version", "Studio v1.0")
                        HorizontalDivider(color = StudioBorder)
                        SettingsInfoRow(Icons.Rounded.Person, "Developer", "Rahul Sah")
                        HorizontalDivider(color = StudioBorder)
                        SettingsInfoRow(Icons.Rounded.LocationOn, "Location", "Birgunj, Nepal")
                        HorizontalDivider(color = StudioBorder)
                        SettingsInfoRow(Icons.Rounded.Code, "Tech Stack", "Kotlin · Compose · FFmpeg")
                    }
                }

                item { Spacer(Modifier.height(40.dp)) }
            }
        }
    }
}

@Composable
private fun SettingsSection(title: String, content: @Composable ColumnScope.() -> Unit) {
    Column {
        Text(
            title.uppercase(),
            style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.5.sp),
            color = StudioViolet,
            modifier = Modifier.padding(start = 4.dp, bottom = 8.dp)
        )
        GlassCard(modifier = Modifier.fillMaxWidth()) { content() }
    }
}

@Composable
private fun SettingsToggle(
    icon: ImageVector, title: String, subtitle: String,
    checked: Boolean, onToggle: (Boolean) -> Unit
) {
    Row(
        Modifier.fillMaxWidth().padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, null, Modifier.size(20.dp), tint = StudioVioletLight)
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.bodyMedium.copy(
                color = StudioTextPrimary, fontWeight = FontWeight.Medium
            ))
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = StudioTextHint)
        }
        Switch(
            checked = checked, onCheckedChange = onToggle,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = StudioViolet,
                uncheckedThumbColor = StudioTextHint,
                uncheckedTrackColor = StudioBorder
            )
        )
    }
}

@Composable
private fun SettingsPicker(
    icon: ImageVector, title: String,
    options: List<String>, selected: Int, onSelect: (Int) -> Unit
) {
    Row(Modifier.fillMaxWidth().padding(vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, null, Modifier.size(20.dp), tint = StudioVioletLight)
        Spacer(Modifier.width(12.dp))
        Text(title, style = MaterialTheme.typography.bodyMedium.copy(color = StudioTextPrimary, fontWeight = FontWeight.Medium), modifier = Modifier.weight(1f))
        options.forEachIndexed { idx, opt ->
            val isSelected = idx == selected
            Box(
                Modifier
                    .padding(start = 4.dp)
                    .then(
                        if (isSelected) Modifier.then(
                            Modifier
                        ) else Modifier
                    )
            ) {
                FilterChip(
                    selected = isSelected,
                    onClick = { onSelect(idx) },
                    label = { Text(opt, style = MaterialTheme.typography.labelSmall) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = StudioViolet,
                        selectedLabelColor = Color.White
                    ),
                    border = FilterChipDefaults.filterChipBorder(
                        enabled = true, selected = isSelected,
                        borderColor = StudioBorder,
                        selectedBorderColor = StudioViolet
                    )
                )
            }
        }
    }
}

@Composable
private fun SettingsInfoRow(icon: ImageVector, label: String, value: String) {
    Row(
        Modifier.fillMaxWidth().padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, null, Modifier.size(18.dp), tint = StudioTextHint)
        Spacer(Modifier.width(12.dp))
        Text(label, style = MaterialTheme.typography.bodyMedium.copy(color = StudioTextSecondary), modifier = Modifier.weight(1f))
        Text(value, style = MaterialTheme.typography.bodySmall.copy(color = StudioVioletLight))
    }
}
