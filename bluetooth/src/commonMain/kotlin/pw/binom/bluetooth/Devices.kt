package pw.binom.bluetooth

expect object Devices {
  fun getDevices():List<LocalDevice>
}
