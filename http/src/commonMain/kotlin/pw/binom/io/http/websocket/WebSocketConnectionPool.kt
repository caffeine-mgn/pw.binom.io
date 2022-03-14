package pw.binom.io.http.websocket

import pw.binom.AsyncInput
import pw.binom.AsyncOutput
import pw.binom.pool.DefaultPool

class WebSocketConnectionPool(capacity: Int) {
    private fun onClose(msg: WebSocketConnectionImpl2) {
        pool.recycle(msg)
    }

    private val pool = DefaultPool(
        capacity = capacity,
        new = { WebSocketConnectionImpl2(onClose = this::onClose) }
    )

    fun new(
        input: AsyncInput,
        output: AsyncOutput,
        masking: Boolean,
    ) = pool.borrow {
        it.reset(
            input = input,
            output = output,
            masking = masking,
        )
    }
}
