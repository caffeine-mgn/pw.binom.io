#include "../include/definition.h"
#include "../include/NNetworkInterface.h"
#include "../include/Network.h"

#if defined(LINUX_LIKE_TARGET) || defined(__APPLE__)

#include <ifaddrs.h>
#include <netdb.h>
#include <netinet/in.h>

#else

#include <winsock2.h>
#include <ws2tcpip.h>
#include <iphlpapi.h>
// TODO вписать сюда нужные include'ы
#endif

#include <stdio.h>
#include <stdlib.h>
#include <string.h>

char *copyString(char *str) {
    if (str == NULL) {
        return NULL;
    }
    int len = strlen(str);
    char *newStr = (char *) malloc(len + 1);
    newStr[len] = '\0';
    strcpy(newStr, str);
    return newStr;
}

// struct sockaddr* copyAddress(struct sockaddr* from) {
//     if (from == NULL) {
//         return NULL;
//     }
//     struct sockaddr* ret = malloc(sizeof(struct sockaddr));
//     return memcpy(ret, from, sizeof(struct sockaddr));
// }

void internal_freeNetworkInterfaces(struct NNetworkInterface *interfaces) {
    for (struct NNetworkInterface *addr = interfaces; addr != NULL; addr = addr->next) {
        if (addr->name != NULL) {
            free(addr->name);
        }
        // if (addr->mask != NULL) {
        //     NInetSocketNetworkAddress_free(addr->mask);
        // }
        free(addr);
    }
}

int internal_sockaddrToNativeNetworkAddress(struct sockaddr *sockAddr, struct NNetworkAddress *dest) {
    if (sockAddr == NULL || dest == NULL) {
        return 0;
    }
    switch (sockAddr->sa_family) {
        case AF_INET6: {
            struct sockaddr_in6 *addr = (struct sockaddr_in6 *) sockAddr;
            memcpy(&dest->data, &addr->sin6_addr, 16);
            dest->protocolFamily = NET_TYPE_INET6;
            return 1;
        }
        case AF_INET: {
            struct sockaddr_in *addr_in = (struct sockaddr_in *) sockAddr;
            memcpy(&dest->data, &addr_in->sin_addr, 4);
            dest->protocolFamily = NET_TYPE_INET4;
            return 1;
        }
        default:
            return 0;
    }
}

#ifdef LINUX_LIKE_TARGET

int addressToNetworkPrefixLength(struct sockaddr *sockAddr) {
    if (sockAddr->sa_family == AF_INET) {
        struct sockaddr_in *ipv4 = (struct sockaddr_in *) sockAddr;
        uint32_t mask = ntohl(ipv4->sin_addr.s_addr);

        int prefixLength = 0;
        while (mask != 0) {
            mask = mask << 1;
            prefixLength++;
        }

        return prefixLength;
    }
    if (sockAddr->sa_family == AF_INET6) {
        struct sockaddr_in6 *ipv6 = (struct sockaddr_in6 *) sockAddr;
        uint8_t *mask = ipv6->sin6_addr.s6_addr;

        int prefixLength = 0;
        for (int i = 0; i < 16; i++) {
            uint8_t octet = mask[i];
            for (int j = 7; j >= 0; j--) {
                if ((octet >> j) & 1) {
                    prefixLength++;
                } else {
                    return prefixLength;
                }
            }
        }

        return prefixLength;
    }
    /*
    int len = 0;
    int prefixLength = 0;
    if (sockAddr->sa_family == AF_INET6) {
        len = 16;
    }
    if (sockAddr->sa_family == AF_INET) {
        len = 4;
    }
    printf("len=%d\n", len);
    uint8_t* mask = (uint8_t *) sockAddr->sa_data;
    for (int i = 0; i < len; i++) {
        uint8_t octet = mask[i];
        for (int j = 7; j >= 0; j--) {
            if ((octet >> j) & 1) {
                prefixLength++;
            } else {
                return prefixLength;
            }
        }
    }
    */
    return 0;
}

#endif

struct NNetworkInterface *internal_getNetworkInterfaces() {
    /*
        struct addrinfo hints;
        struct addrinfo* addresses;

        memset(&hints, 0, sizeof(hints));
        hints.ai_family = AF_UNSPEC;
        hints.ai_socktype = SOCK_STREAM;
        hints.ai_protocol = IPPROTO_TCP;

        int result = getaddrinfo(NULL, NULL, &hints, &addresses);
        if (result != 0) {
            printf("Ошибка при получении информации о сетевых интерфейсах: %d\n", result);
            return NULL;
        }

        struct addrinfo* addr = addresses;
        while (addr!=NULL) {

            addr=addr->ai_next;
        }
        freeaddrinfo(addresses);
    */
    // ----------------------
#if defined(WINDOWS_TARGET)
    // Получение информации о сетевых интерфейсах
    ULONG bufferSize = sizeof(IP_ADAPTER_ADDRESSES);
    PIP_ADAPTER_ADDRESSES adapterAddresses = (PIP_ADAPTER_ADDRESSES) malloc(bufferSize);

    if (GetAdaptersAddresses(AF_UNSPEC, GAA_FLAG_INCLUDE_PREFIX, NULL, adapterAddresses, &bufferSize) ==
        ERROR_BUFFER_OVERFLOW) {
        free(adapterAddresses);
        adapterAddresses = (PIP_ADAPTER_ADDRESSES) malloc(bufferSize);
        if (adapterAddresses == NULL) {
            // printf("Ошибка выделения памяти\n");
            return NULL;
        }
    }

    if (GetAdaptersAddresses(AF_UNSPEC, GAA_FLAG_INCLUDE_PREFIX, NULL, adapterAddresses, &bufferSize) != NO_ERROR) {
        // printf("Ошибка получения информации о сетевых интерфейсах\n");
        free(adapterAddresses);
        return NULL;
    }


    struct NNetworkInterface *first = NULL;
    struct NNetworkInterface *interfaceInstance = NULL;

    // Вывод информации о сетевых интерфейсах
    PIP_ADAPTER_ADDRESSES adapter = adapterAddresses;
    while (adapter != NULL) {
        // printf("Имя интерфейса: %s\n", adapter->AdapterName);
        // printf("Описание: %s\n", adapter->Description);

        PIP_ADAPTER_UNICAST_ADDRESS unicastAddress = adapter->FirstUnicastAddress;
        while (unicastAddress != NULL) {
            if (unicastAddress->Address.lpSockaddr->sa_family != AF_INET &&
                unicastAddress->Address.lpSockaddr->sa_family != AF_INET6) {
                continue;
            }
            struct NNetworkInterface *newInterface = malloc(sizeof(struct NNetworkInterface));
            if (first == NULL) {
                first = newInterface;
            }
            if (interfaceInstance != NULL) {
                interfaceInstance->next = newInterface;
            }

            interfaceInstance->name = copyString(adapter->AdapterName);
            internal_sockaddrToNativeNetworkAddress(unicastAddress->Address.lpSockaddr, &interfaceInstance->address);
            // interfaceInstance->mask = NULL;
            interfaceInstance->prefixLength = unicastAddress->OnLinkPrefixLength;
            interfaceInstance->prefixLength = interfaceInstance->prefixLength > 0 ? interfaceInstance->prefixLength : 0;
            /*
            char ipAddress[INET6_ADDRSTRLEN];
            memset(ipAddress, 0, sizeof(ipAddress));
            if (unicastAddress->Address.lpSockaddr->sa_family == AF_INET) {
                struct sockaddr_in* sockaddrIPv4 = (struct sockaddr_in*)unicastAddress->Address.lpSockaddr;
                inet_ntop(AF_INET, &(sockaddrIPv4->sin_addr), ipAddress, INET_ADDRSTRLEN);
            }
            else if (unicastAddress->Address.lpSockaddr->sa_family == AF_INET6) {
                struct sockaddr_in6* sockaddrIPv6 = (struct sockaddr_in6*)unicastAddress->Address.lpSockaddr;
                inet_ntop(AF_INET6, &(sockaddrIPv6->sin6_addr), ipAddress, INET6_ADDRSTRLEN);
            }

            printf("IP-адрес: %s\n", ipAddress);

            // Получение маски сетевого интерфейса адреса
            if (unicastAddress->OnLinkPrefixLength > 0) {
                printf("Маска: %d\n", unicastAddress->OnLinkPrefixLength);
            }
*/
            unicastAddress = unicastAddress->Next;
        }

        printf("\n");
        adapter = adapter->Next;
    }

    // Освобождение ресурсов
    free(adapterAddresses);
#elif defined(LINUX_LIKE_TARGET) || defined(__APPLE__)
    struct ifaddrs *addrs = NULL;
    if (getifaddrs(&addrs) != 0) {
        return NULL;
    }
    struct NNetworkInterface *first = NULL;
    struct NNetworkInterface *interface = NULL;
    int index = 0;
    for (struct ifaddrs *addr = addrs; addr != NULL; addr = addr->ifa_next) {
        if (addr->ifa_addr == NULL) {
            continue;
        }
        if (addr->ifa_addr->sa_family != AF_INET && addr->ifa_addr->sa_family != AF_INET6) {
            continue;
        }
        struct NNetworkInterface *newInterface = malloc(sizeof(struct NNetworkInterface));
        if (first == NULL) {
            first = newInterface;
        }
        if (interface != NULL) {
            interface->next = newInterface;
        }
        interface = newInterface;
        interface->name = copyString(addr->ifa_name);
        interface->index = index++;
        internal_sockaddrToNativeNetworkAddress(addr->ifa_addr, &interface->address);
        // interface->mask = NULL;//internal_sockaddrToNativeNetworkAddress(addr->ifa_netmask);
        if (addr->ifa_netmask != NULL) {
            interface->prefixLength = addressToNetworkPrefixLength(addr->ifa_netmask);
        } else {
            interface->prefixLength = 0;
        }
    }
    if (interface != NULL) {
        interface->next = NULL;
    }
    freeifaddrs(addrs);

    return first;
#else
#error Not supported
#endif
}
