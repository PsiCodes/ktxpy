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

import android.os.PowerManager
import android.annotation.SuppressLint
import android.os.Build
import androidx.annotation.RequiresApi
import com.wildzeus.pythonktx.R
import android.content.DialogInterface
import android.content.Intent
import android.content.ActivityNotFoundException
import android.content.Context
import android.provider.Settings
import androidx.appcompat.app.AlertDialog
import com.Application
import java.lang.Exception

object WakeLock {
    private var lock: PowerManager.WakeLock? = null
    @JvmStatic
    fun create(context: Context) {
        val pm = context.getSystemService(Context.POWER_SERVICE) as PowerManager
            ?: return
        try {
            lock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, Application.APP_TAG + ":")
        } catch (ignore: Exception) {
        }
    }

    @JvmStatic
    fun release() {
        if (lock == null) return
        if (lock!!.isHeld) lock!!.release()
        lock = null
    }

    val isHeld: Boolean
        get() = lock != null && lock!!.isHeld

    @JvmStatic
    @SuppressLint("WakelockTimeout")
    fun toggle(context: Context) {
        if (lock == null) return
        if (lock!!.isHeld) {
            lock!!.release()
        } else {
            lock!!.acquire()
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M /*API Level 23*/) batteryOptimizations(
                context
            )
        }
    }

    @RequiresApi(23)
    private fun batteryOptimizations(context: Context) {
        val pm = context.getSystemService(Context.POWER_SERVICE) as PowerManager
            ?: return
        val package_name = context.packageName
        if (pm.isIgnoringBatteryOptimizations(package_name)) return
        val res = context.resources
        val app_name = res.getString(R.string.application_terminal)
        val msg = res.getString(R.string.ignore_battery_optimizations, app_name)
        AlertDialog.Builder(context)
            .setMessage(msg)
            .setPositiveButton(
                android.R.string.ok
            ) { dialog: DialogInterface?, which: Int -> openPowerSettings(context) }
            .setNegativeButton(android.R.string.cancel, null)
            .create().show()
    }

    @RequiresApi(23)
    private fun openPowerSettings(context: Context) {
        val intent = Intent()
        intent.action = Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS
        try {
            context.startActivity(intent)
        } catch (ignore: ActivityNotFoundException) {
        }
    }
}