#ifndef _JACKPAL_PROCESS_H
#define _JACKPAL_PROCESS_H 1

#include <stddef.h>
#include "jni.h"
#include <android/log.h>

#define LOG_TAG "jackpal-termexec"

extern "C" {
JNIEXPORT jint JNICALL Java_jackpal_androidterm_TermExec_createSubprocessInternal
      (JNIEnv *, jclass, jstring, jobjectArray, jobjectArray, jint);

    JNIEXPORT jint JNICALL Java_jackpal_androidterm_TermExec_waitFor
      (JNIEnv *, jclass, jint);
}

#endif	/* !defined(_JACKPAL_PROCESS_H) */
