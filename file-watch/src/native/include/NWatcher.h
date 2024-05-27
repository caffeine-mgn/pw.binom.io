#ifndef NWATCHER_H
#define NWATCHER_H

#include "definition.h"

#define Watcher_MODE_CREATE 1
#define Watcher_MODE_MODIFY 2
#define Watcher_MODE_DELETE 4

struct NWatcher {
#if defined(LINUX_LIKE_TARGET)
    int inotifyFd;
#elif defined(__APPLE__)
#elif defined(WINDOWS_TARGET)
#else
#error not supported
#endif
};

struct NWatcher *NWatcher_create();

struct NWatcher *NWatcher_addListening(struct NWatcher *watcher, const char *path, int mode);

int NWatcher_close(struct NWatcher *watcher);

#endif // NWATCHER_H
