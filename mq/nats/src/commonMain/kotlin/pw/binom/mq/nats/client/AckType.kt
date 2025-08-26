package pw.binom.mq.nats.client

enum class AckType(val text: String, val terminal: Boolean) {
  AckAck("+ACK", true),
  AckNak("-NAK", true),
  AckProgress("+WPI", false),
  AckTerm("+TERM", true),

  // pull only option
  AckNext("+NXT", false),
  ;

  internal val bytes = text.encodeToByteArray()
}
