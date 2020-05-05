package pw.binom.io

interface AsyncChannel : AsyncCloseable {
    val input: AsyncInputStream
    val output: AsyncOutputStream
}