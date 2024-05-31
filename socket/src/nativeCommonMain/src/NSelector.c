#include <malloc.h>
#include <unistd.h>
#include "../include/NSelector.h"
#include "../include/definition.h"
#include "../include/wepoll.h"

#if defined(LINUX_TARGET) || defined(ANDROID_TARGET)
#include <sys/epoll.h>
#include <errno.h>
#endif

#ifdef __APPLE__
#include <sys/event.h>
    #ifndef EVFILT_EMPTY
    #define EVFILT_EMPTY 0
    #endif
#endif

#ifndef EINTR
#define EINTR 4
#endif

struct NSelector *NSelector_createSelector(int size)
{
    struct NSelector *selector = malloc(sizeof(struct NSelector));
#ifdef USE_EPOLL
    int epoll = epoll_create(size);
    if (epoll == -1)
    {
        return NULL;
    }
    selector->epoll = epoll;
#elif defined(__APPLE__)
    int k = kqueue();
    if (k == -1)
    {
        return NULL;
    }
    selector->kqueueValue = k;
#else
#error NOT SUPPORTED
#endif
    return selector;
}

void NSelector_closeSelector(struct NSelector *selector)
{
#ifdef USE_EPOLL
    close(selector->epoll);
#elif defined(__APPLE__)
    close(selector->kqueueValue);
#else
#error NOT SUPPORTED
#endif
    free(selector);
}

int NSelector_selectKeys(struct NSelector *selector, struct NSelectedList *selectedList, int timeout)
{
#ifdef USE_EPOLL
    int result = epoll_wait(
        selector->epoll,
        selectedList->events,
        selectedList->size,
        timeout);
#ifdef LINUX_LIKE_TARGET
    if (result == -1 && errno == EINTR)
    {
        return 0;
    }
#endif
    return result;
#elif defined(__APPLE__)
    struct timespec *nativeTimeout;
    struct timespec c;
    if (timeout >= 0)
    {
        c.tv_sec = timeout / 1000L;
        c.tv_nsec = (timeout - c.tv_sec * 1000) * 1000L;
        nativeTimeout = &c;
    }
    else
    {
        nativeTimeout = NULL;
    }
    int size = kevent(selector->kqueueValue, NULL, 0, selectedList->list, SELECTOR_EVENT_LIST_COUNT, nativeTimeout);
    return size;
#else
#error NOT SUPPORTED
#endif
}

int NSelector_registryKey(struct NSelector *selector, int key, struct NEvent *event)
{
#ifdef USE_EPOLL
    return epoll_ctl(selector->epoll, EPOLL_CTL_ADD, key, (struct epoll_event *)event) == 0;
#elif defined(__APPLE__)
    struct kevent *nativeEvent = (struct kevent *)event;
    nativeEvent->ident = key;
    if (nativeEvent->filter == EVFILT_EMPTY)
    {
        nativeEvent->flags = (nativeEvent->flags & ~EV_ENABLE) | EV_DISABLE;
    }
    else
    {
        nativeEvent->flags = (nativeEvent->flags & ~EV_DISABLE) | EV_ENABLE;
    }
    nativeEvent->flags = nativeEvent->flags | EV_ADD;
    if (kevent(selector->kqueueValue, nativeEvent, 1, NULL, 0, NULL) == -1)
    {
        return 0;
    }
    else
    {
        return 1;
    }
#else
#error Not supported
#endif
}

int NSelector_updateKey(struct NSelector *selector, int key, struct NEvent *event)
{
#ifdef USE_EPOLL
    return epoll_ctl(selector->epoll, EPOLL_CTL_MOD, key, (struct epoll_event *)event) == 0;
#elif defined(__APPLE__)
    struct kevent *nativeEvent = (struct kevent *)event;
    nativeEvent->ident = key;

    if (nativeEvent->filter == EVFILT_EMPTY)
    {
        nativeEvent->flags = (nativeEvent->flags & ~EV_ENABLE) | EV_DISABLE;
    }
    else
    {
        nativeEvent->flags = (nativeEvent->flags & ~EV_DISABLE) | EV_ENABLE;
    }
    if (kevent(selector->kqueueValue, nativeEvent, 1, NULL, 0, NULL) != -1)
    {
        return 1;
    }
    else
    {
        return 0;
    }
#else
#error Not supported
#endif
}

int NSelector_removeKey(struct NSelector *selector, int key)
{
#ifdef USE_EPOLL
    return epoll_ctl(selector->epoll, EPOLL_CTL_DEL, key, NULL) == 0;
#elif defined(__APPLE__)
    struct kevent event;
    EV_SET(&event, key, EVFILT_EMPTY, EV_DELETE | EV_CLEAR, 0, 0, NULL);
    if (kevent(selector->kqueueValue, &event, 1, NULL, 0, NULL) != -1)
    {
        return 1;
    }
    else
    {
        return 0;
    }
#else
#error Not supported
#endif
}