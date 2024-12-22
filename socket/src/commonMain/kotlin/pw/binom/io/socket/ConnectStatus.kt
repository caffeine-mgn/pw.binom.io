package pw.binom.io.socket

enum class ConnectStatus {
  OK,
  CONNECTION_REFUSED,
  NO_ROUTE_TO_HOST,
  ALREADY_CONNECTED,
  IN_PROGRESS,
}
