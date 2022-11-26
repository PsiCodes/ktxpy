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
package com.termoneplus.compat

import android.content.Context
import android.graphics.Rect
import android.os.Build
import com.termoneplus.compat.SoftInputCompat.Compat31
import com.termoneplus.compat.SoftInputCompat.Compat3
import android.util.DisplayMetrics
import android.view.View
import android.view.inputmethod.InputMethodManager
import com.termoneplus.compat.SoftInputCompat

object SoftInputCompat {
    @JvmStatic
    fun toggle(view: View) {
        val context = view.context
        val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager

        // TODO: to use only API 31-33 compatible code
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S /*API level 31*/) Compat31.toggle(
            view,
            imm
        ) else Compat3.toggle(imm)
    }

    private fun isSoftInputVisible(view: View): Boolean {
        val root = view.rootView
        val r = Rect()
        root.getWindowVisibleDisplayFrame(r)
        /* "root view" height - status bar height (r.top) - visible frame height (r.bottom - r.top) */
        val delta = root.bottom - r.bottom

        /* threshold size: dp to pixels, multiply with display density */
        val metrics = root.resources.displayMetrics
        /* 128dp = 32dp * 4, minimum button height 32dp and generic 4 rows soft keyboard */return delta > 128 /*threshold*/ * metrics.density
    }

    private object Compat31 {
        fun toggle(view: View, imm: InputMethodManager) {
            if (isSoftInputVisible(view)) {
                imm.hideSoftInputFromWindow(view.windowToken, 0)
            } else {
                if (!view.requestFocus()) return
                // NOTE: SHOW_FORCED was deprecated in API level 33.
                imm.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT)
            }
        }
    }

    private object Compat3 {
        fun toggle(imm: InputMethodManager) {
            // Toggle method was deprecated in API level 31.
            imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0)
        }
    }
}