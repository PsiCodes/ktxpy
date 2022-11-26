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
package com.wildzeus.pythonktx.ui.Screen
import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.preference.PreferenceManager
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import com.wildzeus.pythonktx.Activities.WelcomeActivity
import com.wildzeus.pythonktx.DataStore.SettingsDataStore
import com.wildzeus.pythonktx.R
import com.wildzeus.pythonktx.ui.LayoutComponents.DropDownMenu.DropDownMenuItem
import com.wildzeus.pythonktx.ui.LayoutComponents.SettingsMenu.SettingsMenuItem
import com.wildzeus.pythonktx.ui.destinations.AboutScreenDestination
import com.wildzeus.pythonktx.ui.theme.EditorTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
@Destination
fun SettingsScreen(welcomeActivity: WelcomeActivity,navigator: DestinationsNavigator)
{
     val dataStore= SettingsDataStore(welcomeActivity.applicationContext)
     val mTheme=dataStore.mThemeString.collectAsState(initial = "").value.toString() ?: EditorTheme.QuietLight
    val mPreferenceManager = PreferenceManager.getDefaultSharedPreferences(welcomeActivity)
    Log.d("DataStore",mTheme)
    var isDropDownMenuExpandedTheme by remember { mutableStateOf(false) }
    var isDropDownMenuExpandedFontSize by remember { mutableStateOf(false) }
    var isDropDownMenuExpandedTerminalTheme by remember { mutableStateOf(false) }
    val dropDownMenuItemListTheme=listOf<DropDownMenuItem>(
        DropDownMenuItem("Dracula Theme",R.drawable.ic_baseline_nights_stay_24){
            CoroutineScope(Dispatchers.IO).launch {
                dataStore.updateTheme(EditorTheme.DarculaTheme)
            }
            Log.d("DataStore",mTheme)
            isDropDownMenuExpandedTheme=false
        },
        DropDownMenuItem("Quiet Light",R.drawable.ic_baseline_wb_sunny_24){
            CoroutineScope(Dispatchers.IO).launch {
                dataStore.updateTheme(EditorTheme.QuietLight)
            }
            Log.d("DataStore",mTheme)
            isDropDownMenuExpandedTheme=false
        },
        DropDownMenuItem("Abyss Color",R.drawable.ic_baseline_wb_iridescent_24){
            CoroutineScope(Dispatchers.IO).launch {
                dataStore.updateTheme(EditorTheme.AbyssColor)
            }
            Log.d("DataStore",mTheme)
            isDropDownMenuExpandedTheme=false
        }
    )
    val dropDownMenuItemListFontSize= listOf<DropDownMenuItem>(
        DropDownMenuItem("8dp",R.drawable.ic_baseline_text_increase_24)
        {
            mPreferenceManager.edit().putString("fontsize",(8).toString()).commit()
            isDropDownMenuExpandedFontSize=false
        }, DropDownMenuItem("10dp",R.drawable.ic_baseline_text_increase_24)
        {
            mPreferenceManager.edit().putString("fontsize",(10).toString()).commit()
            isDropDownMenuExpandedFontSize=false
        }, DropDownMenuItem("12dp",R.drawable.ic_baseline_text_increase_24)
        {
            mPreferenceManager.edit().putString("fontsize",(12).toString()).commit()
            isDropDownMenuExpandedFontSize=false
        }, DropDownMenuItem("14dp",R.drawable.ic_baseline_text_increase_24)
        {
            mPreferenceManager.edit().putString("fontsize",(14).toString()).commit()
            isDropDownMenuExpandedFontSize=false
        }, DropDownMenuItem("16dp",R.drawable.ic_baseline_text_increase_24)
        {
            mPreferenceManager.edit().putString("fontsize",(16).toString()).commit()
            isDropDownMenuExpandedFontSize=false
        }
    )

    val dropDownMenuItemListTerminalTheme= listOf<DropDownMenuItem>(
        DropDownMenuItem("Black Text on White",R.drawable.ic_baseline_text_increase_24)
        {
            mPreferenceManager.edit().putString("color",(0).toString()).commit()
            isDropDownMenuExpandedTerminalTheme=false
        }, DropDownMenuItem("White text on Black",R.drawable.ic_baseline_text_increase_24)
        {
            mPreferenceManager.edit().putString("color",(1).toString()).commit()
            isDropDownMenuExpandedTerminalTheme=false
        }, DropDownMenuItem("Solarized Light",R.drawable.ic_baseline_text_increase_24)
        {
            mPreferenceManager.edit().putString("color",(7).toString()).commit()
            isDropDownMenuExpandedTerminalTheme=false
        }, DropDownMenuItem("Solarized Dark",R.drawable.ic_baseline_text_increase_24)
        {
            mPreferenceManager.edit().putString("color",(8).toString()).commit()
            isDropDownMenuExpandedTerminalTheme=false
        }, DropDownMenuItem("Dark pastel",R.drawable.ic_baseline_text_increase_24)
        {
            mPreferenceManager.edit().putString("color",(10).toString()).commit()
            isDropDownMenuExpandedTerminalTheme=false
        }
    )
    val items = listOf(
      SettingsMenuItem("Theme","Select a Theme", resID = R.drawable.ic_baseline_photo_size_select_actual_24, clickable = { isDropDownMenuExpandedTheme=true
          isDropDownMenuExpandedFontSize=false
          isDropDownMenuExpandedTerminalTheme=false

                                                                                                                         },{
          Text(text = "Select a Theme",fontFamily = FontFamily(Font(R.font.custom_sans)))
      }, TrailingContent ={
          Icon(imageVector = Icons.Filled.ArrowDropDown, contentDescription = null, modifier = Modifier.clickable{isDropDownMenuExpandedTheme=true
              isDropDownMenuExpandedFontSize=false
              isDropDownMenuExpandedTerminalTheme=false})
          DropDownMenu(expanded = isDropDownMenuExpandedTheme , DropDownList = dropDownMenuItemListTheme) {
              isDropDownMenuExpandedTheme=false
          }}),
        SettingsMenuItem("Font Size","Set Terminal Font Size",
            resID = R.drawable.ic_baseline_format_size_24,{isDropDownMenuExpandedTheme=false
                isDropDownMenuExpandedFontSize=true
                isDropDownMenuExpandedTerminalTheme=false},
            {Text(text = "Select a Terminal Font Size",fontFamily = FontFamily(Font(R.font.custom_sans)))},
            TrailingContent ={
            Icon(imageVector = Icons.Filled.ArrowDropDown, contentDescription = null, modifier = Modifier.clickable{isDropDownMenuExpandedTheme=false
                isDropDownMenuExpandedFontSize=true
                isDropDownMenuExpandedTerminalTheme=false})
            DropDownMenu(expanded = isDropDownMenuExpandedFontSize , DropDownList = dropDownMenuItemListFontSize) {
                isDropDownMenuExpandedFontSize=false
            }}),
        SettingsMenuItem("Terminal Theme","Set Terminal Theme",
            resID = R.drawable.ic_baseline_terminal_24,{isDropDownMenuExpandedTheme=false
                isDropDownMenuExpandedFontSize=false
                isDropDownMenuExpandedTerminalTheme=true},
            {Text(text = "Select a Terminal Theme",fontFamily = FontFamily(Font(R.font.custom_sans)))},
            TrailingContent ={
                Icon(imageVector = Icons.Filled.ArrowDropDown, contentDescription = null, modifier = Modifier.clickable{isDropDownMenuExpandedTheme=false
                    isDropDownMenuExpandedFontSize=false
                    isDropDownMenuExpandedTerminalTheme=true})
                DropDownMenu(expanded = isDropDownMenuExpandedTerminalTheme, DropDownList = dropDownMenuItemListTerminalTheme) {
                    isDropDownMenuExpandedTerminalTheme=false
                }}),
        SettingsMenuItem("About","", resID = R.drawable.ic_baseline_info_24,
        {
                navigator.navigate(AboutScreenDestination)
        },{},{})
    )
    Scaffold(
        topBar =
        {
            TopAppBar(title = { Text(text = "Settings", fontFamily =FontFamily(Font(R.font.roboto_condensed_bold))) },
                modifier = Modifier.padding(10.dp,0.dp,10.dp,2.dp),
                actions = {
                })
            Divider()
        }
    ){
        LazyColumn(modifier = Modifier.padding(it)) {
            items(items){ SettingsMenuItem->
                ListItem(
                    modifier = Modifier.clickable(onClick = SettingsMenuItem.clickable),
                    headlineText = { Text(SettingsMenuItem.title, fontFamily = FontFamily(Font(R.font.custom_sans)), fontSize = 12.sp) },
                    supportingText = SettingsMenuItem.SupportingValue,
                    trailingContent = SettingsMenuItem.TrailingContent,
                    leadingContent = {
                        Icon(
                            painter = painterResource(id = SettingsMenuItem.resID),
                            contentDescription = "Localized description",
                        )
                    }
                )
                Divider()
                }
            }}}
@Composable
fun DropDownMenu(expanded:Boolean,DropDownList:List<DropDownMenuItem>,onDismissRequest:()->Unit){
    DropdownMenu(
        expanded = expanded ,
        onDismissRequest = onDismissRequest
    ) {
        DropDownList.forEach { DropDownMenuItem->
            DropdownMenuItem(
                text = { Text(DropDownMenuItem.Title) },
                onClick = DropDownMenuItem.Clickable,
                leadingIcon = {
                    Icon(
                        painter = painterResource(id = DropDownMenuItem.Icon),
                        contentDescription = null
                    )
                })
        }
    }

}