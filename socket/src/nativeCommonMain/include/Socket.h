//
// Created by subochev on 04.06.23.
//

#ifndef NATIVE_SOCKET_H
#define NATIVE_SOCKET_H

#include "NativeNetworkAddress.h"

//#ifdef WINDOWS_TARGET
//#include <winsock2.h>
//#include <psdk_inc/_socket_types.h>
//#endif

#define BIND_RESULT_OK 1
#define BIND_RESULT_ALREADY_BINDED 2
#define BIND_RESULT_ADDRESS_ALREADY_IN_USE 3
#define BIND_RESULT_NOT_SUPPORTED 4
#define BIND_RESULT_UNKNOWN_ERROR 5

int Socket_unbind(int socket);
int Socket_bindUnix(int socket, const char *path);
int Socket_accept(int socket, struct NativeNetworkAddress* addr);
int Socket_receive(int socket, void* dest, int max, struct NativeNetworkAddress* addr);

#endif // NATIVE_SOCKET_H
