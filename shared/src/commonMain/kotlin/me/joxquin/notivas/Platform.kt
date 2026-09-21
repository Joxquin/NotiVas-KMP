package me.joxquin.notivas

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform