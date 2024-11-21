package pw.binom.metric

/**
 * Способ формирования результата метрик
 *
 * Порядок вызова:<br>
 * 1. `help(Описание)` - *Вызов 0 или 1 раз*
 * 2. `type(имя типа)` - *Вызов 1 раз*
 * 3. `start(название метрики)` - *Вызов 1 раз*
 * 4. `field(название поля, значение поля)` - *Вызов N раз*
 * 5. `value(значение поля)` - *Вызов 1 раз*
 * 6. `end()` - *Вызов 1 раз*
 */
interface AsyncMetricVisitor {
  suspend fun start(name: String)
  suspend fun help(name:String,text: String)
  suspend fun type(name:String,type: MetricType)
  suspend fun field(name: String, value: String)
  suspend fun value(value: String)
  suspend fun end()

  fun withField(name: String, value: String): AsyncMetricVisitor = WithFieldAsyncMetricVisitor(
    name = name,
    value = value,
    visitor = this
  )

  fun changeName(nameModification: (String) -> String): AsyncMetricVisitor = PrefixAsyncMetricVisitor(
    nameModification = nameModification,
    visitor = this@AsyncMetricVisitor
  )
}
