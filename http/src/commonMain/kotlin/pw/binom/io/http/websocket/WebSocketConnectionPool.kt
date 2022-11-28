package pw.binom.io.http.websocket

import pw.binom.io.AsyncInput
import pw.binom.io.AsyncOutput
import pw.binom.pool.GenericObjectPool

class WebSocketConnectionPool(
    initCapacity: Int = 16,
    maxSize: Int = Int.MAX_VALUE,
    minSize: Int = 0,
    growFactor: Float = 1.5f,
    shrinkFactor: Float = 0.5f
) {
    private val pool = GenericObjectPool(
        factory = WebSocketConnectionImpl2.factory,
        initCapacity = initCapacity,
        maxSize = maxSize,
        minSize = minSize,
        growFactor = growFactor,
        shrinkFactor = shrinkFactor,
    )

    fun new(
        input: AsyncInput,
        output: AsyncOutput,
        masking: Boolean,
    ) = pool.borrow()
        .also {
            it.reset(
                input = input,
                output = output,
                masking = masking,
            )
        }
}
