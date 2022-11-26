package com.termoneplus.compat

import android.icu.lang.UCharacter
import android.os.Build
import androidx.annotation.RequiresApi
import android.icu.lang.UProperty
import android.text.AndroidCharacter

object CharacterCompat {
    @JvmStatic
    fun charCount(cp: Int /*code point*/): Int {
        return if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N /*API Level 24*/) Compat1.charCount(
            cp
        ) else Compat24.charCount(cp)
    }

    @JvmStatic
    fun toChars(cp: Int, dst: CharArray, dstIndex: Int): Int {
        return if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N /*API Level 24*/) Compat1.toChars(
            cp,
            dst,
            dstIndex
        ) else Compat24.toChars(cp, dst, dstIndex)
    }

    @JvmStatic
    fun isEastAsianDoubleWidth(ch: Int /*code point*/): Boolean {
        return if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N /*API Level 24*/) Compat1.isEastAsianDoubleWidth(
            ch
        ) else Compat24.isEastAsianDoubleWidth(ch)
    }

    private object Compat1 {
        fun charCount(cp: Int): Int {
            return Character.charCount(cp)
        }

        fun toChars(cp: Int, dst: CharArray, dstIndex: Int): Int {
            return Character.toChars(cp, dst, dstIndex)
        }

        fun isEastAsianDoubleWidth(ch: Int): Boolean {
            // Android's getEastAsianWidth() only works for BMP characters
            // use fully-qualified class name to avoid deprecation warning on import
            when (AndroidCharacter.getEastAsianWidth(ch.toChar())) {
                AndroidCharacter.EAST_ASIAN_WIDTH_FULL_WIDTH, AndroidCharacter.EAST_ASIAN_WIDTH_WIDE -> return true
            }
            return false
        }
    }

    @RequiresApi(24)
    private object Compat24 {
        fun charCount(cp: Int): Int {
            return UCharacter.charCount(cp)
        }

        fun toChars(cp: Int, dst: CharArray, dstIndex: Int): Int {
            return UCharacter.toChars(cp, dst, dstIndex)
        }

        fun isEastAsianDoubleWidth(ch: Int): Boolean {
            val ea = UCharacter.getIntPropertyValue(ch, UProperty.EAST_ASIAN_WIDTH)
            when (ea) {
                UCharacter.EastAsianWidth.FULLWIDTH, UCharacter.EastAsianWidth.WIDE -> return true
            }
            return false
        }
    }
}