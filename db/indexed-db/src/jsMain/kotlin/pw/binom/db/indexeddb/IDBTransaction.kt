package pw.binom.db.indexeddb

import org.w3c.dom.events.EventTarget

internal abstract external class IDBTransaction : EventTarget {
  /**
   * https://developer.mozilla.org/en-US/docs/Web/API/IDBTransaction/db
   */
  val db: IDBDatabase

  /**
   * https://developer.mozilla.org/en-US/docs/Web/API/IDBTransaction/durability
   */
  val durability: String

  /**
   * https://developer.mozilla.org/en-US/docs/Web/API/IDBTransaction/error
   */
  val error: dynamic

  /**
   * https://developer.mozilla.org/en-US/docs/Web/API/IDBTransaction/mode
   */
  val mode: String

  /**
   * https://developer.mozilla.org/en-US/docs/Web/API/IDBTransaction/ObjectStoreNames
   */
  val objectStoreNames: Array<String>

  /**
   * https://developer.mozilla.org/en-US/docs/Web/API/IDBTransaction/abort
   */
  fun abort()

  /**
   * https://developer.mozilla.org/en-US/docs/Web/API/IDBTransaction/commit
   */
  fun commit()

  /**
   * https://developer.mozilla.org/en-US/docs/Web/API/IDBTransaction/objectStore
   */
  fun objectStore(): IDBObjectStore
}
