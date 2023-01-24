//
// Created by subochev on 21.01.23.
//

#ifndef NATIVE_SELECTEDLIST_H
#define NATIVE_SELECTEDLIST_H

#include "Event.h"
#include "wepoll.h"
#include "definition.h"

struct SelectedList {
    int size;
#ifdef USE_EPOLL
    struct epoll_event *events;
#endif
};

struct SelectedList *createSelectedList(int size);

void closeSelectedList(struct SelectedList *selectedList);

struct Event *getEventFromSelectedList(struct SelectedList *selectedList, int index);

#endif //NATIVE_SELECTEDLIST_H
