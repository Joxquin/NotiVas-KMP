package me.joxquin.notivas.ui.course.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import me.joxquin.notivas.data.local.AppThemeStyle
import me.joxquin.notivas.data.model.Course
import me.joxquin.notivas.shared.resources.*
import me.joxquin.notivas.ui.dashboard.miuix.components.MiuixCourseCoverPlaceholder
import org.jetbrains.compose.resources.stringResource
import top.yukonga.miuix.kmp.basic.Card as MiuixCard
import top.yukonga.miuix.kmp.basic.CardDefaults as MiuixCardDefaults
import top.yukonga.miuix.kmp.theme.MiuixTheme
import top.yukonga.miuix.kmp.utils.PressFeedbackType

@Composable
fun CourseHeroBannerCard(
    course: Course?,
    departmentTag: String,
    professorName: String,
    promedioFormatted: String,
    avancePercentage: Int,
    completedModulesCount: Int,
    totalModulesCount: Int,
    themeStyle: AppThemeStyle,
    modifier: Modifier = Modifier
) {
    val isMiuix = themeStyle == AppThemeStyle.MIUIX

    val cardColor = if (isMiuix) MiuixTheme.colorScheme.surfaceContainer
    else MaterialTheme.colorScheme.surfaceContainer

    val onSurfaceColor = if (isMiuix) MiuixTheme.colorScheme.onSurface
    else MaterialTheme.colorScheme.onSurface

    val onSurfaceVariantColor = if (isMiuix) MiuixTheme.colorScheme.onSurfaceVariantSummary
    else MaterialTheme.colorScheme.onSurfaceVariant

    val primaryColor = if (isMiuix) MiuixTheme.colorScheme.primary
    else MaterialTheme.colorScheme.primary

    val primaryContainerColor = if (isMiuix) MiuixTheme.colorScheme.surfaceContainerHigh
    else MaterialTheme.colorScheme.primaryContainer

    val metricPillBg = if (isMiuix) MiuixTheme.colorScheme.surfaceContainerHigh
    else MaterialTheme.colorScheme.surfaceContainerHigh

    val borderColor = if (isMiuix) MiuixTheme.colorScheme.dividerLine.copy(alpha = 0.5f)
    else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)

    val cardContent = @Composable {
        Box(
            modifier = Modifier.fillMaxWidth()
        ) {
            // Capa 1: Imagen real de Canvas en cabecera con degradado suave hacia abajo
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
            ) {
                if (!course?.imageUrl.isNullOrBlank()) {
                    AsyncImage(
                        model = course.imageUrl,
                        contentDescription = course.name,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    MiuixCourseCoverPlaceholder(
                        courseId = course?.id ?: 0L,
                        modifier = Modifier.fillMaxSize()
                    )
                }

                // Degradado que desvanece la imagen imperceptiblemente hacia el fondo de la tarjeta
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colorStops = arrayOf(
                                    0.0f to cardColor.copy(alpha = 0.20f),
                                    0.45f to cardColor.copy(alpha = 0.65f),
                                    0.80f to cardColor.copy(alpha = 0.92f),
                                    1.0f to cardColor
                                )
                            )
                        )
                )
            }

            // Capa 2: Contenido textual y métricas superpuestas con legibilidad garantizada
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                // Header: Código de curso / etiqueta de sección
                Text(
                    text = (course?.courseCode ?: "CURSO").uppercase(),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.2.sp,
                    color = onSurfaceVariantColor
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Badge Pill: Departamento / Programa
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(primaryContainerColor.copy(alpha = 0.9f))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = departmentTag,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = primaryColor
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Nombre del Curso
                Text(
                    text = course?.name ?: "Detalle del Curso",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    lineHeight = 28.sp,
                    color = onSurfaceColor
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Nombre del Docente
                Text(
                    text = professorName,
                    fontSize = 13.sp,
                    color = onSurfaceVariantColor
                )

                Spacer(modifier = Modifier.height(20.dp))

                // 3 Métricas Rápidas
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Promedio
                    MetricPill(
                        value = promedioFormatted,
                        valueColor = primaryColor,
                        label = stringResource(Res.string.course_detail_stat_promedio),
                        labelColor = onSurfaceVariantColor,
                        bgColor = metricPillBg,
                        modifier = Modifier.weight(1f)
                    )

                    // Avance
                    val avanceColor = if (avancePercentage >= 70) primaryColor else onSurfaceColor
                    MetricPill(
                        value = "$avancePercentage%",
                        valueColor = avanceColor,
                        label = stringResource(Res.string.course_detail_stat_avance),
                        labelColor = onSurfaceVariantColor,
                        bgColor = metricPillBg,
                        modifier = Modifier.weight(1f)
                    )

                    // Módulos
                    MetricPill(
                        value = if (totalModulesCount > 0) "$completedModulesCount/$totalModulesCount" else "0",
                        valueColor = onSurfaceColor,
                        label = stringResource(Res.string.course_detail_stat_modulos),
                        labelColor = onSurfaceVariantColor,
                        bgColor = metricPillBg,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }

    if (isMiuix) {
        MiuixCard(
            modifier = modifier.fillMaxWidth(),
            colors = MiuixCardDefaults.defaultColors(color = cardColor),
            insideMargin = PaddingValues(0.dp),
            pressFeedbackType = PressFeedbackType.None
        ) {
            cardContent()
        }
    } else {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .background(cardColor)
                .border(1.dp, borderColor, RoundedCornerShape(24.dp))
        ) {
            cardContent()
        }
    }
}

@Composable
private fun MetricPill(
    value: String,
    valueColor: Color,
    label: String,
    labelColor: Color,
    bgColor: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(bgColor)
            .padding(vertical = 12.dp, horizontal = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = value,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = valueColor
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = label,
                fontSize = 11.sp,
                color = labelColor
            )
        }
    }
}
