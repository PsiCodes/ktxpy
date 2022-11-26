#include "registration.h"

#if defined(__cplusplus)
# error "__cplusplus"
#endif

#include <stdlib.h>


/*
 * Register several "native"-methods for specified class.
 */
int register_native(
        JNIEnv *env, const char *class_name,
        JNINativeMethod *methods, size_t num_methods
) {
    jint r;
    jclass clazz;
    jint nMethods;

    clazz = (*env)->FindClass(env, class_name);
    if (clazz == NULL) {
        LOGE("Unable to find class '%s'", class_name);
        return JNI_ERR;
    }

    nMethods = (jint) num_methods;
    if (num_methods != (size_t) nMethods) { /*paranoid check*/
        LOGE("Number of methods overflow for '%s'", class_name);
        return JNI_ERR;
    }

    r = (*env)->RegisterNatives(env, clazz, methods, nMethods);
    if (r < 0) {
        return r;
    }

    return JNI_OK;
}


static void
throwNewException(JNIEnv *env, const char *clazz, const char *msg) {
    jclass exception = (*env)->FindClass(env, clazz);
    (*env)->ThrowNew(env, exception, msg);
}

void
throwOutOfMemoryError(JNIEnv *env, const char *msg)  {
    throwNewException(env, "java/lang/OutOfMemoryError", msg);
}

void
throwIOException(JNIEnv *env, const char *fmt, ...) {
    char msg[1024];
    va_list args;

    va_start(args, fmt);
    (void)vsnprintf(msg, sizeof(msg), fmt, args);
    va_end(args);

    throwNewException(env, "java/io/IOException", msg);
}


int
termoneplus_log_print(int prio, const char *fmt, ...) {
    int r;
    va_list args;

    va_start(args, fmt);
    r = __android_log_vprint(prio, "TermOnePlus(native)", fmt, args);
    va_end(args);

    return r;
}


jint JNICALL
JNI_OnLoad(JavaVM *vm, void *reserved) {
    jint r;
    union {
        JNIEnv *p;
        void *v;
    } env;

    (void) reserved;

    r = (*vm)->GetEnv(vm, &env.v, JNI_VERSION_1_2);
    if (r != JNI_OK) {
        LOGE("ERROR: GetEnv failed with error code %d", r);
        return -1;
    }

    r = register_process(env.p);
    if (r != JNI_OK) {
        LOGE("ERROR: 'process' registration fail");
        return -1;
    }

    r = register_termio(env.p);
    if (r != JNI_OK) {
        LOGE("ERROR: 'termio' registration fail");
        return -1;
    }

    return JNI_VERSION_1_2;
}
