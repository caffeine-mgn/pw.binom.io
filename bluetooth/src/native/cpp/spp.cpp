#include "spp.h"
#include "definition.h"
#include "utils.h"
#include <cstring>
#include <string.h>
#include "utils.h"
#include <stdio.h>

#ifdef LINUX_TARGET
#include <bluetooth/bluetooth.h>
#include <bluetooth/rfcomm.h>
#include <unistd.h>
#include <iostream>
#include <bluetooth/sdp.h>
#include <bluetooth/sdp_lib.h>
#include <bluetooth/l2cap.h>
#endif
#ifdef WINDOWS_TARGET
#include <winsock2.h>
#include <bluetoothapis.h>
#include <ws2bth.h>
#endif

#ifdef LINUX_TARGET
#define SPP_SERVICE_UUID "00001101-0000-1000-8000-00805F9B34FB"
#define SOCKET int
#endif
#ifdef WINDOWS_TARGET
// UUID для RFCOMM
static const GUID RFCOMM_PROTOCOL_UUID1 = { 0x00000003, 0x0000, 0x1000, {0x80, 0x00, 0x00, 0x80, 0x5F, 0x9B, 0x34, 0xFB} };

// Вручную определяем UUID для SPP (Serial Port Profile)
static const GUID SPP_PROTOCOL_UUID1 = { 0x00001101, 0x0000, 0x1000, { 0x80, 0x00, 0x00, 0x80, 0x5F, 0x9B, 0x34, 0xFB } };
#endif

SOCKET createSocketL2CAP(){
#ifdef LINUX_TARGET
    return socket(AF_BLUETOOTH, SOCK_STREAM, BTPROTO_L2CAP);
#endif
#ifdef WINDOWS_TARGET
    SOCKET s = socket(AF_BTH, SOCK_STREAM, BTHPROTO_L2CAP);
    if (s == INVALID_SOCKET){
        return -1;
    }
    return s;
#endif
}

SOCKET createSocketRFCOMM(){
#ifdef LINUX_TARGET
    return socket(AF_BLUETOOTH, SOCK_STREAM, BTPROTO_RFCOMM);
#endif
#ifdef WINDOWS_TARGET
    return socket(AF_BTH, SOCK_STREAM, BTHPROTO_RFCOMM);
#endif
}

void closeSocket(SOCKET sock){
#ifdef LINUX_TARGET
    close(sock);
#endif
#ifdef WINDOWS_TARGET
    closesocket(sock);
#endif
}

int bindDeviceL2CAP(SOCKET sock, struct NOpennedDevice *device, int psm){
#ifdef LINUX_TARGET
    fprintf(stdout,"bindDeviceL2CAP #1\n");
    fflush(stdout);
    struct sockaddr_l2 local_addr = {0};
    local_addr.l2_family = AF_BLUETOOTH;
    local_addr.l2_psm = htobs(psm);
    fprintf(stdout,"bindDeviceL2CAP #2\n");
    fflush(stdout);
    copyAddressAndReverseBytes((unsigned char*)&device->address,(unsigned char*)&local_addr.l2_bdaddr);// Адрес локального адаптера
    fprintf(stdout,"bindDeviceL2CAP #3\n");
    fflush(stdout);
    if (bind(sock, (struct sockaddr *)&local_addr, sizeof(local_addr)) < 0) {
        fprintf(stdout,"bindDeviceL2CAP #4 errno=%d\n", errno);
        fflush(stdout);
        return 0;
    }
    fprintf(stdout,"bindDeviceL2CAP #5\n");
    fflush(stdout);
#endif
#ifdef WINDOWS_TARGET

    SOCKADDR_BTH localAddr = {0};
    localAddr.addressFamily = AF_BTH;
    localAddr.btAddr = 0; // Локальный адрес (0 для любого адаптера)
    localAddr.port = psm; // PSM для пользовательского протокола

    // Привязка сокета к выбранному адаптеру
    copyAddressAndReverseBytes(device->address, (unsigned char*)localAddr.btAddr); // Используем адрес выбранного адаптера

    if (bind(sock, (SOCKADDR*)&localAddr, sizeof(localAddr)) == SOCKET_ERROR) {
        return 0;
    }

#endif
    return 1;
}

int bindDevice(SOCKET sock, struct NOpennedDevice *device){
#ifdef LINUX_TARGET
    fprintf(stdout,"bindDevice #1\n");
    fflush(stdout);
    struct sockaddr_rc local_addr = {0};
    local_addr.rc_family = AF_BLUETOOTH;
    fprintf(stdout,"bindDevice #2\n");
    fflush(stdout);
    copyAddressAndReverseBytes((unsigned char*)&device->address,(unsigned char*)&local_addr.rc_bdaddr);// Адрес локального адаптера
    fprintf(stdout,"bindDevice #3\n");
    fflush(stdout);
    if (bind(sock, (struct sockaddr *)&local_addr, sizeof(local_addr)) < 0) {
        fprintf(stdout,"bindDevice #4 errno=%d\n", errno);
        fflush(stdout);
        return 0;
    }
    fprintf(stdout,"bindDevice #5\n");
    fflush(stdout);
#endif
#ifdef WINDOWS_TARGET
    // Привязка сокета к выбранному адаптеру
    SOCKADDR_BTH localAddr = { 0 };
    localAddr.addressFamily = AF_BTH;
    copyAddressAndReverseBytes(device->address, (unsigned char*)localAddr.btAddr); // Используем адрес выбранного адаптера
    localAddr.port = BT_PORT_ANY; // Любой порт

    if (bind(sock, (SOCKADDR*)&localAddr, sizeof(localAddr)) == SOCKET_ERROR) {
        return 0;
    }

#endif
    return 1;
}

int internal_connectToSpp(SOCKET sock, unsigned char *removeDeviceAddress,int channel){
#ifdef LINUX_TARGET
    // Настройка адреса для подключения
    struct sockaddr_rc addr = {0};
    addr.rc_family = AF_BLUETOOTH;
    addr.rc_channel = (uint8_t)channel; // RFCOMM-канал
    copyAddressAndReverseBytes(removeDeviceAddress,(unsigned char*)&addr.rc_bdaddr);

    if (connect(sock, (struct sockaddr *)&addr, sizeof(addr)) < 0) {
        return 0;
    }
#endif
#ifdef WINDOWS_TARGET
    // Настройка адреса Bluetooth устройства
    SOCKADDR_BTH addr;
    memset(&addr, 0, sizeof(addr));
    addr.addressFamily = AF_BTH;
    addr.serviceClassId = RFCOMM_PROTOCOL_UUID1;
    addr.port = channel; // RFCOMM канал (обычно 1)
    copyAddressAndReverseBytes(removeDeviceAddress, (unsigned char*)addr.btAddr);
    // Подключение к устройству
    if (connect(sock, (SOCKADDR*)&addr, sizeof(addr)) == SOCKET_ERROR) {

        return 0;
    }
#endif
    return 1;
}

int internal_connectToL2CAP(SOCKET sock, unsigned char *removeDeviceAddress, int psm){
#ifdef LINUX_TARGET
    // Настройка адреса для подключения
    struct sockaddr_l2 addr = {0};
    addr.l2_family = AF_BLUETOOTH;
    addr.l2_psm = htobs(psm); // PSM для SDP
    copyAddressAndReverseBytes(removeDeviceAddress, (unsigned char*)&addr.l2_bdaddr);
    fprintf(stdout,"internal_connectToL2CAP #1\n");
    fflush(stdout);
    if (connect(sock, (struct sockaddr *)&addr, sizeof(addr)) < 0) {
        fprintf(stdout,"internal_connectToL2CAP #2 errno=%d\n", errno);
        fflush(stdout);
        return 0;
    }
    fprintf(stdout,"internal_connectToL2CAP #3\n");
    fflush(stdout);
#endif
#ifdef WINDOWS_TARGET
    // Настройка адреса Bluetooth устройства
    SOCKADDR_BTH addr = {0};
    addr.addressFamily = AF_BTH;
    addr.port = psm; // PSM для SDP
    copyAddressAndReverseBytes(removeDeviceAddress, (unsigned char*)addr.btAddr);

    // Подключение к устройству
    if (connect(sock, (SOCKADDR*)&addr, sizeof(addr)) == SOCKET_ERROR) {
        return 0;
    }
#endif
    return 1;
}

START_EXTERN
const struct NSPPConnection *connectToBluetooth(
    struct NOpennedDevice *device,
    unsigned char *removeDeviceAddress,
    int protocol,
    int channel) {
    if (!device){
        return NULL;
    }
    if (!removeDeviceAddress){
        return NULL;
    }
    // Создание RFCOMM-сокета
    int sock = createSocketRFCOMM();
    if (sock < 0) {
        return NULL;
    }
    if (!bindDevice(sock, device)){
        closeSocket(sock);
        return NULL;
    }
    // Настройка адреса для подключения
    if (!internal_connectToSpp(sock, removeDeviceAddress, channel)){
        closeSocket(sock);
        return NULL;
    }
    struct NSPPConnection*result = (struct NSPPConnection*) malloc(sizeof(struct NSPPConnection));
    result->socketId = sock;
    memcpy(result->address, removeDeviceAddress, 6);
    return result;
};

EXTERN_DLL_EXPORT const struct NSPPConnection *connectSPP(
    struct NOpennedDevice *device,
    unsigned char *removeDeviceAddress,
    int channel){
    if (!device){
        return NULL;
    }
    if (!removeDeviceAddress){
        return NULL;
    }
    // Создание RFCOMM-сокета
    int sock = createSocketRFCOMM();
    if (sock < 0) {
        return NULL;
    }
    if (!bindDevice(sock, device)){
        closeSocket(sock);
        return NULL;
    }
    // Настройка адреса для подключения
    if (!internal_connectToSpp(sock, removeDeviceAddress, channel)){
        closeSocket(sock);
        return NULL;
    }
    struct NSPPConnection*result = (struct NSPPConnection*) malloc(sizeof(struct NSPPConnection));
    result->socketId = sock;
    memcpy(result->address, removeDeviceAddress, 6);
    return result;
};

EXTERN_DLL_EXPORT const struct NSPPConnection *connectL2CAP(
    struct NOpennedDevice *device,
    unsigned char *removeDeviceAddress,
    int psm){
    if (!device){
        fprintf(stdout,"connectL2CAP #1\n");
        fflush(stdout);
        return NULL;
    }
    if (!removeDeviceAddress){
        fprintf(stdout,"connectL2CAP #2\n");
        fflush(stdout);
        return NULL;
    }
    // Создание RFCOMM-сокета
    int sock = createSocketL2CAP();
    if (sock < 0) {
        fprintf(stdout,"connectL2CAP #3\n");
        fflush(stdout);
        return NULL;
    }
    if (!bindDeviceL2CAP(sock, device, psm)){
        fprintf(stdout,"connectL2CAP #4\n");
        fflush(stdout);
        closeSocket(sock);
        return NULL;
    }
    // Настройка адреса для подключения
    if (!internal_connectToL2CAP(sock, removeDeviceAddress, psm)){
        fprintf(stdout,"connectL2CAP #5\n");
        fflush(stdout);
        closeSocket(sock);
        return NULL;
    }
    fprintf(stdout,"connectL2CAP #6\n");
    fflush(stdout);
    struct NSPPConnection*result = (struct NSPPConnection*) malloc(sizeof(struct NSPPConnection));
    result->socketId = sock;
    memcpy(result->address, removeDeviceAddress, 6);
    return result;
};

EXTERN_DLL_EXPORT int writeToSPP(const struct NSPPConnection *connection, signed char* data,int offset, int dataSize){
    if (!connection){
        return -1;
    }
#ifdef LINUX_TARGET
    return write(connection->socketId, &data[offset], dataSize);
#endif
#ifdef WINDOWS_TARGET
    return send(connection->socketId, (const char *) &data[offset], dataSize, 0);
#endif
}
EXTERN_DLL_EXPORT int readFromSPP(const struct NSPPConnection *connection, signed char* data,int offset, int dataSize){
    if (!connection){
        return -1;
    }
#ifdef LINUX_TARGET
    return read(connection->socketId, &data[offset], dataSize);
#endif
#ifdef WINDOWS_TARGET
    return recv(connection->socketId, (char *) &data[offset], dataSize, 0);
#endif
}
EXTERN_DLL_EXPORT void closeSPPConnection(const struct NSPPConnection *connection){
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
#ifdef LINUX_TARGET
int register_spp_service(bdaddr_t *adapter_bdaddr,uint8_t channel) {
    bdaddr_t local_bdaddr = {{0, 0, 0, 0xff, 0xff, 0xff}};
    sdp_session_t *session = sdp_connect(adapter_bdaddr, &local_bdaddr, SDP_RETRY_IF_BUSY);
    if (!session) {
        perror("Failed to connect to SDP server");
        return -1;
    }

    sdp_record_t *record = sdp_record_alloc();
    if (!record) {
        perror("Failed to allocate SDP record");
        sdp_close(session);
        return -1;
    }

    // Устанавливаем UUID для SPP сервиса
    sdp_uuid128_create(&record->svclass, SPP_SERVICE_UUID);
    sdp_set_info_attr(record, "SPP Service", NULL, NULL);

    // Создаем список протоколов
    sdp_list_t *proto_list = NULL;
    sdp_list_t *proto_desc_list = NULL;
    sdp_data_t *channel_data = NULL;

    // Создаем UUID для RFCOMM
    uuid_t rfcomm_uuid;
    sdp_uuid16_create(&rfcomm_uuid, RFCOMM_UUID);

    // Добавляем UUID RFCOMM в список протоколов
    proto_list = sdp_list_append(NULL, sdp_data_alloc(SDP_UUID16, &rfcomm_uuid));

    // Создаем данные для канала
    channel_data = sdp_data_alloc(SDP_UINT8, &channel);
    proto_list = sdp_list_append(proto_list, channel_data);

    // Создаем список протоколов
    proto_desc_list = sdp_list_append(NULL, proto_list);

    // Устанавливаем протоколы доступа
    sdp_set_access_protos(record, proto_desc_list);

    // Регистрируем SDP запись
    if (sdp_record_register(session, record, 0) < 0) {
        perror("Failed to register SDP record");
        sdp_record_free(record);
        sdp_close(session);
        return -1;
    }

    // Освобождаем ресурсы
    sdp_list_free(proto_list, NULL);
    sdp_list_free(proto_desc_list, NULL);
    sdp_record_free(record);
    sdp_close(session);
    return 0;
}
#endif

EXTERN_DLL_EXPORT const struct NSPPServer *publishSPP(struct NOpennedDevice *device, int port){
#ifdef LINUX_TARGET
    struct sockaddr_rc loc_addr = { 0 }, rem_addr = { 0 };
    char buf[1024] = { 0 };
    int serverSocket, client, bytes_read;
    socklen_t opt = sizeof(rem_addr);

    // Создаем RFCOMM сокет
    serverSocket = socket(AF_BLUETOOTH, SOCK_STREAM, BTPROTO_RFCOMM);
    if (serverSocket < 0) {
        perror("Failed to create socket");
        return NULL;
    }

    // Привязываем сокет к локальному адресу
    loc_addr.rc_family = AF_BLUETOOTH;
    //loc_addr.rc_bdaddr = *BDADDR_ANY;
    loc_addr.rc_channel = port<=0 ? 0 : port; // 0 означает, что ядро само выберет канал
    copyAddressAndReverseBytes(device->address, (unsigned char*)&loc_addr.rc_bdaddr);

    if (bind(serverSocket, (struct sockaddr *)&loc_addr, sizeof(loc_addr)) < 0) {
        perror("Failed to bind");
        close(serverSocket);
        return NULL;
    }

    // Получаем номер канала, который был выделен
    socklen_t len = sizeof(loc_addr);
    if (getsockname(serverSocket, (struct sockaddr *)&loc_addr, &len) < 0) {
        perror("Failed to get socket name");
        close(serverSocket);
        return NULL;
    }

    // Регистрируем SPP сервис
    if (register_spp_service(&loc_addr.rc_bdaddr, loc_addr.rc_channel) < 0) {
        close(serverSocket);
        return NULL;
    }

    // Ожидаем подключения
    listen(serverSocket, 1);
    printf("Waiting for connection on channel %d...\n", loc_addr.rc_channel);
    struct NSPPServer*result = (struct NSPPServer*) malloc(sizeof(struct NSPPServer));
    result->socketId = serverSocket;
    result->port = loc_addr.rc_channel;
    memcpy(device->address, result->address, 6);
    return result;
#endif
#ifdef WINDOWS_TARGET
    WSADATA wsaData;
    SOCKET serverSocket;
    SOCKADDR_BTH serverAddr;

    // Создание сокета
    serverSocket = socket(AF_BTH, SOCK_STREAM, BTHPROTO_RFCOMM);
    if (serverSocket == INVALID_SOCKET) {
        // WSAGetLastError()
        return NULL;
    }

    // Настройка адреса сервера
    memset(&serverAddr, 0, sizeof(serverAddr));
    serverAddr.addressFamily = AF_BTH;
    serverAddr.port = port<=0 ? BT_PORT_ANY : port;

    copyAddressAndReverseBytes(device->address, (unsigned char*)&serverAddr.btAddr);

    // Привязка сокета к адресу
    if (bind(serverSocket, (SOCKADDR*)&serverAddr, sizeof(serverAddr)) == SOCKET_ERROR) {
        // WSAGetLastError()
        closesocket(serverSocket);
        return NULL;
    }

    // Публикация SPP сервиса
    WSAQUERYSET serviceInfo = { 0 };
    CSADDR_INFO addrInfo = { 0 };
    char serviceName[] = "SPP Server";
    char serviceComment[] = "Serial Port Profile Server";

    // Настройка информации о сервисе
    addrInfo.LocalAddr.iSockaddrLength = sizeof(serverAddr);
    addrInfo.LocalAddr.lpSockaddr = (LPSOCKADDR)&serverAddr;
    addrInfo.iSocketType = SOCK_STREAM;
    addrInfo.iProtocol = BTHPROTO_RFCOMM;

    serviceInfo.dwSize = sizeof(serviceInfo);
    serviceInfo.lpszServiceInstanceName = serviceName;
    serviceInfo.lpszComment = serviceComment;
    serviceInfo.lpServiceClassId = (LPGUID)&SPP_PROTOCOL_UUID1;
    serviceInfo.dwNameSpace = NS_BTH;
    serviceInfo.dwNumberOfCsAddrs = 1;
    serviceInfo.lpcsaBuffer = &addrInfo;

    // Регистрация сервиса
    if (WSASetService(&serviceInfo, RNRSERVICE_REGISTER, 0) == SOCKET_ERROR) {
        // WSAGetLastError();
        closesocket(serverSocket);
        return NULL;
    }
    int addrLen = sizeof(serverAddr);
    if (getsockname(serverSocket, (SOCKADDR*)&serverAddr, &addrLen) == SOCKET_ERROR) {
        closesocket(serverSocket);
        return NULL;
    }

    // Ожидание входящих подключений
    if (listen(serverSocket, 1) == SOCKET_ERROR) {
        // WSAGetLastError()
        closesocket(serverSocket);
        return NULL;
    }
    struct NSPPServer*result = (struct NSPPServer*) malloc(sizeof(struct NSPPServer));
    result->socketId = serverSocket;
    result->port = serverAddr.port;
    memcpy(device->address, result->address, 6);
    return result;
#endif
};

EXTERN_DLL_EXPORT const struct NSPPConnection * acceptSPPClient(const struct NSPPServer *server){
#ifdef LINUX_TARGET
    listen(server->socketId, 1);
//    printf("Waiting for connection on channel %d...\n", loc_addr.rc_channel);
    struct sockaddr_rc  rem_addr = { 0 };
    socklen_t opt = sizeof(rem_addr);
    int clientSocket = accept(server->socketId, (struct sockaddr *)&rem_addr, &opt);
    if (clientSocket < 0) {
        return NULL;
    }
    struct NSPPConnection *result = (struct NSPPConnection*) malloc(sizeof(struct NSPPConnection));
    result->socketId = clientSocket;
    copyAddressAndReverseBytes((unsigned char*)&rem_addr.rc_bdaddr, result->address);
#endif
#ifdef WINDOWS_TARGET
    SOCKADDR_BTH clientAddr;
    int clientAddrLen = sizeof(clientAddr);
    SOCKET clientSocket = accept(server->socketId, (SOCKADDR*)&clientAddr, &clientAddrLen);
    if (clientSocket == INVALID_SOCKET) {
        // WSAGetLastError();
        return NULL;
    }

    struct NSPPConnection*result = (struct NSPPConnection*) malloc(sizeof(struct NSPPConnection));
    result->socketId = clientSocket;
    copyAddressAndReverseBytes((unsigned char*)clientAddr.btAddr, result->address);
#endif
return result;
};

EXTERN_DLL_EXPORT void closeSPPServer(const struct NSPPServer *server) {
#ifdef LINUX_TARGET
    close(server->socketId);
#endif
#ifdef WINDOWS_TARGET
    closesocket(server->socketId);
#endif
    free((void*)server);
}

END_EXTERN
