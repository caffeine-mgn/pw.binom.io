#include "../include/NWatchService.h"
#include <malloc.h>
#include <string.h>
#include <sys/inotify.h>
#include <unistd.h>

#ifdef LINUX_LIKE_TARGET
#define BUFFER_LEN (100 * sizeof(struct inotify_event) + 16)
#endif

int NWatcher_isRecursiveSupported() {
#if defined(LINUX_LIKE_TARGET)
    return 0;
#elif defined(__APPLE__)
#error not supported
#elif defined(WINDOWS_TARGET)
#error not supported
#else
#error not supported
#endif
}

struct NWatchService *NWatcher_create() {
    struct NWatchService *instance = (struct NWatchService *) malloc(sizeof(struct NWatchService));
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
    memset(instance, 0, sizeof(struct NWatchService));
    return instance;
}

int NWatcher_addListening(struct NWatchService *watcher, const char *path, int mode) {
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
    return inotify_add_watch(
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

int NWatcher_removeListening(struct NWatchService *watcher, int watcherKey) {
    if (watcher == NULL) {
        return 0;
    }
#if defined(LINUX_LIKE_TARGET)
    return inotify_rm_watch(watcher->inotifyFd, watcherKey) == 0 ? 1 : 0;
#elif defined(__APPLE__)
#error not supported
#elif defined(WINDOWS_TARGET)
#error not supported
#else
#error not supported
#endif
}

int NWatcher_take(struct NWatchService *watcher, int (*func)(const char *path, int mode, int watcherKey)) {
    if (watcher == NULL || func == NULL) {
        return 0;
    }

#if defined(LINUX_LIKE_TARGET)
    int length = read(watcher->inotifyFd, watcher->buffer, BUFFER_LEN);
    int cursor = 0L;
    while (cursor < length) {
        struct inotify_event *event = &watcher->buffer[cursor];
        cursor += sizeof(struct inotify_event);
        if (event->len > 0) {
            int mode = 0;
            if (event->mask & IN_CREATE) {
                mode |= Watcher_MODE_CREATE;
            }
            if (event->mask & IN_MODIFY) {
                mode |= Watcher_MODE_MODIFY;
            }
            if (event->mask & IN_DELETE) {
                mode |= Watcher_MODE_DELETE;
            }
            func(event->name, mode, event->wd);
        }
    }
#elif defined(__APPLE__)
#error not supported
#elif defined(WINDOWS_TARGET)
#error not supported
#else
#error not supported
#endif
}

int NWatcher_close(struct NWatchService *watcher) {
    if (watcher == NULL) {
        return 0;
    }

#if defined(LINUX_LIKE_TARGET)
    close(watcher->inotifyFd);
    free(watcher);
#elif defined(__APPLE__)
#error not supported
#elif defined(WINDOWS_TARGET)
#error not supported
#else
#error not supported
#endif
    return 1;
}