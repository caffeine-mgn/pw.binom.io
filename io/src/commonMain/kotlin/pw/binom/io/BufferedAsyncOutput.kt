package pw.binom.io

interface BufferedAsyncOutput : AsyncOutput {
    val outputBufferSize: Int
}
