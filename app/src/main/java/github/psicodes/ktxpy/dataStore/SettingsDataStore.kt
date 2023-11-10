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
package github.psicodes.ktxpy.dataStore

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class SettingsDataStore(private val mContext: Context) {
       companion object{
          val Context.dataStore by preferencesDataStore(
            name = "Settings"
        )
    }

    private object PreferencesKeys {
        val Theme = stringPreferencesKey("Theme")
        val areFilesExtracted= booleanPreferencesKey("FilesStatus")
    }
    val mThemeString: Flow<String?> = mContext.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.Theme]
    }
    val  areFilesExtracted: Flow<Boolean?> = mContext.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.areFilesExtracted]
    }
    suspend fun updateTheme(mTheme: String) {
        mContext.dataStore.edit { preferences ->
            preferences[PreferencesKeys.Theme] = mTheme
        }
    }
    suspend fun updateFileStatus(fileStatus:Boolean) {
        mContext.dataStore.edit { preferences ->
            preferences[PreferencesKeys.areFilesExtracted] = fileStatus
        }
    }
}