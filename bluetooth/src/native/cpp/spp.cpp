#include "spp.h"
#include "definition.h"
#include "utils.h"
#include <cstring>
#include <string.h>
#include "utils.h"

#ifdef LINUX_TARGET
#include <bluetooth/bluetooth.h>
#include <bluetooth/rfcomm.h>
#include <unistd.h>
#include <iostream>
#endif
#ifdef WINDOWS_TARGET
#include <winsock2.h>
#include <bluetoothapis.h>
#include <ws2bth.h>
#endif
START_EXTERN
#ifdef WINDOWS_TARGET
// UUID для RFCOMM
static const GUID RFCOMM_PROTOCOL_UUID1 = { 0x00000003, 0x0000, 0x1000, {0x80, 0x00, 0x00, 0x80, 0x5F, 0x9B, 0x34, 0xFB} };
#endif
const struct NSPPConnection *openSPP(
    struct NOpennedDevice *device,
    unsigned char *removeDeviceAddress,
    int channel) {
#ifdef LINUX_TARGET
    // Создание RFCOMM-сокета
    int sock = socket(AF_BLUETOOTH, SOCK_STREAM, BTPROTO_RFCOMM);
    if (sock < 0) {
        return NULL;
    }

    struct sockaddr_rc local_addr = {0};
    local_addr.rc_family = AF_BLUETOOTH;

    copyAddressAndReverseBytes((unsigned char*)&device->address,(unsigned char*)&local_addr.rc_bdaddr);// Адрес локального адаптера

    if (bind(sock, (struct sockaddr *)&local_addr, sizeof(local_addr)) < 0) {
        close(sock);
        return NULL;
    }


    // Настройка адреса для подключения
    struct sockaddr_rc addr = {0};
    addr.rc_family = AF_BLUETOOTH;
    addr.rc_channel = (uint8_t)channel; // RFCOMM-канал
    copyAddressAndReverseBytes(removeDeviceAddress,(unsigned char*)&addr.rc_bdaddr);

    if (connect(sock, (struct sockaddr *)&addr, sizeof(addr)) < 0) {
        close(sock);
        return NULL;
    }
    struct NSPPConnection*result = (struct NSPPConnection*) malloc(sizeof(struct NSPPConnection));
    result->socketId = sock;
    return result;
#endif
#ifdef WINDOWS_TARGET
    // Создание сокета
    SOCKET sock = socket(AF_BTH, SOCK_STREAM, BTHPROTO_RFCOMM);
    if (sock == INVALID_SOCKET) {
        // WSAGetLastError()
        // WSACleanup();
        return NULL;
    }

    // Привязка сокета к выбранному адаптеру
    SOCKADDR_BTH localAddr = { 0 };
    localAddr.addressFamily = AF_BTH;
    copyAddressAndReverseBytes(removeDeviceAddress, (unsigned char*)localAddr.btAddr); // Используем адрес выбранного адаптера
    localAddr.port = BT_PORT_ANY; // Любой порт

    if (bind(sock, (SOCKADDR*)&localAddr, sizeof(localAddr)) == SOCKET_ERROR) {
        // WSAGetLastError()
        closesocket(sock);
        WSACleanup();
        return NULL;
    }

    // Настройка адреса Bluetooth устройства
    SOCKADDR_BTH addr;
    memset(&addr, 0, sizeof(addr));
    addr.addressFamily = AF_BTH;
    addr.btAddr = 0x00066608C457; // Замените на адрес вашего Bluetooth устройства
    addr.serviceClassId = RFCOMM_PROTOCOL_UUID1;
    addr.port = channel; // RFCOMM канал (обычно 1)

    // Подключение к устройству
    if (connect(sock, (SOCKADDR*)&addr, sizeof(addr)) == SOCKET_ERROR) {
        // WSAGetLastError()
        closesocket(sock);
        WSACleanup();
        return NULL;
    }
    struct NSPPConnection*result = (struct NSPPConnection*) malloc(sizeof(struct NSPPConnection));
    result->socketId = sock;
    return result;
#endif
};

int writeToSPP(const struct NSPPConnection *connection, signed char* data,int offset, int dataSize){
#ifdef LINUX_TARGET
    return write(connection->socketId, &data[offset], dataSize);
#endif
#ifdef WINDOWS_TARGET
    return send(connection->socketId, (const char *) &data[offset], dataSize, 0);
#endif
}
int readFromSPP(const struct NSPPConnection *connection, signed char* data,int offset, int dataSize){
#ifdef LINUX_TARGET
    return read(connection->socketId, &data[offset], dataSize);
#endif
#ifdef WINDOWS_TARGET
    return recv(connection->socketId, (char *) &data[offset], dataSize, 0);
#endif
}
void closeSPP(const struct NSPPConnection *connection){
    if (!connection){
        return;
    }
#ifdef LINUX_TARGET
    close(connection->socketId);
#endif
#ifdef WINDOWS_TARGET
    closesocket(connection->socketId);
#endif
    free((void*)connection);
};
END_EXTERN
