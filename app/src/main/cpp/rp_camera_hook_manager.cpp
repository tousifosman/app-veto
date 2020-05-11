//
// Created by Tousif on 2020-01-20.
//

#include "rp_camera_hook_manager.h"

#include <set>

// bool wasCameraBlocked = false;

std::set<ACameraCaptureSession *> cameraCaptureSessionRepeatingSet;

void acamera_session_repeating_save(ACameraCaptureSession *session) {
    cameraCaptureSessionRepeatingSet.insert(session);
}

void acamera_session_repeating_remove(ACameraCaptureSession *session) {
    cameraCaptureSessionRepeatingSet.erase(session);
}

void updateCameraAccess() {
    if (isCameraAllowed) {
        /*if (wasCameraBlocked) {
            wasCameraBlocked = false;
        }*/
        return;
    }

    for(auto& it: cameraCaptureSessionRepeatingSet) {
        old_ACameraCaptureSession_stopRepeating_ptr(it);
        // wasCameraBlocked = true;
    }
}