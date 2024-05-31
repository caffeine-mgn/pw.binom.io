#include <malloc.h>
#include <unistd.h>
#include "../include/NSelectedList.h"
#ifdef __APPLE__
#include <sys/event.h>
#endif

struct NSelectedList *NSelectedList_createSelectedList(int size)
{
    struct NSelectedList *selector = malloc(sizeof(struct NSelectedList));
    selector->size = size;
#ifdef USE_EPOLL
    selector->events = malloc(sizeof(struct epoll_event) * size);
#elif defined(__APPLE__)
    selector->size = size;
    selector->list = malloc(sizeof(struct kevent) * size);
#endif
    return selector;
}

void NSelectedList_closeSelectedList(struct NSelectedList *selectedList)
{
#ifdef USE_EPOLL
    free(selectedList->events);
#elif defined(__APPLE__)
    free(selectedList->list);
#else
#error NOT SUPPORTED
#endif
    free(selectedList);
}

struct NEvent *NSelectedList_getEventFromSelectedList(struct NSelectedList *selectedList, int index)
{
    if (index < 0 || index >= selectedList->size)
    {
        return NULL;
    }
#ifdef USE_EPOLL
    struct epoll_event *event = &selectedList->events[index];
    return (struct NEvent *)event;
#elif defined(__APPLE__)
    return (struct NEvent *)&selectedList->list[index];
#else
#error NOT SUPPORTED
#endif
}
