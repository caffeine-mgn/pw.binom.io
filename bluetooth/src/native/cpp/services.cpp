#include "services.h"
#include "devices.h"
#include "utils.h"
#include <cstring>

#ifdef LINUX_TARGET
#include <bluetooth/bluetooth.h>
#include <bluetooth/sdp.h>
#include <bluetooth/sdp_lib.h>
#endif

START_EXTERN
/*
void searchServices(struct NOpennedDevice*device,signed char * removeAddress){
    if (!device){
        return;
    }

    bdaddr_t target_bdaddr;
    bdaddr_t local_bdaddr;
    memcpy(&target_bdaddr,removeAddress, 6);
    memcpy(&local_bdaddr,device->address, 6);

    // Создание SDP-сессии
    sdp_session_t *session = sdp_connect(&local_bdaddr, &target_bdaddr, SDP_RETRY_IF_BUSY);
    if (!session) {
        perror("Failed to connect to SDP server");
        return;
    }

        // Поиск всех сервисов
    uuid_t svc_uuid;
    sdp_list_t *response_list = NULL;
    sdp_list_t *search_list = NULL;
    sdp_list_t *attrid_list = NULL;
    uint32_t range = 0x0000ffff;

    // Указываем UUID для поиска (Public Browse Group)
    sdp_uuid16_create(&svc_uuid, PUBLIC_BROWSE_GROUP);
    search_list = sdp_list_append(NULL, &svc_uuid);
    attrid_list = sdp_list_append(NULL, &range);

        // Выполняем поиск сервисов
    int err = sdp_service_search_attr_req(session, search_list, SDP_ATTR_REQ_RANGE, attrid_list, &response_list);
    if (err < 0) {
        perror("Failed to search for services");
        sdp_close(session);
        sdp_list_free(search_list, NULL);
        sdp_list_free(attrid_list, NULL);
        return;
    }

        // Обработка результатов
    printf("Found services on device\n");
    for (sdp_list_t *r = response_list; r; r = r->next) {
        sdp_record_t *record = (sdp_record_t *)r->data;

        // Извлекаем информацию о сервисе
        sdp_list_t *proto_list = NULL;
        if (sdp_get_access_protos(record, &proto_list) == 0) {
            for (sdp_list_t *p = proto_list; p; p = p->next) {
                sdp_list_t *pds = (sdp_list_t *)p->data;

                for (sdp_list_t *pd = pds; pd; pd = pd->next) {
                    sdp_data_t *d = (sdp_data_t *)pd->data;

                    // Проверяем, является ли протокол RFCOMM
                    if (d->dtd == SDP_UUID16 && d->val.uuid16 == RFCOMM_UUID) {
                        // Извлекаем RFCOMM-канал
                        uint8_t channel = d->val.uint8;
                        printf("RFCOMM service found on channel %d\n", channel);
                    }
                }
                sdp_list_free(pds, NULL);
            }
            sdp_list_free(proto_list, NULL);
        }
    }
}
*/
END_EXTERN
