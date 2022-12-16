package pw.binom.io.socket

interface Event {
    val key: SelectorKey
    val flags: Int
}
