package pw.binom.db.indexeddb

internal external class IDBFactory {
  fun open(name: String): IDBOpenDBRequest
  fun open(name: String, version: Int): IDBOpenDBRequest
}
