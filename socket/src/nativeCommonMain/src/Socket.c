//
// Created by subochev on 04.06.23.
//
#include "../include/Socket.h"
#include "../include/definition.h"

#ifdef LINUX_LIKE_TARGET
#include <sys/socket.h>
#include <sys/un.h>
#endif

#ifdef WINDOWS_TARGET
#include <winsock2.h>
#endif

#include <unistd.h>
#include <errno.h>

int Socket_unbind(int socket){
    int flag = 1;
    return setsockopt(socket, SOL_SOCKET, SO_REUSEADDR, (char*)&flag, sizeof(int));
}

int Socket_bindUnix(int socket, const char *path) {
//    actual fun bindUnixSocket(native: RawSocket, fileName: String): BindStatus {
//    unbind(native)//    unbind(native)
#ifdef WINDOWS_TARGET
    return BIND_RESULT_NOT_SUPPORTED;
#endif
#ifdef LINUX_LIKE_TARGET
    struct sockaddr_un addr;
    memset(&addr, 0, sizeof(struct sockaddr_un));
    addr.sun_family = AF_UNIX;
    strcpy(addr.sun_path, path);
    unlink(path);
    int bindResult = bind(
        socket,
            (struct sockaddr *)&addr,
        sizeof(struct sockaddr_un)
    );

    if (bindResult < 0) {
        int errno1 = errno;
      if (errno1 == EINVAL){
          return BIND_RESULT_ALREADY_BINDED;
        }
        if (errno1 == EADDRINUSE || errno1 == 0){
            return BIND_RESULT_ADDRESS_ALREADY_IN_USE;
        }
        return BIND_RESULT_UNKNOWN_ERROR;
//        throw IOException("Bind error. errno: [$errno], bind: [$bindResult]");
    }
    int listenResult = listen(socket, 1000);
    if (listenResult < 0) {
//            if (errno == ESOCKTNOSUPPORT) {
//                return@memScoped
//            }
//            if (errno == EOPNOTSUPP) {
//                return@memScoped
// //                    unbind(native)
// //                    throw IOException("Can't bind socket: Operation not supported on transport endpoint")
//            }
    Socket_unbind(socket);
    return BIND_RESULT_UNKNOWN_ERROR;
//        throw IOException("Listen error. errno: [$errno], listen: [$listenResult]");
    }
    return BIND_RESULT_OK;
#endif
}

int Socket_accept(int socket, struct NativeNetworkAddress* addr){
    if (addr==NULL){
        return accept(socket, NULL,NULL);
    } else {
        #ifdef WINDOWS_TARGET
        auto size1 = addr->size;
        #else
        auto size1 = (socklen_t)addr->size;
        #endif
        return accept(socket, (struct sockaddr*)&addr->data, &size1);
    }
}

int Socket_receive(int socket, void* dest, int max, struct NativeNetworkAddress* addr){
    if (max <= 0){
        return 0;
    }

    #ifdef WINDOWS_TARGET
    char* dest2 = (char*)dest;
    #else
    void* dest2 = dest;
    #endif

    if (addr == NULL){
        return recvfrom(socket, dest2, max, 0, NULL, NULL);
    } else {
        #ifdef WINDOWS_TARGET
        auto size1 = addr->size;
        #else
        auto size1 = (socklen_t)addr->size;
        #endif
        return recvfrom(socket, dest2, max, 0, (struct sockaddr*)&addr->data, &size1);
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