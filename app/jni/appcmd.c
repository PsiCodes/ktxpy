/*
 * Copyright (C) 2019-2020 Roumen Petrov.  All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

#include <memory.h>
#include <stdio.h>
#include <stdlib.h>
#include <unistd.h>
#include <errno.h>

#include "appinfo.h"


static int/*bool*/
get_info(int argc, char *argv[]) {
    int ret = 0, k = 0;
    char msg[1024];
    int msgres;
    char buf[4096];
    int sock;

    msgres = snprintf(msg, sizeof(msg), "get %s\n", argv[k]);
    if (msgres < 0 || msgres >= sizeof(msg))
        return 0;

    sock = open_appsocket();
    if (sock == -1) return 0;

    if (!write_msg(sock, msg)) goto done;

    for (k++; k < argc; k++) {
        msgres = snprintf(msg, sizeof(msg), "%s\n", argv[k]);
        if (msgres < 0 || msgres >= sizeof(msg))
            goto done;
        if (!write_msg(sock, msg)) goto done;
    }
    if (k > 1) {
        msgres = snprintf(msg, sizeof(msg), "%s\n", "<eol>");
        if (msgres < 0 || msgres >= sizeof(msg))
            goto done;
        if (!write_msg(sock, msg)) goto done;
    }

    while (1) {
        size_t len;
        int read_errno;
        errno = 0;
        len = atomicio(read, sock, buf, sizeof(buf));
        read_errno = errno;
        if (len > 0) {
            size_t res;
            ret = 1;
            errno = 0;
            res = atomicio(vwrite, STDOUT_FILENO, buf, len);
            if (res != len) {
                ret = 0;
                goto done;
            }
        }
        if (read_errno == EPIPE) break;
    }
    (void) fsync(STDOUT_FILENO);

    done:
    close(sock);
    return ret;
}


#include <sysexits.h>

int
main(int argc, char *argv[]) {
    int ret;

    if (argc < 2) exit(EX_USAGE);

    ret = get_info(argc - 1, argv + 1);

    return ret ? 0 : EX_SOFTWARE;
}
