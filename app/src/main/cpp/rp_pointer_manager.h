//
// Created by Tousif on 2019-12-23.
//

#ifndef RP_XPOSED_FRAMEWORK_RP_POINTER_MANAGER_H
#define RP_XPOSED_FRAMEWORK_RP_POINTER_MANAGER_H

#include <stdlib.h>
#include <SLES/OpenSLES_Android.h>
#include <aaudio/AAudio.h>

#ifdef __cplusplus

#include <set>
#include <unordered_map>
//#include <mutex>
//#include <deque>

extern "C"
{
#endif

struct opensles_old_functions {
    SLresult (*Realize) (
            SLObjectItf self,
            SLboolean async);

    SLresult (*GetInterfaceEngine)(
            SLObjectItf self,
            const SLInterfaceID iid,
            void *pInterface);

    SLresult (*GetInterfaceRecord) (
            SLObjectItf self,
            const SLInterfaceID iid,
            void * pInterface);

    void (*Destroy) (
            SLObjectItf self);

    SLresult (*CreateAudioRecorder) (
            SLEngineItf self,
            SLObjectItf * pRecorder,
            SLDataSource *pAudioSrc,
            SLDataSink *pAudioSnk,
            SLuint32 numInterfaces,
            const SLInterfaceID * pInterfaceIds,
            const SLboolean * pInterfaceRequired);

    SLresult (*SetRecordState) (
            SLRecordItf self,
            SLuint32 state);
};

/**
 *
 * @param engineItf
 * @return
 */
void* isEngineExists(SLObjectItf engineItf);
void* opensles_allocate_memory(SLObjectItf engineItf, size_t __byte_count);
void opensles_free_dynamic_memory(SLObjectItf engineItf);
void opensles_setObjectOwnership(SLObjectItf engineItf, void * objPTR);
SLObjectItf opensles_getObjectOwnership(void * objPTR);
struct opensles_old_functions * opensles_getOldFunctionPTR(SLObjectItf engineItf);
void opensles_saveRecordStatePtr(SLObjectItf engineItf, SLRecordItf recordItf);
void opensles_saveRecordState(SLObjectItf engineItf, SLuint32 recordStat);

void aaudio_saveAudioStream(AAudioStream *stream);
void aaudio_removeAudioStream(AAudioStream *stream);

void opensles_updateMicAccess();


#ifdef __cplusplus
}

class OpenSlesData {
public:
    SLRecordItf recordItf;
    SLuint32 recordState = 0;
    bool micBlocked = false;

    OpenSlesData();
    OpenSlesData(SLObjectItf engineItf);
    ~OpenSlesData();
    void* allocateMemory(SLObjectItf pEngineItf, size_t __byte_count);
    void feeAllMemory();
    opensles_old_functions * getOldOpenslesFunctionsPTR();
    const void* getEngineItfPtr();

private:
    SLObjectItf engineItf;
    std::set<void *> memoryPointerSet;
    struct opensles_old_functions *oldOpenslesFunctionsPTR;
};

std::unordered_map<SLObjectItf, OpenSlesData> openSlesDataMap;
std::unordered_map<void *, SLObjectItf> openSlesObjectOwnershipMap;

std::set<AAudioStream *> aaudioStreamSet;

#endif // __cplusplus

#endif //RP_XPOSED_FRAMEWORK_RP_POINTER_MANAGER_H
