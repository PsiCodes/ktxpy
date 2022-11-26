#include "registration.h"
#include "compat.h"

#if defined(__cplusplus)
# error "__cplusplus"
#endif

#include <malloc.h>
#include <fcntl.h>
#include <stdlib.h>
#include <errno.h>
#include <memory.h>
#include <unistd.h>
#include <sys/ioctl.h>
#include <wait.h>


static char *
dup_jbyteArray(JNIEnv *env, jbyteArray array) {
    char *ret;
    jsize len;
    int k;

    len = (*env)->GetArrayLength(env, array);
    if (len < 1) return NULL;

    ret = malloc((size_t)len + 1);
    if (ret == NULL) return NULL;

    jbyte *data = (*env)->GetByteArrayElements(env, array, NULL);
    for (k = 0; k < len; k++)
        ret[k] = data[k];
    ret[k] = '\0';
    (*env)->ReleaseByteArrayElements(env, array, data, JNI_ABORT);

    return ret;
}


static char **
dup_jobjectArray(JNIEnv *env, jobjectArray list) {
    char **ret;
    jsize len;
    int k;

    len = (*env)->GetArrayLength(env, list);
    if (len < 1) return NULL;

    ret = malloc((len + 1) * sizeof(char *));
    if (ret == NULL) return NULL;

    for (k = 0; k < len; k++) {
        jobject item = (*env)->GetObjectArrayElement(env, list, (jsize) k);
        ret[k] = dup_jbyteArray(env, item);
        if (ret[k] == NULL) goto err;
    }
    ret[len] = NULL;

    return ret;

    err:
    for (--k; k >= 0; k--) {
        free(ret[k]);
    }
    free(ret);
    return NULL;
}


static pid_t
process_create_subprocess(
        JNIEnv *env, jobject clazz,
        int ptm, char *path, char **argv, char **envp
) {
    pid_t pid;
    char devname[64]; /*match bionic, see libc/unistd/ptsname_r.c*/

    (void) clazz;

    fcntl(ptm, F_SETFD, FD_CLOEXEC);

    /* openpty part for amaster ... */
    if (grantpt(ptm) < 0) {
        /* bionic stub that returns zero */
        return -1;
    }

    if (unlockpt(ptm) < 0) {
        throwIOException(env, "unlockpt fail / error %d/%s",
                 errno, strerror(errno));
        return -1;
    }

    memset(devname, 0, sizeof(devname));
    if (ptsname_r(ptm, devname, sizeof(devname)) != 0) {
        throwIOException(env, "ptsname_r fail / error %d/%s",
                 errno, strerror(errno));
        return -1;
    }

    pid = fork();
    if (pid < 0) {
        throwIOException(env, "fork fail / error %d/%s",
                 errno, strerror(errno));
        return -1;
    }

    if (pid > 0) {
        /* in parent */
        return pid;
    }
    /* else in child */
    {
        int pts;

        /* make controlling tty ... */

        /* required by TIOCSCTTY */
        if (setsid() < 0) {
            throwIOException(env, "setsid fail / error %d/%s",
                     errno, strerror(errno));
            exit(-1);
        }

        /* openpty part for aslave ... */
        pts = open(devname, O_RDWR | O_NOCTTY);
        if (pts < 0) {
            throwIOException(env, "open pty fail / error %d/%s",
                     errno, strerror(errno));
            exit(-1);
        }

        /* set controlling tty */
        if (ioctl(pts, TIOCSCTTY, 0) < 0) {
            throwIOException(env, "ioctl for TIOCSCTTY fail / error %d/%s",
                     errno, strerror(errno));
            exit(-1);
        }

        /* Redirect stdin/stdout/stderr from the pseudo tty */
        dup2(pts, STDIN_FILENO);
        dup2(pts, STDOUT_FILENO);
        dup2(pts, STDERR_FILENO);

        closefrom(STDERR_FILENO + 1);

        execve(path, argv, envp);
        /* NOTE On success, execve() does not return */
        {
            throwIOException(env, "execve fail / error %d/%s",
                    errno, strerror(errno));
            exit(-1);
        }
    }
}


static jint
jprocess_create_subprocess(
        JNIEnv *env, jobject clazz,
        jint ptm, jbyteArray path_j, jobjectArray argv_j, jobjectArray envp_j
) {
    int k;
    char *path = NULL, **argv = NULL, **envp = NULL;

    path = dup_jbyteArray(env, path_j);
    if (path == NULL) goto err;

    argv = dup_jobjectArray(env, argv_j);
    if (argv == NULL) goto err;

    envp = dup_jobjectArray(env, envp_j);
    if (envp == NULL) goto err;

    /* NOTE:
     * - typedef int __kernel_pid_t => __pid_t => pid_t
     * - typedef int __int32_t => int32_t => jint
     */
    return process_create_subprocess(env, clazz, ptm, path, argv, envp);

    err:
    if (envp != NULL) {
        for (k = 0; envp[k] != NULL; k++)
            free(envp[k]);
        free(envp);
    }
    if (argv != NULL) {
        for (k = 0; argv[k] != NULL; k++)
            free(argv[k]);
        free(argv);
    }
    free(path);

    throwOutOfMemoryError(env, "cannot allocate memory for process arguments");
    return -1;
}


static jint
process_wait_exit(
        JNIEnv *env, jobject clazz,
        jint pid
) {
    int wstatus;
    jint result = -1;

    (void) env;
    (void) clazz;

    waitpid((pid_t) pid, &wstatus, 0);
    if (WIFEXITED(wstatus)) result = WEXITSTATUS(wstatus);
    else if (WIFSIGNALED(wstatus)) result = WTERMSIG(wstatus);

    return result;
}


static void
process_finish_childs(
        JNIEnv *env, jobject clazz,
        jint pid
) {
    (void) env;
    (void) clazz;

    /* send SIGHUP to process group to die child processes... */
    (void) kill(-(pid_t) pid, SIGHUP);
}


int
register_process(JNIEnv *env) {
    static JNINativeMethod methods[] = {
            {"createSubprocess", "(I[B[[B[[B)I", (void *) jprocess_create_subprocess},
            {"waitExit",         "(I)I",         (void *) process_wait_exit},
            {"finishChilds",     "(I)V",         (void *) process_finish_childs}
    };
    return register_native(
            env,
            "com/termoneplus/Process$Native",
            methods, sizeof(methods) / sizeof(*methods)
    );
}
