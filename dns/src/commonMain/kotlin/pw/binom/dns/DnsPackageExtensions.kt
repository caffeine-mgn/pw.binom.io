package pw.binom.dns

import pw.binom.ByteBufferPool
import pw.binom.io.AsyncInput
import pw.binom.io.AsyncOutput
import pw.binom.io.Input
import pw.binom.io.Output
import pw.binom.pool.using

fun DnsPackage.write(output: Output, pool: ByteBufferPool) = pool.using { buffer ->
    write(output = output, buffer = buffer)
}

suspend fun DnsPackage.write(output: AsyncOutput, pool: ByteBufferPool) = pool.using { buffer ->
    write(output = output, buffer = buffer)
}

fun DnsPackage.Companion.read(input: Input, pool: ByteBufferPool) = pool.using { buffer ->
    read(input = input, buffer = buffer)
}

suspend fun DnsPackage.Companion.read(input: AsyncInput, pool: ByteBufferPool) = pool.using { buffer ->
    read(input = input, buffer = buffer)
}
