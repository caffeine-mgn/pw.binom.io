package pw.binom.validate

class ValidateException(val fields: Map<String, List<String>>) : IllegalArgumentException() {
  override val message: String
    get() {
      val sb = StringBuilder()
      var first = true
      fields.forEach { (key, messages) ->
        messages.forEach { message ->
          if (!first) {
            sb.append(", ")
          } else {
            first = false
          }
          sb.append("$key: $message")
        }
      }
      return sb.toString()
    }
}
