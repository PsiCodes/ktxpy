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
package com.termoneplus.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect

object TextIcon {
    fun create(text: String, color: Int, width: Int, height: Int): Bitmap? {
        val text_lines = text.split("\\s*\n\\s*".toRegex()).toTypedArray()
        val lines = text_lines.size
        for (k in 0 until lines) text_lines[k] = text_lines[k].trim { it <= ' ' }
        val shadow_offset = 12.0f
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        run {
            // initialize paint attributes
            paint.textSize = 192f
            paint.setShadowLayer(shadow_offset / 4.0f, shadow_offset, shadow_offset, -0x1000000)
            paint.color = color
            paint.isSubpixelText = true
            paint.textAlign = Paint.Align.CENTER
        }
        var maxAscent = 0
        var textH: Float
        var textW: Float
        var textS: Float
        run {
            // get bounds for each text line
            var maxDescent = 0
            var minL = 1000000
            var maxR = 0
            val bounds = Rect()
            for (line in text_lines) {
                paint.getTextBounds(line, 0, line.length, bounds)
                maxAscent = Math.max(maxAscent, -bounds.top)
                maxDescent = Math.max(maxDescent, bounds.bottom)
                minL = Math.min(minL, bounds.left)
                maxR = Math.max(maxR, bounds.right)
            }
            val maxH = maxAscent + maxDescent
            val maxW = maxR - minL

            // line space: 10% of text line height
            textH = (1.1f * lines - 0.1f) * maxH
            textW = maxW.toFloat()
            textS = if (lines > 1) 1.1f * maxH else maxH.toFloat()
            textH += shadow_offset
        }
        var bitmapH: Int
        var bitmapW: Int
        run {
            // calculate bitmap size taking into account requested aspect
            val aspect = width.toFloat() / height
            // text padding: 7%, i.e. 1 / ( 1 - 2 * 7% ) = 1 / 0.86
            val scale = 1.0f / 0.86f
            val size: Float
            if (textW / textH > aspect) {
                size = scale * textW
                bitmapH = Math.ceil((size / aspect).toDouble()).toInt()
                bitmapW = Math.ceil(size.toDouble()).toInt()
            } else {
                size = scale * textH
                bitmapH = Math.ceil(size.toDouble()).toInt()
                bitmapW = Math.ceil((size * aspect).toDouble()).toInt()
            }
        }
        val bitmap: Bitmap
        bitmap = try {
            Bitmap.createBitmap(bitmapH, bitmapW, Bitmap.Config.ARGB_8888)
        } catch (e: OutOfMemoryError) {
            return null
        }
        bitmap.density = Bitmap.DENSITY_NONE
        val top = (bitmapH - textH - shadow_offset) / 2.0f
        val centerV = bitmapW / 2.0f
        var baseline = top + maxAscent
        run {
            val canvas = Canvas(bitmap)
            var k = 0
            while (k < lines) {
                canvas.drawText(text_lines[k], centerV, baseline, paint)
                ++k
                baseline += textS
            }
        }
        return Bitmap.createScaledBitmap(bitmap, width, height, true)
    }

    fun create(context: Context, text: String, color: Int): Bitmap? {
        val r = context.resources
        val dm = r.displayMetrics

        // launcher icon size = 32 dp * ( dpi / 160 ) * 1.5
        val x = Math.round(dm.xdpi * 0.3f)
        val y = Math.round(dm.ydpi * 0.3f)
        return create(text, color, x, y)
    }
}