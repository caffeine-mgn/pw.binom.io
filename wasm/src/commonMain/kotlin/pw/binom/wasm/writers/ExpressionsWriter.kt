package pw.binom.wasm.writers

import pw.binom.wasm.StreamWriter
import pw.binom.wasm.visitors.ExpressionsVisitor

class ExpressionsWriter(private val out: StreamWriter) : ExpressionsVisitor {
}
