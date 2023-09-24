package pw.binom.url

import kotlin.test.Test
import kotlin.test.assertEquals

class UrlEncoderTest {
  @Test
  fun encodeTest() {
    assertEquals("%D0%9F%D1%80%D0%B8%D0%B2%D0%B5%D1%82", UrlEncoder.encode("Привет"))
  }
  @Test
  fun decodeTest(){
    assertEquals("Привет", UrlEncoder.decode("%D0%9F%D1%80%D0%B8%D0%B2%D0%B5%D1%82"))
  }
}
