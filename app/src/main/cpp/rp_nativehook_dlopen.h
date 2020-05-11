//
// Created by Tousif on 2019-12-26.
//

#ifndef RP_XPOSED_FRAMEWORK_RP_NATIVEHOOK_DLOPEN_H
#define RP_XPOSED_FRAMEWORK_RP_NATIVEHOOK_DLOPEN_H

#include <jni.h>
#include <dlfcn.h>
#include "android/log.h"

#include "dlext_namespaces.h"
#include "xhook.h"

typedef void* old_dlopen(const char* __filename, int __flag);
static old_dlopen *old_dlopen_ptr = NULL;

typedef void* old_android_dlopen_ext(const char* __filename, int __flags, const android_dlextinfo* __info);
static old_android_dlopen_ext *old_android_dlopen_ext_ptr = NULL;

void rp_hookDlopen();
void* hooked_dlopen(const char* __filename, int __flag);

#if __ANDROID_API__ >= 21

void* hooked_android_dlopen_ext(const char* __filename, int __flags, const android_dlextinfo* __info);

#endif /* __ANDROID_API__ >= 21 */

#endif //RP_XPOSED_FRAMEWORK_RP_NATIVEHOOK_DLOPEN_H
