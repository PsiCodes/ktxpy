package com.hzy.lib7z

object ErrorCode {
    const val SZ_OK = 0
    const val SZ_ERROR_DATA = 1
    const val SZ_ERROR_MEM = 2
    const val SZ_ERROR_CRC = 3
    const val SZ_ERROR_UNSUPPORTED = 4
    const val SZ_ERROR_PARAM = 5
    const val SZ_ERROR_INPUT_EOF = 6
    const val SZ_ERROR_OUTPUT_EOF = 7
    const val SZ_ERROR_READ = 8
    const val SZ_ERROR_WRITE = 9
    const val SZ_ERROR_PROGRESS = 10
    const val SZ_ERROR_FAIL = 11
    const val SZ_ERROR_THREAD = 12
    const val SZ_ERROR_ARCHIVE = 16
    const val SZ_ERROR_NO_ARCHIVE = 17
    const val ERROR_CODE_PATH_ERROR = 999
}