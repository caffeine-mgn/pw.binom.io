package pw.binom.io

import pw.binom.AsyncOutput
import pw.binom.AsyncInput

interface AsyncChannel : AsyncCloseable, AsyncOutput,AsyncInput {
//    val input: AsyncInputStream
//    val output: AsyncOutputStream
}