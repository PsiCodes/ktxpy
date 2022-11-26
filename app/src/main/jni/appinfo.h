#ifndef TERMONEPLUS_APPINFO_H
#define TERMONEPLUS_APPINFO_H
#ifndef PACKAGE_NAME
#  error "package name is not defined"
#endif

int/*bool*/ get_socketname(char *buf, size_t len) ;
int open_socket(const char *name);
int open_appsocket(void);


typedef ssize_t (*atomicio_f)(int fd, void *buf, size_t count);
#define vwrite (atomicio_f)write

size_t atomicio(atomicio_f f, int fd, void *buf, size_t count);


int/*bool*/ write_msg(int sock, const char *msg);

#endif /*TERMONEPLUS_APPINFO_H*/
