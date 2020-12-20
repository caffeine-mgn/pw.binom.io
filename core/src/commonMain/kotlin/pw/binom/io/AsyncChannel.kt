package pw.binom.io

import pw.binom.AsyncInput
import pw.binom.AsyncOutput

interface AsyncChannel : AsyncCloseable, AsyncOutput, AsyncInput