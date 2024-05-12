//
// Created by subochev on 04.06.23.
//
#include "../include/NSocket.h"
#include "../include/Network.h"

#ifdef WINDOWS_TARGET

#include <winsock2.h>
#include <netioapi.h>

#elif defined(LINUX_LIKE_TARGET)
#include <sys/socket.h>
#include <sys/un.h>
#include <netinet/in.h>
#include <netinet/tcp.h>
#include <netdb.h>
#include <net/if.h>
#else
#error Not supported
#endif

#include <unistd.h>
#include <errno.h>
#include <stdio.h>
#include <fcntl.h>


#if defined(LINUX_LIKE_TARGET)
#define IS_SOCKET_ERROR(value) value<0
#elif defined(WINDOWS_TARGET)
#define IS_SOCKET_ERROR(value) value==SOCKET_ERROR
#else
#error Not supported
#endif

int internal_NSocket_errorProcessing(struct NSocket *native, size_t wasRead) {
#if defined(LINUX_LIKE_TARGET)
    switch (errno) {
            case EAGAIN:
                return 0;
            case EBADF:
            case ECONNRESET:
            case EPIPE:
            case EINVAL:
            default: {
                NSocket_close(native);
                return -1;
            }
        }
#elif defined(WINDOWS_TARGET)
    switch (GetLastError()) {
        case WSAEWOULDBLOCK:
            return 0;
        case WSAECONNRESET:
            return -1;
        default: {
            NSocket_close(native);
            return -1;
        }
    }
#else
#error not supported
#endif
}

int Socket_typeHostToInternal(int value) {
    switch (value) {
        case SOCK_STREAM:
            return SOCKET_TYPE_TCP;
        case SOCK_DGRAM:
            return SOCKET_TYPE_UDP;
        default:
            return 0;
    }
}

int Socket_typeInternalToHost(int value) {
    switch (value) {
        case SOCKET_TYPE_TCP:
            return SOCK_STREAM;
        case SOCKET_TYPE_UDP:
            return SOCK_DGRAM;
        default:
            return 0;
    }
}

int NSocket_getSocketPort(struct NSocket *native) {
    struct sockaddr_in6 sin;
    socklen_t addrlen = sizeof(sin);
    int r = getsockname(native->native, (struct sockaddr *) &sin, &addrlen);
    if (r == -1) {
        return -1;
    } else {
        return ntohs(sin.sin6_port);
    }
}

int NSocket_create(struct NSocket *dest, int protocolFamily, int socketType) {
    if (dest == NULL) {
        return 0;
    }
#if defined(WINDOWS_TARGET)
    Network_init();
#endif
    int domain = Network_ProtocolFamilyInternalToHost(protocolFamily);
    if (domain <= 0) {
        return 0;
    }
    int type = Socket_typeInternalToHost(socketType);
    if (type <= 0) {
        return 0;
    }
    int native = socket(domain, type, 0);
    if (native < 0) {
        return 0;
    }

    dest->protocolFamily = protocolFamily;
    dest->native = native;
    dest->type = type;

    if (protocolFamily == NET_TYPE_INET6) {
        int flag = 0;
        int iResult = setsockopt(
                native,
                IPPROTO_IPV6,
                IPV6_V6ONLY,
                &flag,
                sizeof(int)
        );
        if (iResult == -1) {
            return 0;
        }
    }

    return 1;
}

int NSocket_close(struct NSocket *dest) {
    if (dest->closed) {
        return 1;
    }
    NSocket_unbind(dest->native);
#ifdef WINDOWS_TARGET
    shutdown(dest->native, SD_BOTH);
    closesocket(dest->native);
#else
    shutdown(dest->native, SHUT_RDWR);
    close(dest->native);
#endif
    dest->closed = 1;
    return 1;
}

int NSocket_isClose(struct NSocket *dest) {
    if (dest == NULL) {
        return 1;
    }
    return dest->closed;
}

int internal_NSocket_connect(int socket, struct sockaddr *address, int addressLen) {
    if (connect(
            socket,
            address,
            addressLen
    ) != 0) {
#ifdef LINUX_LIKE_TARGET
        switch (errno) {
            case ECONNREFUSED:
                return ConnectStatus_CONNECTION_REFUSED;
            case EISCONN:
                return ConnectStatus_ALREADY_CONNECTED;
            case EINPROGRESS:
                return ConnectStatus_IN_PROGRESS;
            default:
                printf("Unknown error %d\n", errno);
                return ConnectStatus_FAIL;
        }
#elif defined(WINDOWS_TARGET)
        switch (GetLastError()) {
            case WSAEWOULDBLOCK:
                return ConnectStatus_IN_PROGRESS;
            case WSAEAFNOSUPPORT:
                return ConnectStatus_FAIL;
            case WSAETIMEDOUT:
                return ConnectStatus_CONNECTION_REFUSED;
            default:
                return ConnectStatus_FAIL;
        }
#else
#error Not supported
#endif
    }
    return ConnectStatus_OK;
}

int NSocket_connectInet(struct NSocket *dest, struct NInetSocketNetworkAddress *address) {
    if (dest == NULL) {
        return 0;
    }
    if (dest->protocolFamily == NET_TYPE_INET6) {
        NInetSocketNetworkAddress_convertToIpv6(address);
    }
    int addressLen;
    switch (dest->protocolFamily) {
        case NET_TYPE_UNIX:
        case NET_TYPE_INET4:
            addressLen = sizeof(struct sockaddr_in);
            break;
        case NET_TYPE_INET6:
            addressLen = sizeof(struct sockaddr_in6);
            break;
        default:
            return 0;
    }
    return internal_NSocket_connect(
            dest->native,
            (struct sockaddr *) address->data,
            addressLen
    );
//    if (connect(
//            dest->native,
//            (struct sockaddr *) address->data,
//            addressLen
//    ) != 0) {
//#ifdef LINUX_LIKE_TARGET
//        switch (errno) {
//            case ECONNREFUSED:
//                return ConnectStatus_CONNECTION_REFUSED;
//            case EISCONN:
//                return ConnectStatus_ALREADY_CONNECTED;
//            case EINPROGRESS:
//                return ConnectStatus_IN_PROGRESS;
//            default:
//                printf("Unknown error %d\n", errno);
//                return ConnectStatus_FAIL;
//        }
//#elif defined(WINDOWS_TARGET)
//#error Not supported
//#else
//#error Not supported
//#endif
//    }
//    return ConnectStatus_OK;
}

int NSocket_connectUnix(struct NSocket *dest, const char *path) {
#ifdef LINUX_LIKE_TARGET
    if (dest == NULL) {
        return 0;
    }
    if (dest->protocolFamily == NET_TYPE_UNIX) {
        return ConnectStatus_FAIL;
    }
    struct sockaddr_un addr;
    memset(&addr, 0, sizeof(struct sockaddr_un));
    addr.sun_family = AF_UNIX;
    strcpy(addr.sun_path, path);
    return internal_NSocket_connect(
            dest->native,
            (struct sockaddr *) &addr,
            sizeof(struct sockaddr_un)
    );
#else
    return ConnectStatus_FAIL;
#endif
}

int NSocket_setReuseaddr(struct NSocket *native, int value) {
    if (setsockopt(native->native, SOL_SOCKET, SO_REUSEADDR, (char *) &value, sizeof(int)) == -1) {
        return 0;
    }
    return 1;
}

int NSocket_setMulticastInterface(struct NSocket *dest, struct NNetworkAddress *networkAddress,
                                  const char *networkInterfaceName) {
    if (dest == NULL) {
        return 0;
    }
    switch (dest->protocolFamily) {
        case NET_TYPE_INET6: {
            unsigned int index = networkInterfaceName == NULL ? 0 : if_nametoindex(networkInterfaceName);
            if (setsockopt(
                    dest->native,
                    IPPROTO_IPV6,
                    IPV6_MULTICAST_IF,
                    &index,
                    sizeof(int)) != 0) {
                return 0;
            }
            return 1;
        }

        case NET_TYPE_INET4: {
            struct in_addr addr;
            if (networkAddress == NULL) {
                addr.s_addr = htonl(INADDR_ANY);
            } else {
                memcpy(&addr, networkAddress->data, sizeof(struct in_addr));
            }
            if (setsockopt(
                    dest->native,
                    IPPROTO_IP,
                    IP_MULTICAST_IF,
                    &addr,
                    sizeof(struct in_addr)) != 0) {
                return 0;
            }
            return 1;
        }

        default:
            return 0;
    }
}

int NSocket_joinGroup(struct NSocket *dest, struct NInetSocketNetworkAddress *address,
                      struct NNetworkAddress *networkAddress, const char *networkInterfaceName) {
    if (dest == NULL || address == NULL) {
        return 0;
    }
    if (dest->protocolFamily == NET_TYPE_INET6 && NInetSocketNetworkAddress_getFamily(address) == NET_TYPE_INET4) {
        NInetSocketNetworkAddress_convertToIpv6(address);
    }
    if (dest->protocolFamily == NET_TYPE_INET4 && NInetSocketNetworkAddress_getFamily(address) == NET_TYPE_INET6) {
        return 0;
    }
    if (!NSocket_setReuseaddr(dest, 1)) {
        return 0;
    }

    struct addrinfo hints = {0};
    struct addrinfo *localAddr = NULL;
    hints.ai_family = ((struct sockaddr_in *) address->data)->sin_family;
    hints.ai_socktype = SOCK_DGRAM;
    int multicastPort = NInetSocketNetworkAddress_getPort(address);
    hints.ai_flags = AI_PASSIVE; // Return an address we can bind to
    char snum[9];
    sprintf(snum, "%d", multicastPort);
    if ((getaddrinfo(NULL, snum, &hints, &localAddr)) != 0) {
        return 0;
    }
    if (bind(dest->native, localAddr->ai_addr, localAddr->ai_addrlen) != 0) {
        freeaddrinfo(localAddr);
        return 0;
    }
    freeaddrinfo(localAddr);
    // https://github.com/bk138/Multicast-Client-Server-Example/blob/master/src/msock.c
    //TODO check windows bind https://habr.com/ru/articles/141021/
    switch (dest->protocolFamily) {
        case NET_TYPE_INET4: {
            struct ip_mreq multicastRequest;  /* Multicast address join structure */

            /* Specify the multicast group */
            memcpy(&multicastRequest.imr_multiaddr,
                   &((struct sockaddr_in *) (address->data))->sin_addr,
                   sizeof(multicastRequest.imr_multiaddr));

            if (networkAddress != NULL) {
                memcpy(&multicastRequest.imr_interface, networkAddress->data, sizeof(struct in_addr));
            } else {
                multicastRequest.imr_interface.s_addr = htonl(INADDR_ANY);
            }
            /* Accept multicast from any interface */

            /* Join the multicast address */
            if (setsockopt(dest->native, IPPROTO_IP, IP_ADD_MEMBERSHIP, (char *) &multicastRequest,
                           sizeof(multicastRequest)) != 0) {
                return 0;
            }
            return 1;
        }
        case NET_TYPE_INET6: {
            struct ipv6_mreq multicastRequest;  /* Multicast address join structure */

            /* Specify the multicast group */
            memcpy(&multicastRequest.ipv6mr_multiaddr,
                   &((struct sockaddr_in6 *) (address->data))->sin6_addr,
                   sizeof(multicastRequest.ipv6mr_multiaddr));
            /* Accept multicast from any interface */

            if (networkInterfaceName != NULL) {
                multicastRequest.ipv6mr_interface = if_nametoindex(networkInterfaceName);
            } else {
                multicastRequest.ipv6mr_interface = 0;
            }

            /* Join the multicast address */
            if (setsockopt(dest->native, IPPROTO_IPV6, IPV6_ADD_MEMBERSHIP, (char *) &multicastRequest,
                           sizeof(multicastRequest)) != 0) {
//                printf("setsockopt() failed Errno:%d\n", (int) errno);
                return 0;
            }
            return 1;
        }
        default:
            return 0;
    }
}

int NSocket_leaveGroup(int socket, struct NNetworkAddress *address, int netIfIndex) {
    if (socket == 0 || address == NULL) {
        return 0;
    }
    NNetworkAddress_convertToIpv6(address);
    struct ipv6_mreq mreq;
    mreq.ipv6mr_multiaddr = *((struct in6_addr *) address->data);
    mreq.ipv6mr_interface = netIfIndex;
    return setsockopt(
            socket,
            IPPROTO_IP,
            IP_DROP_MEMBERSHIP,
            &mreq,
            sizeof(struct ipv6_mreq)
    ) != -1;
}

int NSocket_getTTL(struct NSocket *native, unsigned char *ttl) {
    if (native == NULL) {
        return 0;
    }
    int level;
    int optname;
    switch (native->protocolFamily) {
        case NET_TYPE_INET4:
            level = IPPROTO_IP;
            optname = IP_MULTICAST_TTL;
            break;
        case NET_TYPE_INET6:
            level = IPPROTO_IPV6;
            optname = IPV6_MULTICAST_HOPS;
            break;
        default:
            return 0;
    }
    int size = sizeof(int);
    return getsockopt(native->native, level, optname, ttl, &size) == 0;
}

int NSocket_setTTL(struct NSocket *native, unsigned char ttl) {
    if (native == NULL) {
        return 0;
    }
    int level;
    int optname;
    switch (native->protocolFamily) {
        case NET_TYPE_INET4:
            level = IPPROTO_IP;
            optname = IP_MULTICAST_TTL;
            break;
        case NET_TYPE_INET6:
            level = IPPROTO_IPV6;
            optname = IPV6_MULTICAST_HOPS;
            break;
        default:
            return 0;
    }
    unsigned char multicastTTL = ttl;
    if (setsockopt(native->native,
                   level,
                   optname,
                   (char *) &multicastTTL, sizeof(unsigned char)) != 0) {
        perror("setsockopt() failed");
        return 0;
    }
    return 1;
}

int NSocket_unbind(int socket) {
    int flag = 1;
    return setsockopt(
            socket,
            SOL_SOCKET,
            SO_REUSEADDR,
            (char *) &flag,
            sizeof(int)
    ) != -1;
}

int NSocket_getNoDelay(struct NSocket *native) {
    int value = 0;
    socklen_t len = sizeof(int);
    getsockopt(
            native->native,
            IPPROTO_TCP,
            TCP_NODELAY,
            &value,
            &len
    );
    return value;
}

int NSocket_setNoDelay(struct NSocket *native, int value) {
    int yes = value > 0 ? 1 : 0;
    return setsockopt(native->native,
                      IPPROTO_TCP,
                      TCP_NODELAY,
                      (char *) &yes,
                      sizeof(int)
    ) == 0;
}

int NSocket_send(struct NSocket *native, signed char *buffer, int bufferLen) {
    if (native->closed) {
        return -1;
    }
    if (native->type != SOCKET_TYPE_TCP) {
        return -1;
    }
#if defined(LINUX_LIKE_TARGET)
    int flags=MSG_NOSIGNAL;
#elif defined(WINDOWS_TARGET)
    int flags = 0;
#else
#error Not supported
#endif
    ssize_t sent = send(native->native, buffer, bufferLen, flags);
    if (IS_SOCKET_ERROR(sent)) {
        return internal_NSocket_errorProcessing(native, sent);
    }
    return (int) sent;
}

int NSocket_setBroadcastEnabled(struct NSocket *native, int value) {
    if (native == NULL) {
        return 0;
    }
    int broadcastEnable = value;
    int result = setsockopt(native->native, SOL_SOCKET, SO_BROADCAST, &broadcastEnable, sizeof(broadcastEnable));
    return result == 0;
}

int NSocket_sendTo(
        struct NSocket *native,
        struct NInetSocketNetworkAddress *address,
        signed char *buffer,
        int bufferLen) {
    if (native == NULL) {
        return -1;
    }
    if (address == NULL) {
        return -1;
    }
    if (native->closed) {
        return -1;
    }
    if (native->type != SOCKET_TYPE_UDP) {
        return -1;
    }
    int addressProtocolFamily = NInetSocketNetworkAddress_getFamily(address);
    if (native->protocolFamily == NET_TYPE_INET4 && addressProtocolFamily == NET_TYPE_INET6) {
        return -1;
    }
    if (native->protocolFamily == NET_TYPE_INET6 && addressProtocolFamily == NET_TYPE_INET4) {
        NInetSocketNetworkAddress_convertToIpv6(address);
    }

    size_t sent = sendto(
            native->native,
            buffer,
            bufferLen,
            0,
            (struct sockaddr *) address->data,
            address->size
    );
    if (IS_SOCKET_ERROR(sent)) {
        internal_NSocket_errorProcessing(native, sent);
    }
    return sent;

}

int NSocket_receiveOnly(struct NSocket *native, signed char *buffer, int bufferLen) {
    if (native == NULL) {
        return -1;
    }
    if (native->closed) {
        return -1;
    }
    if (buffer == NULL) {
        return 0;
    }
    ssize_t wasRead = recv(native->native, buffer, bufferLen, 0);
    if (IS_SOCKET_ERROR(wasRead)) {
        return internal_NSocket_errorProcessing(native, wasRead);
    }
    return (int) wasRead;
}

int NSocket_receiveFrom(struct NSocket *native, void *dest, int max, struct NInetSocketNetworkAddress *addr) {
    if (max <= 0) {
        return 0;
    }

#ifdef WINDOWS_TARGET
    char *dest2 = (char *) dest;
#else
    void *dest2 = dest;
#endif
    ssize_t wasRead;
    if (addr == NULL) {
        wasRead = (int) recvfrom(native->native, dest2, max, 0, NULL, NULL);
    } else {
        socklen_t size1 = (socklen_t) addr->size;
        size1 = 28;
        size_t ret = recvfrom(native->native, dest2, max, 0, (struct sockaddr *) &addr->data, &size1);
        addr->size = (int) size1;
        wasRead = (int) ret;
    }
    if (IS_SOCKET_ERROR(wasRead)) {
        return internal_NSocket_errorProcessing(native, wasRead);
    }
    return wasRead;
}

int NSocket_setSoTimeout(struct NSocket *native, unsigned long long timeoutMs) {
    if (native == NULL || native->closed) {
        return 0;
    }
    struct timespec waitUntil;
    waitUntil.tv_sec = timeoutMs / 1000;
    waitUntil.tv_nsec = (timeoutMs % 1000) * 1000000;
    return setsockopt(
            native->native,
            SOL_SOCKET,
            SO_RCVTIMEO,
            &waitUntil,
            sizeof(struct timespec)
    ) >= 0;
}

int NSocket_getBlockedMode(struct NSocket *native) {
    return native->blockedMode;
}

int NSocket_setBlockedMode(struct NSocket *native, int value) {
#ifdef WINDOWS_TARGET
    int nonBlocking = value ? 0 : 1;
    if (ioctlsocket(native->native, FIONBIO, &nonBlocking) != 0) {
//            if (GetLastError() == WSAENOTSOCK) {
//                return 0;
//            }
        return 0;
    }
#else
    int flags = fcntl(native->native, F_GETFL, 0);
    int newFlags = value ? flags ^ O_NONBLOCK : flags | O_NONBLOCK;

    if (fcntl(native->native, F_SETFL, newFlags) != 0) {
        return 0;
    }
#endif
    native->blockedMode = value;
    return 1;
}

int internal_NSocket_bind(int socket, struct sockaddr *addr, int addrSize, int needListen) {

    if (bind(socket, addr, addrSize) < 0) {
#ifdef LINUX_LIKE_TARGET
        if (errno == EINVAL) {
            return BIND_RESULT_ALREADY_BINDED;
        }
        if (errno == EADDRINUSE) {
            return BIND_RESULT_ADDRESS_ALREADY_IN_USE;
        }
#else
        switch (GetLastError()) {
            case WSAEADDRINUSE:
                return BIND_RESULT_ADDRESS_ALREADY_IN_USE;
            case WSAEACCES:
                return BIND_RESULT_UNKNOWN_ERROR; // An attempt was made to access a socket in a way forbidden by its access permissions.
            case WSAEAFNOSUPPORT:
                return BIND_RESULT_PROTOCOL_NOT_SUPPORTED;
            case WSAEFAULT:
                return BIND_RESULT_UNKNOWN_ERROR;
            case WSAEINVAL:
                return BIND_RESULT_ALREADY_BINDED;
        }
#endif
        return BIND_RESULT_UNKNOWN_ERROR;
    }
    if (needListen) {
        if (listen(socket, 5) < 0) {
#ifdef LINUX_LIKE_TARGET
            if (errno == EOPNOTSUPP) {
                NSocket_unbind(socket);
                return BIND_RESULT_NOT_SUPPORTED;
            }
#else
            switch (GetLastError()) {
                case WSAEOPNOTSUPP:
                    return 1; // UDP not supported listen. Ignore
            }
#endif

            NSocket_unbind(socket);
            return BIND_RESULT_UNKNOWN_ERROR;
        }
    }
    return BIND_RESULT_OK;
}

//int NSocket_bindInetAny(struct NSocket *dest){
//    if (dest==NULL){
//        return 0;
//    }
//
//    bind(dest->native,)
//}
int NSocket_bindInet(struct NSocket *native, struct NInetSocketNetworkAddress *address, int needListen) {
    if (native == NULL) {
        return BIND_RESULT_UNKNOWN_ERROR;
    }
    if (native->protocolFamily != NET_TYPE_INET6 && native->protocolFamily != NET_TYPE_INET4) {
        return BIND_RESULT_UNKNOWN_ERROR;
    }
    if (address == NULL) {
        return BIND_RESULT_UNKNOWN_ERROR;
    }
    int addressProtocol = NInetSocketNetworkAddress_getFamily(address);
    if (native->protocolFamily == NET_TYPE_INET4 && addressProtocol == NET_TYPE_INET6) {
        return BIND_RESULT_PROTOCOL_NOT_SUPPORTED;
    }
    if (native->protocolFamily == NET_TYPE_INET6 && addressProtocol == NET_TYPE_INET4) {
        NInetSocketNetworkAddress_convertToIpv6(address);
//        addressProtocol = NET_TYPE_INET6;
    }
    return internal_NSocket_bind(
            native->native,
            (struct sockaddr *) address->data,
            address->size,
            needListen
    );
}

int NSocket_bindUnix(struct NSocket *native, const char *path, int needListen) {
//    actual fun bindUnixSocket(native: RawSocket, fileName: String): BindStatus {
//    unbind(native)//    unbind(native)
#ifdef WINDOWS_TARGET
    return BIND_RESULT_NOT_SUPPORTED;
#elif defined(LINUX_LIKE_TARGET)
    struct sockaddr_un addr;
    memset(&addr, 0, sizeof(struct sockaddr_un));
    addr.sun_family = AF_UNIX;
    strcpy(addr.sun_path, path);
    unlink(path);

    return internal_NSocket_bind(
            native->native,
            (struct sockaddr *) &addr,
            sizeof(struct sockaddr_un),
            needListen
    );
#else
#error Not supported
#endif
}

int NSocket_acceptInetSocketAddress(struct NSocket *native, struct NInetSocketNetworkAddress *addr) {
    if (addr == NULL) {
        return accept(native->native, NULL, NULL);
    } else {
        socklen_t size1 = 28;//(socklen_t *) &addr->size;
        int newSocket = accept(native->native, (struct sockaddr *) addr->data, &size1);
        addr->size = size1;
        return newSocket;
    }
}

/**
actual fun internalReceive(native: RawSocket, data: ByteBuffer, address: MutableNetworkAddress?): Int {
    if (data.remaining == 0) {
        return 0
    }
    return if (address == null) {
        data.ref(0) { dataPtr, remaining ->
            recvfrom(native, dataPtr, remaining.convert(), 0, null, null)
        }.toInt()
    } else {
        val netAddress = if (address is CommonMutableNetworkAddress) {
            address
        } else {
            CommonMutableNetworkAddress(address)
        }
        val readSize = netAddress.addr { addrPtr ->
            data.ref(0) { dataPtr, remaining ->
                memScoped {
                    val len = allocArray<socklen_tVar>(1)
                    len[0] = sizeOf<sockaddr_in6>().convert()
                    val r = recvfrom(
                        native,
                        dataPtr,
                        remaining.convert(),
                        0,
                        addrPtr.reinterpret(),
                        len,
                    )
                    if (r >= 0) {
                        netAddress.size = len[0].convert()
                    }
                    r
                }
            }.toInt()
        }
        if (readSize >= 0 && netAddress !== address) {
            address.update(netAddress.host, netAddress.port)
        }
        readSize
    }
}
*/
