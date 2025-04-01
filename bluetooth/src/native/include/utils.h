#ifndef UTILS_H
#define UTILS_H
#include "definition.h"

#ifdef __cplusplus
    #define START_EXTERN extern "C" {
    #define END_EXTERN }
#else
    #define START_EXTERN
    #define END_EXTERN
#endif

#ifdef BUILD_SHARED_LIB

    #ifdef WINDOWS_TARGET
        #define EXTERN_DLL_EXPORT __declspec(dllexport)
    #else
        #define EXTERN_DLL_EXPORT __attribute__((visibility("default")))
    #endif
#else
    #define EXTERN_DLL_EXPORT
#endif


#if defined(LINUX_TARGET) || defined(WINDOWS_TARGET)
void copyAddressAndReverseBytes(unsigned char *from, unsigned char *to);
#endif

#endif
