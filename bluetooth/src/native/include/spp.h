#ifndef SPP_H
#define SPP_H

#include "definition.h"
#include "utils.h"
#include "devices.h"

#ifdef WINDOWS_TARGET
#include <winsock2.h>
#endif

START_EXTERN

struct NSPPConnection {
#ifdef LINUX_TARGET
    int socketId;
#endif
#ifdef WINDOWS_TARGET
    SOCKET socketId;
#endif
};

const struct NSPPConnection *openSPP(
    struct NOpennedDevice *device,
    unsigned char *removeDeviceAddress,
    int channel);

void closeSPP(
    const struct NSPPConnection *connection);
int writeToSPP(
    const struct NSPPConnection *connection, signed char* data,int offset, int dataSize);
int readFromSPP(
    const struct NSPPConnection *connection, signed char* data,int offset, int dataSize);
END_EXTERN

#endif
