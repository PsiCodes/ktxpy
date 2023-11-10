package github.psicodes.ktxpy.utils

import android.content.Context

object Commands {
    fun getBasicCommand(context: Context): String {
        val appLibDirPath = context.applicationInfo.nativeLibraryDir
        val appFileDirPath = context.filesDir.absolutePath
        val pythonBuildDirPath = "$appFileDirPath/files/usr"
        val pythonLibDirPath = "$pythonBuildDirPath/lib"
        val pythonExecName = "libpython3.so"
        val aliasCommand = "alias python=\"$pythonExecName\" && alias pip=\"$pythonExecName -m pip \""
        return "export PATH=\$PATH:$appLibDirPath && export PYTHONHOME=$pythonBuildDirPath && export PYTHONPATH=$appLibDirPath && export LD_LIBRARY_PATH=\"\$LD_LIBRARY_PATH:\" && export LD_LIBRARY_PATH=\"\$LD_LIBRARY_PATH${pythonLibDirPath}\" && $aliasCommand && clear"
    }
    fun getInterpreterCommand(context: Context, filePath: String): String {
        val appLibDirPath = context.applicationInfo.nativeLibraryDir
        val appFileDirPath = context.filesDir.absolutePath
        val pythonBuildDirPath = "$appFileDirPath/files/usr"
        val pythonLibDirPath = "$pythonBuildDirPath/lib"
        val pythonExecName = "libpython3.so"
        return "export PATH=\$PATH:$appLibDirPath && export PYTHONHOME=$pythonBuildDirPath && export LD_LIBRARY_PATH=\"\$LD_LIBRARY_PATH:\" && export LD_LIBRARY_PATH=\"\$LD_LIBRARY_PATH${pythonLibDirPath}\" && clear && $pythonExecName $filePath && echo \'[Enter to Exit]\' && read junk && exit"
    }

    fun getPythonShellCommand(context: Context): String {
        val appLibDirPath = context.applicationInfo.nativeLibraryDir
        val appFileDirPath = context.filesDir.absolutePath
        val pythonBuildDirPath = "$appFileDirPath/files/usr"
        val pythonLibDirPath = "$pythonBuildDirPath/lib"
        return "export PATH=\$PATH:$appLibDirPath && export PYTHONHOME=$pythonBuildDirPath && export PYTHONPATH=$appLibDirPath && export LD_LIBRARY_PATH=\"\$LD_LIBRARY_PATH:\" && export LD_LIBRARY_PATH=\"\$LD_LIBRARY_PATH${pythonLibDirPath}\" && clear && libpython3.so && echo \'[Enter to Exit]\' && read junk && exit"
    }
}