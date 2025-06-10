package pw.binom.xml.dom

sealed class XnElement {
  var parent: Tag? = null
    set(value) {
      field?.privateChild?.remove(this)
      field = value
      field?.privateChild?.add(this)
    }

  data class Text(val text: String) : XnElement()
  data class CDATA(val text: String) : XnElement()

  data class Config(var name: String) : XnElement() {
    val attributes = LinkedHashMap<String, String>()
  }

  data class Comment(val text: String) : XnElement()

  class Tag(var name: String) : XnElement() {
    internal val privateChild = ArrayList<XnElement>()
    val attributes = LinkedHashMap<String, String>()
    val child: List<XnElement>
      get() = privateChild
  }

}
