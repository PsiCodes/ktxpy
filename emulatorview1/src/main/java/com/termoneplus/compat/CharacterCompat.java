package com.termoneplus.compat;

import android.icu.lang.UCharacter;
import android.icu.lang.UProperty;
import android.os.Build;

import androidx.annotation.RequiresApi;


public class CharacterCompat {
    public static int charCount(int cp /*code point*/) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N /*API Level 24*/)
            return Compat1.charCount(cp);
        else
            return Compat24.charCount(cp);
    }

    public static int toChars(int cp, char[] dst, int dstIndex) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N /*API Level 24*/)
            return Compat1.toChars(cp, dst, dstIndex);
        else
            return Compat24.toChars(cp, dst, dstIndex);
    }

    public static boolean isEastAsianDoubleWidth(int ch /*code point*/) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N /*API Level 24*/)
            return Compat1.isEastAsianDoubleWidth(ch);
        else
            return Compat24.isEastAsianDoubleWidth(ch);
    }

    private static class Compat1 {
        private static int charCount(int cp) {
            return Character.charCount(cp);
        }

        private static int toChars(int cp, char[] dst, int dstIndex) {
            return Character.toChars(cp, dst, dstIndex);
        }

        @SuppressWarnings("deprecation")
        private static boolean isEastAsianDoubleWidth(int ch) {
            // Android's getEastAsianWidth() only works for BMP characters
            // use fully-qualified class name to avoid deprecation warning on import
            switch (android.text.AndroidCharacter.getEastAsianWidth((char) ch)) {
                case android.text.AndroidCharacter.EAST_ASIAN_WIDTH_FULL_WIDTH:
                case android.text.AndroidCharacter.EAST_ASIAN_WIDTH_WIDE:
                    return true;
            }
            return false;
        }
    }

    @RequiresApi(24)
    private static class Compat24 {
        private static int charCount(int cp) {
            return UCharacter.charCount(cp);
        }

        private static int toChars(int cp, char[] dst, int dstIndex) {
            return UCharacter.toChars(cp, dst, dstIndex);
        }

        private static boolean isEastAsianDoubleWidth(int ch) {
            int ea = UCharacter.getIntPropertyValue(ch, UProperty.EAST_ASIAN_WIDTH);
            switch (ea) {
                case UCharacter.EastAsianWidth.FULLWIDTH:
                case UCharacter.EastAsianWidth.WIDE:
                    return true;
            }
            return false;
        }
    }
}
