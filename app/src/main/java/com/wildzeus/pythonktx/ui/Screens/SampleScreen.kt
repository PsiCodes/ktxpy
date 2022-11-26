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
package com.wildzeus.pythonktx.ui.Screens

import android.content.Context
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import com.wildzeus.pythonktx.Activities.WelcomeActivity
import com.wildzeus.pythonktx.R
import com.wildzeus.pythonktx.ui.destinations.AboutScreenDestination
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.nio.file.Files
import java.nio.file.StandardCopyOption

@OptIn(ExperimentalMaterial3Api::class)
@Destination
@Composable
fun SampleScreen(welcomeActivity: WelcomeActivity,navigator:DestinationsNavigator)
{
 val mySampleFiles by remember{ mutableStateOf( welcomeActivity.assets.list("Samples"))}
 Scaffold(
  topBar =
  {
   TopAppBar(title = { Text(text = "Samples", fontFamily= FontFamily(Font(resId = R.font.roboto_condensed_bold))) },
    modifier = Modifier.padding(10.dp,0.dp,10.dp,2.dp),
    actions = {
     Icon(
      Icons.Default.Info, contentDescription = "Info",
      Modifier
       .size(26.dp)
       .clickable { navigator.navigate(AboutScreenDestination) })
    })
  }
 ) {
  LazyColumn(
   Modifier.padding(it)
  )
  {
   items(mySampleFiles!!.size)
   { index ->
    Row(
     Modifier
      .padding(10.dp)
      .fillMaxWidth()
      .clickable {
       getAssetFile(welcomeActivity,mySampleFiles!![index],mySampleFiles!![index])?.let { it1 ->
        welcomeActivity.startEditorActivity(
         it1
        )
       }
      })
    {
     Icon(
      painter = painterResource(id = R.drawable.python_seeklogo_com),
      contentDescription = null,
      modifier = Modifier
       .size(42.dp)
       .padding(4.dp, 2.dp, 2.dp, 4.dp)
     )
     Text(
      text = mySampleFiles!![index],
      fontFamily = FontFamily(Font(resId = R.font.custom_sans)),
      modifier = Modifier.padding(4.dp, 2.dp, 2.dp, 5.dp),
      maxLines = 2,
      fontSize = 14.sp
     )
    }
    Divider(Modifier.fillMaxWidth(1f))
   }
  }
 }}
fun getAssetFile(context: Context, asset_name: String, name: String): File? {
 val file = File(context.cacheDir, asset_name);
//draw image if created successfully
 file.createNewFile()
  Files.copy(context.assets.open("Samples/$asset_name"),file.toPath(),StandardCopyOption.REPLACE_EXISTING)
 return file
}
