package pw.binom.s3

import pw.binom.io.AsyncInput
import pw.binom.s3.dto.ContentHead

class InputFile(
    val data: ContentHead,
    val input: AsyncInput,
)
