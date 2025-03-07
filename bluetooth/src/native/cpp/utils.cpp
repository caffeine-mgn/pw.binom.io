#include "utils.h"

#if defined(LINUX_TARGET) || defined(WINDOWS_TARGET)
void copyAddressAndReverseBytes(unsigned char *from, unsigned char *to) {
    int len = 6;
    for (int i = 0; i < len; i++) {
        to[i] = from[len - 1 - i];
    }
}
#endif

