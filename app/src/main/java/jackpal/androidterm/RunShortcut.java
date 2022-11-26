package jackpal.androidterm;

import android.content.Intent;
import android.util.Log;

import com.Application;

import java.security.GeneralSecurityException;

import androidx.annotation.NonNull;
import jackpal.androidterm.util.ShortcutEncryption;


public final class RunShortcut extends RemoteInterface {

    @Override
    protected void processAction(@NonNull Intent intent, @NonNull String action) {
        switch (action) {
            case Application.ACTION_RUN_SHORTCUT:
                runShortcut(intent);
                break;
        }
    }

    private void runShortcut(@NonNull Intent intent) {
        // Decrypt and verify the command
        String command;
        try {
            String request = intent.getStringExtra(Application.ARGUMENT_SHELL_COMMAND);
            if (request == null) {
                Log.e(Application.APP_TAG, "No command provided in shortcut!");
                return;
            }
            ShortcutEncryption.Keys keys = ShortcutEncryption.getKeys(this);
            if (keys == null) {
                // No keys -- no valid shortcuts can exist
                Log.e(Application.APP_TAG, "No shortcut encryption keys found!");
                return;
            }
            command = ShortcutEncryption.decrypt(request, keys);
        } catch (GeneralSecurityException e) {
            Log.e(Application.APP_TAG, "Invalid shortcut: " + e.toString());
            return;
        }

        String handle = intent.getStringExtra(Application.ARGUMENT_WINDOW_HANDLE);
        if (handle != null) {
            // Target the request at an existing window if open
            handle = appendToWindow(handle, command);
        } else {
            // Open a new window
            handle = openNewWindow(command);
        }

        Intent result = new Intent();
        result.putExtra(Application.ARGUMENT_WINDOW_HANDLE, handle);
        setResult(RESULT_OK, result);
    }
}
