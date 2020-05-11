//
// Created by Tousif on 2020-01-19.
//

#ifndef RP_XPOSED_FRAMEWORK_RP_NATIVEHOOK_CAMERA_H
#define RP_XPOSED_FRAMEWORK_RP_NATIVEHOOK_CAMERA_H

extern int isCameraAllowed;

#ifdef __ANDROID_API__

#define __ANDROID_API_TEMP__ __ANDROID_API__
#undef __ANDROID_API__

#endif // __ANDROID_API__

#define __ANDROID_API__ 29

#include <camera/NdkCameraCaptureSession.h>
#include <camera/NdkCameraDevice.h>
#include <camera/NdkCameraError.h>
#include <camera/NdkCameraManager.h>
#include <camera/NdkCameraMetadata.h>
#include <camera/NdkCameraMetadataTags.h>
#include <camera/NdkCameraWindowType.h>
#include <camera/NdkCaptureRequest.h>

camera_status_t (*old_ACameraCaptureSession_capture_ptr)(
        ACameraCaptureSession* session,
        /*optional*/ACameraCaptureSession_captureCallbacks* callbacks,
        int numRequests, ACaptureRequest** requests,
        /*optional*/int* captureSequenceId);

camera_status_t (*old_ACameraCaptureSession_logicalCamera_capture_ptr)(
        ACameraCaptureSession* session,
        /*optional*/ACameraCaptureSession_logicalCamera_captureCallbacks* callbacks,
        int numRequests, ACaptureRequest** requests,
        /*optional*/int* captureSequenceId);

camera_status_t (*old_ACameraCaptureSession_setRepeatingRequest_ptr)(
        ACameraCaptureSession* session,
        /*optional*/ACameraCaptureSession_captureCallbacks* callbacks,
        int numRequests, ACaptureRequest** requests,
        /*optional*/int* captureSequenceId);

camera_status_t (*old_ACameraCaptureSession_logicalCamera_setRepeatingRequest_ptr)(
        ACameraCaptureSession* session,
        /*optional*/ACameraCaptureSession_logicalCamera_captureCallbacks* callbacks,
        int numRequests, ACaptureRequest** requests,
        /*optional*/int* captureSequenceId);

camera_status_t (*old_ACameraCaptureSession_stopRepeating_ptr)(ACameraCaptureSession* session);

camera_status_t hooked_ACameraCaptureSession_capture (
        ACameraCaptureSession* session,
        /*optional*/ACameraCaptureSession_captureCallbacks* callbacks,
        int numRequests, ACaptureRequest** requests,
        /*optional*/int* captureSequenceId);

camera_status_t hooked_ACameraCaptureSession_logicalCamera_capture (
        ACameraCaptureSession* session,
        /*optional*/ACameraCaptureSession_logicalCamera_captureCallbacks* callbacks,
        int numRequests, ACaptureRequest** requests,
        /*optional*/int* captureSequenceId);

camera_status_t hooked_ACameraCaptureSession_setRepeatingRequest (
        ACameraCaptureSession* session,
        /*optional*/ACameraCaptureSession_captureCallbacks* callbacks,
        int numRequests, ACaptureRequest** requests,
        /*optional*/int* captureSequenceId);

camera_status_t hooked_ACameraCaptureSession_logicalCamera_setRepeatingRequest(
        ACameraCaptureSession* session,
        /*optional*/ACameraCaptureSession_logicalCamera_captureCallbacks* callbacks,
        int numRequests, ACaptureRequest** requests,
        /*optional*/int* captureSequenceId);

camera_status_t hooked_ACameraCaptureSession_stopRepeating(ACameraCaptureSession* session);

void rp_hookCamera();

#undef __ANDROID_API__

#ifdef __ANDROID_API_TEMP__

#define __ANDROID_API__ __ANDROID_API_TEMP__

#endif // __ANDROID_API_TEMP__

#endif //RP_XPOSED_FRAMEWORK_RP_NATIVEHOOK_CAMERA_H
