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

import com.wildzeus.pythonktx.R
import com.termoneplus.utils.ThemeManager
import android.widget.Toast
import android.view.Gravity
import androidx.appcompat.app.AppCompatActivity
import java.lang.Exception

open class AppCompatActivity() : AppCompatActivity() {
    protected var themeId: Int? = null
        private set

    override fun setTheme(resid: Int) {
        var actionbar = false
        try {
            if (R.style.AppTheme == packageManager.getActivityInfo(
                    componentName,
                    0
                ).theme
            ) actionbar = true
        } catch (ignore: Exception) {
        }
        themeId = ThemeManager.presetTheme(this, actionbar, resid)
        super.setTheme(themeId!!)
    }

    protected fun restart(rid: Int) {
        if (rid != 0) {
            val toast = Toast.makeText(applicationContext, rid, Toast.LENGTH_LONG)
            toast.setGravity(Gravity.CENTER, 0, 0)
            toast.show()
        }

        /* Let use function from API level 11
        Intent intent = Intent.makeRestartActivityTask(getComponentName());
        startActivity(intent);
        finish();
        */recreate()
    }
}