package pw.binom.collections

import kotlin.time.Duration

interface BinomCollection {
    val size: Int
    val liveTime: Duration
    var name: String
}

fun <T : BinomCollection> T.useName(name: String) = this.also { it.name = name }
fun <K, V, T : MutableMap<K, V>> T.useName(name: String) = this.also { (this as? BinomCollection)?.useName(name) }
fun <K, T : MutableList<K>> T.useName(name: String) = this.also { (this as? BinomCollection)?.useName(name) }
