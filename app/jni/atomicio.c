/*
 * Copyright (C) 2019-2021 Roumen Petrov.  All rights reserved.
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

#include <string.h>
#include <unistd.h>
#include <poll.h>
#include <errno.h>

#include "appinfo.h"


/* Ensure all of data on socket comes through. f==read || f==vwrite */
size_t
atomicio(atomicio_f f, int fd, void *_buf, size_t n) {
    char *buf = _buf;
    size_t pos = 0;
    ssize_t res;
    struct pollfd pfd;

    pfd.fd = fd;
    pfd.events = (short) ((f == read) ? POLLIN : POLLOUT);
    while (n > pos) {
        res = (f)(fd, buf + pos, n - pos);
        switch (res) {
            case -1:
                if (errno == EINTR) {
                    /* possible SIGALARM? */
                    continue;
                }
                if (errno == EAGAIN || errno == EWOULDBLOCK) {
                    (void) poll(&pfd, 1, -1);
                    continue;
                }
                return pos;
            case 0:
                errno = EPIPE;
                return pos;
            default:
                pos += (size_t) res;
        }
    }
    return pos;
}


int/*bool*/
write_msg(int sock, const char *msg) {
    size_t len, res;
    len = strlen(msg);
    res = atomicio(vwrite, sock, (void *) msg, len);
    return res == len;
}
