package pw.binom.io.socket

enum class BindStatus {
  OK,
  ALREADY_BINDED,
  ADDRESS_ALREADY_IN_USE,
  UNKNOWN,
  PROTOCOL_NOT_SUPPORTED,
}
