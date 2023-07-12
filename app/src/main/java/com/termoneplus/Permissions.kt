/* Copyright (C) 2022-2023  PsiCodes

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

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.google.android.material.snackbar.Snackbar
import com.termoneplus.utils.PermissionManageExternal
import com.wildzeus.pythonktx.R

object Permissions {
    const val REQUEST_EXTERNAL_STORAGE = 101
    val external_storage_permissions: Array<String>

    init {
        val list = ArrayList<String>()

        // On Android 11 (API Level 30) we should use permission MANAGE_EXTERNAL_STORAGE
        // but there is no way terminal application to pass Google policy requirement.
        // Android 10 (API Level 29) with android:requestLegacyExternalStorage set
        // is last that could use permission WRITE_EXTERNAL_STORAGE.
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.Q /*API Level 29*/)
            list.add(
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN /*API Level 16*/) {
            // implicitly granted if WRITE_EXTERNAL_STORAGE is requested
            list.add(Manifest.permission.READ_EXTERNAL_STORAGE)
        }

        // to use more simple checks construct a list, even empty
        external_storage_permissions =list.toTypedArray<String>()
    }

    @JvmStatic
    fun permissionExternalStorage(activity: AppCompatActivity?): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R /*API Level 30*/) {
            if (PermissionManageExternal.isGranted) return true
        }
        for (permission in external_storage_permissions) {
            val status = ActivityCompat.checkSelfPermission(activity!!, permission)
            if (status != PackageManager.PERMISSION_GRANTED) return false
        }
        return external_storage_permissions.size > 0
    }

    private fun shouldShowExternalStorageRationale(activity: AppCompatActivity): Boolean {
        for (permission in external_storage_permissions) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(
                    activity,
                    permission
                )
            ) return true
        }
        return false
    }

    private fun requestPermissionExternalStorage(activity: AppCompatActivity, requestCode: Int) {
        ActivityCompat.requestPermissions(activity, external_storage_permissions, requestCode)
    }

    @JvmStatic
    fun requestExternalStorage(activity: AppCompatActivity, view: View?, requestCode: Int) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R /*API Level 30*/) {
            if (PermissionManageExternal.request(activity)) return
        }
        // We must request at least one permission!
        if (external_storage_permissions.size == 0) return
        if (shouldShowExternalStorageRationale(activity)) Snackbar.make(
            view!!,
            R.string.message_external_storage_rationale,
            Snackbar.LENGTH_INDEFINITE
        )
            .setAction(
                android.R.string.ok
            ) { v: View? -> requestPermissionExternalStorage(activity, requestCode) }
            .show() else requestPermissionExternalStorage(activity, requestCode)
    }

    @JvmStatic
    fun isPermissionGranted(grantResults: IntArray): Boolean {
        // Note if request is cancelled, the result arrays are empty.
        for (result in grantResults) {
            if (result != PackageManager.PERMISSION_GRANTED) return false
        }
        return grantResults.size > 0 // i.e. false by default
    }
}