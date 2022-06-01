package pw.binom

import pw.binom.io.socket.ssl.toHex
import pw.binom.ssl.Key
import pw.binom.ssl.Nid
import pw.binom.ssl.generateEcdsa
import kotlin.test.Test

class NamedCurvesTest {

    private val private = "c52eaea2e485801f914eddf5a19c1b02fe2a3d0c59be0131cae72df2a076689e"
    private val public =
        "044b9ec16085aa980820c3c68511eb5f4125d54fa922bd33be41da261c7c816b15531c24791157bb745c6d1b2000046b8244a5564efe0d79f9d4be46db40a7ba4d"

//    @Test
//    fun vv() {
//        val pair = Key.Pair(
//            public = Key.Public(
//                algorithm = KeyAlgorithm.ECDSA, data = public.hexToByteArray()
//            ),
//            private = Key.Private(
//                algorithm = KeyAlgorithm.ECDSA,
//                data = private.hexToByteArray(),
//            )
//        )
//
//        val ss = Signature.getInstance("SHA3-256withECDSA")
//        ss.init(pair.public)
//        ss.update("Anton".encodeToByteArray())
//        println("signature: ${ss.sign().toHex()}")
//    }

    @Test
    fun ff() {
        val pair = Key.generateEcdsa(Nid.secp256k1)
        val publicHex = pair.public.data.toHex()
        val privateHex = pair.private.data.toHex()
        println("publicHex=$publicHex")
        println("privateHex=$privateHex")
//        pair.public
//        val c = NamedCurves.getByName("secp256k1")
//        println("n->${c.n}")
//        println("h->${c.h}")
    }
}
