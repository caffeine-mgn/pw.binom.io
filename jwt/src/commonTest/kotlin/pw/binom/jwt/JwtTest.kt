package pw.binom.jwt

import pw.binom.crypto.HMac
import kotlin.test.Test
import kotlin.test.assertEquals

class JwtTest {

  val payload = """{"user":"123"}"""
  val key = "123".encodeToByteArray()

  @Test
  fun test256() {
    val token = JWTToken.createHMac(
      alg = HMac.AlgorithmType.SHA256,
      key = key,
      payload = JwtPayload(payload)
    ).toString()
    assertEquals(
      "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJ1c2VyIjoiMTIzIn0.Y8UPDK9EXGoTG9ozEmW-pWcbCZUyMrK0ZcvIB2R8jVs",
      token,
    )
  }
  @Test
  fun test384() {
    val token = JWTToken.createHMac(
      alg = HMac.AlgorithmType.SHA384,
      key = key,
      payload = JwtPayload(payload)
    ).toString()
    assertEquals(
      "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzM4NCJ9.eyJ1c2VyIjoiMTIzIn0.nrno5Kvna0F37DJ1l9bo_apo7XCEw2bpYGzXfHkSBGJf7qTpU2Ep83Vf2KWCC6fq",
      token,
    )
  }
  @Test
  fun test512() {
    val token = JWTToken.createHMac(
      alg = HMac.AlgorithmType.SHA512,
      key = key,
      payload = JwtPayload(payload)
    ).toString()
    assertEquals(
      "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzUxMiJ9.eyJ1c2VyIjoiMTIzIn0._XjFeZvRAl2_qkkGgcZoWZQlKy7ZonE2UYJZqICOd8TaQ2Qo_c9ztHtxoa_cnett8e89HJF-eHKHewWeEUk8Fw",
      token,
    )
  }
}
