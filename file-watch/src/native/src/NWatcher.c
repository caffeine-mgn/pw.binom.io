#include "../include/NWatcher.h"
#include <malloc.h>
#include <string.h>
#include <sys/inotify.h>
#include <unistd.h>

struct NWatcher *NWatcher_create() {
    struct NWatcher *instance = (struct NWatcher *) malloc(sizeof(struct NWatcher));
#if defined(LINUX_LIKE_TARGET)
    int notifyFd = inotify_init1(IN_CLOEXEC);
    if (notifyFd == -1) {
        return NULL;
    }
    instance->inotifyFd = notifyFd;
#elif defined(__APPLE__)
#error not supported
#elif defined(WINDOWS_TARGET)
#error not supported
#else
#error not supported
#endif
    memset(instance, 0, sizeof(struct NWatcher));
    return instance;
}

struct NWatcher *NWatcher_addListening(struct NWatcher *watcher, const char *path, int mode) {
#if defined(LINUX_LIKE_TARGET)
    int resultMode = 0;
    if (mode & Watcher_MODE_CREATE) {
        resultMode |= IN_CREATE;
    }
    if (mode & Watcher_MODE_MODIFY) {
        resultMode |= IN_MODIFY;
    }
    if (mode & Watcher_MODE_DELETE) {
        resultMode |= IN_DELETE;
    }
    inotify_add_watch(
            watcher->inotifyFd,
            path,
            resultMode
    );
#elif defined(__APPLE__)
#error not supported
#elif defined(WINDOWS_TARGET)
#error not supported
#else
#error not supported
#endif
}

int NWatcher_close(struct NWatcher *watcher) {
    if (watcher == NULL) {
        return 0;
    }

#if defined(LINUX_LIKE_TARGET)
    close(watcher->inotifyFd);
#elif defined(__APPLE__)
#error not supported
#elif defined(WINDOWS_TARGET)
#error not supported
#else
#error not supported
#endif
    return 1;
}