package pw.binom.validate

interface ErrorCollector {
  companion object {
    val default = object : ErrorCollector {
      var map = HashMap<String, ArrayList<String>>()
      override fun invalidField(fieldName: String, message: String) {
        map.getOrPut(fieldName) { ArrayList() }.add(message)
      }

      override fun flush(): Map<String, List<String>> {
        val map = this.map
        this.map = HashMap()
        return map
      }
    }
  }

  fun invalidField(fieldName: String, message: String)
  fun flush(): Map<String, List<String>>
  fun flushAndCheck() {
    val errors = flush()
    if (errors.isEmpty()) {
      return
    }
    throw ValidateException(errors)
  }
}
