cmake_minimum_required(VERSION 3.0)
project(term-c)

add_executable(
        libexec-t1plus.so
        main/jni/appcmd.c
        main/jni/socket.c
        main/jni/atomicio.c
)
target_compile_definitions(
        libexec-t1plus.so
        PRIVATE
        PACKAGE_NAME="${APPLICATION_ID}"
)
target_compile_options(
        libexec-t1plus.so
        PRIVATE
        -fPIC
)
target_link_libraries(
        libexec-t1plus.so
        -pie
)

# used if ABI < 16
add_executable(
        libexeo-t1plus.so
        main/jni/appcmd.c
        main/jni/socket.c
        main/jni/atomicio.c
)
target_compile_definitions(
        libexeo-t1plus.so
        PRIVATE
        PACKAGE_NAME="${APPLICATION_ID}"
)
target_compile_options(
        libexeo-t1plus.so
        PRIVATE
)
target_link_libraries(
        libexeo-t1plus.so
        -nopie
)