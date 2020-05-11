//
// Created by Tousif on 2019-12-02.
//

#ifndef RP_XPOSED_FRAMEWORK_RP_NATIVEHOOK_AUDIO_H
#define RP_XPOSED_FRAMEWORK_RP_NATIVEHOOK_AUDIO_H

#include <stdlib.h>

#include <aaudio/AAudio.h>
#include <SLES/OpenSLES.h>

#include <SLES/OpenSLES_Android.h>
#include "android/log.h"
#include "xhook.h"

extern int isMicAllowed;

typedef SLresult old_opensles_slCreateEngine (
        SLObjectItf             *pEngine,
        SLuint32                numOptions,
        const SLEngineOption    *pEngineOptions,
        SLuint32                numInterfaces,
        const SLInterfaceID     *pInterfaceIds,
        const SLboolean         * pInterfaceRequired);

//typedef struct SLObjectItf_ ** SLObjectItf_write;

old_opensles_slCreateEngine *old_opensles_slCreateEngine_ptr, **old_opensles_slCreateEngine_pptr;

SLresult (*old_opensles_Realize) (
        SLObjectItf self,
        SLboolean async);

SLresult (*old_opensles_GetInterface_Engine) (
        SLObjectItf self,
        const SLInterfaceID iid,
        void * pInterface);

void (*old_opensles_Destroy) (
        SLObjectItf self);

SLresult (*old_opensles_GetInterface_Record) (
        SLObjectItf self,
        const SLInterfaceID iid,
        void * pInterface);

SLresult (*old_opensles_CreateAudioRecorder) (
        SLEngineItf self,
        SLObjectItf * pRecorder,
        SLDataSource *pAudioSrc,
        SLDataSink *pAudioSnk,
        SLuint32 numInterfaces,
        const SLInterfaceID * pInterfaceIds,
        const SLboolean * pInterfaceRequired);

SLresult (*old_opensles_SetRecordState) (
        SLRecordItf self,
        SLuint32 state);

SLresult hooked_opensles_slCreateEngine (
        SLObjectItf             *pEngine,
        SLuint32                numOptions,
        const SLEngineOption    *pEngineOptions,
        SLuint32                numInterfaces,
        const SLInterfaceID     *pInterfaceIds,
        const SLboolean         * pInterfaceRequired);

SLresult hooked_opensles_Realize (
        SLObjectItf self,
        SLboolean async);

SLresult hooked_opensles_GetInterface(
        SLObjectItf self,
        const SLInterfaceID iid,
        void *pInterface);

void hooked_opensles_Destroy (
        SLObjectItf self);

SLresult hooked_opensles_CreateAudioRecorder (
        SLEngineItf self,
        SLObjectItf * pRecorder,
        SLDataSource *pAudioSrc,
        SLDataSink *pAudioSnk,
        SLuint32 numInterfaces,
        const SLInterfaceID * pInterfaceIds,
        const SLboolean * pInterfaceRequired);

SLresult hooked_opensles_SetRecordState (
        SLRecordItf self,
        SLuint32 state);

typedef aaudio_result_t old_AAudioStream_requestStart(AAudioStream *stream);
old_AAudioStream_requestStart *old_AAudioStream_requestStart_ptr;

typedef aaudio_result_t old_AAudioStream_requestStop(AAudioStream *stream);
old_AAudioStream_requestStop *old_AAudioStream_requestStop_ptr;

typedef aaudio_result_t  old_AAudioStream_requestPause(AAudioStream* stream);
old_AAudioStream_requestPause *old_AAudioStream_requestPause_ptr;

/*
typedef aaudio_result_t old_AAudioStream_read(AAudioStream* stream, void *buffer, int32_t numFrames, int64_t timeoutNanoseconds);
old_AAudioStream_read *old_AAudioStream_read_ptr = NULL;
*/

void updateMicAccess();

aaudio_result_t hooked_AAudioStream_requestStart(AAudioStream *stream);
aaudio_result_t hooked_AAudioStream_requestStop(AAudioStream *stream);
aaudio_result_t hooked_AAudioStream_requestPause(AAudioStream* stream);
//aaudio_result_t hooked_AAudioStream_read(AAudioStream* stream, void *buffer, int32_t numFrames, int64_t timeoutNanoseconds);

#endif //RP_XPOSED_FRAMEWORK_RP_NATIVEHOOK_AUDIO_H
