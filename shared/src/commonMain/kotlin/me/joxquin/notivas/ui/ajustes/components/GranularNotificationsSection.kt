package me.joxquin.notivas.ui.ajustes.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import me.joxquin.notivas.data.local.AppThemeStyle
import top.yukonga.miuix.kmp.basic.ButtonDefaults as MiuixButtonDefaults
import top.yukonga.miuix.kmp.basic.Card as MiuixCard
import top.yukonga.miuix.kmp.basic.SmallTitle
import top.yukonga.miuix.kmp.basic.TextButton as MiuixTextButton
import top.yukonga.miuix.kmp.preference.ArrowPreference
import top.yukonga.miuix.kmp.preference.SwitchPreference
import top.yukonga.miuix.kmp.preference.WindowDropdownPreference
import top.yukonga.miuix.kmp.theme.MiuixTheme
import top.yukonga.miuix.kmp.window.WindowDialog

import org.jetbrains.compose.resources.stringResource
import me.joxquin.notivas.shared.resources.Res
import me.joxquin.notivas.shared.resources.settings_notif_section_title
import me.joxquin.notivas.shared.resources.notif_24h_title
import me.joxquin.notivas.shared.resources.notif_24h_desc
import me.joxquin.notivas.shared.resources.notif_3h_title
import me.joxquin.notivas.shared.resources.notif_3h_desc
import me.joxquin.notivas.shared.resources.notif_30m_title
import me.joxquin.notivas.shared.resources.notif_30m_desc
import me.joxquin.notivas.shared.resources.notif_morning_title
import me.joxquin.notivas.shared.resources.notif_morning_desc
import me.joxquin.notivas.shared.resources.sync_interval_title
import me.joxquin.notivas.shared.resources.sync_interval_desc
import me.joxquin.notivas.shared.resources.sync_interval_dialog_title
import me.joxquin.notivas.shared.resources.sync_interval_dialog_desc
import me.joxquin.notivas.shared.resources.sync_interval_15m
import me.joxquin.notivas.shared.resources.sync_interval_30m
import me.joxquin.notivas.shared.resources.sync_interval_1h
import me.joxquin.notivas.shared.resources.sync_interval_3h
import me.joxquin.notivas.shared.resources.sync_interval_6h
import me.joxquin.notivas.shared.resources.sync_interval_custom_format
import me.joxquin.notivas.shared.resources.close
import me.joxquin.notivas.shared.resources.change
import me.joxquin.notivas.shared.resources.onboarding_battery_title
import me.joxquin.notivas.shared.resources.onboarding_battery_desc
import me.joxquin.notivas.shared.resources.onboarding_alarms_title
import me.joxquin.notivas.shared.resources.onboarding_alarms_desc
import me.joxquin.notivas.shared.resources.onboarding_miui_title
import me.joxquin.notivas.shared.resources.onboarding_miui_desc
import me.joxquin.notivas.shared.resources.onboarding_miui_autostart_btn
import me.joxquin.notivas.shared.resources.permission_status_granted
import me.joxquin.notivas.shared.resources.permission_status_action
import me.joxquin.notivas.shared.resources.settings_battery_desc
import me.joxquin.notivas.shared.resources.settings_alarms_desc
import me.joxquin.notivas.shared.resources.settings_miui_desc
import androidx.compose.material.icons.outlined.BatteryChargingFull
import androidx.compose.material.icons.outlined.RocketLaunch
import me.joxquin.notivas.shared.resources.morning_briefing_api_key_required_title
import me.joxquin.notivas.shared.resources.morning_briefing_api_key_required_desc
import me.joxquin.notivas.shared.resources.morning_briefing_configure_api_key
import me.joxquin.notivas.shared.resources.cancel

@Composable
fun GranularNotificationsSection(
    notif24h: Boolean,
    notif3h: Boolean,
    notif30m: Boolean,
    notifMorning: Boolean,
    syncIntervalMinutes: Long,
    openRouterApiKey: String? = null,
    onNotif24hChange: (Boolean) -> Unit,
    onNotif3hChange: (Boolean) -> Unit,
    onNotif30mChange: (Boolean) -> Unit,
    onNotifMorningChange: (Boolean) -> Unit,
    onSyncIntervalChange: (Long) -> Unit,
    onNavigateToCopilotSettings: () -> Unit = {},
    themeStyle: AppThemeStyle = AppThemeStyle.MATERIAL,
    modifier: Modifier = Modifier
) {
    var showSyncIntervalDialog by remember { mutableStateOf(false) }
    var showApiKeyRequiredDialog by remember { mutableStateOf(false) }
    val permissionManager = me.joxquin.notivas.util.rememberBackgroundPermissionManager()

    val handleMorningToggle: (Boolean) -> Unit = { enabling ->
        if (enabling && openRouterApiKey.isNullOrBlank()) {
            showApiKeyRequiredDialog = true
        } else {
            onNotifMorningChange(enabling)
        }
    }

    if (showApiKeyRequiredDialog) {
        if (themeStyle == AppThemeStyle.MIUIX) {
            WindowDialog(
                show = showApiKeyRequiredDialog,
                title = stringResource(Res.string.morning_briefing_api_key_required_title),
                summary = stringResource(Res.string.morning_briefing_api_key_required_desc),
                onDismissRequest = { showApiKeyRequiredDialog = false },
                content = {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        MiuixTextButton(
                            text = stringResource(Res.string.cancel),
                            onClick = { showApiKeyRequiredDialog = false },
                            modifier = Modifier.weight(1f)
                        )
                        Spacer(Modifier.width(16.dp))
                        MiuixTextButton(
                            text = stringResource(Res.string.morning_briefing_configure_api_key),
                            onClick = {
                                showApiKeyRequiredDialog = false
                                onNavigateToCopilotSettings()
                            },
                            modifier = Modifier.weight(1f),
                            colors = MiuixButtonDefaults.textButtonColorsPrimary()
                        )
                    }
                }
            )
        } else {
            AlertDialog(
                onDismissRequest = { showApiKeyRequiredDialog = false },
                icon = {
                    Icon(
                        imageVector = Icons.Default.Key,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                },
                title = {
                    Text(
                        text = stringResource(Res.string.morning_briefing_api_key_required_title),
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                    )
                },
                text = {
                    Text(
                        text = stringResource(Res.string.morning_briefing_api_key_required_desc),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            showApiKeyRequiredDialog = false
                            onNavigateToCopilotSettings()
                        }
                    ) {
                        Text(stringResource(Res.string.morning_briefing_configure_api_key))
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showApiKeyRequiredDialog = false }) {
                        Text(stringResource(Res.string.cancel))
                    }
                }
            )
        }
    }

    val sync15 = stringResource(Res.string.sync_interval_15m)
    val sync30 = stringResource(Res.string.sync_interval_30m)
    val sync60 = stringResource(Res.string.sync_interval_1h)
    val sync180 = stringResource(Res.string.sync_interval_3h)
    val sync360 = stringResource(Res.string.sync_interval_6h)

    val syncOptions = remember(sync15, sync30, sync60, sync180, sync360) {
        listOf(
            15L to sync15,
            30L to sync30,
            60L to sync60,
            180L to sync180,
            360L to sync360
        )
    }

    val currentLabel = syncOptions.firstOrNull { it.first == syncIntervalMinutes }?.second
        ?: stringResource(Res.string.sync_interval_custom_format, syncIntervalMinutes)

    if (showSyncIntervalDialog && themeStyle == AppThemeStyle.MATERIAL) {
        AlertDialog(
            onDismissRequest = { showSyncIntervalDialog = false },
            icon = {
                Icon(
                    imageVector = Icons.Default.Sync,
                    contentDescription = null,
                    tint = if (themeStyle == AppThemeStyle.MIUIX) MiuixTheme.colorScheme.primary else MaterialTheme.colorScheme.primary
                )
            },
            title = {
                Text(
                    text = stringResource(Res.string.sync_interval_dialog_title),
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold
                    )
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = stringResource(Res.string.sync_interval_dialog_desc),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    syncOptions.forEach { (minutes, label) ->
                        val isSelected = minutes == syncIntervalMinutes
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (isSelected) {
                                if (themeStyle == AppThemeStyle.MIUIX) MiuixTheme.colorScheme.primary.copy(alpha = 0.2f) else MaterialTheme.colorScheme.primaryContainer
                            } else {
                                if (themeStyle == AppThemeStyle.MIUIX) MiuixTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.surfaceContainerLow
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    onSyncIntervalChange(minutes)
                                    showSyncIntervalDialog = false
                                }
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = label,
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                    ),
                                    color = if (isSelected) {
                                        if (themeStyle == AppThemeStyle.MIUIX) MiuixTheme.colorScheme.primary else MaterialTheme.colorScheme.onPrimaryContainer
                                    } else {
                                        MaterialTheme.colorScheme.onSurface
                                    }
                                )
                                if (isSelected) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = null,
                                        tint = if (themeStyle == AppThemeStyle.MIUIX) MiuixTheme.colorScheme.primary else MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showSyncIntervalDialog = false }) {
                    Text(stringResource(Res.string.close))
                }
            }
        )
    }

    if (themeStyle == AppThemeStyle.MIUIX) {
        Column(modifier = modifier.fillMaxWidth()) {
            SmallTitle(text = stringResource(Res.string.settings_notif_section_title))
            MiuixCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp)
            ) {
                SwitchPreference(
                    title = stringResource(Res.string.notif_24h_title),
                    summary = stringResource(Res.string.notif_24h_desc),
                    checked = notif24h,
                    onCheckedChange = onNotif24hChange
                )
                SwitchPreference(
                    title = stringResource(Res.string.notif_3h_title),
                    summary = stringResource(Res.string.notif_3h_desc),
                    checked = notif3h,
                    onCheckedChange = onNotif3hChange
                )
                SwitchPreference(
                    title = stringResource(Res.string.notif_30m_title),
                    summary = stringResource(Res.string.notif_30m_desc),
                    checked = notif30m,
                    onCheckedChange = onNotif30mChange
                )
                SwitchPreference(
                    title = stringResource(Res.string.notif_morning_title),
                    summary = stringResource(Res.string.notif_morning_desc),
                    checked = notifMorning,
                    onCheckedChange = handleMorningToggle
                )
                val selectedSyncIndex = syncOptions.indexOfFirst { it.first == syncIntervalMinutes }.coerceAtLeast(0)
                WindowDropdownPreference(
                    title = stringResource(Res.string.sync_interval_title),
                    summary = currentLabel,
                    items = syncOptions.map { it.second },
                    selectedIndex = selectedSyncIndex,
                    onSelectedIndexChange = { index ->
                        onSyncIntervalChange(syncOptions[index].first)
                    }
                )
                ArrowPreference(
                    title = stringResource(Res.string.onboarding_battery_title),
                    summary = if (permissionManager.isBatteryOptimizationIgnored()) {
                        stringResource(Res.string.permission_status_granted)
                    } else {
                        stringResource(Res.string.permission_status_action)
                    },
                    onClick = { permissionManager.requestIgnoreBatteryOptimization() }
                )
                ArrowPreference(
                    title = stringResource(Res.string.onboarding_alarms_title),
                    summary = if (permissionManager.canScheduleExactAlarms()) {
                        stringResource(Res.string.permission_status_granted)
                    } else {
                        stringResource(Res.string.permission_status_action)
                    },
                    onClick = { permissionManager.requestScheduleExactAlarms() }
                )
                if (permissionManager.isMiuiOrHyperOs()) {
                    ArrowPreference(
                        title = stringResource(Res.string.onboarding_miui_title),
                        summary = stringResource(Res.string.onboarding_miui_autostart_btn),
                        onClick = { permissionManager.openMiuiAutostartSettings() }
                    )
                }
            }
        }
    } else {
        Card(
            modifier = modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 6.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainer
            )
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(bottom = 4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.NotificationsActive,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(22.dp)
                    )
                    Text(
                        text = stringResource(Res.string.settings_notif_section_title),
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                // Item 1: 24h
                MaterialNotificationToggleRow(
                    icon = Icons.Default.CalendarToday,
                    title = stringResource(Res.string.notif_24h_title),
                    subtitle = stringResource(Res.string.notif_24h_desc),
                    checked = notif24h,
                    onCheckedChange = onNotif24hChange
                )

                HorizontalDivider(color = MaterialTheme.colorScheme.surfaceContainerHigh)

                // Item 2: 3h
                MaterialNotificationToggleRow(
                    icon = Icons.Default.Alarm,
                    title = stringResource(Res.string.notif_3h_title),
                    subtitle = stringResource(Res.string.notif_3h_desc),
                    checked = notif3h,
                    onCheckedChange = onNotif3hChange
                )

                HorizontalDivider(color = MaterialTheme.colorScheme.surfaceContainerHigh)

                // Item 3: 30m
                MaterialNotificationToggleRow(
                    icon = Icons.Default.WarningAmber,
                    title = stringResource(Res.string.notif_30m_title),
                    subtitle = stringResource(Res.string.notif_30m_desc),
                    checked = notif30m,
                    onCheckedChange = onNotif30mChange
                )

                HorizontalDivider(color = MaterialTheme.colorScheme.surfaceContainerHigh)

                // Item 4: Morning Briefing
                MaterialNotificationToggleRow(
                    icon = Icons.Default.WbSunny,
                    title = stringResource(Res.string.notif_morning_title),
                    subtitle = stringResource(Res.string.notif_morning_desc),
                    checked = notifMorning,
                    onCheckedChange = handleMorningToggle
                )

                HorizontalDivider(color = MaterialTheme.colorScheme.surfaceContainerHigh)

                // Item 5: Sync interval
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { showSyncIntervalDialog = true }
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.surfaceContainerHigh,
                            modifier = Modifier.size(40.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Sync,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }

                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = stringResource(Res.string.sync_interval_title),
                                style = MaterialTheme.typography.bodyLarge.copy(
                                    fontWeight = FontWeight.SemiBold
                                ),
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = currentLabel,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.secondaryContainer,
                        modifier = Modifier.padding(start = 8.dp)
                    ) {
                        Text(
                            text = stringResource(Res.string.change),
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.SemiBold
                            ),
                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.surfaceContainerHigh)

                // Diagnóstico y Permisos de Batería y Alarmas
                MaterialNotificationActionRow(
                    icon = Icons.Outlined.BatteryChargingFull,
                    title = stringResource(Res.string.onboarding_battery_title),
                    subtitle = stringResource(Res.string.settings_battery_desc),
                    actionLabel = if (permissionManager.isBatteryOptimizationIgnored()) {
                        stringResource(Res.string.permission_status_granted)
                    } else {
                        stringResource(Res.string.permission_status_action)
                    },
                    isActionActive = permissionManager.isBatteryOptimizationIgnored(),
                    onClick = { permissionManager.requestIgnoreBatteryOptimization() }
                )

                HorizontalDivider(color = MaterialTheme.colorScheme.surfaceContainerHigh)

                MaterialNotificationActionRow(
                    icon = Icons.Default.NotificationsActive,
                    title = stringResource(Res.string.onboarding_alarms_title),
                    subtitle = stringResource(Res.string.settings_alarms_desc),
                    actionLabel = if (permissionManager.canScheduleExactAlarms()) {
                        stringResource(Res.string.permission_status_granted)
                    } else {
                        stringResource(Res.string.permission_status_action)
                    },
                    isActionActive = permissionManager.canScheduleExactAlarms(),
                    onClick = { permissionManager.requestScheduleExactAlarms() }
                )

                if (permissionManager.isMiuiOrHyperOs()) {
                    HorizontalDivider(color = MaterialTheme.colorScheme.surfaceContainerHigh)

                    MaterialNotificationActionRow(
                        icon = Icons.Outlined.RocketLaunch,
                        title = stringResource(Res.string.onboarding_miui_title),
                        subtitle = stringResource(Res.string.settings_miui_desc),
                        actionLabel = stringResource(Res.string.onboarding_miui_autostart_btn),
                        isActionActive = true,
                        actionContainerColor = MaterialTheme.colorScheme.secondaryContainer,
                        actionContentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                        onClick = { permissionManager.openMiuiAutostartSettings() }
                    )
                }
            }
        }
    }
}
