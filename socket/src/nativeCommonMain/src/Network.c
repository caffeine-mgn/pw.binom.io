#include <string.h>
#include "../include/definition.h"
#include "../include/Network.h"

int Network_isMulticastAddressV4(struct in_addr *addr) {
    int address = (int) ntohl(addr->s_addr);
    return (address & 0xF0000000) == 0xE0000000;
}

int Network_isIpV4(struct in6_addr *addr) {
    signed char *addrBytes = (signed char *) addr;
    for (int i = 0; i < 9; i++) {
        if (addrBytes[i] != 0) {
            return 0;
        }
    }
    return 1;
}

#ifdef WINDOWS_TARGET
static int network_inited = 0;
#endif

int Network_init() {
#ifdef WINDOWS_TARGET
    if (network_inited==0){
        WSADATA wsa_data;
        auto r = WSAStartup(MAKEWORD(2, 2), &wsa_data);
        if (r!=0){
            return 0;
        }
    }
#else
    return 1;
#endif
}

int Network_ProtocolFamilyHostToInternal(int value) {
    switch (value) {
        case AF_INET:
            return NET_TYPE_INET4;
        case AF_INET6:
            return NET_TYPE_INET6;
        case AF_UNIX:
            return NET_TYPE_UNIX;
        default:
            return 0;
    }
}

int Network_ProtocolFamilyInternalToHost(int value) {
    switch (value) {
        case NET_TYPE_INET4:
            return AF_INET;
        case NET_TYPE_INET6:
            return AF_INET6;
        case NET_TYPE_UNIX:
            return AF_UNIX;
        default:
            return 0;
    }
}

void Network_convertIpv4ToIpv6(void *src, void *dest, int multicast) {
    memcpy(&dest[16 - 4], src, 4);
    memset(dest, 0, 10);
    memset(&dest[10], multicast ? 0 : 0xFF, 2);
//    memset(&dest[10], 0xFF, 2);
}