package pw.binom.xml

import pw.binom.io.IOException

open class XmlException : IOException {
    constructor(message: String) : super(message)
    constructor() : super()
}