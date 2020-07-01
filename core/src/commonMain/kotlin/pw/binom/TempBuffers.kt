package pw.binom

import kotlin.native.concurrent.ThreadLocal

@ThreadLocal
internal val tmp8 = ByteBuffer.alloc(8)