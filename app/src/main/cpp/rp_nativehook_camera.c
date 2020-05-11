//
// Created by Tousif on 2020-01-19.
//

#include "rp_nativehook_camera.h"

#include <android/log.h>

#include "xhook.h"
#include "rp_camera_hook_manager.h"

#define TAG "rp_nativehook_camera"

int isCameraAllowed = 1;

void rp_hookCamera () {
    xhook_register("^/.*/.*\\.so$",  "ACameraCaptureSession_capture", hooked_ACameraCaptureSession_capture, (void **)&old_ACameraCaptureSession_capture_ptr);
    xhook_register("^/.*/.*\\.so$",  "ACameraCaptureSession_logicalCamera_capture", hooked_ACameraCaptureSession_logicalCamera_capture, (void **)&old_ACameraCaptureSession_logicalCamera_capture_ptr);

    xhook_register("^/.*/.*\\.so$",  "ACameraCaptureSession_setRepeatingRequest", hooked_ACameraCaptureSession_setRepeatingRequest, (void **)&old_ACameraCaptureSession_setRepeatingRequest_ptr);
    xhook_register("^/.*/.*\\.so$",  "ACameraCaptureSession_logicalCamera_setRepeatingRequest", hooked_ACameraCaptureSession_logicalCamera_setRepeatingRequest, (void **)&old_ACameraCaptureSession_logicalCamera_setRepeatingRequest_ptr);

    xhook_register("^/.*/.*\\.so$",  "ACameraCaptureSession_stopRepeating", hooked_ACameraCaptureSession_stopRepeating, (void **)&old_ACameraCaptureSession_stopRepeating_ptr);
}


camera_status_t hooked_ACameraCaptureSession_capture(
        ACameraCaptureSession* session,
        /*optional*/ACameraCaptureSession_captureCallbacks* callbacks,
        int numRequests, ACaptureRequest** requests,
        /*optional*/int* captureSequenceId) {

    __android_log_print(ANDROID_LOG_DEBUG, TAG, "hooked_ACameraCaptureSession_capture -> Hooked");

    return isCameraAllowed? old_ACameraCaptureSession_capture_ptr(session, callbacks, numRequests, requests, captureSequenceId) : ACAMERA_ERROR_CAMERA_DISABLED;
}

camera_status_t hooked_ACameraCaptureSession_logicalCamera_capture (
        ACameraCaptureSession* session,
        /*optional*/ACameraCaptureSession_logicalCamera_captureCallbacks* callbacks,
        int numRequests, ACaptureRequest** requests,
        /*optional*/int* captureSequenceId) {

    __android_log_print(ANDROID_LOG_DEBUG, TAG, "hooked_ACameraCaptureSession_logicalCamera_capture -> Hooked");

    return isCameraAllowed? old_ACameraCaptureSession_logicalCamera_capture_ptr(session, callbacks, numRequests, requests, captureSequenceId) : ACAMERA_ERROR_CAMERA_DISABLED;
}

camera_status_t hooked_ACameraCaptureSession_setRepeatingRequest (
        ACameraCaptureSession* session,
        /*optional*/ACameraCaptureSession_captureCallbacks* callbacks,
        int numRequests, ACaptureRequest** requests,
        /*optional*/int* captureSequenceId) {

    __android_log_print(ANDROID_LOG_DEBUG, TAG, "hooked_ACameraCaptureSession_setRepeatingRequest -> Hooked");

    acamera_session_repeating_save(session);

    return isCameraAllowed? old_ACameraCaptureSession_setRepeatingRequest_ptr(session, callbacks, numRequests, requests, captureSequenceId) : ACAMERA_ERROR_CAMERA_DISABLED;
}

camera_status_t hooked_ACameraCaptureSession_logicalCamera_setRepeatingRequest(
        ACameraCaptureSession* session,
        /*optional*/ACameraCaptureSession_logicalCamera_captureCallbacks* callbacks,
        int numRequests, ACaptureRequest** requests,
        /*optional*/int* captureSequenceId) {

    __android_log_print(ANDROID_LOG_DEBUG, TAG, "hooked_ACameraCaptureSession_logicalCamera_setRepeatingRequest -> Hooked");

    acamera_session_repeating_save(session);

    return isCameraAllowed? old_ACameraCaptureSession_logicalCamera_setRepeatingRequest_ptr(session, callbacks, numRequests, requests, captureSequenceId) : ACAMERA_ERROR_CAMERA_DISABLED;
}

camera_status_t hooked_ACameraCaptureSession_stopRepeating(ACameraCaptureSession* session) {

    __android_log_print(ANDROID_LOG_DEBUG, TAG, "hooked_ACameraCaptureSession_stopRepeating -> Hooked");

    acamera_session_repeating_remove(session);

    return old_ACameraCaptureSession_stopRepeating_ptr(session);
}