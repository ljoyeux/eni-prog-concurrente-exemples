#include "fr_eni_concurrent_examples_jni_NativeExample.h"
#include <stdio.h>
#include <stdlib.h>
#include <pthread.h>
#include <unistd.h>


JNIEXPORT jint JNICALL Java_fr_eni_concurrent_examples_jni_NativeExample_inc(JNIEnv *jEnv, jclass jClass, jint i) {

    return i+1;
}

JNIEXPORT jint JNICALL Java_fr_eni_concurrent_examples_jni_NativeExample_incremente
  (JNIEnv *env, jobject nativeExample){
    jclass  nativeExampleClass = (*env)->GetObjectClass(env, nativeExample);

    jfieldID nativeExampleIField = (*env)->GetFieldID(env, nativeExampleClass, "i", "I");

    if(nativeExampleIField==NULL) {
        jclass runtimeExceptionClass = (*env)->FindClass(env, "java/lang/RuntimeException");
        (*env)->ThrowNew(env, runtimeExceptionClass, "Cannot find field");
        return 0;
    }

    (*env)->MonitorEnter(env, nativeExample);

    jint i = (*env)->GetIntField(env, nativeExample, nativeExampleIField);
    i++;
    (*env)->SetIntField(env, nativeExample, nativeExampleIField, i);

    (*env)->MonitorExit(env, nativeExample);

    return i;
  }

  //     pthread_create(&thread1, NULL, fct_thread1, NULL);

static int getEnv(JavaVM *jvm, JNIEnv **env) {
    *env = NULL;
    // get jni environment
    jint ret = (*jvm)->GetEnv(jvm, (void**)env, JNI_VERSION_1_4);

    switch (ret) {
        case JNI_OK :
            // Success!
//            fprintf(stderr, "getenv successA\n");
            break;

        case JNI_EDETACHED :
            // Thread not attached
//            fprintf(stderr, "thread not attached\n");
            // TODO : If calling AttachCurrentThread() on a native thread
            // must call DetachCurrentThread() in future.
            // see: http://developer.android.com/guide/practices/design/jni.html

            if ((*jvm)->AttachCurrentThread(jvm, (void**) env, NULL) < 0)
            {
//                fprintf(stderr, "Failed to get the environment using AttachCurrentThread()\n");
                *env = NULL;
                break;
            } else {
                // Success : Attached and obtained JNIEnv!
//                fprintf(stderr, "getenv successB");
                break;
            }

        case JNI_EVERSION :
            // Cannot recover from this error
//            fprintf(stderr, "JNI interface version 1.4 not supported\n");
            break;
        default :
//            fprintf(stderr, "Failed to get the environment using GetEnv()\n");
            *env = NULL;
        }

    return ret;
}

struct thread_params {
    JavaVM *jvm;
    jobject nativeExampleObj;
};

static void *fct_thread(void *voidparams) {

    struct thread_params *params = voidparams;

    fprintf(stderr, "Entering in child thread\nWaiting for 3s\n");
    usleep(3*1000*1000);
    fprintf(stderr, "Leaving child thread\n");

    JNIEnv *env = NULL;
    int ret = getEnv(params->jvm, &env);

    jobject nativeExampleObj = params->nativeExampleObj;

    jclass  nativeExampleClass = (*env)->GetObjectClass(env, nativeExampleObj);

    jmethodID pthreadDoneMethodID = (*env)->GetMethodID(env, nativeExampleClass, "pthreadDone", "()V");
    if(pthreadDoneMethodID==NULL) {
        fprintf(stderr, "Cannot find method\n");
    } else {
        (*env)->CallVoidMethod(env, nativeExampleObj, pthreadDoneMethodID);
    }

    (*env)->DeleteGlobalRef(env, nativeExampleObj);

    if(ret==JNI_EDETACHED) {
        (*(params->jvm))->DetachCurrentThread(params->jvm);
    }

    free(params);
    return NULL;
}

/*
 * Class:     fr_eni_concurrent_examples_jni_NativeExample
 * Method:    launchPThread
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_fr_eni_concurrent_examples_jni_NativeExample_launchPThread(JNIEnv *env, jobject nativeExampleObj) {

    struct thread_params *params = malloc(sizeof(struct thread_params));
    (*env)->GetJavaVM(env, &params->jvm);
    params->nativeExampleObj = (*env)->NewGlobalRef(env, nativeExampleObj);

    pthread_t thread;
    pthread_create(&thread, NULL, fct_thread, params);

 }
