#include <cstdio>
#include <cstdlib>
#include <cstring>
#include <cwchar>
#include <stdint.h>
#include "devices.h"
#include "definition.h"
#include "utils.h"

#ifdef LINUX_TARGET
#include <iostream>
#include <unistd.h>
#include <bluetooth/bluetooth.h>
#include <bluetooth/hci.h>
#include <bluetooth/hci_lib.h>
#include <string.h>
#endif

#ifdef WINDOWS_TARGET
#include <windows.h>
#include <bluetoothapis.h>
#include <stdio.h>
#endif

#ifdef LINUX_TARGET
int hci_read_scan_enable(int sock, uint8_t *scan_enable, int timeout) {
    struct hci_request rq;
    uint8_t buf[HCI_MAX_EVENT_SIZE];
    memset(&rq, 0, sizeof(rq));

    // Устанавливаем команду HCI_Read_Scan_Enable
    rq.ogf = OGF_HOST_CTL;
    rq.ocf = 0x0019; // OCF для HCI_Read_Scan_Enable
    rq.rparam = buf;
    rq.rlen = sizeof(buf);

    if (hci_send_req(sock, &rq, timeout) < 0) {
        return -1;
    }

    *scan_enable = buf[0];
    return 0;
}

int hci_write_scan_enable(int sock, uint8_t scan_enable, int timeout) {
    struct hci_request rq;
    uint8_t buf[3];
    memset(&rq, 0, sizeof(rq));

    // Устанавливаем команду HCI_Write_Scan_Enable
    buf[0] = scan_enable; // Режим сканирования
    rq.ogf = OGF_HOST_CTL;
    rq.ocf = 0x001A; // OCF для HCI_Write_Scan_Enable
    rq.cparam = buf;
    rq.clen = 1;

    if (hci_send_req(sock, &rq, timeout) < 0) {
        return -errno;
    }

    return 1;
}
#endif

START_EXTERN
EXTERN_DLL_EXPORT const struct NLocalDevice *getLocalDevices() {
    struct NLocalDevice *result = NULL;
#ifdef LINUX_TARGET
    struct hci_dev_info di;
    // Получаем список всех доступных адаптеров
    for (int dev_id = 0; ; dev_id++) {
        int sock = hci_open_dev(dev_id);
        if (sock < 0) {
            if (errno == ENODEV) {
                break; // Больше адаптеров нет
            }
            continue;
        }

        if (hci_devinfo(dev_id, &di) < 0) {
            close(sock);
            continue;
        }
        struct NLocalDevice *nextDevice = (struct NLocalDevice *) malloc(sizeof(NLocalDevice));
        nextDevice->deviceId = dev_id;
        copyAddressAndReverseBytes((unsigned char *) &di.bdaddr, (unsigned char *) &nextDevice->address);
        memcpy(&nextDevice->name, &di.name, 248);
        nextDevice->next = result;
        result = nextDevice;
        close(sock);
    }
#endif
#ifdef WINDOWS_TARGET
    BLUETOOTH_FIND_RADIO_PARAMS findRadioParams = {0};
    HANDLE hRadio = NULL;
    HBLUETOOTH_RADIO_FIND hFindRadio = NULL;
    DWORD errorCode = 0;

    // Инициализация структуры для поиска радиоустройств
    findRadioParams.dwSize = sizeof(BLUETOOTH_FIND_RADIO_PARAMS);

    // Начинаем поиск первого Bluetooth-радио
    hFindRadio = BluetoothFindFirstRadio(&findRadioParams, &hRadio);
    if (hFindRadio == NULL) {
        errorCode = GetLastError();
        printf("BluetoothFindFirstRadio failed with error: %d\n", (int)errorCode);
        return NULL;
    }

    // Выводим информацию о найденном радиоустройстве
    do {
        BLUETOOTH_RADIO_INFO radioInfo = {0};
        radioInfo.dwSize = sizeof(BLUETOOTH_RADIO_INFO);

        // Получаем информацию о радиоустройстве
        if (BluetoothGetRadioInfo(hRadio, &radioInfo) == ERROR_SUCCESS) {
            struct NLocalDevice* s = (struct NLocalDevice*)malloc(sizeof(NLocalDevice));
            memcpy(&s->name, &radioInfo.szName, BLUETOOTH_MAX_NAME_SIZE);
            copyAddressAndReverseBytes((unsigned char*)&radioInfo.address.rgBytes, (unsigned char*)&s->address);
            s->next = result;
            s->device = hRadio;
            result = s;
        } else {
        }

        // Закрываем дескриптор радиоустройства
        CloseHandle(hRadio);
    } while (BluetoothFindNextRadio(hFindRadio, &hRadio));

    // Завершаем поиск радиоустройств
    BluetoothFindRadioClose(hFindRadio);
#endif
    return result;
}

EXTERN_DLL_EXPORT void detachLocalDevices(struct NLocalDevice *devices) {
    struct NLocalDevice *d = devices;
    while (d != NULL) {
        struct NLocalDevice *n = d;
        d = d->next;
        n->next = (struct NLocalDevice *) NULL;
    }
}

EXTERN_DLL_EXPORT void freeLocalDevices(const struct NLocalDevice *devices) {
    const struct NLocalDevice *d = devices;
    while (d != 0) {
        const struct NLocalDevice *n = d;
        d = d->next;
        delete n;
    }
}

EXTERN_DLL_EXPORT const struct NOpennedDevice *openLocalDevice(const struct NLocalDevice *devices) {
    if (!devices) {
        return NULL;
    }
#ifdef LINUX_TARGET
    int sock = hci_open_dev(devices->deviceId);
    if (sock < 0) {
        return NULL;
    }
    struct NOpennedDevice *result = (struct NOpennedDevice *) malloc(sizeof(NOpennedDevice));
    result->deviceId = devices->deviceId;
    result->socketId = sock;
    memcpy(result->name, devices->name, 248);
    copyAddressAndReverseBytes((unsigned char *) &devices->address, (unsigned char *) &result->address);
    return result;
#endif
#ifdef WINDOWS_TARGET
    struct NOpennedDevice* result = (struct NOpennedDevice*)malloc(sizeof(NOpennedDevice));
    result->device = devices->device;
    return result;
#endif
}

EXTERN_DLL_EXPORT void closeLocalDevice(const struct NOpennedDevice *device) {
    if (!device) {
        return;
    }
#ifdef LINUX_TARGET
    close(device->socketId);
    free((void *) device);
#endif
#ifdef WINDOWS_TARGET
    free((void*)device);
#endif
}

EXTERN_DLL_EXPORT const int getLocalDeviceDiscoverable(const struct NOpennedDevice *device) {
#ifdef LINUX_TARGET
    uint8_t discoverable = 0;
    int err = hci_read_scan_enable(device->socketId, &discoverable, 1000);
    if (err < 0) {
        return -1;
    }
    return discoverable == (SCAN_INQUIRY | SCAN_PAGE);
#endif
#ifdef WINDOWS_TARGET
    return 0;
#endif
}

EXTERN_DLL_EXPORT int setLocalDeviceDiscoverable(const struct NOpennedDevice *device, const int enabled) {
#ifdef LINUX_TARGET
    uint8_t scan_enable = enabled > 0 ? (SCAN_INQUIRY | SCAN_PAGE) : SCAN_PAGE;
    // Устанавливаем режим сканирования
    if (!hci_write_scan_enable(device->socketId, scan_enable, 1000)) {
        return -errno;
    }
    return 1;
#endif
#ifdef WINDOWS_TARGET
    return 0;
#endif
}

EXTERN_DLL_EXPORT const struct NRemoteDevice *searchRemoteDevices(const struct NOpennedDevice *device, int time) {
    //    int len = 8;        // Длительность сканирования: 8 * 1.28 = 10.24 секунд
    struct NRemoteDevice *result = NULL;
#ifdef LINUX_TARGET
    if (!device) {
        return NULL;
    }

    int max_rsp = 255; // Максимальное количество устройств
    int flags = IREQ_CACHE_FLUSH;

    inquiry_info *ii = (inquiry_info *) malloc(max_rsp * sizeof(inquiry_info));
    if (!ii) {
        return NULL;
    }

    int num_rsp = hci_inquiry(device->deviceId, time, max_rsp, NULL, &ii, flags);
    if (num_rsp <= 0) {
        free(ii);
        return NULL;
    }
    for (int i = 0; i < num_rsp; i++) {
        struct NRemoteDevice *s = (struct NRemoteDevice *) malloc(sizeof(NRemoteDevice));
        copyAddressAndReverseBytes((unsigned char *) &ii[i].bdaddr, (unsigned char *) &s->address);
        memset(&s->name, 0, sizeof(s->name));
        hci_read_remote_name(device->socketId, &ii[i].bdaddr, sizeof(s->name), s->name, 0);
        s->next = result;
        result = s;
    }
    free(ii);
#endif
#ifdef WINDOWS_TARGET
    BLUETOOTH_DEVICE_SEARCH_PARAMS searchParams = {0};
    BLUETOOTH_DEVICE_INFO deviceInfo = {0};
    HBLUETOOTH_DEVICE_FIND hFindDevice = NULL;

    // Инициализация структуры для поиска устройств
    searchParams.dwSize = sizeof(BLUETOOTH_DEVICE_SEARCH_PARAMS);
    searchParams.fReturnAuthenticated = TRUE;
    searchParams.fReturnRemembered = TRUE;
    searchParams.fReturnUnknown = TRUE;
    searchParams.fReturnConnected = TRUE;
    searchParams.cTimeoutMultiplier = time; // Время ожидания (в единицах по 1.28 секунд)

    // Указываем выбранное радиоустройство
    searchParams.hRadio = device->device;


    // Инициализация структуры для хранения информации об устройстве
    deviceInfo.dwSize = sizeof(BLUETOOTH_DEVICE_INFO);

    hFindDevice = BluetoothFindFirstDevice(&searchParams, &deviceInfo);
    if (hFindDevice == NULL) {
        printf("No Bluetooth devices found.\n");
        return NULL;
    }


    // Выводим информацию о найденных устройствах
    do {
        printf("Device Name: %S\n", deviceInfo.szName);
        printf("Device Address: %02X:%02X:%02X:%02X:%02X:%02X\n",
               deviceInfo.Address.rgBytes[5], deviceInfo.Address.rgBytes[4],
               deviceInfo.Address.rgBytes[3], deviceInfo.Address.rgBytes[2],
               deviceInfo.Address.rgBytes[1], deviceInfo.Address.rgBytes[0]);
        printf("Device Class: %lu\n", deviceInfo.ulClassofDevice);
        printf("Connected: %s\n", deviceInfo.fConnected ? "Yes" : "No");
        printf("Authenticated: %s\n", deviceInfo.fAuthenticated ? "Yes" : "No");
        printf("Remembered: %s\n", deviceInfo.fRemembered ? "Yes" : "No");
        printf("\n");

        struct NRemoteDevice* s = (struct NRemoteDevice*)malloc(sizeof(NRemoteDevice));
        copyAddressAndReverseBytes((unsigned char*)deviceInfo.Address.rgBytes,(unsigned char*)&s->address);
        memcpy(s->name, deviceInfo.szName, BLUETOOTH_MAX_NAME_SIZE);
        s->next = result;
        result = s;

    } while (BluetoothFindNextDevice(hFindDevice, &deviceInfo));

    // Завершаем поиск устройств
    BluetoothFindDeviceClose(hFindDevice);
#endif
    return result;
}

EXTERN_DLL_EXPORT void freeRemoteDevices(const struct NRemoteDevice *devices) {
    const struct NRemoteDevice *d = devices;
    while (d != 0) {
        const struct NRemoteDevice *n = d;
        d = d->next;
        delete n;
    }
}

END_EXTERN
