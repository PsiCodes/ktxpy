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

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ramcosta.composedestinations.annotation.Destination
import github.psicodes.ktxpy.R

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
@Destination
fun AboutScreen(){
Scaffold(
topBar =
{
    TopAppBar(title = { Text(text = "About", fontFamily =FontFamily(Font(R.font.roboto_condensed_bold))) },
        modifier = Modifier.padding(10.dp,0.dp,10.dp,2.dp),
        actions = {
            Icon(painter = painterResource(id = R.drawable.app_icon), contentDescription = null)
        })
}
){
    Column(modifier = Modifier.padding(it))
    {
        Divider()
        Text(modifier = Modifier.padding(12.dp),text = "Developer = PsiCodes aka Pranjal", fontSize = 14.sp, fontFamily = FontFamily(Font(R.font.roboto_condensed_bold)))
        Text(modifier = Modifier.padding(12.dp),text = "Source Code = At Github @PsiCodes", fontSize = 14.sp, fontFamily = FontFamily(Font(R.font.roboto_condensed_bold)))
        Divider(modifier = Modifier.padding(12.dp))
        Text(modifier = Modifier.padding(20.dp),text = "This App Was Designed To Run Python Code in Android It Uses Cross Compiled Python 3.11.0 Executable(In Files Directory) It Offers High Quality Code Editor Along With Great material3 Theme", fontSize = 12.sp, fontFamily = FontFamily(Font(R.font.custom_sans)))
        Divider(modifier = Modifier.padding(12.dp))
        Text(text = "Special Thanks To Rosmoe ,Termux team & hzy3774", fontSize = 13.sp, fontFamily = FontFamily(Font(R.font.roboto_condensed_bold)), modifier = Modifier.padding(12.dp,6.dp,2.dp,6.dp))
        Divider(modifier = Modifier.padding(12.dp))
    }
}}
