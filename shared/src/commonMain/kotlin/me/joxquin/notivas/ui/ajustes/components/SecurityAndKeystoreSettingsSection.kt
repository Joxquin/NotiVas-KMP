package me.joxquin.notivas.ui.ajustes.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.NoAccounts
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import me.joxquin.notivas.data.local.AppThemeStyle
import top.yukonga.miuix.kmp.basic.Card as MiuixCard
import top.yukonga.miuix.kmp.basic.SmallTitle
import top.yukonga.miuix.kmp.preference.ArrowPreference
import top.yukonga.miuix.kmp.preference.SwitchPreference
import top.yukonga.miuix.kmp.theme.MiuixTheme

import org.jetbrains.compose.resources.stringResource
import me.joxquin.notivas.shared.resources.Res
import me.joxquin.notivas.shared.resources.settings_security_section_title
import me.joxquin.notivas.shared.resources.biometric_lock_title
import me.joxquin.notivas.shared.resources.biometric_lock_desc
import me.joxquin.notivas.shared.resources.api_diagnostics_title
import me.joxquin.notivas.shared.resources.api_diagnostics_summary
import me.joxquin.notivas.shared.resources.last_sync_label
import me.joxquin.notivas.shared.resources.api_latency_label
import me.joxquin.notivas.shared.resources.logout_button
import me.joxquin.notivas.shared.resources.logout_button_desc

@Composable
fun SecurityAndKeystoreSettingsSection(
    biometricLock: Boolean,
    onBiometricLockChange: (Boolean) -> Unit,
    lastSyncText: String,
    apiLatencyMs: Int,
    onRequestLogout: () -> Unit,
    themeStyle: AppThemeStyle = AppThemeStyle.MATERIAL,
    modifier: Modifier = Modifier
) {
    if (themeStyle == AppThemeStyle.MIUIX) {
        Column(modifier = modifier.fillMaxWidth()) {
            SmallTitle(text = stringResource(Res.string.settings_security_section_title))
            MiuixCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp)
            ) {
                SwitchPreference(
                    title = stringResource(Res.string.biometric_lock_title),
                    summary = stringResource(Res.string.biometric_lock_desc),
                    checked = biometricLock,
                    onCheckedChange = onBiometricLockChange
                )
                ArrowPreference(
                    title = stringResource(Res.string.api_diagnostics_title),
                    summary = stringResource(Res.string.api_diagnostics_summary, lastSyncText, apiLatencyMs)
                )
                ArrowPreference(
                    title = stringResource(Res.string.logout_button),
                    summary = stringResource(Res.string.logout_button_desc),
                    onClick = onRequestLogout
                )
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
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Security,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(22.dp)
                    )
                    Text(
                        text = stringResource(Res.string.settings_security_section_title),
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                // Biometric lock row
                MaterialNotificationToggleRow(
                    icon = Icons.Default.Fingerprint,
                    title = stringResource(Res.string.biometric_lock_title),
                    subtitle = stringResource(Res.string.biometric_lock_desc),
                    checked = biometricLock,
                    onCheckedChange = onBiometricLockChange
                )

                HorizontalDivider(color = MaterialTheme.colorScheme.surfaceContainerHigh)

                // Diagnóstico de API
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.surfaceContainerLow,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Speed,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.secondary,
                                modifier = Modifier.size(20.dp)
                            )
                            Column {
                                Text(
                                    text = stringResource(Res.string.api_diagnostics_title),
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = stringResource(Res.string.last_sync_label, lastSyncText),
                                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.secondaryContainer
                        ) {
                            Text(
                                text = "$apiLatencyMs ms",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSecondaryContainer,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }

                // Botón Desvincular Cuenta
                Button(
                    onClick = onRequestLogout,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer,
                        contentColor = MaterialTheme.colorScheme.onErrorContainer
                    ),
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(
                        horizontal = 14.dp,
                        vertical = 10.dp
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Logout,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = stringResource(Res.string.logout_button),
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
            }
        }
    }
}
