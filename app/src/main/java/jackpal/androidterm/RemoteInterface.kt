package jackpal.androidterm

import android.content.Intent
import android.net.Uri
import android.text.TextUtils
import com.Application
import com.termoneplus.RemoteActionActivity
import com.termoneplus.TermActivity
import java.io.File
import java.io.IOException
import java.util.UUID

open class RemoteInterface : RemoteActionActivity() {
    override fun processAction(intent: Intent, action: String) {
        if (Intent.ACTION_SEND == action) {
            /* "permission.RUN_SCRIPT" not required as this is merely opening a new window. */
            processSendAction(intent)
            return
        }
        // Intent sender may not have permissions, ignore any extras
        openNewWindow(null)
    }

    private fun processSendAction(intent: Intent) {
        if (intent.hasExtra(Intent.EXTRA_STREAM)) {
            val extraStream = intent.extras!![Intent.EXTRA_STREAM]
            if (extraStream is Uri) {
                val uri = extraStream
                val scheme = uri.scheme
                if (TextUtils.isEmpty(scheme)) {
                    openNewWindow(null)
                    return
                }
                when (scheme) {
                    "file" -> {
                        val path = uri.path
                        val file = File(path)
                        val dirPath = if (file.isDirectory) path else file.parent
                        openNewWindow("cd " + quoteForBash(dirPath))
                        return
                    }
                }
            }
        }
        openNewWindow(null)
    }

    protected fun openNewWindow(iInitialCommand: String?): String? {
        val service = termService!!
        return try {
            val session = TermActivity.createTermSession(this, mSettings, iInitialCommand)
            service.addSession(session)
            val handle = UUID.randomUUID().toString()
            (session as GenericTermSession).handle = handle
            val intent = Intent(this, TermActivity::class.java)
                .setAction(Application.ACTION_OPEN_NEW_WINDOW)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
            handle
        } catch (e: IOException) {
            null
        }
    }

    protected fun appendToWindow(handle: String, iInitialCommand: String?): String? {
        val service = termService!!

        // Find the target window
        var target: GenericTermSession? = null
        var index: Int
        index = 0
        while (index < service.sessionCount) {
            val session = service.getSession(index) as GenericTermSession
            val h = session.handle
            if (h != null && h == handle) {
                target = session
                break
            }
            ++index
        }
        if (target == null) {
            // Target window not found, open a new one
            return openNewWindow(iInitialCommand)
        }
        if (iInitialCommand != null) {
            target.write(iInitialCommand)
            target.write('\r'.code)
        }
        val intent = Intent(this, TermActivity::class.java)
            .setAction(Application.ACTION_SWITCH_WINDOW)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            .putExtra(Application.ARGUMENT_TARGET_WINDOW, index)
        startActivity(intent)
        return handle
    }

    companion object {
        /**
         * Quote a string so it can be used as a parameter in bash and similar shells.
         */
        fun quoteForBash(s: String?): String {
            val builder = StringBuilder()
            val specialChars = "\"\\$`!"
            builder.append('"')
            val length = s!!.length
            for (i in 0 until length) {
                val c = s[i]
                if (specialChars.indexOf(c) >= 0) {
                    builder.append('\\')
                }
                builder.append(c)
            }
            builder.append('"')
            return builder.toString()
        }
    }
}