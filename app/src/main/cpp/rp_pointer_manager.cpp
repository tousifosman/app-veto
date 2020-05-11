//
// Created by Tousif on 2019-12-23.
//

#include "rp_pointer_manager.h"

#include "android/log.h"

#include "rp_backtrace.h"
#include "rp_nativehook_audio.h"

#define TAG "rp_nativehook_pointer_manager"

extern int isMicAllowed;

//std::mutex mtx_openSlesDataMap;



OpenSlesData::OpenSlesData() {
    engineItf = nullptr;
    recordItf = nullptr;
    oldOpenslesFunctionsPTR = new opensles_old_functions;
    __android_log_print(ANDROID_LOG_WARN, TAG, "xhook_pointer_manager -> OpenSlesData() -> No Owner Engine");
    printBackTrace(30);
}

OpenSlesData::OpenSlesData(SLObjectItf engineItf) {
    this->engineItf = engineItf;
    oldOpenslesFunctionsPTR = new opensles_old_functions;
}

OpenSlesData::~OpenSlesData() {
    __android_log_print(ANDROID_LOG_ERROR, TAG, "xhook_pointer_manager -> ~OpenSlesData()");
    //delete oldOpenslesFunctionsPTR;
    printBackTrace(30);
}

void* OpenSlesData::allocateMemory(SLObjectItf engineItf, size_t __byte_count) {

    __android_log_print(ANDROID_LOG_DEBUG, TAG, "xhook_pointer_manager -> before obj memory allocation");

    this->engineItf = engineItf;
    void *p = malloc(__byte_count);
    memoryPointerSet.insert(p);

    return p;
}

void OpenSlesData::feeAllMemory() {
    __android_log_print(ANDROID_LOG_DEBUG, TAG, "xhook_pointer_manager -> before free memory");
    for (auto iter = memoryPointerSet.begin();iter != memoryPointerSet.end(); ++iter) {
        free(*iter);
    }
}

opensles_old_functions* OpenSlesData::getOldOpenslesFunctionsPTR() {
    return oldOpenslesFunctionsPTR;
}

const void* OpenSlesData::getEngineItfPtr() {
    return engineItf;
}

extern "C"
void* isEngineExists(SLObjectItf engineItf) {

    auto iterator = openSlesDataMap.find(engineItf);

    if (iterator == openSlesDataMap.end()) {
        return nullptr;
    }
    return &iterator->second;
}

extern "C"
void* opensles_allocate_memory(SLObjectItf engineItf, size_t __byte_count) {

    auto *dataPTR = static_cast<OpenSlesData *>(isEngineExists(engineItf));

    auto iterator = openSlesDataMap.find(engineItf);

    if (dataPTR == nullptr) {
        OpenSlesData data(engineItf);
        dataPTR = &data;
        openSlesDataMap[engineItf] = data;
    } else {
        dataPTR = &iterator->second;
    }

    void *p = dataPTR->allocateMemory(engineItf, __byte_count);

    return p;
}

extern "C"
void opensles_free_dynamic_memory(SLObjectItf engineItf) {
    OpenSlesData data = openSlesDataMap[engineItf];
    data.feeAllMemory();
}

extern "C"
void opensles_setObjectOwnership(SLObjectItf engineItf, void * objPTR) {
    __android_log_print(ANDROID_LOG_DEBUG, TAG, "xhook_audio_sles -> opensles_setObjectOwnership -> Owner SLObjectItf: %p, objPtr: %p", engineItf, objPTR);
    openSlesObjectOwnershipMap[objPTR] = engineItf;
}


extern "C"
SLObjectItf opensles_getObjectOwnership(void * objPTR) {
    SLObjectItf engineItf = openSlesObjectOwnershipMap[objPTR];
    __android_log_print(ANDROID_LOG_DEBUG, TAG, "xhook_audio_sles -> opensles_getObjectOwnership -> Owner SLObjectItf: %p, objPtr: %p", engineItf, objPTR);
    return engineItf;
}

extern "C"
struct opensles_old_functions * opensles_getOldFunctionPTR(SLObjectItf engineItf) {
    return openSlesDataMap[engineItf].getOldOpenslesFunctionsPTR();
}

extern "C"
void opensles_saveRecordStatePtr(SLObjectItf engineItf, SLRecordItf recordItf) {
    openSlesDataMap[engineItf].recordItf = recordItf;
}

extern "C"
void opensles_saveRecordState(SLObjectItf engineItf, SLuint32 recordStat) {
    OpenSlesData *data = &openSlesDataMap[engineItf];
    data->recordState = recordStat;
    if (recordStat != SL_RECORDSTATE_RECORDING) {
        data->micBlocked = false;
    }
    __android_log_print(ANDROID_LOG_DEBUG, TAG, "xhook_pointer_manager -> opensles_saveRecordState -> Saved State: %d, Owner Engine: %p", data->recordState, engineItf);
}

extern "C"
void aaudio_saveAudioStream(AAudioStream *stream) {
    aaudioStreamSet.insert(stream);
    __android_log_print(ANDROID_LOG_DEBUG, TAG, "xhook_pointer_manager -> aaudio_saveAudioStream -> Saved Stream : %p", stream);
}

extern "C"
void aaudio_removeAudioStream(AAudioStream *stream) {
    aaudioStreamSet.erase(stream);
    __android_log_print(ANDROID_LOG_DEBUG, TAG, "xhook_pointer_manager -> aaudio_removeAudioStream -> Removed Stream : %p", stream);
}

extern "C"
void opensles_updateMicAccess() {

    __android_log_print(ANDROID_LOG_DEBUG, TAG, "xhook_pointer_manager -> Updating Mic access for Opensl ES");

    //mtx_openSlesDataMap.lock();

    for (auto& it: openSlesDataMap) {
        OpenSlesData *openSlesDataPTR = &(it.second);
        //OpenSlesData *openSlesDataPTR = &openSlesDataMap[it.first];

        if (openSlesDataPTR->recordItf != nullptr) {
            __android_log_print(ANDROID_LOG_DEBUG, TAG, "xhook_pointer_manager -> RecordItf is initialized for EngineItf: %p, Record State: %d", openSlesDataPTR->getEngineItfPtr(), openSlesDataPTR->recordState);
            if (openSlesDataPTR->recordState == SL_RECORDSTATE_RECORDING) {
                if (!isMicAllowed) {
                    if (!openSlesDataPTR->micBlocked) {
                        openSlesDataPTR->micBlocked = true;

                        __android_log_print(ANDROID_LOG_DEBUG, TAG,
                                            "xhook_pointer_manager -> opensles_updateMicAccess -> block before");

                        openSlesDataPTR->getOldOpenslesFunctionsPTR()->SetRecordState(
                                openSlesDataPTR->recordItf, SL_RECORDSTATE_PAUSED);

                        __android_log_print(ANDROID_LOG_DEBUG, TAG,
                                            "xhook_pointer_manager -> opensles_updateMicAccess -> block after");
                    } else {
                        __android_log_print(ANDROID_LOG_DEBUG, TAG, "xhook_pointer_manager -> opensles_updateMicAccess -> Previously blocked");
                    }
                } else {
                    __android_log_print(ANDROID_LOG_DEBUG, TAG,
                                        "xhook_pointer_manager -> opensles_updateMicAccess -> Previously Unblocked, %d", openSlesDataPTR->micBlocked);
                    if (openSlesDataPTR->micBlocked) {

                        openSlesDataPTR->micBlocked = false;

                        SLresult result = openSlesDataPTR->getOldOpenslesFunctionsPTR()->SetRecordState(
                                openSlesDataPTR->recordItf, SL_RECORDSTATE_RECORDING);

                        __android_log_print(ANDROID_LOG_DEBUG, TAG,
                                            "xhook_pointer_manager -> opensles_updateMicAccess -> Unblock, %d", result);
                    }
                    __android_log_print(ANDROID_LOG_DEBUG, TAG,
                                        "xhook_pointer_manager -> opensles_updateMicAccess -> Previously Unblocked");
                }
            } else {
                __android_log_print(ANDROID_LOG_DEBUG, TAG, "xhook_pointer_manager -> opensles_updateMicAccess -> Not Recording");
            }
        } else {
            __android_log_print(ANDROID_LOG_DEBUG, TAG, "xhook_pointer_manager -> RecordItf is not initialized for EngineItf: %p", openSlesDataPTR->getEngineItfPtr());
        }
    }

    __android_log_print(ANDROID_LOG_DEBUG, TAG, "xhook_pointer_manager -> Updated Mic access for Opensl ES");

    if (!isMicAllowed) {
        __android_log_print(ANDROID_LOG_DEBUG, TAG, "xhook_pointer_manager -> Allow Mic access for AAudio");
        for (auto &it: aaudioStreamSet) {
            old_AAudioStream_requestPause_ptr(it);
        }
    } else {
        __android_log_print(ANDROID_LOG_DEBUG, TAG, "xhook_pointer_manager -> Block Mic access for AAudio");
        for (auto &it: aaudioStreamSet) {
            old_AAudioStream_requestStart_ptr(it) ;
        }
    }
}