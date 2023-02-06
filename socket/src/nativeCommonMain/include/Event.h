#ifndef NATIVE_EVENT_H
#define NATIVE_EVENT_H

struct Event {
    int value;
};
#define FLAG_READ 0b0001
#define FLAG_WRITE 0b0010
#define FLAG_ERROR 0b0100
#define FLAG_ONCE 0b1000

struct Event *mallocEvent();

void freeEvent(struct Event *event);

int getEventFlags(struct Event *event);

void setEventFlags(struct Event *event, int flags, int isServerFlag);

void setEventDataFd(struct Event *event, int value);

void setEventDataPtr(struct Event *event, void *value);

int getEventDataFd(struct Event *event);

void *getEventDataPtr(struct Event *event);

#endif //NATIVE_EVENT_H
