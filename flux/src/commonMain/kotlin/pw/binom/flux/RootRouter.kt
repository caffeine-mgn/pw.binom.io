package pw.binom.flux

@Deprecated(message = "Use HttpRouting")
class RootRouter(override val serialization: FluxServerSerialization = FluxServerSerializationStab) : AbstractRoute() {
    interface ExceptionHandler {
        fun exception(exception: Throwable)
    }
}
