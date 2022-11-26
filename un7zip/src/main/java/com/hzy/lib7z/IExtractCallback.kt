package com.hzy.lib7z

interface IExtractCallback {
    fun onStart()
    fun onGetFileNum(fileNum: Int)
    fun onProgress(name: String?, size: Long)
    fun onError(errorCode: Int, message: String?)
    fun onSucceed()
}