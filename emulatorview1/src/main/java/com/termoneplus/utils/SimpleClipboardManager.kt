package com.termoneplus.utils

import android.content.ClipData
import android.content.ClipDescription
import android.content.ClipboardManager
import android.content.Context

class SimpleClipboardManager(context: Context) {
    private val clip: ClipboardManager

    init {
        clip = context.applicationContext
            .getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    }

    var text: CharSequence?
        get() {
            val item = clip.primaryClip!!.getItemAt(0)
            return item.text
        }
        set(text) {
            val clipData = ClipData.newPlainText("simple text", text)
            clip.setPrimaryClip(clipData)
        }

    fun hasText(): Boolean {
        return clip.hasPrimaryClip() &&
                clip.primaryClipDescription!!.hasMimeType(ClipDescription.MIMETYPE_TEXT_PLAIN)
    }
}