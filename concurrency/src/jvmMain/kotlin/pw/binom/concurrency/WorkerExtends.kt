package pw.binom.concurrency

actual fun Worker.Companion.create(name: String?): Worker = WorkerImpl(name)