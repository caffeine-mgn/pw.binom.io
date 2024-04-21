# Binom Validation Library

### How to add new validator

```kotlin
@SerialInfo
@Target(AnnotationTarget.PROPERTY)
@Retention(AnnotationRetention.BINARY)
annotation class IntFormat

object NumberFormatValidator:: Validator.FieldValidator<IntFormat> {
    override fun valid(annotation: NotNull, descriptor: SerialDescriptor, value: String?): ValueValidateResult {
        value?:return ValueValidateResult.success() 
        value.toIntOrNull() ?: return ValueValidateResult.fail("Value \"$value\" is not Integer")
        return ValueValidateResult.success()
    }
}

val myValidatorModule = ValidatorModule.default + ValidatorModule {
    define(IntFormat::class, NumberFormatValidator)
}

@Serializable
class Data(
    val age:String,
)

Validation.validateAndCheck(Data("123"))
```
