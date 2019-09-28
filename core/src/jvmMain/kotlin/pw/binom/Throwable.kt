package pw.binom

actual val Throwable.stackTrace: List<String>
    get() = this.stackTrace.map {
        "${it.className}.${it.methodName}(${it.fileName}:${it.lineNumber})"
    }