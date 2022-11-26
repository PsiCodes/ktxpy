cmake_minimum_required(VERSION 3.0)
project(libtermexec-c)

add_library(
        term-system
        SHARED
        main/cpp/registration.c
        main/cpp/process.c
        main/cpp/termio.c
        main/cpp/compat.c
)
target_link_libraries(
        term-system
        log
)
