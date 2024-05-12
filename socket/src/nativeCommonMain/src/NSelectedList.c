#include <malloc.h>
#include "../include/NSelectedList.h"

struct NSelectedList *NSelectedList_createSelectedList(int size) {
    struct NSelectedList *selector = malloc(sizeof(struct NSelectedList));
    selector->size = size;
#ifdef USE_EPOLL
    selector->events = malloc(sizeof(struct epoll_event) * size);
#endif
    return selector;
}

void NSelectedList_closeSelectedList(struct NSelectedList *selectedList) {
#ifdef USE_EPOLL
    free(selectedList->events);
#endif
    free(selectedList);
}

struct NEvent *NSelectedList_getEventFromSelectedList(struct NSelectedList *selectedList, int index) {
#ifdef USE_EPOLL
    struct epoll_event *event = &selectedList->events[index];
    return (struct NEvent *) event;
#else
#error NOT SUPPORTED
#endif
}