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
package com.termoneplus.utils

import android.text.TextUtils
import com.termoneplus.utils.ConsoleStartupScript
import java.io.*
import java.lang.StringBuilder
import java.text.DateFormat
import java.util.*

object ConsoleStartupScript {
    fun read(homedir: String?): String {
        val builder = StringBuilder()
        val nl = System.getProperty("line.separator")
        try {
            val `in` = BufferedReader(FileReader(getScriptFile(homedir)))
            while (true) {
                val line = `in`.readLine() ?: break
                builder.append(line).append(nl)
            }
            return builder.toString()
        } catch (ignored: IOException) {
        }
        return "# ~/.shrc"
    }

    @JvmStatic
    fun write(homedir: String?, script: String?) {
        if (script == null) return
        try {
            val out = PrintWriter(getScriptFile(homedir))
            for (line in script.split("\n".toRegex()).toTypedArray()) out.println(line)
            out.flush()
            out.close()
        } catch (ignored: IOException) {
        }
    }

    @JvmStatic
    fun rename(olddir: String?, newdir: String?) {
        val oldshrc = getScriptFile(olddir)
        if (!oldshrc.exists()) return
        val shrc = getScriptFile(newdir)
        oldshrc.renameTo(shrc)
    }

    fun getScriptFile(homedir: String?): File {
        return File(homedir, ".shrc")
    }

    fun migrateInitialCommand(homedir: String?, cmd: String?) {
        if (TextUtils.isEmpty(cmd)) return
        try {
            val out = PrintWriter(
                FileWriter(getScriptFile(homedir), true)
            )
            out.println("")
            val timestamp = DateFormat.getDateTimeInstance().format(Date())
            out.println("# migrated initial command ($timestamp):")
            out.println(cmd)
            out.flush()
            out.close()
        } catch (ignored: IOException) {
        }
    }
}