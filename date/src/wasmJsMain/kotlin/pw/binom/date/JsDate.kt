package pw.binom.date

@JsName("Date")
external class JsDate : JsAny {
  constructor()
  constructor(value: JsNumber)
  companion object {
    fun now(): JsNumber
    fun UTC(
      year: JsNumber,
      month: JsNumber,
      dayOfMonth: JsNumber,
      hours: JsNumber,
      minutes: JsNumber,
      seconds: JsNumber,
      millis: JsNumber,
    ): JsNumber
  }
  fun getTimezoneOffset():JsNumber
  fun getTime():JsNumber
}
