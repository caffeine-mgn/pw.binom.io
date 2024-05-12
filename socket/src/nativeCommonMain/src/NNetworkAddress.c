#include "../include/NNetworkAddress.h"
#include "../include/definition.h"
#include "../include/Network.h"

#include <malloc.h>
#include <string.h>

#ifdef LINUX_LIKE_TARGET

#include <arpa/inet.h>
#include <netdb.h>
#include <sys/un.h>

#endif
#ifdef WINDOWS_TARGET
#include <winsock2.h>
#include <ws2def.h>
#include <ws2tcpip.h>
#endif

struct NNetworkAddress *NNetworkAddress_malloc() {
    struct NNetworkAddress *result = (struct NNetworkAddress *) malloc(
            sizeof(struct NNetworkAddress));
    memset(result, 0, sizeof(struct NNetworkAddress));
    return result;
}

int NNetworkAddress_copy(struct NNetworkAddress *from, struct NNetworkAddress *to) {
    if (from == NULL) {
        return 0;
    }
    if (to == NULL) {
        return 0;
    }
    memcpy(to, from, sizeof(struct NNetworkAddress));
    return 1;
}

void NNetworkAddress_free(struct NNetworkAddress *ptr) {
    free(ptr);
}

int NNetworkAddress_convertToIpv6(struct NNetworkAddress *ptr) {
    if (ptr->protocolFamily == NET_TYPE_INET4) {
        int isMulticast = Network_isMulticastAddressV4((struct in_addr *) ptr->data);
        Network_convertIpv4ToIpv6(ptr->data, ptr->data, isMulticast);
        ptr->protocolFamily = NET_TYPE_INET6;
        return 1;
    }
    return 1;
}

int NNetworkAddress_isMulticast(struct NNetworkAddress *ptr) {
    switch (ptr->protocolFamily) {
        case NET_TYPE_INET4:
            return Network_isMulticastAddressV4((struct in_addr *) ptr->data);
        case NET_TYPE_INET6:
            return IN6_IS_ADDR_MULTICAST((struct in_addr *) ptr->data);
        default:
            return 0;
    }
}

int convertAddressTypeToNative(int protocolFamily) {
    switch (protocolFamily) {
        case NET_TYPE_INET4:
            return AF_INET;
        case NET_TYPE_INET6:
            return AF_INET6;
        case NET_TYPE_UNIX:
            return AF_UNIX;
        default:
        case NET_TYPE_UNKNOWN:
            return 0;

    }
}

int NNetworkAddress_getAddressBytes(struct NNetworkAddress *ptr, signed char *buffer) {
    switch (ptr->protocolFamily) {
        case NET_TYPE_INET4: {
            int result = ((int *) ptr->data)[0];
            memcpy(buffer, &result, 4);
            return 1;
        }
        case NET_TYPE_INET6:
            memcpy(buffer, ptr->data, 16);
            return 1;
        default:
            return 0;
    }
}

int NNetworkAddress_get_host(struct NNetworkAddress *ptr, char *buffer, int bufferLen) {
    if (inet_ntop(convertAddressTypeToNative(ptr->protocolFamily), ptr->data, buffer, bufferLen) == NULL) {
        return 0;
    } else {
        return 1;
    }
}

int NNetworkAddress_set_host(struct NNetworkAddress *ptr, const char *host) {
    struct addrinfo hints, *res, *result;

    memset(&hints, 0, sizeof(hints));
    hints.ai_family = PF_UNSPEC;
    hints.ai_socktype = SOCK_STREAM;
    hints.ai_flags |= AI_CANONNAME;
    int errcode = getaddrinfo(host, NULL, &hints, &result);
    if (errcode != 0) {
        return 0;
    }

    res = result;
    while (res) {
        switch (res->ai_family) {
            case AF_INET:
                memcpy(ptr->data, &((struct sockaddr_in *) res->ai_addr)->sin_addr, 4/*sizeof(struct in_addr)*/);
                ptr->protocolFamily = NET_TYPE_INET4;
                break;
            case AF_INET6: {
                memcpy(ptr->data, &((struct sockaddr_in6 *) res->ai_addr)->sin6_addr, 16/*sizeof(struct in_addr)*/);
                ptr->protocolFamily = NET_TYPE_INET6;
                break;
            }
            default:
                memset(ptr->data, 0, 16);
                ptr->protocolFamily = NET_TYPE_UNKNOWN;
        }
        res = res->ai_next;
    }

    freeaddrinfo(result);
    return 1;
}

struct NNetworkAddressList *NNetworkAddressList_getAll(const char *host, int *size) {
    struct NNetworkAddressList *first = NULL;
    struct NNetworkAddressList *last = NULL;
    struct addrinfo hints, *res, *result;

    memset(&hints, 0, sizeof(hints));
    hints.ai_family = PF_UNSPEC;
    hints.ai_socktype = SOCK_STREAM;
    hints.ai_flags |= AI_CANONNAME;
    int errcode = getaddrinfo(host, NULL, &hints, &result);
    if (errcode != 0) {
        return 0;
    }
    int length = 0;
    res = result;
    while (res) {
        length++;
        struct NNetworkAddressList *current = (struct NNetworkAddressList *) malloc(sizeof(struct NNetworkAddressList));
        if (last != NULL) {
            last->next = current;
        }
        if (first == NULL) {
            first = current;
        }
        last = current;
        switch (res->ai_family) {
            case AF_INET:
                memcpy(current->address.data, &((struct sockaddr_in *) res->ai_addr)->sin_addr,
                       4/*sizeof(struct in_addr)*/);
                current->address.protocolFamily = NET_TYPE_INET4;
                break;
            case AF_INET6: {
                memcpy(current->address.data, &((struct sockaddr_in6 *) res->ai_addr)->sin6_addr,
                       16/*sizeof(struct in_addr)*/);
                current->address.protocolFamily = NET_TYPE_INET6;
                break;
            }
            default:
                memset(current->address.data, 0, 16);
                current->address.protocolFamily = NET_TYPE_UNKNOWN;
        }
        res = res->ai_next;
    }
    if (last != NULL) {
        last->next = NULL;
    }
    if (size != NULL) {
        *size = length;
    }
    freeaddrinfo(result);
    return first;
}

void NNetworkAddressList_free(struct NNetworkAddressList *list) {
    struct NNetworkAddressList *last = list;
    while (last) {
        struct NNetworkAddressList *current = last;
        last = last->next;
        free(current);
    }
}