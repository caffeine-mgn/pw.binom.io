package pw.binom.flux

class RootRouter : AbstractRoute() {
    interface ExceptionHandler {
        fun exception(exception: Throwable)
    }
}