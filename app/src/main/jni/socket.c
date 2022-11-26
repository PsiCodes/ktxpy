#include <sys/types.h>
#include <sys/socket.h>
#include <sys/un.h>
#include <memory.h>
#include <stdio.h>
#include <string.h>
#include <unistd.h>

#include "appinfo.h"


int/*bool*/
get_socketname(char *buf, size_t len) {
    int ret = snprintf(buf, len, "%s-app_info-%ld", PACKAGE_NAME, (long) getuid());
    return 0 < ret && ret < len;
}

int
open_socket(const char *name) {
    int sock;
    struct sockaddr_un addr;
    size_t namelen;
    socklen_t addrlen;

    sock = socket(AF_UNIX, SOCK_STREAM, 0);
    if (sock == -1) {
        return -1;
    }

    memset(&addr, 0, sizeof(addr));
    addr.sun_family = AF_UNIX;

    namelen = strlen(name);
    if (namelen >= sizeof(addr.sun_path))
        namelen = sizeof(addr.sun_path) - 1;
    strncpy(addr.sun_path + 1, name, namelen);
    addrlen = (socklen_t) offsetof(struct sockaddr_un, sun_path) + 1 + (socklen_t) namelen;

    if (connect(sock, (const struct sockaddr *) &addr, addrlen) == -1) {
        close(sock);
        return -1;
    }

    return sock;
}


int
open_appsocket() {
    char sockname[PATH_MAX + 1];

    if (!get_socketname(sockname, sizeof(sockname)))
        return -1;

    return open_socket(sockname);
}
