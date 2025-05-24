package pw.binom.strong.serialization

class ProviderNotFoundException(val mime: String) : IllegalStateException() {
  override val message: String?
    get() = "SerializationProvider for type \"$mime\" not found"
}
