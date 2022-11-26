#ifndef TERMONEPLUS_APPINFO_H
#define TERMONEPLUS_APPINFO_H

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
