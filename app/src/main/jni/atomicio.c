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
