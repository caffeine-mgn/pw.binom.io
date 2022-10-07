package pw.binom.s3

import pw.binom.s3.dto.Bucket
import pw.binom.s3.dto.Owner

data class Buckets(
    val owner: Owner,
    val list: List<Bucket>
)
