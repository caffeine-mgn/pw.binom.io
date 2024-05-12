
#ifndef NATIVE_NETWORK_H
#define NATIVE_NETWORK_H

#include "definition.h"

#ifdef LINUX_LIKE_TARGET

#include <netinet/in.h>

#endif
#ifdef WINDOWS_TARGET
#include <winsock2.h>
#include <ws2def.h>
#include <ws2tcpip.h>
#endif

#define NET_TYPE_INET4 1
#define NET_TYPE_INET6 2
#define NET_TYPE_UNIX 3
#define NET_TYPE_UNKNOWN 0

int Network_isMulticastAddressV4(struct in_addr *addr);
int Network_isIpV4(struct in6_addr *addr);
int Network_init();

int Network_ProtocolFamilyHostToInternal(int value);
int Network_ProtocolFamilyInternalToHost(int value);

void Network_convertIpv4ToIpv6(void *src, void *dest, int multicast);

#endif //NATIVE_NETWORK_H
