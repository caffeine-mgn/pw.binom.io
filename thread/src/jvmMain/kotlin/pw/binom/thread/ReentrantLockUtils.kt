package pw.binom.thread

import java.util.concurrent.locks.ReentrantLock

internal inline fun <T> ReentrantLock.lock(func: () -> T): T =
        try {
            lock()
            func()
        } finally {
            unlock()
        }