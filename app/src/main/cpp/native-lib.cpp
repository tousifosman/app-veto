#include <jni.h>
#include <string>

#include "xhook.h"

extern "C" JNIEXPORT jstring JNICALL
Java_com_reversepermission_xposedxhooknative_MainActivity_stringFromJNI(
        JNIEnv* env,
        jobject /* this */) {
    std::string hello = "Hello from C++";
    return env->NewStringUTF(hello.c_str());
}

/*
extern "C" JNIEXPORT void JNICALL
Java_com_reversepermission_xposedxhooknative_MainActivity_hookJNI(JNIEnv *env, jobject instance) {

    // TODO

}
*/
