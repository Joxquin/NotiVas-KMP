package me.joxquin.notivas.ui.ajustes

import me.joxquin.notivas.shared.resources.Res
import me.joxquin.notivas.shared.resources.licenses_category_data
import me.joxquin.notivas.shared.resources.licenses_category_framework
import me.joxquin.notivas.shared.resources.licenses_category_google
import me.joxquin.notivas.shared.resources.licenses_kind_apache
import me.joxquin.notivas.shared.resources.licenses_kind_mit
import me.joxquin.notivas.shared.resources.licenses_lib_androidx_activity
import me.joxquin.notivas.shared.resources.licenses_lib_androidx_lifecycle
import me.joxquin.notivas.shared.resources.licenses_lib_androidx_room
import me.joxquin.notivas.shared.resources.licenses_lib_androidx_work
import me.joxquin.notivas.shared.resources.licenses_lib_coil
import me.joxquin.notivas.shared.resources.licenses_lib_compose
import me.joxquin.notivas.shared.resources.licenses_lib_coroutines
import me.joxquin.notivas.shared.resources.licenses_lib_firebase
import me.joxquin.notivas.shared.resources.licenses_lib_kotlin
import me.joxquin.notivas.shared.resources.licenses_lib_ktor
import me.joxquin.notivas.shared.resources.licenses_lib_material_icons
import me.joxquin.notivas.shared.resources.licenses_lib_miuix
import me.joxquin.notivas.shared.resources.licenses_lib_serialization
import org.jetbrains.compose.resources.StringResource

/**
 * Catálogo de licencias de los componentes distribuidos con la aplicación.
 *
 * Las versiones deben mantenerse sincronizadas con `gradle/libs.versions.toml` cuando se
 * actualicen las dependencias.
 */
enum class LicenseKind(
    val assetPath: String,
    val labelRes: StringResource
) {
    MIT(
        assetPath = "files/licenses/mit.txt",
        labelRes = Res.string.licenses_kind_mit
    ),
    APACHE_2_0(
        assetPath = "files/licenses/apache-2.0.txt",
        labelRes = Res.string.licenses_kind_apache
    )
}

enum class LicenseCategory(val titleRes: StringResource) {
    FRAMEWORK(Res.string.licenses_category_framework),
    DATA(Res.string.licenses_category_data),
    GOOGLE(Res.string.licenses_category_google)
}

data class LicenseEntry(
    val nameRes: StringResource,
    val version: String,
    val kind: LicenseKind,
    val url: String
)

data class LicenseSection(
    val category: LicenseCategory,
    val entries: List<LicenseEntry>
)

val openSourceLicenseSections: List<LicenseSection> = listOf(
    LicenseSection(
        category = LicenseCategory.FRAMEWORK,
        entries = listOf(
            LicenseEntry(
                nameRes = Res.string.licenses_lib_kotlin,
                version = "2.4.20",
                kind = LicenseKind.APACHE_2_0,
                url = "https://github.com/JetBrains/kotlin"
            ),
            LicenseEntry(
                nameRes = Res.string.licenses_lib_compose,
                version = "1.13.0-rc01",
                kind = LicenseKind.APACHE_2_0,
                url = "https://github.com/JetBrains/compose-multiplatform"
            ),
            LicenseEntry(
                nameRes = Res.string.licenses_lib_miuix,
                version = "0.9.5-rc01",
                kind = LicenseKind.APACHE_2_0,
                url = "https://github.com/compose-miuix-ui/miuix"
            ),
            LicenseEntry(
                nameRes = Res.string.licenses_lib_material_icons,
                version = "1.7.3",
                kind = LicenseKind.APACHE_2_0,
                url = "https://github.com/google/material-design-icons"
            ),
            LicenseEntry(
                nameRes = Res.string.licenses_lib_androidx_activity,
                version = "1.13.0",
                kind = LicenseKind.APACHE_2_0,
                url = "https://github.com/androidx/androidx"
            ),
            LicenseEntry(
                nameRes = Res.string.licenses_lib_androidx_lifecycle,
                version = "2.11.0",
                kind = LicenseKind.APACHE_2_0,
                url = "https://github.com/androidx/androidx"
            ),
            LicenseEntry(
                nameRes = Res.string.licenses_lib_androidx_room,
                version = "3.0.3",
                kind = LicenseKind.APACHE_2_0,
                url = "https://github.com/androidx/androidx"
            ),
            LicenseEntry(
                nameRes = Res.string.licenses_lib_androidx_work,
                version = "2.10.0",
                kind = LicenseKind.APACHE_2_0,
                url = "https://github.com/androidx/androidx"
            )
        )
    ),
    LicenseSection(
        category = LicenseCategory.DATA,
        entries = listOf(
            LicenseEntry(
                nameRes = Res.string.licenses_lib_ktor,
                version = "3.1.1",
                kind = LicenseKind.APACHE_2_0,
                url = "https://github.com/ktorio/ktor"
            ),
            LicenseEntry(
                nameRes = Res.string.licenses_lib_coroutines,
                version = "1.11.0",
                kind = LicenseKind.APACHE_2_0,
                url = "https://github.com/Kotlin/kotlinx.coroutines"
            ),
            LicenseEntry(
                nameRes = Res.string.licenses_lib_serialization,
                version = "1.8.0",
                kind = LicenseKind.APACHE_2_0,
                url = "https://github.com/Kotlin/kotlinx.serialization"
            ),
            LicenseEntry(
                nameRes = Res.string.licenses_lib_coil,
                version = "3.1.0",
                kind = LicenseKind.APACHE_2_0,
                url = "https://github.com/coil-kt/coil"
            )
        )
    ),
    LicenseSection(
        category = LicenseCategory.GOOGLE,
        entries = listOf(
            LicenseEntry(
                nameRes = Res.string.licenses_lib_firebase,
                version = "BoM 35.1.0",
                kind = LicenseKind.APACHE_2_0,
                url = "https://github.com/firebase/firebase-android-sdk"
            )
        )
    )
)
