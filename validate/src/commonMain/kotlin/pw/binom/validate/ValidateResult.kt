package pw.binom.validate

interface ValidateResult {
  val isValid: Boolean
  val isNotValid
    get() = !isValid
  val messageOrNull: String?
}
