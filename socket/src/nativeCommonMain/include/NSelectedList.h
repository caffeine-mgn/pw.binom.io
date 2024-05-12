//
// Created by subochev on 21.01.23.
//

#ifndef NATIVE_NSELECTEDLIST_H
#define NATIVE_NSELECTEDLIST_H

#include "NEvent.h"
#include "wepoll.h"
#include "definition.h"

#if defined(LINUX_TARGET) || defined(ANDROID_TARGET)
    #include <sys/epoll.h>
#endif

struct NSelectedList {
    int size;
#ifdef USE_EPOLL
    struct epoll_event *events;
#endif
};

struct NSelectedList *NSelectedList_createSelectedList(int size);

void NSelectedList_closeSelectedList(struct NSelectedList *selectedList);

struct NEvent *NSelectedList_getEventFromSelectedList(struct NSelectedList *selectedList, int index);

#endif //NATIVE_NSELECTEDLIST_H
