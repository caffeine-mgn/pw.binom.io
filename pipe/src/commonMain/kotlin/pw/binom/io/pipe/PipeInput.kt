package pw.binom.io.pipe

import pw.binom.io.Input

expect class PipeInput : Input {
    constructor()
    constructor(output: PipeOutput)
}
