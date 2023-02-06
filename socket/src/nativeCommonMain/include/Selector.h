#ifndef SELECTOR_H
#define SELECTOR_H

#include "definition.h"
#include "SelectedList.h"
#include "Event.h"

#ifndef SOCKET
#define SOCKET int
#endif

struct Selector {
#ifdef USE_EPOLL
    int epoll;
#endif
};

struct Selector *createSelector(int size);

void closeSelector(struct Selector *selector);

int selectKeys(struct Selector *selector, struct SelectedList *selectedList, int timeout);

int registryKey(struct Selector *selector, SOCKET key, struct Event *event);

int updateKey(struct Selector *selector, SOCKET key, struct Event *event);

int removeKey(struct Selector *selector, SOCKET key);

#endif