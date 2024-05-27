#ifndef NATIVE_DEFINITION_H
#define NATIVE_DEFINITION_H
//#define __linux__
//#define LINUX_LIKE_TARGET
#if defined(WIN32) || defined(_WIN32) || defined(__WIN32__) || defined(__NT__)
#define WINDOWS_TARGET
// define something for Windows (32-bit and 64-bit, this part is common)
#ifdef _WIN64
// define something for Windows (64-bit only)
#else
// define something for Windows (32-bit only)
#endif
#elif __APPLE__
#define APPLE_TARGET
#include <TargetConditionals.h>
#if TARGET_OS_IPHONE_SIMULATOR
// iOS, tvOS, or watchOS Simulator
#elif TARGET_OS_MACCATALYST
// Mac's Catalyst (ports iOS API into Mac, like UIKit).
#elif TARGET_OS_IPHONE
// iOS, tvOS, or watchOS device
#elif TARGET_OS_MAC
// Other kinds of Apple platforms
#else
#error "Unknown Apple platform"
#endif
#elif __ANDROID__
#define ANDROID_TARGET
#define LINUX_LIKE_TARGET
// Below __linux__ check should be enough to handle Android,
// but something may be unique to Android.
#elif __linux__
#define LINUX_LIKE_TARGET
#define LINUX_TARGET
// linux
#elif __unix__ // all unices not caught above
#define LINUX_LIKE_TARGET
#define POSIX_TARGET
// Unix
#elif defined(_POSIX_VERSION)
// POSIX
#else
#error "Unknown compiler"
#endif

#if defined(LINUX_LIKE_TARGET) || defined(WINDOWS_TARGET)
#define USE_EPOLL
#endif

#endif // NATIVE_DEFINITION_H
