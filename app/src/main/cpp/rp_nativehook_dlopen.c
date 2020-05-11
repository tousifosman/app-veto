//
// Created by Tousif on 2019-12-26.
//

#include "rp_nativehook_dlopen.h"
#include "rp_xhook_jni.h"

#include <string.h>
#include <android/dlext.h>
#include <dlfcn.h>
#include <errno.h>

#include "dlext_namespaces.h"

#include "stdlib.h"

#define TAG "rp_nativehook_dlopen"

void rp_hookDlopen() {
    if (nativeLibDirPath != NULL) {
        int nativeLibDirLen = strlen(nativeLibDirPath);
        char *nativeLibDirPathTmp = malloc(nativeLibDirLen + 10);

        strcpy(nativeLibDirPathTmp, "^");
        strcat(nativeLibDirPathTmp, nativeLibDirPath);
        strcat(nativeLibDirPathTmp, "/.*\\.so$");
        //xhook_register("^/.*/.*\\.so$", "dlopen", hooked_dlopen, (void **) &old_dlopen_ptr);
        xhook_register(nativeLibDirPathTmp, "dlopen", hooked_dlopen, (void **) &old_dlopen_ptr);
        __android_log_print(ANDROID_LOG_DEBUG, TAG, "rp_hookDlopen -> hooked_dlopen ptr: %p, old_dlopen_ptr: %p", &hooked_dlopen, &old_dlopen_ptr);

#if __ANDROID_API__ >= 21

        xhook_register(nativeLibDirPathTmp, "android_dlopen_ext", hooked_android_dlopen_ext, (void **) &old_android_dlopen_ext_ptr);
        //xhook_register("^/.*/.*\\.so$", "android_dlopen_ext", hooked_android_dlopen_ext, (void **) &old_android_dlopen_ext_ptr);
        __android_log_print(ANDROID_LOG_DEBUG, TAG, "rp_hookDlopen -> hooked_dlopen ptr: %p, old_android_dlopen_ext: %p", &hooked_dlopen, &old_android_dlopen_ext_ptr);

#endif /* __ANDROID_API__ >= 21 */

        free(nativeLibDirPathTmp);
        __android_log_print(ANDROID_LOG_DEBUG, TAG, "hooked_dlopen -> dlopen() -> Init Inited");
    } else {
        __android_log_print(ANDROID_LOG_DEBUG, TAG, "hooked_dlopen -> dlopen() -> Init Not Inited");
    }
}

static const char *getLibPath(void) {
#ifndef __aarch64__
    return "/system/lib/";
#else
    return "/system/lib64/";
#endif
}

void* hooked_dlopen(const char* __filename, int __flag) {

    __android_log_print(ANDROID_LOG_DEBUG, TAG, "hooked_dlopen -> dlopen() -> Lib: %s", __filename);

    void *result;

//    if (globalJniEnv != NULL) {
//        (*globalJniEnv)->CallStaticVoidMethod(globalJniEnv, classSystem, methodSystem_LoadLibrary, __filename);
//        __android_log_print(ANDROID_LOG_DEBUG, TAG, "hooked_dlopen -> System.loadLibrary() -> Lib Loaded");
//    }

    /*if (__filename == NULL) {
        result = old_dlopen_ptr(__filename, __flag);
        __android_log_print(ANDROID_LOG_DEBUG, TAG, "hooked_dlopen -> dlopen(null) -> Result: %p", result);
        __android_log_print(ANDROID_LOG_DEBUG, TAG, "hooked_dlopen -> dlopen(null) -> Error: %s", dlerror());

        return result;
    }*/

#if __ANDROID_API__ >= 21

    const char *lib_path = getLibPath();

    /*char pathBuffer[1000];
    android_get_LD_LIBRARY_PATH(pathBuffer, 1000);

    __android_log_print(ANDROID_LOG_DEBUG, TAG, "hooked_dlopen -> LD_LIBRARY_PATH: %s, Native Lib Path: %s", pathBuffer, nativeLibDirPath);*/

    struct android_namespace_t *ns = android_create_namespace(
            "trustme",
            lib_path,
            //lib_path,
            (nativeLibDirPath != NULL) ? nativeLibDirPath : lib_path,
            ANDROID_NAMESPACE_TYPE_SHARED |
            ANDROID_NAMESPACE_TYPE_ISOLATED// |
            //ANDROID_NAMESPACE_TYPE_REGULAR
            // |
            //__flag
            ,
            "/system/:/data/:/vendor/",
            android_get_exported_namespace("default"));

    const android_dlextinfo dlextinfo = {
            .flags = ANDROID_DLEXT_USE_NAMESPACE,
            .library_namespace = ns,
    };

    if (old_android_dlopen_ext_ptr == NULL) {
        result = android_dlopen_ext(__filename, __flag /*| RTLD_LOCAL | RTLD_NOW*/, &dlextinfo);
    } else {
        result = old_android_dlopen_ext_ptr(__filename, __flag /*| RTLD_LOCAL | RTLD_NOW*/, &dlextinfo);
    }

    __android_log_print(ANDROID_LOG_DEBUG, TAG, "hooked_dlopen -> android_dlopen_ext() -> Result: %p", result);
    __android_log_print(ANDROID_LOG_DEBUG, TAG, "hooked_dlopen -> android_dlopen_ext() -> Error no: %d", errno);
    __android_log_print(ANDROID_LOG_DEBUG, TAG, "hooked_dlopen -> dlopen() -> Error: %s", dlerror());

#else

    result = old_dlopen_ptr(__filename, __flag);
    __android_log_print(ANDROID_LOG_DEBUG, TAG, "hooked_dlopen -> dlopen() -> Result: %p", result);
    __android_log_print(ANDROID_LOG_DEBUG, TAG, "hooked_dlopen -> dlopen() -> Error: %s", dlerror());

#endif /* __ANDROID_API__ >= 21 */

    xhook_refresh(0);

    return result;
}

#if __ANDROID_API__ >= 21

void* hooked_android_dlopen_ext(const char* __filename, int __flags, const android_dlextinfo* __info) {

    const char *lib_path = getLibPath();

    __android_log_print(ANDROID_LOG_DEBUG, TAG, "hooked_android_dlopen_ext -> dlopen() -> Lib: %s", __filename);

    void *result;

    struct android_namespace_t *ns = android_create_namespace(
            "trustme",
            lib_path,
            //lib_path,
            (nativeLibDirPath != NULL) ? nativeLibDirPath : lib_path,
            ANDROID_NAMESPACE_TYPE_SHARED |
            ANDROID_NAMESPACE_TYPE_ISOLATED// |
            //ANDROID_NAMESPACE_TYPE_REGULAR
            // |
            //__flag
            ,
            "/system/:/data/:/vendor/",
            android_get_exported_namespace("default")); //**********************************************************

    const android_dlextinfo dlextinfo = {
            .flags = ANDROID_DLEXT_USE_NAMESPACE,
            .library_namespace = ns,
    };

    result = old_android_dlopen_ext_ptr(__filename, __flags, &dlextinfo);

    __android_log_print(ANDROID_LOG_DEBUG, TAG, "hooked_android_dlopen_ext -> android_dlopen_ext() -> Result: %p", result);
    __android_log_print(ANDROID_LOG_DEBUG, TAG, "hooked_android_dlopen_ext -> android_dlopen_ext() -> Error no: %d", errno);
    __android_log_print(ANDROID_LOG_DEBUG, TAG, "hooked_android_dlopen_ext -> dlopen() -> Error: %s", dlerror());

    xhook_refresh(0);

    return result;

}

#endif /* __ANDROID_API__ >= 21 */
