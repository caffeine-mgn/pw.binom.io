package pw.binom.compression.tar

import pw.binom.io.file.File
import pw.binom.io.file.openWrite
import pw.binom.io.use
import pw.binom.io.wrap
import kotlin.test.Ignore
import kotlin.test.Test

class TarWriteTest {
  @Ignore
  @Test
  fun writeTest() {
    File("C:\\TEMP\\testGodotLib\\.build\\123.tar").openWrite().use {
      TarWriter(it, closeStream = false).use {
        it.newEntity(
          name = "dir",
          mode = "100777".toUShort(8),
          uid = 0.toUShort(),
          gid = 0.toUShort(),
          time = 1627445903000L,
          type = TarEntityType.DIRECTORY,
          dataSize = null,
        ).close()
        it.newEntity(
          name = "text1.txt",
          mode = "100777".toUShort(8),
          uid = 0.toUShort(),
          gid = 0.toUShort(),
          time = 1627445903000L,
          type = TarEntityType.NORMAL,
          dataSize = null,
        ).use {
          "File #1".encodeToByteArray().wrap { buf ->
            it.writeFully(buf)
          }
        }
        it.newEntity(
          name = "text2.txt",
          mode = "100777".toUShort(8),
          uid = 0.toUShort(),
          gid = 0.toUShort(),
          time = 1627445903000L,
          type = TarEntityType.NORMAL,
          dataSize = null,
        ).use {
          it.writeFully("File #2".encodeToByteArray().wrap())
        }
      }
    }
  }
}
