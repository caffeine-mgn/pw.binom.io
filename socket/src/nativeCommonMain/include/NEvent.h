#ifndef NATIVE_NEVENT_H
#define NATIVE_NEVENT_H

struct NEvent {
    int value;
};
#define FLAG_READ 0b0001
#define FLAG_WRITE 0b0010
#define FLAG_ERROR 0b0100
#define FLAG_ONCE 0b1000

struct NEvent *NEvent_malloc();

void NEvent_free(struct NEvent *event);

int NEvent_getEventFlags(struct NEvent *event);

void NEvent_setEventFlags(struct NEvent *event, int flags, int isServerFlag);

void NEvent_setEventDataFd(struct NEvent *event, int value);

void NEvent_setEventDataPtr(struct NEvent *event, void *value);

int NEvent_getEventDataFd(struct NEvent *event);

void *NEvent_getEventDataPtr(struct NEvent *event);

#endif //NATIVE_NEVENT_H
