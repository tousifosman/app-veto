//
// Created by Tousif on 2019-10-02.
//

#ifndef RP_XPOSED_FRAMEWORK_RP_NATIVEHOOK_SENSORS_H
#define RP_XPOSED_FRAMEWORK_RP_NATIVEHOOK_SENSORS_H

#include <jni.h>
#include <stdio.h>

#include "android/log.h"
#include "android/sensor.h"
#include "xhook.h"

extern int* sensorAccessMap;
extern int sensorAccessMapLen;
extern int isAllocatedSensorAccessMap;

void rp_hookSensors();
ssize_t hooked_ASensorEventQueue_getEvents(ASensorEventQueue* queue, ASensorEvent* events, size_t count);

#endif //RP_XPOSED_FRAMEWORK_RP_NATIVEHOOK_SENSORS_H