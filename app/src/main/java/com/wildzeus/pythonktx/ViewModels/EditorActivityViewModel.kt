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

import androidx.lifecycle.ViewModel
import io.github.rosemoe.sora.text.ContentCreator
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import java.io.File

class EditorActivityViewModel(file: File?): ViewModel() {
    var text:String = ""
    init {
        CoroutineScope(Dispatchers.IO).run {
             text = file?.inputStream()?.let { ContentCreator.fromStream(it).toString() }.toString()
        }
    }
}