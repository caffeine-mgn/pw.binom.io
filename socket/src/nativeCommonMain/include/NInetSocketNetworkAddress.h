#ifndef NATIVE_NINETSOCKETNETWORKADDRESS_H
#define NATIVE_NINETSOCKETNETWORKADDRESS_H

#include "NNetworkAddress.h"

struct NInetSocketNetworkAddress {
    signed char data[28];
    int size;
};

struct NInetSocketNetworkAddress *NInetSocketNetworkAddress_malloc();

void NInetSocketNetworkAddress_copy(struct NInetSocketNetworkAddress *from, struct NInetSocketNetworkAddress *to);

void NInetSocketNetworkAddress_free(struct NInetSocketNetworkAddress *ptr);
int NInetSocketNetworkAddress_isMulticast(struct NInetSocketNetworkAddress *ptr);

int NInetSocketNetworkAddress_getFamily(struct NInetSocketNetworkAddress *ptr);

int NInetSocketNetworkAddress_convertToIpv6(struct NInetSocketNetworkAddress *ptr);

int NInetSocketNetworkAddress_getPort(struct NInetSocketNetworkAddress *ptr);

int NInetSocketNetworkAddress_setPort(struct NInetSocketNetworkAddress *ptr, int port);
int NInetSocketNetworkAddress_getAddressBytes(struct NInetSocketNetworkAddress *ptr, signed char *buffer);

int NInetSocketNetworkAddress_getHostString(struct NInetSocketNetworkAddress *ptr, char *buffer, int buffLen);

int NInetSocketNetworkAddress_getHost(struct NInetSocketNetworkAddress *ptr, struct NNetworkAddress *dest);
int NInetSocketNetworkAddress_setHost(struct NInetSocketNetworkAddress *ptr, struct NNetworkAddress *address);

#endif //NATIVE_NINETSOCKETNETWORKADDRESS_H
