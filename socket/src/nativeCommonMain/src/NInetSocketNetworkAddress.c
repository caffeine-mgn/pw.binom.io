#include "../include/NInetSocketNetworkAddress.h"
#include "../include/definition.h"
#include "../include/Network.h"

#include <malloc.h>

#if defined(LINUX_LIKE_TARGET) || defined(__APPLE__)

#include <arpa/inet.h>
#include <fcntl.h>
#include <netdb.h>
#include <netinet/tcp.h>

#elif defined(WINDOWS_TARGET)
#include <winsock.h>
#include <ws2def.h>
#include <ws2tcpip.h>
#include <wspiapi.h>

typedef int socklen_t;
#else
#error NOT SUPPORTED
#endif

#include <string.h>

struct NInetSocketNetworkAddress *NInetSocketNetworkAddress_malloc() {
    struct NInetSocketNetworkAddress *result = (struct NInetSocketNetworkAddress *) malloc(
            sizeof(struct NInetSocketNetworkAddress));
    memset(result, 0, sizeof(struct NInetSocketNetworkAddress));
    return result;
}

void NInetSocketNetworkAddress_copy(struct NInetSocketNetworkAddress *from,
                                    struct NInetSocketNetworkAddress *to) {
    memcpy(to, from, sizeof(struct NInetSocketNetworkAddress));
};

void NInetSocketNetworkAddress_free(struct NInetSocketNetworkAddress *ptr) {
    free(ptr);
}

int NInetSocketNetworkAddress_isMulticast(struct NInetSocketNetworkAddress *ptr) {
    struct sockaddr_in *addr = (struct sockaddr_in *) ptr->data;
    switch (addr->sin_family) {
        case AF_INET:
            return Network_isMulticastAddressV4((struct in_addr *) &addr->sin_addr);
        case AF_INET6:
            return IN6_IS_ADDR_MULTICAST((struct in_addr *) &addr->sin_addr);
        default:
            return 0;
    }
}

int NInetSocketNetworkAddress_getFamily(struct NInetSocketNetworkAddress *ptr) {
    if (ptr == NULL) {
        return NET_TYPE_UNKNOWN;
    }
    struct sockaddr_in *addr = (struct sockaddr_in *) ptr->data;
    if (addr->sin_family == AF_INET6) {
        return NET_TYPE_INET6;
    }
    if (addr->sin_family == AF_INET) {
        return NET_TYPE_INET4;
    }
#ifndef WINDOWS_TARGET
    if (addr->sin_family == AF_UNIX) {
        return NET_TYPE_UNIX;
    }
#endif
    return NET_TYPE_UNKNOWN;
}

int NInetSocketNetworkAddress_convertToIpv6(struct NInetSocketNetworkAddress *ptr) {
    if (ptr == NULL) {
        return 0;
    }
    struct sockaddr_in *addr = (struct sockaddr_in *) ptr->data;
    if (addr->sin_family == AF_INET) {
        struct sockaddr_in6 *ipv6 = (struct sockaddr_in6 *) ptr->data;
        int multicast = Network_isMulticastAddressV4(&addr->sin_addr);
        Network_convertIpv4ToIpv6(&addr->sin_addr, &ipv6->sin6_addr, multicast);
        addr->sin_family = AF_INET6;
        ptr->size = sizeof(struct sockaddr_in6);
    }
    return 1;
}

int NInetSocketNetworkAddress_getPort(struct NInetSocketNetworkAddress *ptr) {
    if (ptr == NULL) {
        return 0;
    }
    struct sockaddr_in *addr = (struct sockaddr_in *) ptr->data;
    return ntohs(addr->sin_port);
}

int NInetSocketNetworkAddress_setPort(struct NInetSocketNetworkAddress *ptr, int port) {
    if (ptr == NULL) {
        return 0;
    }
    struct sockaddr_in *addr = (struct sockaddr_in *) ptr->data;
    switch (addr->sin_family) {
        case AF_INET: {
            addr->sin_port = htons(port);
            return 1;
        }
        case AF_INET6: {
            struct sockaddr_in6 *ipv6 = (struct sockaddr_in6 *) ptr->data;
            ipv6->sin6_port = htons(port);
            return 1;
        }
        default:
            return 0;
    }
}

int NInetSocketNetworkAddress_getAddressBytes(struct NInetSocketNetworkAddress *ptr, signed char *buffer) {
    struct sockaddr_in *addr = (struct sockaddr_in *) ptr->data;
    switch (addr->sin_family) {
        case AF_INET: {
            int result = ((int *) &addr->sin_addr)[0];
            memcpy(buffer, &result, 4);
            return 1;
        }
        case AF_INET6: {
            struct sockaddr_in6 *ipv6 = (struct sockaddr_in6 *) ptr->data;
            memcpy(buffer, &ipv6->sin6_addr, 16);
            return 1;
        }
        default:
            return 0;
    }
}

int NInetSocketNetworkAddress_getHostString(struct NInetSocketNetworkAddress *ptr, char *buffer, int buffLen) {
    if (ptr == NULL) {
        return 0;
    }
    if (buffer == NULL) {
        return 0;
    }
    struct sockaddr_in *addr = (struct sockaddr_in *) ptr->data;
    int family = addr->sin_family;
    switch (family) {
        case AF_INET:
            inet_ntop(
                    AF_INET,
                    &addr->sin_addr,
                    buffer,
                    buffLen
            );
            return 1;
        case AF_INET6: {
            inet_ntop(
                    AF_INET6,
                    &(((struct sockaddr_in6 *) ptr->data)->sin6_addr),
                    buffer,
                    buffLen
            );
            return 1;
        }
        default:
            return 0;
    }
}

int NInetSocketNetworkAddress_getHost(struct NInetSocketNetworkAddress *ptr, struct NNetworkAddress *dest) {
    struct sockaddr_in *addr = (struct sockaddr_in *) ptr->data;
    printf("NInetSocketNetworkAddress_getHost  sin_family=%d   size=%d\n", (int) addr->sin_family, (int) ptr->size);
    switch (addr->sin_family) {
        case AF_INET: {
            memcpy(dest->data, &addr->sin_addr, 4);
            dest->protocolFamily = NET_TYPE_INET4;
            return 1;
        }
        case AF_INET6: {
            struct sockaddr_in6 *ipv6 = (struct sockaddr_in6 *) ptr->data;
            memcpy(dest->data, &ipv6->sin6_addr, 16);
            dest->protocolFamily = NET_TYPE_INET6;
            return 1;
        }
        default:
            return 0;
    }

}

int NInetSocketNetworkAddress_setHost(struct NInetSocketNetworkAddress *ptr, struct NNetworkAddress *address) {
    struct sockaddr_in *addr = (struct sockaddr_in *) ptr->data;
    switch (address->protocolFamily) {
        case NET_TYPE_INET4: {
            memcpy(&addr->sin_addr, (struct in_addr *) address->data, 4);
            addr->sin_family = AF_INET;
            ptr->size = sizeof(struct sockaddr_in);
            return 1;
        }
        case NET_TYPE_INET6: {
            struct sockaddr_in6 *ipv6 = (struct sockaddr_in6 *) ptr->data;
            memcpy(&ipv6->sin6_addr, (struct in_addr6 *) address->data, 16);
            ipv6->sin6_family = AF_INET6;
            ptr->size = sizeof(struct sockaddr_in6);
            return 1;
        }
        default:
            return 0;
    }
}

