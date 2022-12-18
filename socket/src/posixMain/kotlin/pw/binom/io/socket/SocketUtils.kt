package pw.binom.io.socket

// actual fun allowIpv4(native: RawSocket) {
//    memScoped {
//        val flag = allocArray<IntVar>(1)
//        flag[0] = 0
//        val iResult = setsockopt(
//            native,
//            IPPROTO_IPV6,
//            IPV6_V6ONLY,
//            flag,
//            sizeOf<IntVar>().convert(),
//        )
//        if (iResult == -1) {
//            close(native)
//            throw IOException("Can't allow ipv6 connection for UDP socket. Error: $errno")
//        }
//    }
// }
