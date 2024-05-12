package pw.binom.io.socket

import kotlin.time.Duration

interface UdpSocket : Socket {
  fun setSoTimeout(duration: Duration)
}
