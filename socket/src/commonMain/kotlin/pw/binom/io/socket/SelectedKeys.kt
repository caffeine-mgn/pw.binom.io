package pw.binom.io.socket

@Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")
expect class SelectedKeys {
  constructor()

  fun forEach(func: (Event) -> Unit)
  fun collect(collection: MutableCollection<Event>)
  fun toList(): List<Event>
}
