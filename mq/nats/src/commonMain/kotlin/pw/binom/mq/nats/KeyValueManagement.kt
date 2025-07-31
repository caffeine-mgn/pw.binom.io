package pw.binom.mq.nats

interface KeyValueManagement {
  suspend fun getBucketNames(): List<String>
  suspend fun delete(bucketName: String)
}
