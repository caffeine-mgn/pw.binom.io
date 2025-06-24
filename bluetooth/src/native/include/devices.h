//
// Created by subochev on 24.02.25.
//

#ifndef DEVICES_H
#define DEVICES_H
#include "definition.h"
#include "utils.h"

#ifdef WINDOWS_TARGET
//#include <windows.h>
#include <windef.h>
#endif

struct NLocalDevice {
  unsigned char address[6];
  char name[248];
  struct NLocalDevice *next;
#ifdef LINUX_TARGET
  int deviceId;
#endif
#ifdef WINDOWS_TARGET
  HANDLE device;
#endif
};

struct NOpennedDevice {
  unsigned char address[6];
#ifdef LINUX_TARGET
  char name[248];
  int deviceId;
  int socketId;
#endif
#ifdef WINDOWS_TARGET
  HANDLE device;
#endif
};

struct NRemoteDevice {
  char name[248];
  unsigned char address[6];
  const struct NRemoteDevice *next;
};

START_EXTERN

EXTERN_DLL_EXPORT const struct NLocalDevice *getLocalDevices();
EXTERN_DLL_EXPORT void detachLocalDevices(struct NLocalDevice *devices);
EXTERN_DLL_EXPORT const struct NOpennedDevice *openLocalDevice(const struct NLocalDevice *device);

EXTERN_DLL_EXPORT void closeLocalDevice(const struct NOpennedDevice *device);
EXTERN_DLL_EXPORT const int getLocalDeviceDiscoverable(const struct NOpennedDevice *device);
EXTERN_DLL_EXPORT int setLocalDeviceDiscoverable(const struct NOpennedDevice *device, const int enabled);

EXTERN_DLL_EXPORT void freeLocalDevices(const struct NLocalDevice *devices);

EXTERN_DLL_EXPORT const struct NRemoteDevice *searchRemoteDevices(const struct NOpennedDevice *device, int time);
EXTERN_DLL_EXPORT void freeRemoteDevices(const struct NRemoteDevice *devices);


END_EXTERN

#endif //DEVICES_H
