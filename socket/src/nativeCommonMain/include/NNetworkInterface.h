//
// Created by subochev on 06.12.23.
//

#ifndef NETWORKINTERFACE_H
#define NETWORKINTERFACE_H

#include "NInetSocketNetworkAddress.h"
#include "NNetworkAddress.h"

struct NNetworkInterface {
    const char* name;
    struct NNetworkAddress address;
    int prefixLength;
    struct NNetworkInterface* next;
    int index;
};
void internal_freeNetworkInterfaces(struct NNetworkInterface*interfaces);
struct NNetworkInterface* internal_getNetworkInterfaces();

#endif //NETWORKINTERFACE_H
