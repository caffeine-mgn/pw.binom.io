//
// Created by subochev on 04.06.23.
//

#ifndef NATIVE_NATIVENETWORKADDRESS_H
#define NATIVE_NATIVENETWORKADDRESS_H

struct NativeNetworkAddress {
    signed char data[28];
    int size;
};

#define NET_TYPE_INET4 1
#define NET_TYPE_INET6 2
#define NET_TYPE_UNIX 3
#define NET_TYPE_UNKNOWN 0

struct NativeNetworkAddress *mallocNativeNetworkAddress();

void NativeNetworkAddress_copy(struct NativeNetworkAddress *from, struct NativeNetworkAddress *to);

void NativeNetworkAddress_free(struct NativeNetworkAddress *ptr);

int NativeNetworkAddress_getFamily(struct NativeNetworkAddress *ptr);

#endif //NATIVE_NATIVENETWORKADDRESS_H
