package pw.binom.io.http.websocket

import pw.binom.AsyncInput
import pw.binom.pool.DefaultPool

class MessagePool(capacity: Int) {
    private val pool = DefaultPool(capacity = capacity, new = { MessageImpl2(this::onClose) })
    private fun onClose(msg: MessageImpl2) {
        pool.recycle(msg)
    }

    fun new(
        initLength: ULong,
        type: MessageType,
        lastFrame: Boolean,
        maskFlag: Boolean,
        mask: Int,
        input: AsyncInput
    ): Message = pool.borrow {
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
