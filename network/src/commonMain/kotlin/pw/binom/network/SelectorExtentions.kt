package pw.binom.network

internal fun SelectorOld.Key.generateToString(): String {
    val sb = StringBuilder()
    sb.append("listensFlag=")
    if (listensFlag and SelectorOld.EVENT_CONNECTED != 0) {
        sb.append("EVENT_CONNECTED ")
    }
    if (listensFlag and SelectorOld.INPUT_READY != 0) {
        sb.append("INPUT_READY ")
    }
    if (listensFlag and SelectorOld.OUTPUT_READY != 0) {
        sb.append("OUTPUT_READY ")
    }
    if (listensFlag and SelectorOld.EVENT_ERROR != 0) {
        sb.append("EVENT_ERROR ")
    }
    sb.append(", ").append("attachment=").append(this.attachment)
    return sb.toString()
}
