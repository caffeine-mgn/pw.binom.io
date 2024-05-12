#ifndef NATIVE_NNETWORKADDRESS_H
#define NATIVE_NNETWORKADDRESS_H

struct NNetworkAddress {
    /**
     * in_addr or in6_addr
     * @see in_addr
     * @see in6_addr
     */
    signed char data[16];

    /**
     * Can be NET_TYPE_INET4, NET_TYPE_INET6 or NET_TYPE_UNKNOWN
     */
    int protocolFamily;
};

struct NNetworkAddressList {
    struct NNetworkAddress address;
    struct NNetworkAddressList *next;
};

struct NNetworkAddress *NNetworkAddress_malloc();

int NNetworkAddress_copy(struct NNetworkAddress *from, struct NNetworkAddress *to);

void NNetworkAddress_free(struct NNetworkAddress *ptr);

int NNetworkAddress_convertToIpv6(struct NNetworkAddress *ptr);

int NNetworkAddress_isMulticast(struct NNetworkAddress *ptr);

int NNetworkAddress_getAddressBytes(struct NNetworkAddress *ptr, signed char *buffer);

int NNetworkAddress_get_host(struct NNetworkAddress *ptr, char *buffer, int bufferLen);

int NNetworkAddress_set_host(struct NNetworkAddress *ptr, const char *host);

struct NNetworkAddressList *NNetworkAddressList_getAll(const char *host, int *size);

void NNetworkAddressList_free(struct NNetworkAddressList *list);


#endif //NATIVE_NNETWORKADDRESS_H
