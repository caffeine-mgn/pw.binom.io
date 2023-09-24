package pw.binom.db.indexeddb

import org.w3c.dom.events.Event

internal abstract external class IDBOpenDBRequest : IDBRequest {
  var onupgradeneeded: (Event) -> Unit
  var onblocked: (Event) -> Unit
}
