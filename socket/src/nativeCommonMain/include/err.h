#ifndef ERR_H
#define ERR_H
#include "definition.h"
#ifdef WINDOWS_TARGET
#include <windows.h>
#endif

/**
 * Address family not supported by protocol
 */
#define INVALID_ADDRESS_BY_PROTOCOL 97

/**
 * Cannot assign requested address
 */
#define ADDRESS_NOT_AVAILABLE 99

int err_getLastNetworkError();

#endif
