package github.psicodes.ktxpy.ui.screens

import android.content.Intent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import com.ramcosta.composedestinations.annotation.Destination
import github.psicodes.ktxpy.R
import github.psicodes.ktxpy.activities.EditorActivity
import github.psicodes.ktxpy.activities.HomeActivity
import github.psicodes.ktxpy.ui.layoutComponents.ListComponent
import github.psicodes.ktxpy.utils.Keys
import java.io.File


@OptIn(ExperimentalMaterial3Api::class)
@Destination
@Composable
fun FilePickerScreen() {
    val context = LocalContext.current
    val previousDirectory by remember{mutableStateOf(File("/storage/emulated/0/"))}
    var directory by remember{mutableStateOf(File("/storage/emulated/0/"))}
    val files = directory.listFiles { it ->
        (it.isDirectory && it.name!="Android") || it.extension == "py" && !it.isHidden
    } ?: emptyArray()
    Scaffold(
        topBar =
        {
            Surface(shadowElevation = 10.dp){
                TopAppBar(
                    title =
                    {
                        Text(
                            text = "File Picker",
                            fontFamily = FontFamily(Font(resId = R.font.roboto_condensed_bold))
                        )
                    },
                    navigationIcon =
                    {
                        if (directory != previousDirectory) {
                            Icon(
                                painter = painterResource(id = R.drawable.back_icon),
                                contentDescription = "Previous directory",
                                modifier = Modifier.clickable
                                {
                                    directory = previousDirectory
                                }
                            )
                        }
                    }
                )
            }
        }
    )
    {
        if (files.isNotEmpty())
        {
            LazyColumn(
                modifier = Modifier
                    .padding(it)
            )
            {
                items(files.size) { index ->
                    if (files[index].isDirectory) {
                        ListComponent(
                            file = files[index],
                            icon = R.drawable.folder_icon,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(5.dp)
                                .clickable {
                                    directory = files[index]
                                }
                        )
                    } else {
                        ListComponent(
                            file = files[index],
                            icon = R.drawable.python_icon,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(5.dp)
                                .clickable{
                                    val intent = Intent(context as HomeActivity, EditorActivity::class.java)
                                    intent.putExtra(Keys.KEY_FILE_PATH, files[index].absolutePath)
                                    context.startActivity(intent)
                                }
                        )
                    }
                    Divider()
                }
            }
        }
        else
        {
            Column(
                modifier = Modifier
                    .fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            )
            {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = "No files found",
                    modifier = Modifier
                        .size(100.dp)
                        .padding(5.dp)
                )
                Text(
                    text = "No files found",
                    fontFamily = FontFamily(Font(resId = R.font.custom_sans)),
                    modifier = Modifier.padding(top = 2.dp),
                )
            }
        }
    }

}