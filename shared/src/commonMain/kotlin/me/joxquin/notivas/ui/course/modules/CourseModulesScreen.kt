package me.joxquin.notivas.ui.course.modules

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowDropUp
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Forum
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.outlined.CloudDownload
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import me.joxquin.notivas.data.local.AppThemeStyle
import me.joxquin.notivas.shared.resources.*
import me.joxquin.notivas.ui.components.miuix.BackNavigationIcon
import me.joxquin.notivas.ui.components.miuix.MiuixBlurredBar
import me.joxquin.notivas.ui.components.miuix.MiuixPageTopAppBar
import me.joxquin.notivas.ui.components.miuix.miuixBackdropModifier
import me.joxquin.notivas.ui.components.miuix.miuixPageContentPadding
import me.joxquin.notivas.ui.components.miuix.miuixPageScrollModifiers
import me.joxquin.notivas.ui.components.miuix.rememberMiuixBlurBackdrop
import me.joxquin.notivas.ui.components.miuix.shortenCourseSubtitle
import me.joxquin.notivas.ui.course.CourseDetailViewModel
import me.joxquin.notivas.ui.course.CourseModuleItemUi
import me.joxquin.notivas.ui.course.CourseModuleUi
import org.jetbrains.compose.resources.stringResource
import top.yukonga.miuix.kmp.basic.Card as MiuixCard
import top.yukonga.miuix.kmp.basic.CardDefaults as MiuixCardDefaults
import top.yukonga.miuix.kmp.basic.MiuixScrollBehavior
import top.yukonga.miuix.kmp.basic.VerticalScrollBar
import top.yukonga.miuix.kmp.basic.rememberScrollBarAdapter
import top.yukonga.miuix.kmp.theme.MiuixTheme
import top.yukonga.miuix.kmp.utils.PressFeedbackType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CourseModulesScreen(
    viewModel: CourseDetailViewModel,
    themeStyle: AppThemeStyle,
    onBack: () -> Unit,
    onNavigateToAssignmentDraft: (Long, String) -> Unit = { _, _ -> },
    onNavigateToForoDetail: (Long) -> Unit = {},
    onOpenUrl: (String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val courseName = uiState.course?.name ?: "Curso"

    if (themeStyle == AppThemeStyle.MIUIX) {
        val scrollBehavior = MiuixScrollBehavior()
        val listState = rememberLazyListState()
        val blurBackdrop = rememberMiuixBlurBackdrop()

        top.yukonga.miuix.kmp.basic.Scaffold(
            modifier = modifier.fillMaxSize(),
            topBar = {
                MiuixBlurredBar(
                    backdrop = blurBackdrop,
                    scrollBehavior = scrollBehavior
                ) {
                    MiuixPageTopAppBar(
                        title = stringResource(Res.string.course_modules_screen_title),
                        subtitle = shortenCourseSubtitle(uiState.course?.name),
                        scrollBehavior = scrollBehavior,
                        navigationIcon = {
                            BackNavigationIcon(onClick = onBack)
                        }
                    )
                }
            }
        ) { innerPadding ->
            val contentPadding = miuixPageContentPadding(
                innerPadding = innerPadding,
                extraTop = 8.dp,
                extraBottom = 24.dp
            )

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .miuixBackdropModifier(blurBackdrop)
            ) {
                if (uiState.isLoading) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            color = MiuixTheme.colorScheme.primary,
                            modifier = Modifier.size(36.dp)
                        )
                    }
                } else if (uiState.modules.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = stringResource(Res.string.course_detail_empty_modules),
                            color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                            fontSize = 14.sp
                        )
                    }
                } else {
                    LazyColumn(
                        state = listState,
                        contentPadding = contentPadding,
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.miuixPageScrollModifiers(scrollBehavior)
                    ) {
                        items(uiState.modules, key = { it.id }) { module ->
                            ModuleExpandableCard(
                                module = module,
                                isMiuix = true,
                                onToggleExpand = { viewModel.toggleModuleExpanded(module.id) },
                                onItemClick = { item ->
                                    handleItemClick(
                                        item = item,
                                        courseName = courseName,
                                        onNavigateToAssignmentDraft = onNavigateToAssignmentDraft,
                                        onNavigateToForoDetail = onNavigateToForoDetail,
                                        onOpenUrl = onOpenUrl
                                    )
                                }
                            )
                        }
                    }

                    VerticalScrollBar(
                        adapter = rememberScrollBarAdapter(listState),
                        modifier = Modifier
                            .align(Alignment.CenterEnd)
                            .fillMaxHeight()
                            .padding(innerPadding)
                    )
                }
            }
        }
    } else {
        // Material Design 3 Scaffolding
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Column {
                            Text(
                                text = stringResource(Res.string.course_modules_screen_title),
                                style = MaterialTheme.typography.titleLarge
                            )
                            if (!uiState.course?.name.isNullOrBlank()) {
                                Text(
                                    text = uiState.course?.name.orEmpty(),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Atrás",
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface,
                        scrolledContainerColor = MaterialTheme.colorScheme.surfaceContainer
                    )
                )
            },
            containerColor = MaterialTheme.colorScheme.surface
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                if (uiState.isLoading) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(36.dp)
                        )
                    }
                } else if (uiState.modules.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = stringResource(Res.string.course_detail_empty_modules),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 14.sp
                        )
                    }
                } else {
                    LazyColumn(
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(uiState.modules, key = { it.id }) { module ->
                            ModuleExpandableCard(
                                module = module,
                                isMiuix = false,
                                onToggleExpand = { viewModel.toggleModuleExpanded(module.id) },
                                onItemClick = { item ->
                                    handleItemClick(
                                        item = item,
                                        courseName = courseName,
                                        onNavigateToAssignmentDraft = onNavigateToAssignmentDraft,
                                        onNavigateToForoDetail = onNavigateToForoDetail,
                                        onOpenUrl = onOpenUrl
                                    )
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

private fun handleItemClick(
    item: CourseModuleItemUi,
    courseName: String,
    onNavigateToAssignmentDraft: (Long, String) -> Unit,
    onNavigateToForoDetail: (Long) -> Unit,
    onOpenUrl: (String) -> Unit
) {
    if (item.type.equals("Assignment", ignoreCase = true)) {
        onNavigateToAssignmentDraft(item.assignmentId ?: item.id, courseName)
    } else if (item.type.equals("Discussion", ignoreCase = true)) {
        onNavigateToForoDetail(item.id)
    } else if (!item.actionUrl.isNullOrBlank()) {
        onOpenUrl(item.actionUrl)
    }
}

@Composable
private fun ModuleExpandableCard(
    module: CourseModuleUi,
    isMiuix: Boolean,
    onToggleExpand: () -> Unit,
    onItemClick: (CourseModuleItemUi) -> Unit,
    modifier: Modifier = Modifier
) {
    val cardColor = if (isMiuix) MiuixTheme.colorScheme.surfaceContainer
    else MaterialTheme.colorScheme.surfaceContainer

    val onSurfaceColor = if (isMiuix) MiuixTheme.colorScheme.onSurface
    else MaterialTheme.colorScheme.onSurface

    val onSurfaceVariantColor = if (isMiuix) MiuixTheme.colorScheme.onSurfaceVariantSummary
    else MaterialTheme.colorScheme.onSurfaceVariant

    val primaryColor = if (isMiuix) MiuixTheme.colorScheme.primary
    else MaterialTheme.colorScheme.primary

    val successColor = if (isMiuix) MiuixTheme.colorScheme.primary
    else Color(0xFF4CAF50)

    val cardContent = @Composable {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Fila de encabezado del módulo (como en la Imagen 2)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onToggleExpand)
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Indicador de completitud o bloqueo a la izquierda (según Imagen 1 y 2)
                if (module.isLocked) {
                    Icon(
                        imageVector = Icons.Filled.Lock,
                        contentDescription = stringResource(Res.string.course_module_locked_status),
                        tint = onSurfaceVariantColor,
                        modifier = Modifier.size(20.dp)
                    )
                } else if (module.isCompleted) {
                    Icon(
                        imageVector = Icons.Filled.Check,
                        contentDescription = "Completado",
                        tint = successColor,
                        modifier = Modifier.size(20.dp)
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .size(18.dp)
                            .clip(CircleShape)
                            .background(primaryColor.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(primaryColor)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                // Nombre completo del módulo en Canvas
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = module.name,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (module.isLocked) onSurfaceVariantColor else onSurfaceColor
                    )
                    if (module.isLocked && !module.unlockAtFormatted.isNullOrBlank()) {
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = stringResource(Res.string.course_module_unlocks_at, module.unlockAtFormatted),
                            fontSize = 11.sp,
                            color = onSurfaceVariantColor
                        )
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Chevron indicador de despliegue
                Icon(
                    imageVector = if (module.isExpanded) Icons.Filled.ArrowDropUp else Icons.Filled.ArrowDropDown,
                    contentDescription = if (module.isExpanded) "Colapsar" else "Desplegar",
                    tint = onSurfaceVariantColor,
                    modifier = Modifier.size(24.dp)
                )
            }

            // Cuerpo desplegado organizado por SubHeaders (como en la Imagen 3)
            AnimatedVisibility(
                visible = module.isExpanded,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .padding(bottom = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    if (module.isLocked) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(onSurfaceVariantColor.copy(alpha = 0.08f))
                                .padding(horizontal = 12.dp, vertical = 8.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Filled.Lock,
                                    contentDescription = null,
                                    tint = onSurfaceVariantColor,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = if (!module.unlockAtFormatted.isNullOrBlank()) {
                                        stringResource(Res.string.course_module_unlocks_at, module.unlockAtFormatted)
                                    } else {
                                        stringResource(Res.string.course_module_locked_status)
                                    },
                                    fontSize = 12.sp,
                                    color = onSurfaceVariantColor
                                )
                            }
                        }
                    }

                    if (module.sections.isEmpty()) {
                        Text(
                            text = "Este módulo no contiene elementos.",
                            fontSize = 12.sp,
                            color = onSurfaceVariantColor,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    } else {
                        module.sections.forEach { section ->
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                // Título de sección (ej. "Antes de la clase", "Durante la clase", "Después de la clase", "Recursos adicionales")
                                if (section.title.isNotBlank()) {
                                    Text(
                                        text = section.title,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = onSurfaceVariantColor,
                                        modifier = Modifier.padding(top = 4.dp, bottom = 2.dp)
                                    )
                                }

                                // Lista de elementos de la sección
                                section.items.forEach { item ->
                                    ModuleItemDetailRow(
                                        item = item,
                                        onSurfaceColor = onSurfaceColor,
                                        onSurfaceVariantColor = onSurfaceVariantColor,
                                        primaryColor = primaryColor,
                                        onClick = { if (!module.isLocked) onItemClick(item) }
                                    )
                                }
                            }
                        }
                    }
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
                .clip(RoundedCornerShape(16.dp))
                .background(cardColor)
        ) {
            cardContent()
        }
    }
}

@Composable
private fun ModuleItemDetailRow(
    item: CourseModuleItemUi,
    onSurfaceColor: Color,
    onSurfaceVariantColor: Color,
    primaryColor: Color,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 6.dp, horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Icono semántico según el tipo de elemento Canvas (como en la Imagen 3)
        val iconVector = when {
            item.type.equals("Discussion", ignoreCase = true) -> Icons.Default.Forum
            item.type.equals("Assignment", ignoreCase = true) || item.type.equals("Quiz", ignoreCase = true) -> Icons.AutoMirrored.Filled.Assignment
            item.type.equals("File", ignoreCase = true) -> Icons.Outlined.CloudDownload
            item.type.equals("ExternalUrl", ignoreCase = true) -> Icons.Default.Link
            else -> Icons.Outlined.Description
        }

        Icon(
            imageVector = iconVector,
            contentDescription = item.type,
            tint = onSurfaceVariantColor,
            modifier = Modifier.size(20.dp)
        )

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = item.title,
                fontSize = 13.sp,
                fontWeight = FontWeight.Normal,
                color = onSurfaceColor,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            // Subtítulo de puntaje o metadata
            val subtitle = item.pointsText ?: if (item.metaSubtitle.isNotBlank()) item.metaSubtitle else null
            if (subtitle != null) {
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    fontSize = 11.sp,
                    color = onSurfaceVariantColor
                )
            }
        }
    }
}
