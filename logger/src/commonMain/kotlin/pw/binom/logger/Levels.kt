package pw.binom.logger

private object DEBUG_LEVEL : Logger.Level {
    override val name: String
        get() = "D"
    override val priority: UInt
        get() = 700u
}

val Logger.Companion.DEBUG: Logger.Level
    get() = DEBUG_LEVEL

suspend fun Logger.debug(text: String, trace: String? = null, exception: Throwable? = null) {
    log(Logger.DEBUG, text = text, trace = trace, exception = exception)
}

fun Logger.debugSync(text: String, trace: String? = null, exception: Throwable? = null) {
    logSync(Logger.DEBUG, text = text, trace = trace, exception = exception)
}

suspend fun Logger.debug(trace: String? = null, exception: Throwable) {
    log(Logger.DEBUG, null, trace = trace, exception = exception)
}

fun Logger.debugSync(trace: String? = null, exception: Throwable) {
    logSync(Logger.DEBUG, null, trace = trace, exception = exception)
}

private object INFO_LEVEL : Logger.Level {
    override val name: String
        get() = "I"
    override val priority: UInt
        get() = 800u
}

val Logger.Companion.INFO: Logger.Level
    get() = INFO_LEVEL


suspend fun Logger.info(text: String, trace: String? = null, exception: Throwable? = null) {
    log(Logger.INFO, text = text, trace = trace, exception = exception)
}

fun Logger.infoSync(text: String, trace: String? = null, exception: Throwable? = null) {
    logSync(Logger.INFO, text = text, trace = trace, exception = exception)
}

suspend fun Logger.info(trace: String? = null, exception: Throwable) {
    log(Logger.INFO, null, trace = trace, exception = exception)
}

fun Logger.infoSync(trace: String? = null, exception: Throwable) {
    logSync(Logger.INFO, null, trace = trace, exception = exception)
}

private object WARN_LEVEL : Logger.Level {
    override val name: String
        get() = "W"
    override val priority: UInt
        get() = 900u
}

val Logger.Companion.WARNING: Logger.Level
    get() = WARN_LEVEL

suspend fun Logger.warn(text: String, trace: String? = null, exception: Throwable? = null) {
    log(Logger.WARNING, text, trace = trace, exception = exception)
}

fun Logger.warnSync(text: String, trace: String? = null, exception: Throwable? = null) {
    logSync(Logger.WARNING, text, trace = trace, exception = exception)
}

suspend fun Logger.warn(trace: String? = null, exception: Throwable? = null) {
    log(Logger.WARNING, null, trace = trace, exception = exception)
}

fun Logger.warnSync(trace: String? = null, exception: Throwable? = null) {
    logSync(Logger.WARNING, null, trace = trace, exception = exception)
}

private object SEVERE_LEVEL : Logger.Level {
    override val name: String
        get() = "S"
    override val priority: UInt
        get() = 1000u
}

val Logger.Companion.SEVERE: Logger.Level
    get() = SEVERE_LEVEL

suspend fun Logger.severe(text: String, trace: String? = null, exception: Throwable? = null) {
    log(Logger.SEVERE, text, trace = trace, exception = exception)
}

suspend fun Logger.severe(trace: String? = null, exception: Throwable? = null) {
    log(Logger.SEVERE, null, trace = trace, exception = exception)
}

fun Logger.severeSync(text: String, trace: String? = null, exception: Throwable? = null) {
    logSync(Logger.SEVERE, text, trace = trace, exception = exception)
}

fun Logger.severeSync(trace: String? = null, exception: Throwable? = null) {
    logSync(Logger.SEVERE, null, trace = trace, exception = exception)
}