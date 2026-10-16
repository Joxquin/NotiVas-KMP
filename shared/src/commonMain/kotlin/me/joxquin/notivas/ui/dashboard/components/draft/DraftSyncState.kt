package me.joxquin.notivas.ui.dashboard.components.draft

/**
 * Estados del ciclo de vida de un borrador sincronizado con Google Drive / Canvas.
 */
enum class DraftSyncStage {
    IDLE_BEFORE_SYNC,
    SYNCING,
    SYNCED_AFTER_SYNC
}

/**
 * Estado canónico del borrador de Drive.
 */
enum class DraftStatusEnum(val label: String) {
    NOT_STARTED("Sin iniciar"),
    IN_PROGRESS("En curso"),
    READY_TO_SUBMIT("Listo para entregar"),
    SUBMITTED("Entregada")
}

/**
 * Información del borrador y su ubicación en Google Drive.
 */
data class DraftSyncInfo(
    val fileName: String,
    val driveUrl: String = "",
    val driveFileId: String? = null,
    val lastEditedTime: String = "Hace un momento",
    val accountEmail: String = "usuario@institucional.edu",
    val detectedTemplate: String? = null,
    val courseFolder: String = "General",
    val draftStatus: DraftStatusEnum = DraftStatusEnum.IN_PROGRESS,
    val hasSubmitted: Boolean = false
)
