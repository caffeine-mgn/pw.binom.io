package pw.binom.io.socket

expect class SelectedKeys {
    constructor()

    fun forEach(func: (Event) -> Unit)
    fun collect(collection: MutableCollection<Event>)
    fun toList(): List<Event>
}
