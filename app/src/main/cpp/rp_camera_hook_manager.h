//
// Created by Tousif on 2020-01-20.
//

#ifndef RP_XPOSED_FRAMEWORK_RP_CAMERA_HOOK_MANAGER_H
#define RP_XPOSED_FRAMEWORK_RP_CAMERA_HOOK_MANAGER_H

#include "rp_nativehook_camera.h"

#ifdef __cplusplus

extern "C"
{
#endif

//    Binding functions between c and c++
void acamera_session_repeating_save(ACameraCaptureSession *session);
void acamera_session_repeating_remove(ACameraCaptureSession *session);

void updateCameraAccess();

#ifdef __cplusplus
}
#endif // __cplusplus

#endif //RP_XPOSED_FRAMEWORK_RP_CAMERA_HOOK_MANAGER_H
