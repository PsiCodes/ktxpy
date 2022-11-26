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

import androidx.annotation.RequiresApi
import android.os.Environment
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.appcompat.app.AppCompatActivity
import com.wildzeus.pythonktx.BuildConfig
import java.lang.Exception

@RequiresApi(30)
object PermissionManageExternal {
    /* MANAGE_EXTERNAL_STORAGE permission:
     * - https://stackoverflow.com/questions/65876736/how-do-you-request-manage-external-storage-permission-in-android
     * - https://developer.android.com/training/data-storage/manage-all-files
     * Remark: Looks like there is no way native file management to pass Google policy!
     */
    val isGranted: Boolean
        get() = Environment.isExternalStorageManager()

    fun request(activity: AppCompatActivity): Boolean {
        try {
            val uri = Uri.parse("package:" + BuildConfig.APPLICATION_ID)
            val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION, uri)
            // startActivityForResult does not work here
            activity.startActivity(intent)
            return true
        } catch (ignore: Exception) {
        }
        try {
            val intent = Intent()
            intent.action = Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION
            activity.startActivity(intent)
            return true
        } catch (ignore: Exception) {
        }
        return false
    }
}