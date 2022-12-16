package pw.binom.io.socket

import pw.binom.io.Closeable

expect class SelectorKey : Closeable {
    var attachment: Any?
    var listenFlags: Int
    val isClosed: Boolean
    val selector: Selector
}

fun SelectorKey.addListen(code: Int) {
    listenFlags = listenFlags or code
}

fun SelectorKey.removeListen(code: Int) {
    listenFlags = (listenFlags.inv() or code).inv()
}
