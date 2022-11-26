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

import android.content.Context
import android.content.SharedPreferences
import android.text.TextUtils
import com.wildzeus.pythonktx.R
import com.termoneplus.Installer
import jackpal.androidterm.emulatorview.ColorScheme
import java.lang.Exception

class Settings(context: Context, preferences: SharedPreferences) {
    private var source_sys_shrc: Boolean

    init {
        val r = context.resources
        source_sys_shrc = parseBoolean(
            preferences,
            context.getString(R.string.key_source_sys_shrc_preference),
            r.getBoolean(R.bool.pref_source_sys_shrc_default)
        )
    }

    fun parsePreference(context: Context, preferences: SharedPreferences, key: String): Boolean {
        return if (TextUtils.isEmpty(key)) false else parseSourceSysRC(context, preferences, key)
    }

    fun sourceSystemShellStartupFile(): Boolean {
        return source_sys_shrc
    }

    private fun parseBoolean(preferences: SharedPreferences, key: String, def: Boolean): Boolean {
        try {
            return preferences.getBoolean(key, def)
        } catch (ignored: Exception) {
        }
        return def
    }

    private fun parseSourceSysRC(
        context: Context,
        preferences: SharedPreferences,
        key: String
    ): Boolean {
        val pref = context.getString(R.string.key_source_sys_shrc_preference)
        if (key != pref) return false
        val value = parseBoolean(preferences, key, source_sys_shrc)
        if (value != source_sys_shrc) {
            source_sys_shrc = value
            Installer.installAppScriptFile()
        }
        return true
    }

    companion object {
        // foreground and background as ARGB color pair
        /* Note keep synchronized with names in @array.entries_color_preference
    and index in @array.entryvalues_color_preference. */
        @JvmField
        val color_schemes = arrayOf(
            ColorScheme(-0x1000000, -0x1) /*black on white*/,
            ColorScheme(-0x1, -0x1000000) /*white on black*/,
            ColorScheme(-0x1, -0xcbb143) /*white on blue*/,
            ColorScheme(-0xff0100, -0x1000000) /*green on black*/,
            ColorScheme(-0x49af, -0x1000000) /*amber on black*/,
            ColorScheme(-0xfeed, -0x1000000) /*red on black*/,
            ColorScheme(-0xcc4a1b, -0x1000000) /*holo-blue on black*/,
            ColorScheme(-0x9a847d, -0x2091d) /*solarized light*/,
            ColorScheme(-0x7c6b6a, -0xffd4ca) /*solarized dark*/,
            ColorScheme(-0x555556, -0x1000000) /*linux console*/,
            ColorScheme(-0x232334, -0xd3d3d4) /*dark pastels*/
        )
        private const val SOURCE_SYS_SHRC_KEY = "source_sys_shrc"
    }
}