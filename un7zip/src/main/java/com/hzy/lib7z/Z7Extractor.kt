package com.hzy.lib7z

import com.hzy.lib7z.Z7Extractor.LibLoader
import com.hzy.lib7z.Z7Extractor
import com.hzy.lib7z.IExtractCallback
import com.hzy.lib7z.ErrorCode
import android.content.res.AssetManager
import android.text.TextUtils
import java.io.File

object Z7Extractor {
    const val DEFAULT_IN_BUF_SIZE: Long = 0x4000000
    private const val lib7z = "un7zip"
    private var mLibLoaded = false
    @JvmOverloads
    fun init(loader: LibLoader? = null) {
        if (!mLibLoaded) {
            if (loader != null) {
                loader.loadLibrary(lib7z)
            } else {
                System.loadLibrary(lib7z)
            }
            mLibLoaded = true
        }
    }

    /**
     * Get the Lzma version name
     *
     * @return Lzma version name
     */
    val lzmaVersion: String
        get() {
            if (!mLibLoaded) {
                init()
            }
            return nGetLzmaVersion()
        }

    /**
     * Extract every thing from a 7z file to some place
     *
     * @param filePath in file
     * @param outPath  output path
     * @param callback callback
     * @return status
     */
    fun extractFile(
        filePath: String?, outPath: String?,
        callback: IExtractCallback?
    ): Int {
        if (!mLibLoaded) {
            init()
        }
        val inputFile = File(filePath)
        if (TextUtils.isEmpty(filePath) || !inputFile.exists() ||
            TextUtils.isEmpty(outPath) || !prepareOutPath(outPath)
        ) {
            callback?.onError(ErrorCode.ERROR_CODE_PATH_ERROR, "File Path Error!")
            return ErrorCode.ERROR_CODE_PATH_ERROR
        }
        return nExtractFile(filePath, outPath, callback, DEFAULT_IN_BUF_SIZE)
    }

    /**
     * extract some stream from assets
     *
     * @param assetManager assetManager
     * @param fileName     fileName
     * @param outPath      out Path
     * @param callback     callback
     * @return status
     */
    fun extractAsset(
        assetManager: AssetManager?, fileName: String?,
        outPath: String?, callback: IExtractCallback?
    ): Int {
        if (!mLibLoaded) {
            init()
        }
        if (TextUtils.isEmpty(fileName) || TextUtils.isEmpty(outPath) || !prepareOutPath(outPath)) {
            callback?.onError(ErrorCode.ERROR_CODE_PATH_ERROR, "File Path Error!")
            return ErrorCode.ERROR_CODE_PATH_ERROR
        }
        return nExtractAsset(assetManager, fileName, outPath, callback, DEFAULT_IN_BUF_SIZE)
    }

    /**
     * make sure out path exists
     *
     * @param outPath out path
     * @return status
     */
    private fun prepareOutPath(outPath: String?): Boolean {
        val outDir = File(outPath)
        if (!outDir.exists()) {
            if (outDir.mkdirs()) return true
        }
        return outDir.exists() && outDir.isDirectory
    }

    external fun nExtractFile(
        filePath: String?, outPath: String?,
        callback: IExtractCallback?, inBufSize: Long
    ): Int

    external fun nExtractAsset(
        assetManager: AssetManager?,
        fileName: String?, outPath: String?,
        callback: IExtractCallback?, inBufSize: Long
    ): Int

    external fun nGetLzmaVersion(): String
    interface LibLoader {
        fun loadLibrary(libName: String?)
    }
}