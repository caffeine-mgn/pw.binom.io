#ifndef SELECTOR_H
#define SELECTOR_H

#include "definition.h"
#include "NSelectedList.h"
#include "NEvent.h"
#include "NSocket.h"

struct NSelector {
#ifdef USE_EPOLL
    int epoll;
#endif
};

#ifdef WINDOWS_TARGET
#include <windows.h>
#endif

struct NSelector *NSelector_createSelector(int size);

void NSelector_closeSelector(struct NSelector *selector);

int NSelector_selectKeys(struct NSelector *selector, struct NSelectedList *selectedList, int timeout);

int NSelector_registryKey(struct NSelector *selector, int key, struct NEvent *event);

int NSelector_updateKey(struct NSelector *selector, int key, struct NEvent *event);

int NSelector_removeKey(struct NSelector *selector, int key);

#endif