package pw.binom.route

import org.jetbrains.kotlin.cli.common.messages.CompilerMessageLocationWithRange
import org.jetbrains.kotlin.cli.common.messages.CompilerMessageSeverity
import org.jetbrains.kotlin.cli.common.messages.MessageCollector
import org.jetbrains.kotlin.ir.declarations.IrDeclaration
import org.jetbrains.kotlin.ir.util.fileEntry

fun String?.takeIfIsNotEmpty() = this?.takeIf { it.isNotEmpty() }
fun MessageCollector.error(message: String, declaration: IrDeclaration) {
  report(
    severity = CompilerMessageSeverity.ERROR,
    message = message,
    declaration = declaration,
  )
}

fun MessageCollector.warn(message: String, declaration: IrDeclaration) {
  report(
    severity = CompilerMessageSeverity.WARNING,
    message = message,
    declaration = declaration,
  )
}

fun MessageCollector.report(severity: CompilerMessageSeverity, message: String, declaration: IrDeclaration) {
  val range = declaration.fileEntry.getSourceRangeInfo(declaration.startOffset, declaration.endOffset)
  val location = CompilerMessageLocationWithRange.create(
    path = range.filePath,
    lineStart = range.startLineNumber + 1,
    columnStart = range.startColumnNumber + 1,
    lineEnd = range.endLineNumber + 1,
    columnEnd = range.endColumnNumber + 1,
    lineContent = null,
  )
  report(
    severity = severity,
    message = message,
    location = location,
  )
}
