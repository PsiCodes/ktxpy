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
@file:OptIn(ExperimentalMaterial3Api::class)
package com.wildzeus.pythonktx.ui.Screens
import android.annotation.SuppressLint
import android.content.Intent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModelProvider
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.annotation.RootNavGraph
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import com.termoneplus.TermActivity
import com.wildzeus.pythonktx.R
import com.wildzeus.pythonktx.Activities.WelcomeActivity
import com.wildzeus.pythonktx.Utils.Commands
import com.wildzeus.pythonktx.ViewModels.WelcomeScreenViewModel
import com.wildzeus.pythonktx.ui.LayoutComponents.NavigationDrawer.MenuItem
import com.wildzeus.pythonktx.ui.destinations.AboutScreenDestination
import com.wildzeus.pythonktx.ui.destinations.SampleScreenDestination
import com.wildzeus.pythonktx.ui.destinations.SettingsScreenDestination
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter", "CoroutineCreationDuringComposition")
@RootNavGraph(start = true)
@Destination
@Composable
fun WelcomeScreen(welcomeActivity: WelcomeActivity, navigator: DestinationsNavigator){
    val mViewModel=ViewModelProvider(welcomeActivity)[WelcomeScreenViewModel::class.java]
    val scope = rememberCoroutineScope()
    val pythonFilesList = mViewModel.mPythonFiles.value

    pythonFilesList.sortBy { it.lastModified() }
    val items = listOf(
        MenuItem("Home","Home",R.drawable.ic_baseline_home_24){},
        MenuItem("Info", "About",R.drawable.ic_baseline_info_24){
            navigator.navigate(AboutScreenDestination)
            scope.launch { mViewModel.mDrawerState.value.close() }},
        MenuItem("Terminal","Terminal",R.drawable.ic_baseline_terminal_24){
            val intent=Intent(welcomeActivity, TermActivity::class.java)
            intent.putExtra("Command",Commands.getInitialCommand(welcomeActivity))
            welcomeActivity.startActivity(intent)
                scope.launch { mViewModel.mDrawerState.value.close() }},
        MenuItem("Samples","Samples",R.drawable.ic_baseline_laptop_24){
            navigator.navigate(SampleScreenDestination)
            scope.launch { mViewModel.mDrawerState.value.close() }}, MenuItem("PythonShell","Interactive Mode",R.drawable.ic_baseline_keyboard_double_arrow_right_24){
            val intent=Intent(welcomeActivity, TermActivity::class.java)
            intent.putExtra("Command",Commands.getInteractiveMode(welcomeActivity))
            welcomeActivity.startActivity(intent)
            scope.launch { mViewModel.mDrawerState.value.close() }},
        MenuItem("New File","Create new file",R.drawable.ic_baseline_create_new_folder_24){
            scope.launch {
                mViewModel.showDialog()
            }
        },
        MenuItem("Open File","Open file",R.drawable.ic_baseline_file_open_24) {
            welcomeActivity.openPythonFile()
        },
        MenuItem("Settings","Settings",R.drawable.ic_baseline_settings_24){
            navigator.navigate(SettingsScreenDestination())
        }
    )
    ModalNavigationDrawer(
        drawerState = mViewModel.mDrawerState.value,
        drawerContent = {
            val width=if(LocalConfiguration.current.orientation==1) (LocalConfiguration.current.screenWidthDp/1.5).dp else (LocalConfiguration.current.screenWidthDp/2.5).dp
            ModalDrawerSheet(){
                Spacer(Modifier.height(12.dp))
                items.forEach { item ->
                    NavigationDrawerItem(
                        icon = { Icon(painter = painterResource(id = item.resID), contentDescription = null) },
                        label = { Text(item.title) },
                        selected = items[0]==item,
                        onClick =item.clickable,
                        modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                    )
                }
            }
        }
    ){
    Scaffold(
        topBar =
        {
            TopAppBar(title = { Text(text = "KtxPy", fontFamily=FontFamily(Font(resId = R.font.roboto_condensed_bold)))},
                modifier = Modifier.padding(10.dp,0.dp,10.dp,2.dp),
                navigationIcon = {Icon(Icons.Filled.Menu ,contentDescription ="Menu", modifier = Modifier.clickable {scope.launch {mViewModel.mDrawerState.value.open()}})},
                actions = {
                    Icon(Icons.Default.Info, contentDescription = "Info",
                        Modifier
                            .size(26.dp)
                            .clickable { navigator.navigate(AboutScreenDestination) })
                })
        },
        floatingActionButton =
        {   FloatingActionButton(
            onClick = {
                scope.launch{
                    mViewModel.showDialog()
                }
                      },
            content = { Icon(Icons.Filled.Add,contentDescription ="Plus button")},
        )}
    ){
        LazyColumn(
            Modifier.padding(it)
        )
        {
        items(pythonFilesList.size)
        { index->
            Row(
                Modifier
                    .padding(10.dp)
                    .fillMaxWidth()
                    .clickable {
                        welcomeActivity.startEditorActivity(pythonFilesList[index])
                    })
            {
                Icon(painter = painterResource(id = R.drawable.python_seeklogo_com), contentDescription = null, modifier = Modifier
                    .size(42.dp)
                    .padding(4.dp, 2.dp, 2.dp, 4.dp))
                Text(text =pythonFilesList[index].name,
                    fontFamily = FontFamily(Font(resId = R.font.custom_sans)),
                    modifier = Modifier.padding(4.dp,2.dp,2.dp,5.dp),
                    maxLines = 2 ,
                    fontSize = 14.sp
                )
            }
            Divider(Modifier.fillMaxWidth(1f))
        }

        }
        if (mViewModel.mDialogState.value)
        {
            MyDialog (mViewModel.mFileName.value,"Create new file",
                "Create",
                onDismissReq =
                {
                    mViewModel.dismissDialog()
                },
                onclickConfirmButton =
                {
                    scope.launch {
                        mViewModel.saveFile(mViewModel.mFileName.value)
                        mViewModel.dismissDialog()
                        mViewModel.changeFileName("")
                    }
                },
                onclickDeclineButton =
                {
                    scope.launch {
                        mViewModel.dismissDialog()
                    }
                },
                onValueChange = { string->
                    scope.launch {
                   mViewModel.changeFileName(string)}
                }
                )
        }
}}}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyDialog(text:String,title:String,confirm:String,onDismissReq:()->Unit,onclickConfirmButton:()->Unit,onclickDeclineButton: () -> Unit,onValueChange:(String)->Unit){
    AlertDialog(
        onDismissRequest = onDismissReq,
        icon = { Icon(painter = painterResource(id = R.drawable.ic_baseline_create_new_folder_24), contentDescription = null) },
        title = {
            Text(text = title)
        },
        text = {
            TextField(value =text , onValueChange = onValueChange, maxLines = 1, keyboardOptions = KeyboardOptions())
        },
        confirmButton = {
            TextButton(
                onClick = onclickConfirmButton
            ) {
                Text(confirm)
            }
        },
        dismissButton = {
            TextButton(
                onClick =onclickDeclineButton
            ) {
                Text("Exit")
            }
        }
    )
}