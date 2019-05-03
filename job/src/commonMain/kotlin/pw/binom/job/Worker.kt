package pw.binom.job

import pw.binom.io.Closeable

expect class Worker:Closeable {
    constructor()
    fun<P,R> execute(param:()->P,task:(P)->R):FuturePromise<R>
}