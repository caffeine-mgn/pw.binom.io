package pw.binom.collections

import kotlin.jvm.JvmName

@Suppress("UNCHECKED_CAST")
fun <E> Map<E, *>.toBridgeSet(): Set<E> = SetBridge(this as Map<E, Boolean>)

@JvmName("toMutableBridgeSet")
@Suppress("UNCHECKED_CAST")
fun <E> MutableMap<E, *>.toBridgeSet(): MutableSet<E> = MutableSetBridge(this as MutableMap<E, Boolean>)
