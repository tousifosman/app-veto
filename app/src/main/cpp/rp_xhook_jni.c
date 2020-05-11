//
// Created by Tousif on 2019-05-13.
//

#include <stdio.h>
#include <stdlib.h>
#include <dlfcn.h>
#include "xhook.h"
#include "xh_log.h"


#include "rp_nativehook_dlopen.h"
#include "rp_nativehook_sensors.h"
#include "rp_nativehook_audio.h"
#include "rp_nativehook_camera.h"

#define TAG "rp_nativehook_jni"

#define JNI_API_DEF(f) Java_me_tousifosman_appveto_xposedhooks_nativehooks_RpNativeHookInit_##f

int* sensorAccessMap;

char *nativeLibDirPath = NULL;
jstring oldNativeLibDirJPath;

void rp_hookAudio() {
    xhook_register("^/.*/.*\\.so$",  "AAudioStream_requestStart", hooked_AAudioStream_requestStart, (void **)&(old_AAudioStream_requestStart_ptr));
    xhook_register("^/.*/.*\\.so$",  "AAudioStream_requestStop", hooked_AAudioStream_requestStop, (void **)&(old_AAudioStream_requestStop_ptr));
    xhook_register("^/.*/.*\\.so$",  "AAudioStream_requestPause", hooked_AAudioStream_requestPause, (void **)&(old_AAudioStream_requestPause_ptr));

    old_opensles_slCreateEngine_pptr = &old_opensles_slCreateEngine_ptr;
    xhook_register("^/.*/.*\\.so$", "slCreateEngine", hooked_opensles_slCreateEngine, (void **) old_opensles_slCreateEngine_pptr);
    __android_log_print(ANDROID_LOG_DEBUG, TAG, "Native Sensor Framework Hooked");
}

JNIEXPORT void JNICALL
JNI_API_DEF(hookNativeJNI)(JNIEnv *env, jobject instance) {

    __android_log_print(ANDROID_LOG_DEBUG, TAG, "hooked_dlopen -> dlopen() -> Init Hook");

    rp_hookSensors();
    rp_hookAudio();
    rp_hookCamera();
    rp_hookDlopen();

   /* if (methodSystem_LoadLibrary == NULL) {
        globalJniEnv = env;
        classSystem = (*env)->FindClass(env, "java/lang/System");
        methodSystem_LoadLibrary = (*env)->GetStaticMethodID(env, classSystem, "loadLibrary", "(Ljava/lang/String;)V");
        __android_log_print(ANDROID_LOG_DEBUG, TAG, "hooked_dlopen -> dlopen() -> JNI Load %p", methodSystem_LoadLibrary);
    }*/
}

JNIEXPORT void JNICALL
JNI_API_DEF(setNativeLibPathJNI)(JNIEnv *env, jobject instance, jstring jpath) {
    char *tmpNativeLibDirPath = (char *) (*env)->GetStringUTFChars(env, jpath, 0);
    if (nativeLibDirPath != NULL) {
        //(*env)->ReleaseStringUTFChars(env, nativeLibDirPath, oldNativeLibDirJPath);
    }
    nativeLibDirPath = tmpNativeLibDirPath;
    oldNativeLibDirJPath = jpath;
}

JNIEXPORT void JNICALL
JNI_API_DEF(updateSensorAccessJNI)(JNIEnv *env, jobject instance, jintArray sensor_access_map) {
    if (isAllocatedSensorAccessMap) {
        free(sensorAccessMap);
    }
    sensorAccessMapLen = (*env)->GetArrayLength(env, sensor_access_map);
    sensorAccessMap = malloc(sensorAccessMapLen * sizeof(int));
    (*env)->GetIntArrayRegion(env, sensor_access_map, 0, sensorAccessMapLen, sensorAccessMap);
    isAllocatedSensorAccessMap = 1;
}

JNIEXPORT void JNICALL
JNI_API_DEF(updateMicAccessJNI)(JNIEnv *env, jobject instance, jboolean allowed) {

    __android_log_print(ANDROID_LOG_DEBUG, TAG, "rp_xhook_jni -> Mic Access Updated: %d", allowed);
    isMicAllowed = allowed == JNI_TRUE? 1 : 0;
    updateMicAccess();
}

JNIEXPORT void JNICALL
JNI_API_DEF(updateCameraAccessJNI)(JNIEnv *env, jobject instance, jboolean allowed) {
    isCameraAllowed = allowed == JNI_TRUE? 1 : 0;
}

static char* getDummyString() {
    return "String from Native App";
}

JNIEXPORT jstring JNICALL
JNI_API_DEF(getTestStr)(JNIEnv *env, jobject instance) {
    return (*env)->NewStringUTF(env, getDummyString());
}