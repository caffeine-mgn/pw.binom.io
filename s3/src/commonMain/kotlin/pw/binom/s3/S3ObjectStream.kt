package pw.binom.s3

import pw.binom.io.AsyncInput
import pw.binom.s3.dto.ContentHead

class S3ObjectStream(
  val data: ContentHead,
  private val source: AsyncInput,
) : AsyncInput by source
