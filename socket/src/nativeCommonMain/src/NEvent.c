#include <malloc.h>
#include "../include/wepoll.h"
#include "../include/NEvent.h"
#include "../include/definition.h"
#if defined(LINUX_TARGET) || defined(ANDROID_TARGET)
#include <sys/epoll.h>
#endif

#ifdef __APPLE__
#include <sys/event.h>

    #ifndef EVFILT_EMPTY
    #define EVFILT_EMPTY 0
    #endif
#endif

struct NEvent *NEvent_malloc()
{
#ifdef USE_EPOLL
    return (struct NEvent *)malloc(sizeof(struct epoll_event));
#elif defined(__APPLE__)
    return (struct NEvent *)malloc(sizeof(struct kevent));
#else
#error NOT SUPPORTED
#endif
}

void NEvent_free(struct NEvent *event)
{
#ifdef USE_EPOLL
    free((struct epoll_event *)event);
#endif
}

int NEvent_getEventFlags(struct NEvent *event)
{
#ifdef USE_EPOLL
    int output = 0;
    int events = (int)((struct epoll_event *)event)->events;
    if (events & EPOLLIN)
    {
        output = output | FLAG_READ;
    }
    if (events & EPOLLOUT)
    {
        output = output | FLAG_WRITE;
    }
    if (events & EPOLLONESHOT)
    {
        output = output | FLAG_ONCE;
    }
    if (events & EPOLLERR || events & EPOLLHUP)
    {
        output = output | FLAG_ERROR;
    }
    return output;
#elif defined(__APPLE__)
    struct kevent *nativeEvent = (struct kevent *)event;
    int output = 0;
    if (nativeEvent->filter & EVFILT_READ)
    {
        output = output | FLAG_READ;
    }
    if (nativeEvent->filter & EVFILT_WRITE)
    {
        output = output | FLAG_WRITE;
    }
    if (nativeEvent->flags & EV_EOF)
    {
        output = output | FLAG_ERROR;
    }
    if (nativeEvent->flags & EV_ONESHOT)
    {
        output = output | FLAG_ONCE;
    }
    return output;
#else
#error NOT SUPPORTED
#endif
}

void NEvent_setEventFlags(struct NEvent *event, int flags, int isServerFlag)
{
#ifdef USE_EPOLL
    int events = 0;
    if (flags & FLAG_READ)
    {
        events = events | EPOLLIN | EPOLLERR;
        if (!isServerFlag)
        {
            events = events | EPOLLHUP;
        }
    }
    if (flags & FLAG_WRITE)
    {
        events = events | EPOLLOUT | EPOLLERR;
    }
    if (flags & FLAG_ERROR)
    {
        events = events | EPOLLERR;
        if (!isServerFlag)
        {
            events = events | EPOLLHUP;
        }
    }
    if (flags & FLAG_ONCE)
    {
        events = events | EPOLLONESHOT;
    }
    ((struct epoll_event *)event)->events = events;
#elif defined(__APPLE__)
    struct kevent *nativeEvent = (struct kevent *)event;

    nativeEvent->flags = flags & FLAG_ONCE ? EV_ONESHOT : 0;
    if (flags & FLAG_ERROR)
    {
        nativeEvent->flags = nativeEvent->flags | EV_EOF;
    }
    int filter = 0;
    if (flags & FLAG_WRITE)
    {
        filter = filter | EVFILT_WRITE;
    }
    if (flags & FLAG_READ)
    {
        filter = filter | EVFILT_READ;
    }
    nativeEvent->filter = filter ? filter : EVFILT_EMPTY;

#else
#error NOT SUPPORTED
#endif
}

void NEvent_setEventDataFd(struct NEvent *event, int value)
{
#ifdef USE_EPOLL
    ((struct epoll_event *)event)->data.fd = value;
#elif defined(__APPLE__)
    struct kevent *nativeEvent = (struct kevent *)event;
    nativeEvent->ident = value;
#else
#error NOT SUPPORTED
#endif
}

void NEvent_setEventDataPtr(struct NEvent *event, void *value)
{
#ifdef USE_EPOLL
    ((struct epoll_event *)event)->data.ptr = value;
#elif defined(__APPLE__)
    struct kevent *nativeEvent = (struct kevent *)event;
    nativeEvent->udata = value;
#else
#error NOT SUPPORTED
#endif
}

int NEvent_getEventDataFd(struct NEvent *event)
{
#ifdef USE_EPOLL
    return ((struct epoll_event *)event)->data.fd;
#elif defined(__APPLE__)
    struct kevent *nativeEvent = (struct kevent *)event;
    return nativeEvent->ident;
#else
#error NOT SUPPORTED
#endif
}

void *NEvent_getEventDataPtr(struct NEvent *event)
{
#ifdef USE_EPOLL
    return ((struct epoll_event *)event)->data.ptr;
#elif defined(__APPLE__)
    struct kevent *nativeEvent = (struct kevent *)event;
    return (void *)nativeEvent->udata;
#else
#error NOT SUPPORTED
#endif
}