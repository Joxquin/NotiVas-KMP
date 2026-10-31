package me.joxquin.notivas.util

import java.awt.Toolkit
import java.awt.datatransfer.StringSelection

actual object PlatformShareHandler {
    actual fun shareText(text: String, title: String?) {
        try {
            val selection = StringSelection(text)
            Toolkit.getDefaultToolkit().systemClipboard.setContents(selection, selection)
        } catch (_: Exception) {}
    }
}
