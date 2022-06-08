package pw.binom.crypto

import pw.binom.base64.Base64
import pw.binom.ssl.Nid
import kotlin.test.Test
import kotlin.test.assertEquals

class ECCurveTest {
    @Test
    fun decodePointTest() {
        val c = ECPublicKey.load(Base64.decode(PUBLIC))
        val newPointUncompressed = c.q.curve.decodePoint(Base64.decode(UNCOMPRESSED_POINT))
        assertEquals(UNCOMPRESSED_POINT, Base64.encode(newPointUncompressed.getEncoded(false)))
        assertEquals(COMPRESSED_POINT, Base64.encode(newPointUncompressed.getEncoded(true)))

        val newPointCompressed1 = c.q.curve.decodePoint(Base64.decode(COMPRESSED_POINT))
        assertEquals(UNCOMPRESSED_POINT, Base64.encode(newPointCompressed1.getEncoded(false)))
        assertEquals(COMPRESSED_POINT, Base64.encode(newPointCompressed1.getEncoded(true)))
    }
}
