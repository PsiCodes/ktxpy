#ifndef TERMONEPLUS_REGISTRATION_H
#define TERMONEPLUS_REGISTRATION_H
#include <jni.h>
#include <android/log.h>


int register_native(
        JNIEnv *env, const char *class_name,
        JNINativeMethod *methods, size_t num_methods
);

int register_process(JNIEnv *env);
int register_termio(JNIEnv *env);


void throwOutOfMemoryError(JNIEnv *env, const char *msg) ;
void throwIOException(JNIEnv *env, const char *fmt, ...);


int termoneplus_log_print(int prio, const char *fmt, ...);
#define LOGE(...) do { termoneplus_log_print(ANDROID_LOG_ERROR, __VA_ARGS__); } while(0)

#endif /* ndef TERMONEPLUS_REGISTRATION_H */
