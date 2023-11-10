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
package github.psicodes.ktxpy.activities
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.google.accompanist.navigation.material.ExperimentalMaterialNavigationApi
import com.hzy.libp7zip.P7ZipApi
import com.ramcosta.composedestinations.DestinationsNavHost
import com.ramcosta.composedestinations.animations.defaults.RootNavGraphDefaultAnimations
import com.ramcosta.composedestinations.animations.rememberAnimatedNavHostEngine
import com.ramcosta.composedestinations.navigation.dependency
import github.psicodes.ktxpy.dataStore.SettingsDataStore
import github.psicodes.ktxpy.ui.theme.KtxPyTheme
import github.psicodes.ktxpy.utils.Keys
import github.psicodes.ktxpy.utils.PythonFileManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.io.File
import java.nio.file.Files
import java.nio.file.StandardCopyOption

@OptIn(ExperimentalAnimationApi::class, ExperimentalMaterialNavigationApi::class)
class HomeActivity: ComponentActivity() {
    private lateinit var dataStore:SettingsDataStore
    private val isFileExtracting= mutableStateOf(false)
    @OptIn(ExperimentalAnimationApi::class, ExperimentalMaterialNavigationApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        dataStore=SettingsDataStore(applicationContext)
        // create a directory for python files if not exists
        val pythonFilesDir = File(filesDir.absolutePath+"/pythonFiles")
        if (!pythonFilesDir.exists()) {
            pythonFilesDir.mkdir()
        }
        PythonFileManager.filesDir=pythonFilesDir.absolutePath
        PythonFileManager.init()
        setContent()
        {
            KtxPyTheme()
            {
                if (isFileExtracting.value){
                    Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator()
                        Text(text = "Extracting files...")
                    }
                }
                else{
                    val navHostEngine = rememberAnimatedNavHostEngine(
                        navHostContentAlignment = Alignment.TopCenter,
                        rootDefaultAnimations = RootNavGraphDefaultAnimations.ACCOMPANIST_FADING)

                    DestinationsNavHost(
                        navGraph = github.psicodes.ktxpy.ui.screens.NavGraphs.root,
                        dependenciesContainerBuilder = { dependency(this@HomeActivity) },
                        engine = navHostEngine
                    )
                }
            }
        }
        extractFiles()
    }
    private fun extractFiles() {
        isFileExtracting.value = true
        CoroutineScope(Dispatchers.IO).launch {
            if (dataStore.areFilesExtracted.first() == true) {
                isFileExtracting.value = false
            } else {
                dataStore.updateFileStatus(false)
                CoroutineScope(Dispatchers.IO).launch {
                    val temp7zStream = assets.open("python.7z")
                    val file = File("${filesDir.absolutePath}/python.7z")
                    file.createNewFile()
                    Files.copy(temp7zStream,file.toPath(),StandardCopyOption.REPLACE_EXISTING)
                    val exitCode = P7ZipApi.executeCommand("7z x ${file.absolutePath} -o${filesDir.absolutePath}")
                    Log.d(TAG, "extractFiles: $exitCode")
                    file.delete()
                    temp7zStream.close()
                    CoroutineScope(Dispatchers.IO).launch {
                        dataStore.updateFileStatus(true)
                    }
                    isFileExtracting.value = false
                }
            }
        }
    }
    fun startEditorActivity(file: File)
    {
        val mIntent = Intent(this, EditorActivity::class.java)
        mIntent.putExtra(Keys.KEY_FILE_PATH,file.absoluteFile.toString())
        startActivity(mIntent)
    }

    companion object {
        const val TAG = "WelcomeActivity"
    }
}