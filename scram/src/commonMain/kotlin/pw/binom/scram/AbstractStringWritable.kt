package pw.binom.scram

abstract class AbstractStringWritable : StringWritable {
    override fun toString(): String {
        return writeTo(StringBuilder()).toString()
    }
}
