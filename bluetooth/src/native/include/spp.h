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
    unsigned char address[6];
#ifdef LINUX_TARGET
    int socketId;
#endif
#ifdef WINDOWS_TARGET
    SOCKET socketId;
#endif
};

struct NSPPServer {
    unsigned char address[6];
    int port;
#ifdef LINUX_TARGET
    int socketId;
#endif
#ifdef WINDOWS_TARGET
    SOCKET socketId;
#endif
};

const struct NSPPConnection *connectSPP(
    struct NOpennedDevice *device,
    unsigned char *removeDeviceAddress,
    int channel);

EXTERN_DLL_EXPORT void closeSPPConnection(const struct NSPPConnection *connection);
EXTERN_DLL_EXPORT int writeToSPP(const struct NSPPConnection *connection, signed char* data,int offset, int dataSize);
EXTERN_DLL_EXPORT int readFromSPP(const struct NSPPConnection *connection, signed char* data,int offset, int dataSize);


EXTERN_DLL_EXPORT const struct NSPPServer *publishSPP(struct NOpennedDevice *device, int port);
EXTERN_DLL_EXPORT const struct NSPPConnection * acceptSPPClient(const struct NSPPServer *server);
EXTERN_DLL_EXPORT void closeSPPServer(const struct NSPPServer *server);
END_EXTERN

#endif
