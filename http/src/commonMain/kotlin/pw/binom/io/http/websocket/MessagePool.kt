package pw.binom.io.http.websocket

/*
import pw.binom.io.AsyncInput
import pw.binom.pool.GenericObjectPool

class MessagePool(
    initCapacity: Int = 16,
    maxSize: Int = Int.MAX_VALUE,
    minSize: Int = 0,
    growFactor: Float = 1.5f,
    shrinkFactor: Float = 0.5f
) {
    private val pool =
        GenericObjectPool(
            factory = MessageImpl2.factory,
            initCapacity = initCapacity,
            maxSize = maxSize,
            minSize = minSize,
            growFactor = growFactor,
            shrinkFactor = shrinkFactor,
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
*/
