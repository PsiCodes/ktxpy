/*
Copyright (C) 2022-2023  PsiCodes

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package com.termoneplus

import com.Application.Companion.scriptFilePath
import com.Application.Companion.scriptFile
import android.os.Build
import com.termoneplus.Installer
import android.content.res.AssetManager
import com.Application
import java.io.*
import java.lang.Exception
import java.util.ArrayList

object Installer {
    var APPINFO_COMMAND: String? = null

    init {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN /*API level 16*/)
            APPINFO_COMMAND =
            "libexeo-t1plus.so" else APPINFO_COMMAND = "libexec-t1plus.so"
    }

    fun install_directory(dir: File, share: Boolean): Boolean {
        return if (!(dir.exists() || dir.mkdir())) false else dir.setReadable(true, !share) &&
                dir.setExecutable(true, false)

        // always preset directory permissions
    }

    fun install_text_file(script: Array<String?>, file: File): Boolean {
        try {
            val out = PrintWriter(file)
            for (line in script) out.println(line)
            out.flush()
            out.close()
            // always preset permissions
            return file.setReadable(true, true)
        } catch (ignore: IOException) {
        }
        return false
    }

    fun installAppScriptFile(): Boolean {
        val shell_script = ArrayList<String?>()
        val sysmkshrc = "/system/etc/mkshrc"
        if (scriptFilePath != sysmkshrc &&
            Application.settings!!.sourceSystemShellStartupFile() &&
            File(sysmkshrc).exists()
        ) shell_script.add(". $sysmkshrc")

        // Source application startup script
        // Test command is not supported by built-in shell on devices before API Level 16("Jelly Bean"/4.1).
        // Jelly Bean switchs to "MIRBSD KSH R40 2011/10/07" where test is a shell built-in.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN /*API level 16*/) shell_script.add(
            "test -f ~/.shrc && . ~/.shrc"
        ) else shell_script.add(". ~/.shrc")

        //Next work fine with mksh but fail with ash.
        //shell_script.add(". /proc/self/fd/0 <<< \"$(libexec-t1plus.so aliases)\"");
        shell_script.add(". /proc/self/fd/0 <<EOF")
        shell_script.add("$(" + APPINFO_COMMAND + " aliases)")
        shell_script.add("EOF")
        return install_text_file(shell_script.toTypedArray(), scriptFile)
    }

    fun copy_executable(source: File, target_path: File?): Boolean {
        val buflen = 32 * 1024 // 32k
        val buf = ByteArray(buflen)
        val target = File(target_path, source.name)
        val backup = File(target.absolutePath + "-bak")
        if (target.exists()) if (!target.renameTo(backup)) return false
        try {
            val os: OutputStream = FileOutputStream(target)
            val `is`: InputStream = FileInputStream(source)
            var len: Int
            while (`is`.read(buf, 0, buflen).also { len = it } > 0) {
                os.write(buf, 0, len)
            }
            os.close()
            `is`.close()
            if (backup.exists()) backup.delete()

            // always preset executable permissions
            return target.setReadable(true) &&
                    target.setExecutable(true, false)
        } catch (ignore: Exception) {
        }
        return false
    }

    fun install_asset(am: AssetManager, asset: String?, target: File?): Boolean {
        val buflen = 32 * 1024 // 32k
        val buf = ByteArray(buflen)
        try {
            val os: OutputStream = FileOutputStream(target)
            val `is` = am.open(asset!!, AssetManager.ACCESS_STREAMING)
            var len: Int
            while (`is`.read(buf, 0, buflen).also { len = it } > 0) {
                os.write(buf, 0, len)
            }
            `is`.close()
            os.close()
            return true
        } catch (ignore: IOException) {
        }
        return false
    }
}