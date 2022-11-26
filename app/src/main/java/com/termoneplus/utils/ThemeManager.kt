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

import android.content.Context
import android.content.SharedPreferences
import com.termoneplus.utils.ThemeManager
import android.widget.Toast
import android.view.Gravity
import androidx.preference.PreferenceManager
import com.wildzeus.pythonktx.R
import java.io.File

object ThemeManager {
    const val PREF_THEME_MODE = "thememode"
    private const val PREFERENCES_FILE = "file_selection" /*obsolete*/
    private const val PREFERENCE_LIGHT_THEME = "light_theme" /*obsolete*/
    fun migrateFileSelectionThemeMode(context: Context) {
        val preferences = context.getSharedPreferences(PREFERENCES_FILE, Context.MODE_PRIVATE)
        if (!preferences.contains(PREFERENCE_LIGHT_THEME)) return
        val light_theme = preferences.getBoolean(PREFERENCE_LIGHT_THEME, false)
        val toast = Toast.makeText(
            context.applicationContext,
            "Migrate \"File Selection\" theme mode", Toast.LENGTH_LONG
        )
        toast.setGravity(Gravity.CENTER, 0, 0)
        toast.show()
        preferences.edit().remove(PREFERENCE_LIGHT_THEME).commit()
        // Note obsolete "FileSelection" preferences have only one item - light_theme!
        run {
            val prefs_path = File(context.filesDir.parentFile, "shared_prefs")
            val list = prefs_path.listFiles { dir: File?, name: String ->
                name.startsWith(
                    PREFERENCES_FILE
                )
            }
            if (list != null) for (file in list) file.delete()
        }
        val prefs = PreferenceManager.getDefaultSharedPreferences(context)
        val prefs_editor = prefs.edit()
        if (light_theme) prefs_editor.putString(
            PREF_THEME_MODE,
            "light"
        ) else prefs_editor.putString(
            PREF_THEME_MODE, "dark"
        )
        prefs_editor.apply()
    }

    @JvmStatic
    fun presetTheme(context: Context, actionbar: Boolean, resid: Int): Int {
        var resid = resid
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
        var mode = sharedPreferences.getString(PREF_THEME_MODE, "")
        if (mode == "") mode = context.resources.getString(R.string.pref_thememode_default)
        when (mode) {
            "dark" -> resid = if (actionbar) R.style.AppTheme else R.style.AppTheme_NoActionBar
            "light" -> resid =
                if (actionbar) R.style.AppTheme_Light else R.style.AppTheme_Light_NoActionBar
            "system" -> resid =
                if (actionbar) R.style.AppTheme_DayNight else R.style.AppTheme_DayNight_NoActionBar
        }
        return resid
    }
}