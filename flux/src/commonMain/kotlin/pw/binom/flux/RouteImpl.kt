package pw.binom.flux

internal class RouteImpl(
    override val serialization: FluxServerSerialization,
    wrapperPoolCapacity: Int = 16
) : Route, AbstractRoute(wrapperPoolCapacity = wrapperPoolCapacity)
