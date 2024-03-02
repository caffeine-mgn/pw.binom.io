package pw.binom.mq

interface Message {
  companion object;

  val headers: Headers
  val topic: String
  val body: ByteArray

  suspend fun ack()
}
