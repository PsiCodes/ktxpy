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
package com

import android.app.Application
import android.content.Context
import android.content.res.AssetManager
import android.preference.PreferenceManager
import android.util.Log
import com.termoneplus.Installer
import com.termoneplus.Settings
import com.termoneplus.utils.ConsoleStartupScript
import com.termoneplus.utils.ThemeManager
import com.wildzeus.pythonktx.BuildConfig
import com.wildzeus.pythonktx.R
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

class Application : Application() {
    override fun onCreate() {
        super.onCreate()
        Log.d(APP_TAG, applicationInfo.nativeLibraryDir)
        rootdir = filesDir.parentFile
        etcdir = File(rootdir, "etc")
        xbindir = File(applicationInfo.nativeLibraryDir)
        tmpDir = cacheDir
        setupPreferences()
        ThemeManager.migrateFileSelectionThemeMode(this)
        Installer.install_directory(etcdir!!, false)
        install_skeleton()
        val exe = File(xbindir, Installer.APPINFO_COMMAND)
        if (!exe.canExecute()) {
            // Old Android (API Level < 17) - libraries are without executable bit set
            xbindir = File(rootdir, ".x")
            Installer.install_directory(xbindir!!, false)
            Installer.copy_executable(exe, xbindir)
        }
        Installer.installAppScriptFile()
        migrateInitialCommand()
    }

    private fun setupPreferences() {
        var updated = false
        val prefs = PreferenceManager.getDefaultSharedPreferences(
            applicationContext
        )
        val editor = prefs.edit()
        val pref_home_path = getString(R.string.key_home_path_preference)
        if (!prefs.contains(pref_home_path)) {
            val path = getDir("HOME", MODE_PRIVATE).absolutePath
            editor.putString(pref_home_path, path)
            updated = true
        }

        // clean-up obsolete preferences:
        // "allow_prepend_path" was removed in 3.1.0
        if (prefs.contains("allow_prepend_path")) {
            // Note depends from do_path_extensions
            editor.remove("allow_prepend_path")
            updated = true
        }
        // "do_path_extensions" was removed in 3.1.0
        if (prefs.contains("do_path_extensions")) {
            editor.remove("do_path_extensions")
            updated = true
        }
        if (updated) editor.apply()
        settings = Settings(this, prefs)
    }

    private fun migrateInitialCommand() {
        val prefs = PreferenceManager.getDefaultSharedPreferences(
            applicationContext
        )
        // "Shell startup command" replace "Initial Command" after 3.3.5
        if (!prefs.contains("initialcommand")) return
        val pref_home_path = getString(R.string.key_home_path_preference)
        // just in case
        if (!prefs.contains(pref_home_path)) return
        val homedir = prefs.getString(pref_home_path, "")
        val cmd = prefs.getString("initialcommand", null)
        ConsoleStartupScript.migrateInitialCommand(homedir, cmd)
        prefs.edit().remove("initialcommand").apply()
    }

    private fun install_skeleton(): Boolean {
        val asset_path = "skel"
        val prefs = PreferenceManager.getDefaultSharedPreferences(
            applicationContext
        )
        val homedir = prefs.getString(getString(R.string.key_home_path_preference), "")
        val am = assets
        try {
            val list = am.list(asset_path) ?: return true
            for (item in list) if (!install_skeleton(homedir, am, asset_path, item)!!) return false
        } catch (ignore: Exception) {
        }
        return true
    }

    protected fun install_skeleton(
        homedir: String?,
        am: AssetManager?,
        asset_path: String,
        item: String
    ): Boolean? {
        val target = File(homedir, ".$item")
        return if (target.exists()) true else am?.let {
            Installer.install_asset(
                it,
                "$asset_path/$item",
                target
            )
        }
    }

    companion object {
        const val ID = BuildConfig.APPLICATION_ID
        const val APP_TAG = "KtxPy"
        const val NOTIFICATION_CHANNEL_SESSIONS = BuildConfig.APPLICATION_ID + ".sessions"
        const val ACTION_OPEN_NEW_WINDOW = BuildConfig.APPLICATION_ID + ".OPEN_NEW_WINDOW"
        const val ACTION_RUN_SHORTCUT = BuildConfig.APPLICATION_ID + ".RUN_SHORTCUT"
        const val ACTION_RUN_SCRIPT = BuildConfig.APPLICATION_ID + ".RUN_SCRIPT"

        // internal
        const val ACTION_SWITCH_WINDOW = "com.termoneplus.SWITCH_WINDOW"
        const val ARGUMENT_TARGET_WINDOW = "target_window"
        const val ARGUMENT_WINDOW_ID = "window_id"

        /* arguments for use by external applications */
        const val ARGUMENT_SHELL_COMMAND = "com.termoneplus.Command"
        const val ARGUMENT_WINDOW_HANDLE = "com.termoneplus.WindowHandle"
        @JvmField
        var settings: Settings? = null
        @JvmField
        var xbindir: File? = null
        private var rootdir: File? = null
        private var etcdir: File? = null
        private val libdir: File? = null
        var tmpDir: File? = null
            private set
        @JvmStatic
        val tmpPath: String
            get() = tmpDir!!.absolutePath
        @JvmStatic
        val scriptFile: File
            get() = File(etcdir, "mkshrc")
        @JvmStatic
        val scriptFilePath: String
            get() = scriptFile.path

        @Throws(IOException::class)
        fun getAssetFile(context: Context, asset_name: String, name: String?): File {
            val cacheFile = File(context.filesDir, name)
            try {
                val inputStream = context.assets.open(asset_name)
                try {
                    val outputStream = FileOutputStream(cacheFile)
                    try {
                        val buf = ByteArray(1024)
                        var len: Int
                        while (inputStream.read(buf).also { len = it } > 0) {
                            outputStream.write(buf, 0, len)
                        }
                    } finally {
                        outputStream.close()
                    }
                } finally {
                    inputStream.close()
                }
            } catch (e: IOException) {
                throw IOException("Could not open file$asset_name", e)
            }
            return cacheFile
        }
    }
}