//
// Created by Tousif on 2019-12-27.
//

#ifndef RP_XPOSED_FRAMEWORK_RP_XHOOK_JNI_H
#define RP_XPOSED_FRAMEWORK_RP_XHOOK_JNI_H

#include <jni.h>

extern JNIEnv *globalJniEnv;
extern jclass classSystem;
extern jmethodID methodSystem_LoadLibrary;

extern char *nativeLibDirPath;

#endif //RP_XPOSED_FRAMEWORK_RP_XHOOK_JNI_H
