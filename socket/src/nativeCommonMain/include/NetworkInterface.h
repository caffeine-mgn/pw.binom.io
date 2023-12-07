//
// Created by subochev on 06.12.23.
//

#ifndef NETWORKINTERFACE_H
#define NETWORKINTERFACE_H

struct InternalNetworkInterface {
    const char* name;
    struct NativeNetworkAddress* address;
    // struct NativeNetworkAddress* mask;
    int prefixLength;
    struct InternalNetworkInterface* next;
};
void internal_freeNetworkInterfaces(struct InternalNetworkInterface*interfaces);
struct InternalNetworkInterface* internal_getNetworkInterfaces();

#endif //NETWORKINTERFACE_H
