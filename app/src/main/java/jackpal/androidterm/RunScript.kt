package jackpal.androidterm

import android.content.Intent
import android.text.TextUtils
import android.util.Log
import com.Application
import java.util.Locale

/*
 * New procedure for launching a command in ATE.
 * Build the path and arguments into a Uri and set that into Intent.data.
 * intent.data(new Uri.Builder().setScheme("file").setPath(path).setFragment(arguments))
 *
 * The old procedure of using Intent.Extra is still available but is discouraged.
 */
class RunScript : RemoteInterface() {
    override fun processAction(intent: Intent, action: String) {
        when (action) {
            Application.ACTION_RUN_SCRIPT ->                 /* Someone with the appropriate permissions has asked us to run a script */runScript(
                intent
            )
        }
    }

    private fun runScript(intent: Intent) {
        var command: String? = null

        // First look in Intent URI (data) for the path; if not there, revert to
        // the "shell command" location.
        val uri = intent.data
        if (uri != null) {
            var s = uri.scheme
            if (s != null) s = s.lowercase(Locale.getDefault())
            if (s != null) {
                when (s) {
                    "file" -> {
                        command = uri.path
                        if (TextUtils.isEmpty(command))
                        command = quoteForBash(command)

                        // consider scheme fragment as command arguments
                        s = uri.fragment
                        if (s != null) command += " $s"
                    }
                }
            }
        }
        if (command == null) {
            command = intent.getStringExtra(Application.ARGUMENT_SHELL_COMMAND)
            /* TODO: use of quoteForBash()
               Call of quoteForBash() was added by commit
               "rewrite processing on RunScript similarly to RunShortcut"
               but it breaks intents sample "Run Script".
            if (command != null)
                command = quoteForBash(command);
            */
        }
        if (command == null) {
            Log.e(Application.APP_TAG, "No command provided in script!")
            return
        }
        var handle = intent.getStringExtra(Application.ARGUMENT_WINDOW_HANDLE)
        handle = handle?.let {
            // Target the request at an existing window if open
            appendToWindow(it, command)
        } ?: // Open a new window
                openNewWindow(command)
        val result = Intent()
        result.putExtra(Application.ARGUMENT_WINDOW_HANDLE, handle)
        setResult(RESULT_OK, result)
    }
}