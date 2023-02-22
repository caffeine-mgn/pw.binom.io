#include <malloc.h>
#include "../include/wepoll.h"
#include "../include/Event.h"
#include "../include/definition.h"

#ifdef LINUX_TARGET

#include <sys/epoll.h>

#endif

struct Event *mallocEvent() {
#ifdef USE_EPOLL
    return (struct Event *) malloc(sizeof(struct epoll_event));
#endif
}

void freeEvent(struct Event *event) {
#ifdef USE_EPOLL
    free((struct epoll_event *) event);
#endif
}

int getEventFlags(struct Event *event) {
#ifdef USE_EPOLL
    int output = 0;
    int events = (int) ((struct epoll_event *) event)->events;
    if (events & EPOLLIN) {
        output = output | FLAG_READ;
    }
    if (events & EPOLLOUT) {
        output = output | FLAG_WRITE;
    }
    if (events & EPOLLONESHOT) {
        output = output | FLAG_ONCE;
    }
    if (events & EPOLLERR || events & EPOLLHUP) {
        output = output | FLAG_ERROR;
    }
    return output;
#endif
}

void setEventFlags(struct Event *event, int flags, int isServerFlag) {
#ifdef USE_EPOLL
    int events = 0;
    if (flags & FLAG_READ) {
        events = events | EPOLLIN | EPOLLERR;
        if (!isServerFlag) {
            events = events | EPOLLHUP;
        }
    }
    if (flags & FLAG_WRITE) {
        events = events | EPOLLOUT | EPOLLERR;
    }
    if (flags & FLAG_ERROR) {
        events = events | EPOLLERR;
        if (!isServerFlag) {
            events = events | EPOLLHUP;
        }
    }
    if (flags & FLAG_ONCE) {
        events = events | EPOLLONESHOT;
    }
    ((struct epoll_event *) event)->events = events;
#endif
}

void setEventDataFd(struct Event *event, int value) {
#ifdef USE_EPOLL
    ((struct epoll_event *) event)->data.fd = value;
#endif
}

void setEventDataPtr(struct Event *event, void *value) {
#ifdef USE_EPOLL
    ((struct epoll_event *) event)->data.ptr = value;
#endif
}

int getEventDataFd(struct Event *event) {
#ifdef USE_EPOLL
    return ((struct epoll_event *) event)->data.fd;
#endif
}

void *getEventDataPtr(struct Event *event) {
#ifdef USE_EPOLL
    return ((struct epoll_event *) event)->data.ptr;
#endif
}