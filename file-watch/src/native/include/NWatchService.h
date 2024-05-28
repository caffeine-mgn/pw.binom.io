#ifndef NWATCHER_H
#define NWATCHER_H

#include "definition.h"

#ifdef LINUX_LIKE_TARGET

#include <sys/inotify.h>

#endif

#define Watcher_MODE_CREATE 1
#define Watcher_MODE_MODIFY 2
#define Watcher_MODE_DELETE 4

struct NWatchService {
#if defined(LINUX_LIKE_TARGET)
    int inotifyFd;
    struct inotify_event buffer[100 + 1];
#elif defined(__APPLE__)
#elif defined(WINDOWS_TARGET)
#else
#error not supported
#endif
};

int NWatcher_isRecursiveSupported();

struct NWatchService *NWatcher_create();

int NWatcher_addListening(struct NWatchService *watcher, const char *path, int mode);
int NWatcher_removeListening(struct NWatchService *watcher, int watcherKey);


int NWatcher_take(struct NWatchService *watcher, int (*func)(const char *path, int mode, int watcherKey));

int NWatcher_close(struct NWatchService *watcher);

#endif // NWATCHER_H
