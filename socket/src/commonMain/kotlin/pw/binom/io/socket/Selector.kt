package pw.binom.io.socket

import pw.binom.io.Closeable
import kotlin.time.Duration

@Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING", "NO_ACTUAL_FOR_EXPECT")
expect class Selector : Closeable {
  constructor()

  fun attach(socket: Socket): SelectorKey
  fun select(timeout: Duration, selectedKeys: SelectedKeys)
  fun select(timeout: Duration, eventFunc: (Event) -> Unit)
  fun wakeup()
  override fun close()
}
