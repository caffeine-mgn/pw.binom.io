#include <malloc.h>
#include "../include/SelectedList.h"

struct SelectedList *createSelectedList(int size) {
    struct SelectedList *selector = malloc(sizeof(struct SelectedList));
    selector->size = size;
#ifdef USE_EPOLL
    selector->events = malloc(sizeof(struct epoll_event) * size);
#endif
    return selector;
}

void closeSelectedList(struct SelectedList *selectedList) {
#ifdef USE_EPOLL
    free(selectedList->events);
#endif
    free(selectedList);
}

struct Event *getEventFromSelectedList(struct SelectedList *selectedList, int index) {
#ifdef USE_EPOLL
    struct epoll_event *event = &selectedList->events[index];
    return (struct Event *) event;
#endif
}