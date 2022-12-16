package pw.binom.io.pipe

import pw.binom.io.Output

expect class PipeOutput : Output {
    constructor()
    constructor(input: PipeInput)
}
