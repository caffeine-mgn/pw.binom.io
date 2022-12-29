package pw.binom.io.socket

import pw.binom.io.Closeable

abstract class AbstractNativeKey : Closeable {
//    protected val self = StableRef.create(this)

    protected fun freeSelfClose() {
//        self.dispose()
    }
}
