package pw.binom

actual object System {
    actual fun gc() {
        java.lang.System.gc()
    }
}