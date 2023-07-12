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

import android.net.Uri
import android.view.Gravity
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.wildzeus.pythonktx.R
import jackpal.androidterm.emulatorview.TermSession
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream

object ScriptImporter {
    @JvmStatic
    fun paste(activity: AppCompatActivity, uri: Uri?, session: TermSession) {
        if (uri == null) return
        Thread {
            try {
                val inraw = activity.contentResolver.openInputStream(uri)
                    ?: throw IOException("null script input stream")
                copyStream(inraw, session.termOut)
            } catch (ignore: IOException) {
                activity.runOnUiThread { showError(activity, R.string.script_import_error) }
            } catch (ignore: SecurityException) {
                activity.runOnUiThread { showError(activity, R.string.script_access_error) }
            }
        }.start()
    }

    private fun showError(activity: AppCompatActivity, rid: Int) {
        val toast = Toast.makeText(
            activity.applicationContext,
            rid, Toast.LENGTH_LONG
        )
        toast.setGravity(Gravity.TOP, 0, 0)
        toast.show()
    }

    @Throws(IOException::class)
    private fun copyStream(`in`: InputStream, out: OutputStream) {
        val buf = ByteArray(4 * 1024)
        while (true) {
            val count = `in`.read(buf, 0, buf.size)
            if (count < 0) break
            out.write(buf, 0, count)
        }
        out.flush()
    }
}