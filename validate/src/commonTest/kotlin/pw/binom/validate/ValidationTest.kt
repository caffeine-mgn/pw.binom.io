package pw.binom.validate

import kotlinx.serialization.Serializable
import pw.binom.testing.Testing
import pw.binom.validate.annotations.OneOf
import kotlin.test.Test

class ValidationTest {

  @Serializable
  @OneOf("a", "b", "c")
  class Data(
    val a: String? = null,
    val b: String? = null,
    val c: String? = null,
  )

  @Serializable
  class Data2(
    val data: Data,
  )

  @Serializable
  class Sub()

  @OneOf("data1", "data2")
  @Serializable
  class Data3(
    val data1: Data? = null,
    val data2: Data? = null,
  )

  @Test
  fun testWithSubObjects() = Testing.sync {
    test("success") {
      Validation.validateAndCheck(
        strategy = Data3.serializer(),
        value = Data3(data1 = Data(a = "")),
      )
    }
  }

  @Test
  fun successTest() {
    Validation.validateAndCheck(
      strategy = Data.serializer(),
      value = Data(a = ""),
    )
  }

  @Test
  fun allIsNullTest() {
    Validation.validateAndCheck(
      strategy = Data.serializer(),
      value = Data(),
    )
  }

  @Test
  fun twoFieldsNotNull() {
    Validation.validateAndCheck(
      strategy = Data.serializer(),
      value = Data(a = "", b = ""),
    )
  }

  @Test
  fun allFieldsNullInDeep() {
    Validation.validateAndCheck(
      strategy = Data2.serializer(),
      value = Data2(data = Data()),
    )
  }

  @Test
  fun twoFieldsNotNullInDeep() {
    Validation.validateAndCheck(
      strategy = Data2.serializer(),
      value = Data2(data = Data()),
    )
  }

  @Test
  fun successInDeep() {
    Validation.validateAndCheck(
      strategy = Data2.serializer(),
      value = Data2(data = Data(a = "")),
    )
  }
}
