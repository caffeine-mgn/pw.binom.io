package pw.binom.bluetooth

import com.sun.jna.*
import pw.binom.*
import java.nio.ByteBuffer
import java.io.File
import java.lang.System
import java.lang.ref.Cleaner


internal interface NativeLibrary : Library {
  companion object {
    const val PSM_SDP = 0x0001
    fun <T> createCleaner(arg: T, func: (T) -> Unit): Cleaner {
      val c = Cleaner.create()
      c.register(arg) { func(arg) }
      return c
    }

    private val os = System.getProperty("os.name")
    private val arch = System.getProperty("os.arch")
    private val libName = when {
      os == "Linux" && arch == "aarch64" -> "linux_arm64"
      os == "Linux" && arch == "amd64" -> "linux_x64"
      "windows" in os.lowercase() && arch == "amd64" -> "mingw_x64"
      else -> TODO("Library for $os $arch not supported")
    }

    val INSTANCE: NativeLibrary

    init {
      val libExt = when (Environment.os) {
        OS.WINDOWS -> "dll"
        OS.ANDROID,
        OS.LINUX,
          -> "so"

        OS.MACOS,
        OS.TVOS,
        OS.IOS,
        OS.WATCHOS,
          -> "dylib"

        else -> TODO()
      }

      val resultLibName = "bluetooth_binom_$libName-${BuildConfig.PROJECT_VERSION}.$libExt"
      val resultPathToLibOnFS = File(System.getProperty("java.io.tmpdir")).resolve(resultLibName)
      resultPathToLibOnFS.parentFile?.mkdirs()
      if (!resultPathToLibOnFS.isFile) {
        val stream = this::class.java.classLoader.getResourceAsStream(resultLibName)
          ?: TODO("Can't find resource $resultLibName")
        stream.use {
          resultPathToLibOnFS.outputStream().use { output ->
            it.copyTo(output)
          }
        }
      }
      INSTANCE = Native.load(resultPathToLibOnFS.absolutePath, NativeLibrary::class.java)
    }
  }

  open class NLocalDevice : Structure() {
    @JvmField
    var address = ByteArray(6)

    @JvmField
    var name = ByteArray(248)

    @JvmField
    var next: Pointer? = Pointer.NULL

    @Override
    override fun getFieldOrder(): List<String> =
      listOf("address", "name", "next")

    class ByReference : NLocalDevice, Structure.ByReference {
      constructor() : super()
      constructor(p: Pointer) : super() {
        useMemory(p);
        read();
      }
    }
  }

  class NOpennedDevice : Structure() {
    @JvmField
    var address = ByteArray(6)

    @Override
    override fun getFieldOrder(): List<String> =
      listOf("address")
  }

  open class NRemoteDevice : Structure() {
    @JvmField
    var name = ByteArray(248)

    @JvmField
    var address = ByteArray(6)

    @JvmField
    var next: Pointer? = Pointer.NULL

    @Override
    override fun getFieldOrder(): List<String> =
      listOf("name", "address", "next")

    class ByReference : NRemoteDevice, Structure.ByReference {
      constructor() : super()
      constructor(p: Pointer) : super() {
        useMemory(p);
        read();
      }
    }
  }

  open class NSPPConnection : Structure() {
    @JvmField
    var address = ByteArray(6)

    @Override
    override fun getFieldOrder(): List<String> =
      listOf("address")

    class ByReference : NSPPConnection, Structure.ByReference {
      constructor() : super()
      constructor(p: Pointer) : super() {
        useMemory(p);
        read();
      }
    }
  }

  open class NSPPServer : Structure() {
    @JvmField
    var address = ByteArray(6)

    @Override
    override fun getFieldOrder(): List<String> =
      listOf("address")

    class ByReference : NSPPServer, Structure.ByReference {
      constructor() : super()
      constructor(p: Pointer) : super() {
        useMemory(p);
        read();
      }
    }
  }

  /**
   * @return pointer to [NLocalDevice]
   */
  fun getLocalDevices(): Pointer

  /**
   * @param devices pointer to [NLocalDevice]
   */
  fun detachLocalDevices(devices: Pointer?)

  /**
   * @param device pointer to NLocalDevice
   * @return pointer to [NOpennedDevice]
   */
  fun openLocalDevice(device: Pointer): Pointer

  fun SDP_Request(device: Pointer,removeDeviceAddress: ByteArray)

  /**
   * @param device pointer to [NOpennedDevice]
   */
  fun closeLocalDevice(device: Pointer)

  /**
   * @param device pointer to [NOpennedDevice]
   */
  fun getLocalDeviceDiscoverable(device: Pointer): Int

  /**
   * @param device pointer to [NOpennedDevice]
   * @param enabled 1 - true, 0 - false
   */
  fun setLocalDeviceDiscoverable(device: Pointer, enabled: Int): Int

  /**
   * @param devices pointer to [NLocalDevice]
   */
  fun freeLocalDevices(devices: Pointer)

  /**
   * @param device pointer to [NOpennedDevice]
   * @param time time for searching: [time] * 1.28. For example where time=8 `8 * 1.28 = 10.24 seconds`
   * @return pointer to [NRemoteDevice]
   */
  fun searchRemoteDevices(device: Pointer, time:Int): Pointer?

  /**
   * @param devices pointer to [NRemoteDevice]
   */
  fun freeRemoteDevices(devices: Pointer?)

  // -------

  /**
   * @param device pointer to [NOpennedDevice]
   * @param removeDeviceAddress address to device
   * @param channel channel
   * @return pointer to [NSPPConnection]
   */
  fun connectSPP(
    device: Pointer,
    removeDeviceAddress: ByteArray,
    channel: Int,
  ): Pointer?

  /**
   * @param device pointer to [NOpennedDevice]
   * @param removeDeviceAddress address to device
   * @param channel channel
   * @return pointer to [NSPPConnection]
   */
  fun connectL2CAP(
    device: Pointer,
    removeDeviceAddress: ByteArray,
    psm: Int,
  ): Pointer?

  /**
   * @param connection pointer to [NSPPConnection]
   */
  fun closeSPPConnection(connection: Pointer)

  /**
   * @param connection pointer to [NSPPConnection]
   * @param data data for send
   * @param dataSize size of data
   * @return returns bytes of send data
   */
  fun writeToSPP(connection: Pointer, data: ByteArray, offset: Int, dataSize: Int): Int

  /**
   * @param connection pointer to [NSPPConnection]
   * @param data data for send
   * @param dataSize size of data
   * @return returns bytes of send data
   */
  fun writeToSPP(connection: Pointer, data: ByteBuffer, offset: Int, dataSize: Int): Int

  /**
   * @param connection pointer to [NSPPConnection]
   * @param data data for send
   * @param dataSize size of data
   * @return returns count of bytes was read
   */
  fun readFromSPP(connection: Pointer, data: ByteArray, offset: Int, dataSize: Int): Int

  /**
   * @param connection pointer to [NSPPConnection]
   * @param data data for send
   * @param dataSize size of data
   * @return returns count of bytes was read
   */
  fun readFromSPP(connection: Pointer, data: ByteBuffer, offset: Int, dataSize: Int): Int

  /**
   * Publishing SSP service on device [device]
   * @param device device for publishing service
   * @return pointer to NSPPConnection
   */
  fun publishSPP(device: Pointer, port: Int): Pointer?

  /**
   * accept client by spp on from [server]
   * @param server server for listening
   * @return pointer to NSPPConnection
   */
  fun acceptSPPClient(server: Pointer): Pointer?

  /**
   * Close SPP server
   */
  fun closeSPPServer(device: Pointer)
}

fun ByteArray.asString(): String {
  var length = 0
  while (length < size && this[length] != 0.toByte()) {
    length++
  }

  // Создаём строку из массива байт, обрезая по нулевому байту
  return decodeToString(0, length)
}
