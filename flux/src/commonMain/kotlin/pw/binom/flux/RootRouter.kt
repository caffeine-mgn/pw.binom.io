package pw.binom.flux

class RootRouter(override val serialization: Serialization = SerializationStab) : AbstractRoute() {
    interface ExceptionHandler {
        fun exception(exception: Throwable)
    }
}