package pw.binom.bluetooth

expect class LocalDevice {
  val address: Address
  val name: String
  fun open(): OpenedLocalDevice
}
