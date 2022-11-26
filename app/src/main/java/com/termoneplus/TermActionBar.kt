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
package com.termoneplus

import android.content.Context
import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.util.TypedValue
import android.view.MenuItem
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.AdapterView
import android.widget.Spinner
import androidx.annotation.ColorInt
import androidx.annotation.RequiresApi
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.material.navigation.NavigationView
import com.termoneplus.utils.WrapOpenURL
import com.wildzeus.pythonktx.R

/**
 * An action bar for terminal emulator activity.
 */
class TermActionBar private constructor(context: AppCompatActivity, floating: Boolean) {
    private val drawer: DrawerLayout
    private val nav_view: NavigationView
    private val toolbar: Toolbar
    private val spinner: Spinner

    init {
        toolbar = context.findViewById(R.id.toolbar)
        context.setSupportActionBar(toolbar)
        drawer = context.findViewById(R.id.drawer_layout)
        val toggle: ActionBarDrawerToggle = object : ActionBarDrawerToggle(
            context, drawer, toolbar,
            R.string.navigation_drawer_open, R.string.navigation_drawer_close
        ) {
            override fun onDrawerOpened(drawerView: View) {
                hideSoftInput(drawerView)
                super.onDrawerOpened(drawerView)
            }
        }
        nav_view = context.findViewById(R.id.nav_view)
        NavigationBackground.presetColors(context, nav_view)
        val appbar = context.supportActionBar
        if (appbar != null) {
            appbar.setDisplayShowTitleEnabled(false)
            appbar.setDisplayShowHomeEnabled(false)
        }
        spinner = context.findViewById(R.id.spinner)
        hide()
    }


    fun setOnItemSelectedListener(listener: OnItemSelectedListener) {
        val wrapper: AdapterView.OnItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View,
                    position: Int,
                    id: Long
                ) {
                    listener.onItemSelected(position)
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {}
            }
        spinner.onItemSelectedListener = wrapper
    }

    fun setOnNavigationItemSelectedListener(
        listener: NavigationView.OnNavigationItemSelectedListener
    ) {
        nav_view.setNavigationItemSelectedListener { item: MenuItem? ->
            val result = listener.onNavigationItemSelected(
                item!!
            )
            drawer.closeDrawer(GravityCompat.START)
            result
        }
    }

    fun setSelection(position: Int) {
        spinner.setSelection(position)
    }

    val isShowing: Boolean
        get() = toolbar.visibility == View.VISIBLE

    fun hide() {
        toolbar.visibility = View.GONE
    }

    fun show() {
        toolbar.visibility = View.VISIBLE
    }

    fun doToggleActionBar() {
        if (isShowing) {
            hide()
        } else {
            show()
        }
    }

    fun lockDrawer(flag: Boolean) {
        if (flag) drawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED) else drawer.setDrawerLockMode(
            DrawerLayout.LOCK_MODE_UNLOCKED
        )
    }

    private fun hideSoftInput(view: View) {
        object : Thread() {
            override fun run() {
                val context = view.context
                val imm =
                    context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                        ?: return
                val token = view.windowToken
                imm.hideSoftInputFromWindow(token, 0)
            }
        }.start()
    }

    fun onAppIconClicked(view: View) {
        WrapOpenURL.launch(view.context, urlApplicationSite())
    }

    fun onEmailAddressClicked(view: View) {
        WrapOpenURL.launch(view.context, urlApplicationMail())
    }

    interface OnItemSelectedListener {
        fun onItemSelected(position: Int)
    }

    private object NavigationBackground {
        fun presetColors(context: AppCompatActivity, view: NavigationView) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP /* API Level 21*/) {
                // managed as attribute in drawable header_background
                return
            }
            @ColorInt val colors = intArrayOf( // see header_background
                0x78909C /* Blue Gray 50, 400 */,
                0x607D8B /* Blue Gray 50, 500 */,
                0x455A64 /* Blue Gray 50, 700 */
            )
            val typedValue = TypedValue()
            val theme = context.theme
            if (theme.resolveAttribute(R.attr.colorPrimaryLight, typedValue, true)) colors[0] =
                typedValue.data
            if (theme.resolveAttribute(
                    androidx.appcompat.R.attr.colorPrimary,
                    typedValue,
                    true
                )
            ) colors[1] = typedValue.data
            if (theme.resolveAttribute(
                    androidx.appcompat.R.attr.colorPrimaryDark,
                    typedValue,
                    true
                )
            ) colors[2] = typedValue.data
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN /* API Level 16*/) Compat16.setColors(
                view,
                colors
            ) else Compat1.setColors(view, colors)
        }

        @RequiresApi(16)
        private object Compat16 {
            fun setColors(view: NavigationView, colors: IntArray) {
                try {
                    val header = view.getHeaderView(0)
                    val drawable = header.background as GradientDrawable
                    drawable.colors = colors
                } catch (ignore: Exception) {
                }
            }
        }

        private object Compat1 {
            // note suppression is not redundant - for setBackgroundDrawable
            fun setColors(view: NavigationView, colors: IntArray) {
                try {
                    val header = view.getHeaderView(0)
                    val drawable = GradientDrawable(GradientDrawable.Orientation.TL_BR, colors)
                    header.setBackgroundDrawable(drawable)
                } catch (ignore: Exception) {
                }
            }
        }
    }

    private fun urlApplicationSite(): String {
        val context = drawer.context
        return context.resources.getString(R.string.application_site)
    }

    private fun urlApplicationMail(): String {
        val context = drawer.context
        return "mailto:" + context.resources.getString(R.string.application_email)
    }

    companion object {
        @JvmStatic
        fun setTermContentView(context: AppCompatActivity, floating: Boolean): TermActionBar {
            if (floating) context.setContentView(R.layout.drawer_term_floatbar) else context.setContentView(
                R.layout.drawer_term
            )
            return TermActionBar(context, floating)
        }
    }
}