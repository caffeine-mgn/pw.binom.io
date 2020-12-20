package pw.binom.io.socket

/**
 * Exception thrown when a blocking-mode-specific operation
 * is invoked upon a channel in the incorrect blocking mode.
 */
class IllegalBlockingModeException : IllegalStateException()