package pw.binom.crypto

import pw.binom.base64.Base64
import kotlin.test.Test
import kotlin.test.assertEquals

const val PUBLIC =
    "MFkwEwYHKoZIzj0CAQYIKoZIzj0DAQcDQgAEmYIpIrpTa4vfe/sW+LEHsHhb8ldCDRy10rQkdd9Izj2bUtYu7r2S5TVZneXE7UnkUR4OOT6iJZwskI3UCgQerg=="
const val COMPRESSED_POINT = "ApmCKSK6U2uL33v7FvixB7B4W/JXQg0ctdK0JHXfSM49"
const val UNCOMPRESSED_POINT =
    "BJmCKSK6U2uL33v7FvixB7B4W/JXQg0ctdK0JHXfSM49m1LWLu69kuU1WZ3lxO1J5FEeDjk+oiWcLJCN1AoEHq4="

class EcPointTest {

    @Test
    fun encodeTest() {
        val public = ECPublicKey.load(Base64.decode(PUBLIC))
        assertEquals(COMPRESSED_POINT, Base64.encode(public.q.getEncoded(true)))
        assertEquals(UNCOMPRESSED_POINT, Base64.encode(public.q.getEncoded(false)))
        // 0499822922ba536b8bdf7bfb16f8b107b0785bf257420d1cb5d2b42475df48ce3d9b52d62eeebd92e535599de5c4ed49e4511e0e393ea2259c2c908dd40a041eae
    }
}
