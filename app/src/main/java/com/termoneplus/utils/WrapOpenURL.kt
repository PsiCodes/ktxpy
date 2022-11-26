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

import android.R
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.content.ActivityNotFoundException
import android.content.Context
import android.net.Uri
import androidx.appcompat.app.AlertDialog
import com.termoneplus.utils.WrapOpenURL
import androidx.fragment.app.FragmentActivity

object WrapOpenURL {
    fun launch(context: Context, uri: Uri?) {
        val intent = Intent(Intent.ACTION_VIEW, uri)
        val pm = context.packageManager
        val activities = pm.queryIntentActivities(intent, 0)
        if (activities.size > 0) {
            try {
                context.startActivity(intent)
            } catch (e: ActivityNotFoundException) {
                alert(
                    context,
                    R.drawable.ic_dialog_alert,
                    "Failed to launch view action!"
                )
            }
        } else {
            alert(
                context,
                R.drawable.ic_dialog_info,
                "Missing view actions!"
            )
        }
    }

    fun launch(context: Context, path: String?) {
        val uri = Uri.parse(path)
        launch(context, uri)
    }

    @JvmStatic
    fun launch(context: Context, resId: Int) {
        val path = context.getString(resId)
        launch(context, path)
    }

    private fun alert(context: Context, iconId: Int, message: CharSequence) {
        val clazz: Class<*> = FragmentActivity::class.java
        if (clazz.isInstance(context)) {
            AlertDialog.Builder(context)
                .setTitle(R.string.dialog_alert_title)
                .setIcon(iconId)
                .setMessage(message)
                .setNeutralButton(R.string.ok, null)
                .create().show()
        } else {
            AlertDialog.Builder(context)
                .setTitle(R.string.dialog_alert_title)
                .setIcon(iconId)
                .setMessage(message)
                .setNeutralButton(R.string.ok, null)
                .create().show()
        }
    }
}