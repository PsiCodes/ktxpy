#include "compat.h"

#if defined(__cplusplus)
# error "__cplusplus"
#endif

#include <dirent.h>
#include <stdlib.h>
#include <limits.h>
#include <unistd.h>


void
closefrom(int lowfd) {
    int pws_fd = -1;
    DIR *dirp;

    { /* keep android property workspace open */
        char *pws_env = getenv("ANDROID_PROPERTY_WORKSPACE");
        if (pws_env) {
            /* format "int,int" */
            pws_fd = atoi(pws_env);
        }
    }

    dirp = opendir("/proc/self/fd");
    if (dirp != NULL) {
        int dir_fd = dirfd(dirp);
        struct dirent *dent;
        long fd;
        char *endp;

        for (dent = readdir(dirp); dent != NULL; dent = readdir(dirp)) {
            fd = strtol(dent->d_name, &endp, 10);
            if (
                    (dent->d_name != endp) && (*endp == '\0') &&
                    (fd >= 0) && (fd < INT_MAX) &&
                    (fd >= lowfd) &&
                    (fd != pws_fd) && (fd != dir_fd)
                    )
                (void) close((int) fd);
        }

        (void) closedir(dirp);
    }
}
