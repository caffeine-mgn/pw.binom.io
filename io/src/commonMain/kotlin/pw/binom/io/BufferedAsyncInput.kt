package pw.binom.io

interface BufferedAsyncInput : AsyncInput {
  val inputBufferSize: Int
}
