package pw.binom.db.indexeddb

import kotlinx.browser.window

class IndexedDbConnector {
  companion object{
    fun open(name:String){
      val factory = window.asDynamic()["indexedDB"].unsafeCast<IDBFactory>()
      val request = factory.open(name)
      request.onsuccess={
        request.result
      }
    }
  }
}
