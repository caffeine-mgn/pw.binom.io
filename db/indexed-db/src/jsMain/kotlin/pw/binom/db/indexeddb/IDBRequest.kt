package pw.binom.db.indexeddb

import org.w3c.dom.events.Event
import org.w3c.dom.events.EventTarget

internal abstract external class IDBRequest : EventTarget {
  abstract val error: dynamic
  abstract val result: dynamic
  abstract val readyState: String
  abstract val source: dynamic
  abstract val transaction: IDBTransaction?
  var onsuccess: (Event) -> Unit
  var onerror: (Event) -> Unit
}
