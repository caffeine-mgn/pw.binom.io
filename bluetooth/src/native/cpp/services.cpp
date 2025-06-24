#include "services.h"
#include "devices.h"
#include "utils.h"
#include <cstring>
#include <errno.h>

#ifdef LINUX_TARGET
#include <bluetooth/bluetooth.h>
#include <bluetooth/sdp.h>
#include <bluetooth/sdp_lib.h>
#include <stdlib.h>
#endif

void freeAttribute(struct SDPServiceAttribute *d) {
    if (d == NULL) return;
    if (d->data != NULL) {
        free(d->data);
    }
    if (d->next != NULL) {
        free(d->next);
    }
    free(d);
}

#ifdef LINUX_TARGET
struct SDPServiceAttribute *createAttribute(sdp_data_t *d) {
    struct SDPServiceAttribute *nextAttribute = (struct SDPServiceAttribute *) malloc(sizeof(SDPServiceAttribute));

    nextAttribute->attrId = d->attrId;
    nextAttribute->type = d->dtd;
    nextAttribute->next = NULL;

    switch (d->dtd) {
        case SDP_DATA_NIL: {
            nextAttribute->type = ATTRIBUTE_TYPE_DATA_NIL;
            nextAttribute->data = NULL;
            break;
        }
        case SDP_UINT8: {
            nextAttribute->type = ATTRIBUTE_TYPE_UINT8;
            nextAttribute->data = malloc(1);
            memcpy(nextAttribute->data, &d->val, 1);
            break;
        }
        case SDP_UINT16: {
            nextAttribute->type = ATTRIBUTE_TYPE_UINT16;
            nextAttribute->data = malloc(2);
            memcpy(nextAttribute->data, &d->val, 2);
            break;
        }
        case SDP_UINT32: {
            nextAttribute->type = ATTRIBUTE_TYPE_UINT32;
            nextAttribute->data = malloc(4);
            memcpy(nextAttribute->data, &d->val, 4);
            break;
        }
        case SDP_UINT64: {
            nextAttribute->type = ATTRIBUTE_TYPE_UINT64;
            nextAttribute->data = malloc(8);
            memcpy(nextAttribute->data, &d->val, 8);
            break;
        }
        case SDP_UINT128: {
            nextAttribute->type = ATTRIBUTE_TYPE_UINT128;
            nextAttribute->data = malloc(16);
            memcpy(nextAttribute->data, &d->val, 16);
            break;
        }
        case SDP_INT8: {
            nextAttribute->type = ATTRIBUTE_TYPE_INT8;
            nextAttribute->data = malloc(1);
            memcpy(nextAttribute->data, &d->val, 1);
            break;
        }
        case SDP_INT16: {
            nextAttribute->type = ATTRIBUTE_TYPE_INT16;
            nextAttribute->data = malloc(2);
            memcpy(nextAttribute->data, &d->val, 2);
            break;
        }
        case SDP_INT32: {
            nextAttribute->type = ATTRIBUTE_TYPE_INT32;
            nextAttribute->data = malloc(4);
            memcpy(nextAttribute->data, &d->val, 4);
            break;
        }
        case SDP_INT64: {
            nextAttribute->type = ATTRIBUTE_TYPE_INT64;
            nextAttribute->data = malloc(8);
            memcpy(nextAttribute->data, &d->val, 8);
            break;
        }
        case SDP_INT128: {
            nextAttribute->type = ATTRIBUTE_TYPE_INT128;
            nextAttribute->data = malloc(8);
            memcpy(nextAttribute->data, &d->val, 8);
            break;
        }
        case SDP_UUID_UNSPEC: {
            nextAttribute->type = ATTRIBUTE_TYPE_UUID_UNSPEC;
            int size = 37;
            nextAttribute->data = malloc(size);
            sdp_uuid2strn(&d->val.uuid, (char *) nextAttribute->data, size);
            break;
        }
        case SDP_UUID16: {
            nextAttribute->type = ATTRIBUTE_TYPE_UUID16;
            int size = 37;
            nextAttribute->data = malloc(size);
            sdp_uuid2strn(&d->val.uuid, (char *) nextAttribute->data, size);
            break;
        }
        case SDP_UUID32: {
            nextAttribute->type = ATTRIBUTE_TYPE_UUID32;
            int size = 37;
            nextAttribute->data = malloc(size);
            sdp_uuid2strn(&d->val.uuid, (char *) nextAttribute->data, size);
            break;
        }
        case SDP_UUID128: {
            nextAttribute->type = ATTRIBUTE_TYPE_UUID128;
            int size = 37;
            nextAttribute->data = malloc(size);
            sdp_uuid2strn(&d->val.uuid, (char *) nextAttribute->data, size);
            break;
        }
        case SDP_TEXT_STR_UNSPEC: break;
        case SDP_TEXT_STR8: break;
        case SDP_TEXT_STR16: break;
        case SDP_TEXT_STR32: break;
        case SDP_BOOL: break;
        case SDP_SEQ_UNSPEC: break;
        case SDP_SEQ8: break;
        case SDP_SEQ16: break;
        case SDP_SEQ32: break;
        case SDP_ALT_UNSPEC: break;
        case SDP_ALT8: break;
        case SDP_ALT16: break;
        case SDP_ALT32: break;
        case SDP_URL_STR_UNSPEC: break;
        case SDP_URL_STR8: break;
        case SDP_URL_STR16: break;
        case SDP_URL_STR32: break;
    }
    return nextAttribute;
}
#endif

START_EXTERN
EXTERN_DLL_EXPORT struct SDPRecord *SDP_Request(const struct NOpennedDevice *device, unsigned char *remoteAddress) {
    struct SDPRecord *result = NULL;
    // fprintf(stdout, "!!!! #1\n");
    // fflush(stdout);
#ifdef LINUX_TARGET
    fprintf(stdout, "!!!! #2\n");
    fflush(stdout);

    char addr_str[18];
    // bdaddr_t adapter_addr;
    // copyAddressAndReverseBytes((unsigned char *) &device->address, (unsigned char *) &adapter_addr);


    // ba2str(&adapter_addr, addr_str);
    // fprintf(stdout, "!!!! #4 %s\n", addr_str);
    // fflush(stdout);

    bdaddr_t target;
    copyAddressAndReverseBytes(remoteAddress, (unsigned char *) &target);


    // str2ba("8A:88:4B:21:20:34", &adapter_addr);

    // ba2str(&adapter_addr, addr_str);
    // fprintf(stdout, "!!!! #5 %s\n", addr_str);
    // fflush(stdout);

    sdp_session_t *session = sdp_connect((bdaddr_t *) &device->address, &target, SDP_RETRY_IF_BUSY);
    if (!session) {
        perror("SDP connection");
        fprintf(stdout, "!!!! #3 errno=%d\n",errno);
        fflush(stdout);
        return NULL;
    }

    // Создаем SDP запрос
    uuid_t svc_uuid;
    sdp_list_t *response_list;
    sdp_record_t *record;
    // sdp_list_t *proto_list;


    /**
     * PUBLIC_BROWSE_GROUP
     * SERIAL_PORT_SVCLASS_ID
     */
    // UUID для поиска всех сервисов
    uuid_t public_browse_group;
    sdp_uuid16_create(&public_browse_group, PUBLIC_BROWSE_GROUP);

    // Создаём список для поиска
    sdp_list_t *search_list = sdp_list_append(NULL, &public_browse_group);

    // sdp_uuid16_create(&public_browse_group, SERIAL_PORT_SVCLASS_ID);
    // search_list = sdp_list_append(search_list, &public_browse_group);

    /*
    uint32_t range = 0x0000ffff;
    sdp_list_t *attrid_list = sdp_list_append(NULL, &range);
    */

    uint32_t range = 0x0000ffff;
    // Запрашиваем все атрибуты (диапазон 0x0000-0xffff)
    sdp_list_t *attrid_list = sdp_list_append(0, &range);

    // sdp_service_search_req(session,search_list,256,&response_list);
    // Выполняем запрос
    if (sdp_service_search_attr_req(session, search_list, SDP_ATTR_REQ_RANGE, attrid_list, &response_list) < 0) {
    // if (sdp_service_search_req(session, search_list, 256, &response_list)) {
        perror("SDP search");
        sdp_close(session);
        return (struct SDPRecord *) 2;
    }
    fprintf(stdout, "!!!!Service found:\n");
    // Парсим результаты
    for (sdp_list_t *r = response_list; r; r = r->next) {
        record = (sdp_record_t *) r->data;
        sdp_list_t *proto_list;

        if (sdp_get_access_protos(record, &proto_list)) {
            fprintf(stdout, "Failed to get access protocols\n");
            proto_list=NULL;
            // continue;
        }

        struct SDPRecord *nextRecord = (struct SDPRecord *) malloc(sizeof(SDPRecord));
        nextRecord->next = result;
        nextRecord->services = NULL;
        result = nextRecord;

        sdp_data_t *sname = sdp_data_get(record, SDP_ATTR_SVCNAME_PRIMARY);
        if (sname) {
            int nameLen = strlen(sname->val.str);
            nextRecord->name = (char *) malloc(nameLen + 1);
            strcpy((char *) nextRecord->name, sname->val.str);
            // memcpy((void *) nextRecord->name, sname->val.str, nameLen);
        } else {
            nextRecord->name = NULL;
        }

        // Извлечение UUID сервиса
        // char uuid_str[37] = "Unknown";
        uuid_t svc_uuid;
        if (sdp_get_service_id(record, &svc_uuid) == 0) {
            int size = 37;
            nextRecord->uuid = (char *) malloc(size);
            // memset((void *) nextRecord->uuid, size, 1);
            sdp_uuid2strn(&svc_uuid, (char *) nextRecord->uuid, size);
            fprintf(stdout, "UUID of service got!\n");
        } else {
            fprintf(stdout, "Can't get service UUID\n");
            nextRecord->uuid = NULL;
        }

        /*
        struct SDPService *nextService = (struct SDPService *) malloc(sizeof(SDPService));
        nextService->next = result->services;
        nextService->attributes = NULL;
        result->services = nextService;
        fprintf(stdout, "Iterate attrs....\n");
        for (sdp_list_t *pd = record->attrlist; pd; pd = pd->next) {
            fprintf(stdout, "Found Attribute!\n");
            sdp_data_t *d = (sdp_data_t *) pd->data;

            struct SDPServiceAttribute *nextAttribute = createAttribute((sdp_data_t *) pd->data);
            nextAttribute->next = nextService->attributes;
            nextService->attributes = nextAttribute;
        }
        */
        if (proto_list != NULL) {
            // Выводим информацию о сервисе
            fprintf(stdout, "Service found:\n");
            for (sdp_list_t *p = proto_list; p; p = p->next) {
                sdp_list_t *pds = (sdp_list_t *) p->data;
                struct SDPService *nextService = (struct SDPService *) malloc(sizeof(SDPService));
                nextService->next = result->services;
                nextService->attributes = NULL;
                result->services = nextService;
                for (sdp_list_t *pd = pds; pd; pd = pd->next) {
                    sdp_data_t *d = (sdp_data_t *) pd->data;

                    struct SDPServiceAttribute *nextAttribute = createAttribute((sdp_data_t *) pd->data);
                    nextAttribute->next = nextService->attributes;
                    nextService->attributes = nextAttribute;

                    fprintf(stdout, "Protocol: %d, dtd: %d attrId: %d\n", (int) d->val.uint16, (int) d->dtd,
                            (int) d->attrId);
                }
            }

            sdp_list_free(proto_list, 0);
        }
    }

    // Освобождаем ресурсы
    sdp_list_free(response_list, 0);
    sdp_list_free(search_list, 0);
    sdp_list_free(attrid_list, 0);
    sdp_close(session);
    fflush(stdout);
#endif

    return result;
}

EXTERN_DLL_EXPORT void free_SDPService(struct SDPRecord *services) {
    const struct SDPRecord *recordIterator = services;
    while (recordIterator != 0) {
        const struct SDPRecord *record = recordIterator;
        if (record->name != NULL) {
            free((void *) record->name);
        }
        if (record->uuid != NULL) {
            free((void *) record->uuid);
        }
        const struct SDPService *serviceIterator = record->services;
        while (serviceIterator != NULL) {
            const struct SDPService *service = serviceIterator;
            const struct SDPServiceAttribute *attributeIterator = service->attributes;
            while (attributeIterator != NULL) {
                const struct SDPServiceAttribute *attribute = attributeIterator;
                attributeIterator = attributeIterator->next;
                delete attribute;
            }
            serviceIterator = serviceIterator->next;
            delete service;
        }
        recordIterator = recordIterator->next;
        delete record;
    }
}

END_EXTERN
