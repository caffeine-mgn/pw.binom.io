package pw.binom.io.http.websocket

import pw.binom.io.AsyncInput
import pw.binom.io.AsyncOutput
import pw.binom.pool.DefaultPool

class WebSocketConnectionPool(capacity: Int) {
    private val pool = DefaultPool<WebSocketConnectionImpl2>(
        capacity = capacity,
        new = { pool -> WebSocketConnectionImpl2 { self -> pool.recycle(self) } }
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
