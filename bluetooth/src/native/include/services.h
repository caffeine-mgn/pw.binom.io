#ifndef SERVICES_H
#define SERVICES_H

#include "definition.h"
#include "utils.h"
#include "devices.h"

#define ATTRIBUTE_TYPE_DATA_NIL		0x00
#define ATTRIBUTE_TYPE_UINT8		0x08
#define ATTRIBUTE_TYPE_UINT16		0x09
#define ATTRIBUTE_TYPE_UINT32		0x0A
#define ATTRIBUTE_TYPE_UINT64		0x0B
#define ATTRIBUTE_TYPE_UINT128		0x0C
#define ATTRIBUTE_TYPE_INT8		0x10
#define ATTRIBUTE_TYPE_INT16		0x11
#define ATTRIBUTE_TYPE_INT32		0x12
#define ATTRIBUTE_TYPE_INT64		0x13
#define ATTRIBUTE_TYPE_INT128		0x14
#define ATTRIBUTE_TYPE_UUID_UNSPEC		0x18
#define ATTRIBUTE_TYPE_UUID16		0x19
#define ATTRIBUTE_TYPE_UUID32		0x1A
#define ATTRIBUTE_TYPE_UUID128		0x1C
#define ATTRIBUTE_TYPE_TEXT_STR_UNSPEC	0x20
#define ATTRIBUTE_TYPE_TEXT_STR8		0x25
#define ATTRIBUTE_TYPE_TEXT_STR16		0x26
#define ATTRIBUTE_TYPE_TEXT_STR32		0x27
#define ATTRIBUTE_TYPE_BOOL		0x28
#define ATTRIBUTE_TYPE_SEQ_UNSPEC		0x30
#define ATTRIBUTE_TYPE_SEQ8		0x35
#define ATTRIBUTE_TYPE_SEQ16		0x36
#define ATTRIBUTE_TYPE_SEQ32		0x37
#define ATTRIBUTE_TYPE_ALT_UNSPEC		0x38
#define ATTRIBUTE_TYPE_ALT8		0x3D
#define ATTRIBUTE_TYPE_ALT16		0x3E
#define ATTRIBUTE_TYPE_ALT32		0x3F
#define ATTRIBUTE_TYPE_URL_STR_UNSPEC	0x40
#define ATTRIBUTE_TYPE_URL_STR8		0x45
#define ATTRIBUTE_TYPE_URL_STR16		0x46
#define ATTRIBUTE_TYPE_URL_STR32		0x47

START_EXTERN
struct SDPServiceAttribute {
    short unsigned int attrId;
    unsigned char type;
    void*data;
    struct SDPServiceAttribute *next;
    // sizeof
    // (sdp_data_struct::val)
};

struct SDPService {
    struct SDPService *next;
    struct SDPServiceAttribute *attributes;
};

struct SDPRecord {
    struct SDPRecord *next;
    struct SDPService *services;
    const char *name;
    const char *uuid;
};

EXTERN_DLL_EXPORT struct SDPRecord *SDP_Request(const struct NOpennedDevice *device, unsigned char *remoteAddress);

EXTERN_DLL_EXPORT void free_SDPService(struct SDPRecord *services);

END_EXTERN

#endif
