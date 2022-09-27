package pw.binom.io.http.websocket

import pw.binom.io.AsyncInput
import pw.binom.pool.DefaultPool

class MessagePool(capacity: Int) {
    private val pool =
        DefaultPool<MessageImpl2>(
            capacity = capacity,
            new = { pool -> MessageImpl2 { self -> pool.recycle(self) } }
        )

    fun new(
        initLength: Long,
        type: MessageType,
        lastFrame: Boolean,
        maskFlag: Boolean,
        mask: Int,
        input: AsyncInput
    ): Message = pool.borrow()
        .also {
            it.reset(
                initLength = initLength,
                type = type,
                lastFrame = lastFrame,
                maskFlag = maskFlag,
                mask = mask,
                input = input,
            )
        }
}
