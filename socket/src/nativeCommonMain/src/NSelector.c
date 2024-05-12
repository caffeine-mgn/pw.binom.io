#include <malloc.h>
#include <unistd.h>
#include "../include/NSelector.h"
#include "../include/definition.h"
#include "../include/wepoll.h"

#if defined(LINUX_TARGET) || defined(ANDROID_TARGET)

#include <sys/epoll.h>
#include <errno.h>

#endif

#ifndef EINTR
#define EINTR 4
#endif

struct NSelector *NSelector_createSelector(int size) {
    struct NSelector *selector = malloc(sizeof(struct NSelector));
#ifdef USE_EPOLL
    int epoll = epoll_create(size);
    if (epoll == -1) {
        return NULL;
    }
    selector->epoll = epoll;
#endif
    return selector;
}

void NSelector_closeSelector(struct NSelector *selector) {
#ifdef USE_EPOLL
    close(selector->epoll);
    free(selector);
#endif
}

int NSelector_selectKeys(struct NSelector *selector, struct NSelectedList *selectedList, int timeout) {
#ifdef USE_EPOLL
    int result = epoll_wait(
            selector->epoll,
            selectedList->events,
            selectedList->size,
            timeout
    );
#ifdef LINUX_LIKE_TARGET
    if (result == -1 && errno == EINTR) {
        return 0;
    }
#endif
    return result;
#endif
}

int NSelector_registryKey(struct NSelector *selector, int key, struct NEvent *event) {
#ifdef USE_EPOLL
    return epoll_ctl(selector->epoll, EPOLL_CTL_ADD, key, (struct epoll_event *) event) == 0;
#else
#error Not supported
#endif
}

int NSelector_updateKey(struct NSelector *selector, int key, struct NEvent *event) {
#ifdef USE_EPOLL
    return epoll_ctl(selector->epoll, EPOLL_CTL_MOD, key, (struct epoll_event *) event) == 0;
#else
#error Not supported
#endif
}

int NSelector_removeKey(struct NSelector *selector, int key) {
#ifdef USE_EPOLL
    return epoll_ctl(selector->epoll, EPOLL_CTL_DEL, key, NULL)==0;
#else
#error Not supported
#endif
}