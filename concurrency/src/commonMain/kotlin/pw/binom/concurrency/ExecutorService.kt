package pw.binom.concurrency

import pw.binom.Future2

interface ExecutorService {
    fun <T> submit(f: () -> T): Future2<T>
}