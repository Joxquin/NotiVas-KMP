package me.joxquin.notivas.util

import kotlinx.coroutines.CoroutineDispatcher

expect object AppDispatchers {
    val IO: CoroutineDispatcher
    val Default: CoroutineDispatcher
    val Main: CoroutineDispatcher
}
