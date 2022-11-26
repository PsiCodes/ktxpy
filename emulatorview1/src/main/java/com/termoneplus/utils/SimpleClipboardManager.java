package com.termoneplus.utils;


import android.content.ClipData;
import android.content.ClipDescription;
import android.content.ClipboardManager;
import android.content.Context;


public class SimpleClipboardManager {
    private final ClipboardManager clip;

    public SimpleClipboardManager(Context context) {
        clip = (ClipboardManager) context.getApplicationContext()
                .getSystemService(Context.CLIPBOARD_SERVICE);
    }

    public CharSequence getText() {
        ClipData.Item item = clip.getPrimaryClip().getItemAt(0);
        return item.getText();
    }

    public void setText(CharSequence text) {
        ClipData clipData = ClipData.newPlainText("simple text", text);
        clip.setPrimaryClip(clipData);
    }

    public boolean hasText() {
        return (clip.hasPrimaryClip() &&
                clip.getPrimaryClipDescription().hasMimeType(ClipDescription.MIMETYPE_TEXT_PLAIN));
    }
}

