package pw.binom

actual val Throwable.stackTrace: List<String>
    get() = this.getStackTrace().toList()