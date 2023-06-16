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
package com.wildzeus.pythonktx.Activities
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.google.accompanist.navigation.material.ExperimentalMaterialNavigationApi
import com.hzy.lib7z.IExtractCallback
import com.hzy.lib7z.Z7Extractor
import com.ramcosta.composedestinations.DestinationsNavHost
import com.ramcosta.composedestinations.animations.defaults.RootNavGraphDefaultAnimations
import com.ramcosta.composedestinations.animations.rememberAnimatedNavHostEngine
import com.ramcosta.composedestinations.navigation.dependency
import com.wildzeus.pythonktx.DataStore.SettingsDataStore
import com.wildzeus.pythonktx.FileEditing.utils.RealPathUtil.getRealPathFromURIAPI19
import com.wildzeus.pythonktx.ui.NavGraphs
import com.wildzeus.pythonktx.ui.theme.PythonkTXTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.io.File


class WelcomeActivity: ComponentActivity() {
    private val loadTMTLauncher =
        registerForActivityResult(ActivityResultContracts.GetContent()) { result: Uri? ->
            try {
                if(result!=null){
                val filePath:String= getRealPathFromURIAPI19(this, result)?:""
                if (filePath.endsWith(".py") )   {
                            val mIntent = Intent(this, EditorActivity::class.java)
                            mIntent.putExtra("fileUrl", filePath)
                            Log.d("TAG",result.toString())
                            startActivity(mIntent)
                }
                }}catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(this,"Only Python files are allowed",Toast.LENGTH_LONG).show()
            }
        }
    private lateinit var dataStore:SettingsDataStore
    private val isFileExtracting= mutableStateOf(false)
    @OptIn(ExperimentalAnimationApi::class, ExperimentalMaterialNavigationApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        dataStore=SettingsDataStore(applicationContext)
        setContent()
        {
            if (isFileExtracting.value){
               Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                   CircularProgressIndicator()
               }
            }
            else{
            val navHostEngine = rememberAnimatedNavHostEngine(
                navHostContentAlignment = Alignment.TopCenter,
                rootDefaultAnimations = RootNavGraphDefaultAnimations.ACCOMPANIST_FADING,
                )
            PythonkTXTheme()
            {
                DestinationsNavHost(navGraph = NavGraphs.root,
                    dependenciesContainerBuilder = { dependency(this@WelcomeActivity) },
                    engine = navHostEngine
                    )
            }
            }}
        Log.d(TAG,this.getApplicationInfo().nativeLibraryDir.toString())
        extractFiles()
        }
    private fun extractFiles() {
        isFileExtracting.value = true
        CoroutineScope(Dispatchers.IO).launch {
            if (dataStore.areFilesExtracted.first() == true) {
                isFileExtracting.value = false
            } else {
                Z7Extractor.extractAsset(assets,
                    "build.7z", filesDir.path,object : IExtractCallback{
                        override fun onStart() {
                        }

                        override fun onGetFileNum(fileNum: Int) {
                        }

                        override fun onProgress(name: String?, size: Long) {
                        }

                        override fun onError(errorCode: Int, message: String?) {
                        }

                        override fun onSucceed() {
                            CoroutineScope(Dispatchers.IO).launch {
                                dataStore.updateFileStatus(true)
                            }
                            isFileExtracting.value = false
                        }

                    })
            }
        }
    }

    fun startEditorActivity(file: File)
    {
        val mIntent = Intent(this, EditorActivity::class.java)
        mIntent.putExtra("fileUrl",file.absoluteFile.toString())
        startActivity(mIntent)
    }
    fun openPythonFile()
    {
        loadTMTLauncher.launch("text/*")
    }
    companion object {
        const val TAG = "WelcomeActivity"
    }
}