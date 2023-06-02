package pw.binom.process

// import kotlinx.cinterop.convert
// import pw.binom.atomic.AtomicBoolean
// import pw.binom.io.ByteBuffer
// import pw.binom.io.Input
// import pw.binom.io.Output
//
// class PipeInputOutput : Pipe(), Input, Output {
//
//    private var endded = AtomicBoolean(false)
//
//    override fun read(dest: ByteBuffer): Int {
//        if (endded.getValue()) {
//            return 0
//        }
//
//        val r = dest.ref(0) { destPtr, remaining ->
//            if (remaining > 0) {
//                platform.posix.read(read, destPtr, remaining.convert()).convert<Int>()
//            } else {
//                0
//            }
//        }
//        if (r <= 0) {
//            endded.setValue(true)
//        } else {
//            dest.position += r
//        }
//        return r
//    }
//
//    override fun write(data: ByteBuffer): Int {
//        val wrote = data.ref(0) { dataPtr, remaining ->
//            if (remaining > 0) {
//                platform.posix.write(write, dataPtr, remaining.convert()).convert<Int>()
//            } else {
//                0
//            }
//        }
//        println("PipeInputOutput.write wrote=$wrote")
//        data.position += wrote
//        return wrote
//    }
//
//    override fun flush() {
//        // Do nothing
//    }
//
//    override fun close() {
//    }
// }
