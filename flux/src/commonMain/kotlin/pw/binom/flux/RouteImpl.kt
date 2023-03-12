package pw.binom.flux

@Deprecated(message = "Use HttpRouting")
internal class RouteImpl(
    override val serialization: FluxServerSerialization,
    wrapperPoolCapacity: Int = 16,
) : Route, AbstractRoute(wrapperPoolCapacity = wrapperPoolCapacity)
