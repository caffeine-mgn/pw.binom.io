package pw.binom.plugins

import java.io.BufferedReader
import java.io.IOException

import java.io.InputStream
import java.io.InputStreamReader


class StreamGobbler constructor(var stream: InputStream) : Thread() {
    val out = StringBuilder()
    override fun run() {
        try {
            val isr = InputStreamReader(stream)
            val br = BufferedReader(isr)
            var line: String? = null
            while (br.readLine().also { line = it } != null)
                out.append("$line\n")
        } catch (ioe: IOException) {
            ioe.printStackTrace()
        }
    }
}