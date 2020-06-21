package pw.binom.io.socket.nio

import pw.binom.AppendableQueue
import pw.binom.PopResult

class WaitEventQueue<T : Any>(val loadFactor: Float = 0.75f, val compactFactor: Float = 0.5f) : AppendableQueue<T> {
    var value: T? = null
    override val isEmpty: Boolean
        get() = value == null
    override val size: Int
        get() = if (isEmpty) 0 else 1

    override fun pop(): T {
        return value ?: throw NoSuchElementException()
    }

    override fun pop(dist: PopResult<T>) {
        if (value == null) {
            dist.clear()
        } else {
            dist.set(value!!)
            this.value = null
        }
    }

    override fun push(value: T) {
        if (this.value != null)
            throw RuntimeException("План не удался! :(")
        this.value = value
    }

    override fun peek(): T {
        return value ?: throw NoSuchElementException()
    }
}

class WaitEventQueue11<T : Any>(val loadFactor: Float = 0.75f, val compactFactor: Float = 0.5f) : AppendableQueue<T> {
    private var body = arrayOfNulls<Any>(0)
    var endIndex = 0

    private fun checkBody() {
        val needExtend = body.size * loadFactor < (endIndex.toFloat() + 1f)
        val needCompact = body.size * compactFactor > (endIndex.toFloat() + 1f)

        if (needExtend || needCompact) {
            val minFreeSize = (endIndex + 1) * 2
            val newBody = arrayOfNulls<Any>(minFreeSize)
            if (endIndex > 0) {
                body.copyInto(newBody, 0, 0, endIndex)
            }
            body = newBody
        }
    }

    override val isEmpty: Boolean
        get() = size == 0

    override val size: Int
        get() = endIndex

    override fun pop(): T {
        if (endIndex == 0)
            throw NoSuchElementException()
        val index = --endIndex
        val r = body[index]!!
        body[index] = null
        checkBody()
        return r as T
    }

    override fun pop(dist: PopResult<T>) {
        if (endIndex == 0) {
            dist.clear()
            return
        }
        dist.set(body[--endIndex]!! as T)
        checkBody()
    }

    override fun push(value: T) {
        checkBody()
        body[endIndex++] = value
    }

    override fun peek(): T {
        if (endIndex == 0)
            throw NoSuchElementException()
        return body[endIndex - 1]!! as T
    }
}