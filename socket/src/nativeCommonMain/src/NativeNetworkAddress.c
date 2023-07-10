//
// Created by subochev on 04.06.23.
//
#include "../include/NativeNetworkAddress.h"
#include "../include/definition.h"

#include <malloc.h>

#ifdef LINUX_LIKE_TARGET
#include <arpa/inet.h>
#include <fcntl.h>
#include <netdb.h>
#include <netinet/tcp.h>
#else

#include <winsock.h>
#include <ws2def.h>
#include <ws2tcpip.h>
#include <wspiapi.h>

typedef int socklen_t;
#endif
#include <string.h>
struct NativeNetworkAddress *mallocNativeNetworkAddress() {
  auto result = (struct NativeNetworkAddress *)malloc(
      sizeof(struct NativeNetworkAddress));
  memset(result, 0, sizeof(struct NativeNetworkAddress));
  return result;
}

void NativeNetworkAddress_free(struct NativeNetworkAddress *ptr) { free(ptr); }

int NativeNetworkAddress_getFamily(struct NativeNetworkAddress *ptr) {
  struct sockaddr_in *addr = (struct sockaddr_in *)ptr->data;
  if (addr->sin_family == AF_INET6) {
    return NET_TYPE_INET6;
  }
  if (addr->sin_family == AF_INET) {
    return NET_TYPE_INET4;
  }
#ifndef _WIN32
  if (addr->sin_family == AF_UNIX) {
    return NET_TYPE_UNIX;
  }
#endif
  return NET_TYPE_UNKNOWN;
}