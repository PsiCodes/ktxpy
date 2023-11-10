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
package github.psicodes.ktxpy.viewModels

import android.app.Application
import androidx.compose.material3.DrawerState
import androidx.compose.material3.DrawerValue
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import java.io.File


class HomeScreenViewModel(application: Application) :AndroidViewModel(application) {

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
            // create a new file in files-dir
            val fileDir = File(getApplication<Application>().filesDir.toString()+"/pythonFiles")
            val nameWithoutSpaces=fileName.replace(" ","_")
            val file= File(fileDir,"$nameWithoutSpaces.py")
            file.createNewFile()
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