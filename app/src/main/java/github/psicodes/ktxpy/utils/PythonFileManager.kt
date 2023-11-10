package github.psicodes.ktxpy.utils
import android.os.Build
import android.os.FileObserver
import androidx.compose.runtime.mutableStateOf
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import java.io.File

object PythonFileManager {
    var  pythonFiles = mutableStateOf( listOf<File>())
    private lateinit var observer: FileObserver
    var filesDir: String = ""
    fun init() {
        pythonFiles.value = File(filesDir).listFiles { _, name -> name!!.endsWith(".py") }?.toList() ?: listOf()
        observer = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val file = File(filesDir)
            object : FileObserver(file) {
                override fun onEvent(event: Int, file: String?) {
                    if (event == CREATE || event == DELETE || event == MODIFY) {
                        updatePythonFileList()
                    }
                }
            }
        } else {
            object : FileObserver(filesDir) {
                override fun onEvent(event: Int, file: String?) {
                    if (event == CREATE || event == DELETE || event == MODIFY) {
                        updatePythonFileList()
                    }
                }
            }
        }
        observer.startWatching()
    }
    fun updatePythonFileList(){
        CoroutineScope(Dispatchers.Main).run {
            pythonFiles.value = (File(filesDir).listFiles { _, name -> name!!.endsWith(".py") }?.toList() ?: listOf()).sortedBy {
                it.lastModified()
            }.reversed()
        }
    }
}