//
// Created by subochev on 04.06.23.
//

#ifndef NATIVE_NSOCKET_H
#define NATIVE_NSOCKET_H

#include "NInetSocketNetworkAddress.h"
#include "NNetworkAddress.h"
#include "definition.h"

//#ifdef WINDOWS_TARGET
//#include <winsock2.h>
//#include <psdk_inc/_socket_types.h>
//#endif

#define SOCKET_TYPE_TCP 1
#define SOCKET_TYPE_UDP 2

#define BIND_RESULT_OK 1
#define BIND_RESULT_ALREADY_BINDED -2
#define BIND_RESULT_ADDRESS_ALREADY_IN_USE -3
#define BIND_RESULT_NOT_SUPPORTED -4
#define BIND_RESULT_PROTOCOL_NOT_SUPPORTED -5
#define BIND_RESULT_UNKNOWN_ERROR 0

#define ConnectStatus_OK 1
#define ConnectStatus_FAIL -1
#define ConnectStatus_CONNECTION_REFUSED -2
#define ConnectStatus_ALREADY_CONNECTED -3
#define ConnectStatus_IN_PROGRESS -4

struct NSocket {
    int native;
    int type;
    int protocolFamily;
    int blockedMode;
    int closed;
};

int NSocket_getSocketPort(struct NSocket *native);

int NSocket_create(struct NSocket *dest, int protocolFamily, int socketType);

int NSocket_close(struct NSocket *dest);

int NSocket_isClose(struct NSocket *dest);

int NSocket_connectInet(struct NSocket *dest, struct NInetSocketNetworkAddress *address);

int NSocket_connectUnix(struct NSocket *dest, const char *path);

int NSocket_setReuseaddr(struct NSocket *native, int value);

int NSocket_setMulticastInterface(struct NSocket *dest, struct NNetworkAddress *networkAddress,
                                  const char *networkInterfaceName);

int NSocket_joinGroup(struct NSocket *dest, struct NInetSocketNetworkAddress *address,
                      struct NNetworkAddress *networkAddress, const char *networkInterfaceName);

int NSocket_leaveGroup(int socket, struct NNetworkAddress *address, int netIfIndex);

int NSocket_getTTL(struct NSocket *native, unsigned char *ttl);
int NSocket_setTTL(struct NSocket *native, unsigned char ttl);

int NSocket_unbind(int socket);

int NSocket_getNoDelay(struct NSocket *native);

int NSocket_setNoDelay(struct NSocket *native, int value);

int NSocket_send(struct NSocket *native, signed char *buffer, int bufferLen);

int NSocket_setBroadcastEnabled(struct NSocket *native, int value);
int NSocket_sendTo(struct NSocket *native, struct NInetSocketNetworkAddress *address, signed char *buffer, int bufferLen);

int NSocket_receiveOnly(struct NSocket *native, signed char *buffer, int bufferLen);

int NSocket_setSoTimeout(struct NSocket *native, unsigned long long timeoutMs);

int NSocket_getBlockedMode(struct NSocket *native);

int NSocket_setBlockedMode(struct NSocket *native, int value);

int NSocket_bindInetAny(struct NSocket *dest);

int NSocket_bindInet(struct NSocket *native, struct NInetSocketNetworkAddress *address, int needListen);

int NSocket_bindUnix(struct NSocket *native, const char *path, int needListen);

int NSocket_acceptInetSocketAddress(struct NSocket *native, struct NInetSocketNetworkAddress *addr);

int NSocket_receiveFrom(struct NSocket *native, void *dest, int max, struct NInetSocketNetworkAddress *addr);

#endif // NATIVE_NSOCKET_H
