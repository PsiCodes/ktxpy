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
package github.psicodes.ktxpy.ui.screens

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import com.ramcosta.composedestinations.annotation.Destination
import github.psicodes.ktxpy.R
import github.psicodes.ktxpy.activities.HomeActivity
import github.psicodes.ktxpy.ui.layoutComponents.ListComponent
import java.io.File
import java.nio.file.Files
import java.nio.file.StandardCopyOption

@OptIn(ExperimentalMaterial3Api::class)
@Destination
@Composable
fun SampleScreen(welcomeActivity: HomeActivity) {
    val sampleFiles by remember { mutableStateOf(welcomeActivity.assets.list("Samples")) }
    Scaffold(
        topBar =
        {
            Surface(shadowElevation = 10.dp){
                TopAppBar(
                    title = {
                        Text(
                            text = "Samples",
                            fontFamily = FontFamily(Font(resId = R.font.roboto_condensed_bold))
                        )
                    }
                )
            }
        }
    ) {
        LazyColumn(
            Modifier.padding(it)
        )
        {
            items(sampleFiles!!.size)
            { index ->
                val file = getAssetFile(welcomeActivity, sampleFiles!![index])
                ListComponent(
                    file = file,
                    modifier = Modifier
                        .padding(5.dp)
                        .fillMaxWidth()
                        .background(
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(8.dp)
                        )
                        .clickable { welcomeActivity.startEditorActivity(file) }
                )
                Divider(Modifier.fillMaxWidth(1f))
            }
        }
    }
}

fun getAssetFile(context: Context, assetName: String): File {
    val file = File(context.cacheDir, assetName)
//draw image if created successfully
    file.createNewFile()
    Files.copy(
        context.assets.open("Samples/$assetName"),
        file.toPath(),
        StandardCopyOption.REPLACE_EXISTING
    )
    return file
}
