//
// Created by Tousif on 2019-10-02.
//
#include "android/sensor.h"
#include <aaudio/AAudio.h>
#include <SLES/OpenSLES.h>
#include <SLES/OpenSLES_Android.h>
#include "rp_nativehook_sensors.h"

#define TAG "rp_nativehook_sensors"

int sensorAccessMapLen = 0;
int isAllocatedSensorAccessMap = 0;


typedef ssize_t old_ASensorEventQueue_getEvents(ASensorEventQueue*,  ASensorEvent*, size_t);
old_ASensorEventQueue_getEvents old_func, *old_func_ptr, **old_func_pptr;

void rp_hookSensors() {

    old_func_pptr = &old_func_ptr;

    xhook_register("^/.*/.*\\.so$",  "ASensorEventQueue_getEvents", hooked_ASensorEventQueue_getEvents, (void **)old_func_pptr);
    __android_log_print(ANDROID_LOG_DEBUG, TAG, "Native Sensor Framework Hooked");

}





ssize_t hooked_ASensorEventQueue_getEvents(ASensorEventQueue* queue, ASensorEvent* events, size_t count) {

    //__android_log_print(ANDROID_LOG_DEBUG, TAG, "Hooked method called");
    //__android_log_print(ANDROID_LOG_DEBUG, TAG, "Sensor Type: %d, %d", events->type, events->sensor);


    if (sensorAccessMapLen > events->type && sensorAccessMap[0] == 1 && sensorAccessMap[events->type]) {
        __android_log_print(ANDROID_LOG_DEBUG, TAG, "Sensor Hooked");
        return 0;
    }

    __android_log_print(ANDROID_LOG_DEBUG, TAG, "Sensor Hook Bypassed");

    //old_ASensorEventQueue_getEvents =
    //old_func_ptr = &p;


    //return ASensorEventQueue_getEvents(queue, events, count);
    return (*old_func_ptr)(queue, events, count);
}

