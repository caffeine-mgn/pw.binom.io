package pw.binom.network

internal fun Selector.Key.generateToString(): String {
    val sb = StringBuilder()
    sb.append("listensFlag=")
    if (listensFlag and Selector.EVENT_CONNECTED != 0) {
        sb.append("EVENT_CONNECTED ")
    }
    if (listensFlag and Selector.INPUT_READY != 0) {
        sb.append("INPUT_READY ")
    }
    if (listensFlag and Selector.OUTPUT_READY != 0) {
        sb.append("OUTPUT_READY ")
    }
    if (listensFlag and Selector.EVENT_ERROR != 0) {
        sb.append("EVENT_ERROR ")
    }
    sb.append(", ").append("attachment=").append(this.attachment)
    return sb.toString()
}
