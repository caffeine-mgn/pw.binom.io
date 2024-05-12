#include <errno.h>
#include <stdio.h>
#include "../include/err.h"
#include "../include/definition.h"

int err_getLastNetworkError() {
#ifdef WINDOWS_TARGET
    return GetLastError();
#elif defined(LINUX_LIKE_TARGET)
    switch (errno) {
        case EAFNOSUPPORT:
            return INVALID_ADDRESS_BY_PROTOCOL;
        case EADDRNOTAVAIL:
            return ADDRESS_NOT_AVAILABLE;
        default:
            printf("Not supported error %d\n",errno);
            return errno;
    }
    return errno;
#else
#error "Unknown target"
#endif
}