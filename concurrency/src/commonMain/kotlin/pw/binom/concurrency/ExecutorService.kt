package pw.binom.concurrency

import pw.binom.Future

interface ExecutorService {
    fun <T> submit(f: () -> T): Future<T>
}