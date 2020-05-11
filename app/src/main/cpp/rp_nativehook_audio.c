//
// Created by Tousif on 2019-12-02.
//


#include "rp_nativehook_audio.h"
#include "rp_pointer_manager.h"

#define TAG "rp_nativehook_audio"

int isMicAllowed = 1;

//void *ppp;

SLresult hooked_opensles_slCreateEngine(
        SLObjectItf *pEngine,
        SLuint32 numOptions,
        const SLEngineOption *pEngineOptions,
        SLuint32 numInterfaces,
        const SLInterfaceID *pInterfaceIds,
        const SLboolean *pInterfaceRequired) {

    __android_log_print(ANDROID_LOG_DEBUG, TAG, "xhook_audio_sles -> slCreateEngine -> Hooked");

    struct SLObjectItf_ *newSLObjectItfPTR, **pEngineTemp;

    SLresult sLresult = (*old_opensles_slCreateEngine_ptr)((SLObjectItf *) &pEngineTemp, numOptions, pEngineOptions, numInterfaces, pInterfaceIds, pInterfaceRequired);

    __android_log_print(ANDROID_LOG_DEBUG, TAG, "xhook_audio_sles -> slCreateEngine -> Org Engine: %p, Realize: %p, GetInterface:%p, Destroy: %p",
            *pEngineTemp, (*pEngineTemp)->Realize, (*pEngineTemp)->GetInterface, (*pEngineTemp)->Destroy);

    if (!isEngineExists((SLObjectItf)pEngineTemp)) {
        struct opensles_old_functions *oldFunctionsPTR = opensles_getOldFunctionPTR((SLObjectItf) pEngineTemp);

        oldFunctionsPTR->Realize = (*pEngineTemp)->Realize;
        oldFunctionsPTR->GetInterfaceEngine = (*pEngineTemp)->GetInterface;
        oldFunctionsPTR->Destroy = (*pEngineTemp)->Destroy;

        newSLObjectItfPTR = opensles_allocate_memory((SLObjectItf) pEngineTemp, sizeof(struct SLObjectItf_));

        //__android_log_print(ANDROID_LOG_DEBUG, TAG, "xhook_audio_sles -> Allocated Memory Address %p", newSLObjectItfPTR);

        // Copy structure value
        *newSLObjectItfPTR = **pEngineTemp;

        //__android_log_print(ANDROID_LOG_DEBUG, TAG, "xhook_audio_sles -> before modify");

        newSLObjectItfPTR->Realize = &hooked_opensles_Realize;
        newSLObjectItfPTR->GetInterface = &hooked_opensles_GetInterface;
        newSLObjectItfPTR->Destroy = &hooked_opensles_Destroy;

        *pEngineTemp = newSLObjectItfPTR;

        opensles_setObjectOwnership((SLObjectItf)pEngineTemp, pEngineTemp);

    } else {
        __android_log_print(ANDROID_LOG_DEBUG, TAG, "xhook_audio_sles -> slCreateEngine -> Engine already exists");
    }

    *pEngine = (SLObjectItf) pEngineTemp;

    __android_log_print(ANDROID_LOG_DEBUG, TAG, "xhook_audio_sles -> slCreateEngine -> Mod Engine: %p, Realize: %p, GetInterface:%p, Destroy: %p",
            *pEngineTemp, (*pEngineTemp)->Realize, (*pEngineTemp)->GetInterface, (*pEngineTemp)->Destroy);
   // __android_log_print(ANDROID_LOG_DEBUG, TAG, "xhook_audio_sles -> after modify");

    return sLresult;
}

SLresult hooked_opensles_Realize (SLObjectItf self, SLboolean async) {

    /**
     * Although Realize can be called from different object but this is working because we have hooked this for only one type.
     */

    __android_log_print(ANDROID_LOG_DEBUG, TAG, "xhook_audio_sles -> Realize -> Hooked");

    SLresult sLresult = opensles_getOldFunctionPTR(opensles_getObjectOwnership((void *) self))->Realize(self, async);

    return sLresult;
}

SLresult hooked_opensles_GetInterface(SLObjectItf self, const SLInterfaceID iid, void *pInterface) {

    __android_log_print(ANDROID_LOG_DEBUG, TAG, "xhook_audio_sles -> GetInterface Hooked");

    SLresult sLresult;

    SLObjectItf ownerEngine = opensles_getObjectOwnership((void *) self);
    struct opensles_old_functions *oldFunctions = opensles_getOldFunctionPTR(ownerEngine);

    if (iid == SL_IID_ENGINE) {
        struct SLEngineItf_ *newSLEngineItfPTR, **pEngineItfTemp;

        __android_log_print(ANDROID_LOG_DEBUG, TAG, "xhook_audio_sles -> GetInterface -> EngineItf -> Hooked, Org Ptr: %p", oldFunctions->GetInterfaceEngine);

        sLresult = oldFunctions->GetInterfaceEngine(self, iid, &pEngineItfTemp);

        __android_log_print(ANDROID_LOG_DEBUG, TAG, "xhook_audio_sles -> GetInterface -> Org EngineItf: %p, CreateAudioRecorder: %p, Old Func: %p", *pEngineItfTemp, (*pEngineItfTemp)->CreateAudioRecorder, oldFunctions);

        if ((*pEngineItfTemp)->CreateAudioRecorder != &hooked_opensles_CreateAudioRecorder) {

            oldFunctions->CreateAudioRecorder = (*pEngineItfTemp)->CreateAudioRecorder;

            //ppp = (*pEngineItfTemp)->CreateAudioRecorder;

            newSLEngineItfPTR = opensles_allocate_memory(self, sizeof(struct SLEngineItf_));

            // Copy structure value
            *newSLEngineItfPTR = **pEngineItfTemp;

            newSLEngineItfPTR->CreateAudioRecorder = &hooked_opensles_CreateAudioRecorder;

            *pEngineItfTemp = newSLEngineItfPTR;

            opensles_setObjectOwnership(ownerEngine, pEngineItfTemp);

        } else {
            __android_log_print(ANDROID_LOG_DEBUG, TAG, "xhook_audio_sles -> GetInterface -> EngineItf already exists");
        }

        *(SLEngineItf*)pInterface = (SLEngineItf) pEngineItfTemp;

        __android_log_print(ANDROID_LOG_DEBUG, TAG, "xhook_audio_sles -> GetInterface -> Mod EngineItf: %p, CreateAudioRecorder: %p", *pEngineItfTemp, (*pEngineItfTemp)->CreateAudioRecorder);

    } else if(iid == SL_IID_RECORD) {
        struct SLRecordItf_ **pRecordItfTemp, *newSLRecordItfPTR;

        __android_log_print(ANDROID_LOG_DEBUG, TAG, "xhook_audio_sles -> GetInterface for Record Hooked");

        sLresult = oldFunctions->GetInterfaceRecord(self, iid, &pRecordItfTemp);

        __android_log_print(ANDROID_LOG_DEBUG, TAG, "xhook_audio_sles -> GetInterface -> SLRecordItf: after original call");

        oldFunctions->SetRecordState = (*pRecordItfTemp)->SetRecordState;

        newSLRecordItfPTR = opensles_allocate_memory(ownerEngine, sizeof(struct SLRecordItf_));

        *newSLRecordItfPTR = **pRecordItfTemp;

        __android_log_print(ANDROID_LOG_DEBUG, TAG, "xhook_audio_sles -> GetInterface -> SLRecordItf: before modification");

        newSLRecordItfPTR->SetRecordState = &hooked_opensles_SetRecordState;

        __android_log_print(ANDROID_LOG_DEBUG, TAG, "xhook_audio_sles -> GetInterface -> SLRecordItf: after modification");

        *pRecordItfTemp = newSLRecordItfPTR;
        *(SLRecordItf*)pInterface = (SLRecordItf) pRecordItfTemp;

        opensles_saveRecordStatePtr(ownerEngine, (SLRecordItf) pRecordItfTemp);
        opensles_setObjectOwnership(ownerEngine, pRecordItfTemp);

        __android_log_print(ANDROID_LOG_DEBUG, TAG, "xhook_audio_sles -> GetInterface -> SLRecordItf Hooked");
    } else {

        /**
         * We are having this problem because we have hooked two structures chain that has GetInterface call from two object.
         * In future if we hook more objects of different type calling this function we need to separate the hook functions for different type.
         */

        __android_log_print(ANDROID_LOG_DEBUG, TAG, "xhook_audio_sles -> GetInterface -> Unknown State******");

        __android_log_print(ANDROID_LOG_DEBUG, TAG, "xhook_audio_sles -> GetInterface -> Unknown State -> Self: %p, Owner: %p", self, ownerEngine);

        if (ownerEngine == self) {
            sLresult = oldFunctions->GetInterfaceEngine(self, iid, pInterface);
        } else {
            sLresult = oldFunctions->GetInterfaceRecord(self, iid, pInterface);
        }

        opensles_setObjectOwnership(ownerEngine, *(void **)pInterface);

    }
    return sLresult;
}

void hooked_opensles_Destroy (SLObjectItf self) {
    __android_log_print(ANDROID_LOG_DEBUG, TAG, "xhook_audio_sles -> Destroy Hooked");

    SLObjectItf ownerEngine = opensles_getObjectOwnership((void *) self);

    opensles_getOldFunctionPTR(self)->Destroy(ownerEngine);

    /*
    if (ownerEngine == self)
        opensles_free_dynamic_memory(self);
    */
}

SLresult hooked_opensles_CreateAudioRecorder (SLEngineItf self, SLObjectItf * pRecorder, SLDataSource *pAudioSrc, SLDataSink *pAudioSnk, SLuint32 numInterfaces, const SLInterfaceID * pInterfaceIds, const SLboolean * pInterfaceRequired) {

    struct SLObjectItf_ *newSLObjectItfPTR, **pRecorderTemp;

    SLObjectItf ownerEngine = opensles_getObjectOwnership((void *) self);

    struct opensles_old_functions *oldFunctions = opensles_getOldFunctionPTR(ownerEngine);

    __android_log_print(ANDROID_LOG_DEBUG, TAG, "xhook_audio_sles -> CreateAudioRecorder -> Hooked, Org CreateAudioRecorder: %p, Old Func: %p", oldFunctions->CreateAudioRecorder, oldFunctions);

    SLresult sLresult = oldFunctions->CreateAudioRecorder(self, (SLObjectItf *) &pRecorderTemp, pAudioSrc, pAudioSnk, numInterfaces, pInterfaceIds, pInterfaceRequired);

    // If not success, return the result without modifying
    if (sLresult != SL_RESULT_SUCCESS) {
        __android_log_print(ANDROID_LOG_DEBUG, TAG, "xhook_audio_sles -> CreateAudioRecorder -> Failed -> Code: %u, RecordItf: %p", sLresult, pRecorderTemp);
        *pRecorder = (SLObjectItf) pRecorderTemp;
        __android_log_print(ANDROID_LOG_DEBUG, TAG, "xhook_audio_sles -> CreateAudioRecorder -> Not Hooked");
        return sLresult;
    }

    __android_log_print(ANDROID_LOG_DEBUG, TAG, "xhook_audio_sles -> CreateAudioRecorder -> OwnerEngineItfPtr: %p, RecordItfPtr: %p", ownerEngine, pRecorderTemp);

    oldFunctions->GetInterfaceRecord = (*pRecorderTemp)->GetInterface;

    newSLObjectItfPTR = opensles_allocate_memory(ownerEngine, sizeof(struct SLObjectItf_));

    *newSLObjectItfPTR = **pRecorderTemp;

    __android_log_print(ANDROID_LOG_DEBUG, TAG, "xhook_audio_sles -> CreateAudioRecorder: before modification");

    newSLObjectItfPTR->GetInterface = hooked_opensles_GetInterface;

    __android_log_print(ANDROID_LOG_DEBUG, TAG, "xhook_audio_sles -> CreateAudioRecorder: after modification");

    *pRecorderTemp = newSLObjectItfPTR;
    *pRecorder = (SLObjectItf) pRecorderTemp;

    opensles_setObjectOwnership(ownerEngine, pRecorderTemp);

    __android_log_print(ANDROID_LOG_DEBUG, TAG, "xhook_audio_sles -> GetInterface -> CreateAudioRecorder Hooked");

    return sLresult;
}

SLresult hooked_opensles_SetRecordState (SLRecordItf self, SLuint32 state) {
    __android_log_print(ANDROID_LOG_DEBUG, TAG, "xhook_audio_sles -> SetRecordState Hooked");

    if (!isMicAllowed) {
        __android_log_print(ANDROID_LOG_DEBUG, TAG, "xhook_audio_sles -> SetRecordState -> SL_RESULT_RESOURCE_LOST");
        return SL_RESULT_RESOURCE_LOST;
    }

    SLObjectItf ownerEngine = opensles_getObjectOwnership((void *) self);

    __android_log_print(ANDROID_LOG_DEBUG, TAG, "xhook_audio_sles -> OwnerEngineItfPtr: %p, RecordItfPtr: %p", ownerEngine, self);

    opensles_saveRecordState(ownerEngine, state);

    SLresult sLresult = opensles_getOldFunctionPTR(ownerEngine)->SetRecordState(self, state);

    __android_log_print(ANDROID_LOG_DEBUG, TAG, "xhook_audio_sles -> SetRecordState -> Original function called");

    return sLresult;
}

void updateMicAccess() {
    opensles_updateMicAccess();
}

aaudio_result_t hooked_AAudioStream_requestStart(AAudioStream *stream) {
    __android_log_print(ANDROID_LOG_DEBUG, TAG, "xhook_audio_aaudio -> AAudioStream_requestStart -> Hooked");

    if (!isMicAllowed) {
        __android_log_print(ANDROID_LOG_DEBUG, TAG, "xhook_audio_aaudio -> AAudioStream_requestStart -> Blocked");
        return AAUDIO_ERROR_UNAVAILABLE;
    }

    aaudio_saveAudioStream(stream);
    return old_AAudioStream_requestStart_ptr(stream);
}

aaudio_result_t hooked_AAudioStream_requestStop(AAudioStream *stream) {
    __android_log_print(ANDROID_LOG_DEBUG, TAG, "xhook_audio_aaudio -> AAudioStream_requestStop -> Hooked");

    aaudio_removeAudioStream(stream);
    return old_AAudioStream_requestStop_ptr(stream);
}

aaudio_result_t hooked_AAudioStream_requestPause(AAudioStream* stream) {
    __android_log_print(ANDROID_LOG_DEBUG, TAG, "xhook_audio_aaudio -> AAudioStream_requestPause -> Hooked");
    return old_AAudioStream_requestPause_ptr(stream);
}

/*
aaudio_result_t hooked_AAudioStream_read(AAudioStream* stream, void *buffer, int32_t numFrames, int64_t timeoutNanoseconds) {

    if (!isMicAllowed) {
        return AAUDIO_STREAM_STATE_PAUSED;
    }

    return old_AAudioStream_read_ptr(stream, buffer, numFrames, timeoutNanoseconds);
}
*/