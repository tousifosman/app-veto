//
// Created by Tousif on 2019-05-13.
//
#include <jni.h>
#include <stdio.h>
#include "xhook.h"
#include "xh_log.h"

#define JNI_API_DEF(f) Java_me_tousifosman_appveto_xposedhooks_nativehooks_RpXhookInit_##f

static char* hooked_getDummyString() {
    return (void *) "String Hooked";
}

JNIEXPORT void JNICALL
JNI_API_DEF(hookNative)(JNIEnv *env, jobject instance) {

    FILE *fp;

    if(NULL == (fp = fopen("/proc/self/cmdline", "r")))
    {
        XH_LOG_ERROR("fopen /proc/self/maps failed");
        return;
    }

    char line[512];
    fgets(line, sizeof(line), fp);
    __android_log_print(ANDROID_LOG_DEBUG, "xposed_xhook", "Current Process Name from proc: %s", line);

    xhook_register(".*/libnative-lib\\.so$", "_Z15getNativeStringv", hooked_getDummyString, NULL);

}

static char* getDummyString() {
    return "String from Native App";
}

JNIEXPORT jstring JNICALL
JNI_API_DEF(getTestStr)(JNIEnv *env, jobject instance) {
    return (*env)->NewStringUTF(env, getDummyString());
}