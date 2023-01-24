#include <malloc.h>
#include <unistd.h>
#include "../include/Selector.h"
#include "../include/definition.h"
#include "../include/wepoll.h"

struct Selector *createSelector(int size) {
    struct Selector *selector = malloc(sizeof(struct Selector));
#ifdef USE_EPOLL
    int epoll = epoll_create(size);
    if (epoll == -1) {
        return NULL;
    }
    selector->epoll = epoll;
#endif
    return selector;
}

void closeSelector(struct Selector *selector) {
#ifdef USE_EPOLL
    close(selector->epoll);
    free(selector);
#endif
}

int selectKeys(struct Selector *selector, struct SelectedList *selectedList, int timeout) {
#ifdef USE_EPOLL
    return epoll_wait(
            selector->epoll,
            selectedList->events,
            selectedList->size,
            timeout
    );
#endif
}

int registryKey(struct Selector *selector, SOCKET key, struct Event *event) {
#ifdef USE_EPOLL
    return epoll_ctl(selector->epoll, EPOLL_CTL_ADD, key, (struct epoll_event *) event);
#endif
}

int updateKey(struct Selector *selector, SOCKET key, struct Event *event) {
#ifdef USE_EPOLL
    return epoll_ctl(selector->epoll, EPOLL_CTL_MOD, key, (struct epoll_event *) event);
#endif
}

int removeKey(struct Selector *selector, SOCKET key) {
#ifdef USE_EPOLL
    return epoll_ctl(selector->epoll, EPOLL_CTL_DEL, key, NULL);
#endif
}