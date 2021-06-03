#include <stdatomic.h>
#ifndef NULL
#define NULL (0)
#endif
#define MIN(X, Y) (((X) < (Y)) ? (X) : (Y))

typedef struct nativeByteBuffer {
    int position;
    int limit;
    int capacity;
    signed char *data;
} nativeByteBuffer;

nativeByteBuffer *createNativeByteBuffer(int capacity) {
    nativeByteBuffer *ptr = (nativeByteBuffer *) malloc(sizeof(nativeByteBuffer));
    ptr->position = 0;
    ptr->limit = capacity;
    ptr->capacity = capacity;
    ptr->data = (signed char *) malloc(capacity);
    return ptr;
}

signed char *nativeByteBufferRefTo(nativeByteBuffer *self, int position) {
    if (position < 0 || position > self->capacity) {
        return NULL;
    }

    return &self->data[position];
}

void destroyNativeByteBuffer(nativeByteBuffer *ptr) {
    free(ptr->data);
    free(ptr);
}

int nativeByteBuffer_read(nativeByteBuffer *from, nativeByteBuffer *to) {
    int fromRemaining = from->limit - from->position;
    int toRemaining = to->limit - to->position;
    int remaining = fromRemaining;
    if (toRemaining < fromRemaining) {
        remaining = toRemaining;
    }

    memcpy(&to->data[to->position], &from->data[from->position], remaining);
    to->position += remaining;
    from->position += remaining;
    return remaining;
}

signed char nativeByteBuffer_getByteIndexed(nativeByteBuffer *from, int index) {
    return from->data[index];
}

void nativeByteBuffer_setByteIndexed(nativeByteBuffer *from, int index, signed char data) {
    from->data[index] = data;
}

signed char nativeByteBuffer_getNextByte(nativeByteBuffer *from) {
    return from->data[from->position++];
}

signed char nativeByteBuffer_putNextByte(nativeByteBuffer *from, signed char data) {
    from->data[from->position++] = data;
}

void nativeByteBuffer_copy(nativeByteBuffer *from, nativeByteBuffer *to) {
    memcpy(to->data, from->data, MIN(to->capacity, from->capacity));
    to->position = MIN(from->position, to->capacity);
    to->limit = MIN(from->limit, to->capacity);
}

void nativeByteBuffer_copyToPtr(nativeByteBuffer *from, void *ptr) {
    memcpy(ptr, from->data, from->limit - from->position);
}