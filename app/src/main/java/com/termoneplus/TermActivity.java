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
package com.termoneplus;

import android.annotation.SuppressLint;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.view.ContextMenu;
import android.view.Gravity;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.termoneplus.utils.ScriptImporter;
import com.termoneplus.utils.ThemeManager;
import com.wildzeus.pythonktx.R;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.preference.PreferenceManager;

import jackpal.androidterm.emulatorview.TermSession;


public class TermActivity extends jackpal.androidterm.Term {
    private final ActivityResultLauncher<Intent> request_paste_script =
            registerForActivityResult(
                    new ActivityResultContracts.StartActivityForResult(),
                    result -> onRequestPasteScript(result.getResultCode(), result.getData())
            );


    @Override
    public void onCreate(Bundle icicle) {
        SharedPreferences mPreferenceManager= PreferenceManager.getDefaultSharedPreferences(this);
        mPreferenceManager.edit().putString("shell",getIntent().getStringExtra("Command")).commit();
        super.onCreate(icicle);
    }

    private void doPasteScript() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT)
                .setType("text/*")
                .putExtra("CONTENT_TYPE", "text/x-shellscript")
                .putExtra("TITLE", R.string.script_intent_title);
        try {
            request_paste_script.launch(intent);
        } catch (ActivityNotFoundException ignore) {
            Toast toast = Toast.makeText(getApplicationContext(),
                    R.string.script_source_content_error, Toast.LENGTH_LONG);
            toast.setGravity(Gravity.TOP, 0, 0);
            toast.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void onRequestPasteScript(int resultCode, @Nullable Intent data) {
        if (resultCode != RESULT_OK) return;
        if (data == null) return;

        TermSession session = getCurrentTermSession();
        if (session == null) return;
        ScriptImporter.paste(this, data.getData(), session);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);

        menu.setHeaderTitle(R.string.edit_text);

        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_session, menu);
        if (!canPaste()) {
            MenuItem item = menu.findItem(R.id.session_paste);
            if (item != null) item.setEnabled(false);
        }
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        int id = item.getItemId();
        /* NOTE: Resource IDs will be non-final in Android Gradle Plugin version 5.0,
           avoid using them in switch case statements */
        if (id == R.id.session_select_text)
            getCurrentEmulatorView().toggleSelectingText();
        else if (id == R.id.session_copy_all)
            doCopyAll();
        else if (id == R.id.session_paste)
            doPaste();
        else if (id == R.id.session_paste_script)
            doPasteScript();
        else if (id == R.id.session_send_cntr)
            getCurrentEmulatorView().sendControlKey();
        else if (id == R.id.session_send_fn)
            getCurrentEmulatorView().sendFnKey();
        else
            return super.onContextItemSelected(item);
        return true;
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        // do not process preference "Theme Mode"
        if (ThemeManager.PREF_THEME_MODE.equals(key)) return;

        super.onSharedPreferenceChanged(sharedPreferences, key);
    }

    @Override
    protected void updatePrefs() {
        Integer theme_resid = getThemeId();
        if (theme_resid != null) {
            if (theme_resid != ThemeManager.presetTheme(this, false, theme_resid)) {
                restart(R.string.restart_thememode_change);
                return;
            }
        }
        super.updatePrefs();
    }
}
