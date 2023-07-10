#ifndef SELECTOR_H
#define SELECTOR_H

#include "definition.h"
#include "SelectedList.h"
#include "Event.h"
#include "Socket.h"

struct Selector {
#ifdef USE_EPOLL
    int epoll;
#endif
};

#ifdef WINDOWS_TARGET
#include <windows.h>
#endif

int getInternalError() {
#ifdef WINDOWS_TARGET
  return GetLastError();
#else
  return 0;
#endif
}

struct Selector *createSelector(int size);

void closeSelector(struct Selector *selector);

int selectKeys(struct Selector *selector, struct SelectedList *selectedList, int timeout);

int registryKey(struct Selector *selector, int key, struct Event *event);

int updateKey(struct Selector *selector, int key, struct Event *event);

int removeKey(struct Selector *selector, int key);

#endif