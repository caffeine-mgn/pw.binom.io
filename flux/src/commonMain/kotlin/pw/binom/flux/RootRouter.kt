package pw.binom.flux

class RootRouter(override val serialization: FluxServerSerialization = FluxServerSerializationStab) : AbstractRoute() {
    interface ExceptionHandler {
        fun exception(exception: Throwable)
    }
}