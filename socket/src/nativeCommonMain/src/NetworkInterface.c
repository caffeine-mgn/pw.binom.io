#include "../include/NetworkInterface.h"
#include "../include/NativeNetworkAddress.h"
#include "../include/definition.h"
#ifdef LINUX_LIKE_TARGET
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

char* copyString(char* str) {
    if (str == NULL) {
        return NULL;
    }
    int len = strlen(str);
    char* newStr = (char *) malloc(len + 1);
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

void internal_freeNetworkInterfaces(struct InternalNetworkInterface* interfaces) {
    for (struct InternalNetworkInterface* addr = interfaces; addr != NULL; addr = addr->next) {
        if (addr->name != NULL) {
            free(addr->name);
        }
        if (addr->address != NULL) {
            NativeNetworkAddress_free(addr->address);
        }
        // if (addr->mask != NULL) {
        //     NativeNetworkAddress_free(addr->mask);
        // }
        free(addr);
    }
}

struct NativeNetworkAddress* internal_sockaddrToNativeNetworkAddress(struct sockaddr* sockAddr) {
    if (sockAddr == NULL) {
        return NULL;
    }
    struct NativeNetworkAddress* addr = mallocNativeNetworkAddress();
    if (sockAddr->sa_family == AF_INET6) {
        memcpy(addr->data, sockAddr, sizeof(struct sockaddr_in6));
        addr->size = sizeof(struct sockaddr_in6);
    } else {
        memcpy(addr->data, sockAddr, sizeof(struct sockaddr_in));
        addr->size = sizeof(struct sockaddr_in);
    }

    return addr;
}

#ifdef LINUX_LIKE_TARGET
int addressToNetworkPrefixLength(struct sockaddr* sockAddr) {
    if (sockAddr->sa_family == AF_INET) {
        struct sockaddr_in *ipv4 = (struct sockaddr_in *)sockAddr;
        uint32_t mask = ntohl(ipv4->sin_addr.s_addr);

        int prefixLength = 0;
        while (mask != 0) {
            mask = mask << 1;
            prefixLength++;
        }

        return prefixLength;
    }
    if (sockAddr->sa_family == AF_INET6) {
        struct sockaddr_in6 *ipv6 = (struct sockaddr_in6 *)sockAddr;
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

struct InternalNetworkInterface* internal_getNetworkInterfaces() {
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
#ifdef WINDOWS_TARGET
    // Получение информации о сетевых интерфейсах
    ULONG bufferSize = sizeof(IP_ADAPTER_ADDRESSES);
    PIP_ADAPTER_ADDRESSES adapterAddresses = (PIP_ADAPTER_ADDRESSES)malloc(bufferSize);

    if (GetAdaptersAddresses(AF_UNSPEC, GAA_FLAG_INCLUDE_PREFIX, NULL, adapterAddresses, &bufferSize) == ERROR_BUFFER_OVERFLOW) {
        free(adapterAddresses);
        adapterAddresses = (PIP_ADAPTER_ADDRESSES)malloc(bufferSize);
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


    struct InternalNetworkInterface* first = NULL;
    struct InternalNetworkInterface* interfaceInstance = NULL;

    // Вывод информации о сетевых интерфейсах
    PIP_ADAPTER_ADDRESSES adapter = adapterAddresses;
    while (adapter != NULL) {
        // printf("Имя интерфейса: %s\n", adapter->AdapterName);
        // printf("Описание: %s\n", adapter->Description);

        PIP_ADAPTER_UNICAST_ADDRESS unicastAddress = adapter->FirstUnicastAddress;
        while (unicastAddress != NULL) {
            if (unicastAddress->Address.lpSockaddr->sa_family != AF_INET && unicastAddress->Address.lpSockaddr->sa_family != AF_INET6) {
                continue;
            }
            struct InternalNetworkInterface* newInterface = malloc(sizeof(struct InternalNetworkInterface));
            if (first == NULL) {
                first = newInterface;
            }
            if (interfaceInstance != NULL) {
                interfaceInstance->next = newInterface;
            }

            interfaceInstance->name = copyString(adapter->AdapterName);
            interfaceInstance->address = internal_sockaddrToNativeNetworkAddress(unicastAddress->Address.lpSockaddr);
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
#endif
#ifdef LINUX_LIKE_TARGET
    struct ifaddrs* addrs = NULL;
    if (getifaddrs(&addrs) != 0) {
        return NULL;
    }
    struct InternalNetworkInterface* first = NULL;
    struct InternalNetworkInterface* interface = NULL;
    for (struct ifaddrs* addr = addrs; addr != NULL; addr = addr->ifa_next) {
        if (addr->ifa_addr->sa_family != AF_INET && addr->ifa_addr->sa_family != AF_INET6) {
            continue;
        }
        struct InternalNetworkInterface* newInterface = malloc(sizeof(struct InternalNetworkInterface));
        if (first == NULL) {
            first = newInterface;
        }
        if (interface != NULL) {
            interface->next = newInterface;
        }
        interface = newInterface;
        interface->name = copyString(addr->ifa_name);
        interface->address = internal_sockaddrToNativeNetworkAddress(addr->ifa_addr);
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
#endif
}