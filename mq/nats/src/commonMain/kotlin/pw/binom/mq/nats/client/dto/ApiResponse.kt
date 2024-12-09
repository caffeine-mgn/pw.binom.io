package pw.binom.mq.nats.client.dto

interface ApiResponse {
  val type: String
  val error: ErrorDto?
}
