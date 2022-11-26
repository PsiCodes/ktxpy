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
package com.wildzeus.pythonktx.ViewModels

import android.app.Application
import android.content.res.Configuration
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.compose.material3.DrawerState
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.hzy.lib7z.IExtractCallback
import com.hzy.lib7z.Z7Extractor
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
class WelcomeScreenViewModel(application: Application) :AndroidViewModel(application) {
    companion object{
        private const val TAG = "WelcomeScreenViewModel"
    }
    private val _pythonFiles=mutableStateOf(getApplication<Application>().filesDir.listFiles { _, name -> name!!.endsWith(".py") } as Array<File>)
    val mPythonFiles
        get() = _pythonFiles
    private val _mDrawerState= mutableStateOf(DrawerState(DrawerValue.Closed))
    val mDrawerState
        get()=_mDrawerState
    private val _mFileName= mutableStateOf("")
    val mFileName
        get() = _mFileName
    private val _mDialogState= mutableStateOf(false)
    val mDialogState
        get() = _mDialogState
    fun saveFile(fileName: String){
        CoroutineScope(Dispatchers.IO).run {
            val fos=getApplication<Application>().openFileOutput("${fileName.filter { !it.isWhitespace()}}.py", ComponentActivity.MODE_PRIVATE)
            fos.close()
            _pythonFiles.value=getApplication<Application>().filesDir.listFiles { _, name -> name!!.endsWith(".py") } as Array<File>
        }
    }
    fun changeFileName(fileName: String) {
        _mFileName.value=fileName
    }

    fun dismissDialog() {
        _mFileName.value=""
        _mDialogState.value=false

    }
    fun showDialog() {
        _mDialogState.value=true
    }

}