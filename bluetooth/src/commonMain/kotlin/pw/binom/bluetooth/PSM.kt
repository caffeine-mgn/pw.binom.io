package pw.binom.bluetooth

import kotlin.jvm.JvmInline

@JvmInline
value class PSM(val value:Int){
  companion object {

    // Используется для обнаружения сервисов на удаленном устройстве.
    val SPD = PSM(0x0001)

    // Эмуляция последовательного порта (Serial Port Emulation).
    val RFCOMM = PSM(0x0003)

    // Используется для управления телефонией (например, гарнитуры).
    val TCS_BIN = PSM(0x0005)

    // Используется для беспроводной телефонии.
    val TCS_BIN_CORDLESS = PSM(0x0007)

    // Используется для передачи сетевого трафика (например, PAN).
    val BNEP = PSM(0x000F)

    // Используется для управления устройствами HID (например, клавиатуры).
    val HID_CONTROL = PSM(0x0011)

    // Используется для передачи прерываний от устройств HID.
    val HID_INTERRUPT = PSM(0x0013)

    // Используется для обнаружения устройств UPnP.
    val UPnP = PSM(0x0015)

    // Используется для управления аудио/видео устройствами.
    val AVCTP = PSM(0x0017)

    // Используется для передачи аудио/видео данных.
    val AVDTP = PSM(0x0019)

    // Используется для управления браузингом аудио/видео устройств.
    val AVCTP_Browsing = PSM(0x001B)

    // Используется для передачи цифровых данных.
    val UDI = PSM(0x001D)
  }
}

