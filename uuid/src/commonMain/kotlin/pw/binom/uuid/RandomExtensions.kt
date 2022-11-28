package pw.binom.uuid

import kotlin.random.Random

@Suppress("NOTHING_TO_INLINE")
inline fun Random.nextUuid() = UUID.random()
