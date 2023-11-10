package github.psicodes.ktxpy.activities

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.KeyEvent
import android.view.MotionEvent
import androidx.activity.ComponentActivity
import androidx.activity.addCallback
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.res.ResourcesCompat
import com.blankj.utilcode.util.ClipboardUtils
import com.blankj.utilcode.util.KeyboardUtils
import com.termux.terminal.BuildConfig
import com.termux.terminal.TerminalEmulator
import com.termux.terminal.TerminalSession
import com.termux.terminal.TerminalSessionClient
import com.termux.view.TerminalRenderer
import com.termux.view.TerminalView
import com.termux.view.TerminalViewClient
import github.psicodes.ktxpy.R
import github.psicodes.ktxpy.ui.theme.KtxPyTheme
import github.psicodes.ktxpy.utils.Commands
import github.psicodes.ktxpy.utils.Keys
import java.io.File
import java.lang.ref.WeakReference


class TermActivity : ComponentActivity(),TerminalViewClient {
    private lateinit var mTermView : TerminalView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent{
            KtxPyTheme {
                TermScreen()
            }
        }
        onBackPressedDispatcher.addCallback(this,true) {
            mTermView.mTermSession.finishIfRunning()
            finish()
        }
    }

    @Composable
    fun TermScreen()
    {
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { context ->
                mTermView = TerminalView(context , null)
                Log.d(TAG,"Terminal has been created")
                mTermView.attachSession(getTerminalSession())
                mTermView.setTerminalViewClient(this)
                mTermView.mRenderer= TerminalRenderer(20,ResourcesCompat.getFont(this, R.font.jetbrainsmono_medium)!!)
                val fileName=intent.getStringExtra(Keys.KEY_FILE_PATH)
                if(fileName!=null){
                    val command = fileName.let { Commands.getInterpreterCommand(this, it) }
                    Log.d(TAG, "Command is $command")
                    Handler(Looper.getMainLooper()).post {
                        mTermView.mTermSession.write("$command\r")
                    }
                }
                else if (intent.getBooleanExtra(Keys.IS_SHELL_MODE_KEY,false))
                {
                    Handler(Looper.getMainLooper()).post {
                        mTermView.mTermSession.write("${Commands.getPythonShellCommand(this)}\r")
                    }
                }
                else {
                    Handler(Looper.getMainLooper()).post {
                        mTermView.mTermSession.write("${Commands.getBasicCommand(this)}\r")
                    }
                }
                mTermView.setBackgroundColor(this.getColor(R.color.terminal_colour))
                mTermView
            }
        )
    }

    private fun getTerminalSession(): TerminalSession {
        val cwd = filesDir.absolutePath
        var shell = "/bin/sh"
        if (File("/bin/sh").exists().not())
        {
            shell="/system/bin/sh"
        }
        return TerminalSession(
            shell,
            cwd, arrayOf<String>(),
            arrayOf(),
            TerminalEmulator.DEFAULT_TERMINAL_TRANSCRIPT_ROWS,
            getTermSessionClient()
        )
    }
    override fun logError(tag: String?, message: String?) {
        if (BuildConfig.DEBUG && message != null) {
            Log.e(tag, message)
        }
    }

    override fun logWarn(tag: String?, message: String?) {
        if (BuildConfig.DEBUG && message != null) {
            Log.w(tag, message)
        }
    }

    override fun logInfo(tag: String?, message: String?) {
        if (BuildConfig.DEBUG && message != null) {
            Log.i(tag, message)
        }
    }

    override fun logDebug(tag: String?, message: String?) {
        if (BuildConfig.DEBUG && message != null) {
            Log.d(tag, message)
        }
    }

    override fun logVerbose(tag: String?, message: String?) {
        if (BuildConfig.DEBUG && message != null) {
            Log.v(tag, message)
        }
    }

    override fun logStackTraceWithMessage(
        tag: String?,
        message: String?,
        e: Exception?
    ) {
        if (BuildConfig.DEBUG) {
            Log.e(tag, message + "\n" + Log.getStackTraceString(e))
        }
    }

    override fun logStackTrace(tag: String?, e: Exception?) {
        if (BuildConfig.DEBUG) {
            Log.e(tag, Log.getStackTraceString(e))
        }
    }
    override fun onScale(scale: Float): Float {
        return scale
    }

    override fun onSingleTapUp(e: MotionEvent?) {
        if (mTermView.mTermSession.isRunning) {
            mTermView.requestFocus()
            KeyboardUtils.showSoftInput(mTermView)
        }
    }

    override fun shouldBackButtonBeMappedToEscape(): Boolean { return false }

    override fun shouldEnforceCharBasedInput(): Boolean {
        return true
    }

    override fun shouldUseCtrlSpaceWorkaround(): Boolean {
        return false
    }

    override fun isTerminalViewSelected(): Boolean {
        return true
    }

    override fun copyModeChanged(copyMode: Boolean) { }

    override fun onKeyDown(keyCode: Int, e: KeyEvent?, session: TerminalSession?): Boolean {
        return false
    }

    override fun onKeyUp(keyCode: Int, e: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (mTermView.mTermSession.isRunning) {
                mTermView.mTermSession.finishIfRunning()
                finish()
            }
            return true
        }
        return false
    }

    override fun onLongPress(event: MotionEvent?): Boolean {
        return false
    }

    override fun readControlKey(): Boolean {
        return false
    }

    override fun readAltKey(): Boolean {
        return false
    }

    override fun readShiftKey(): Boolean {
        return false
    }

    override fun readFnKey(): Boolean {
        return false
    }

    override fun onCodePoint(
        codePoint: Int,
        ctrlDown: Boolean,
        session: TerminalSession?
    ): Boolean {
        return false
    }
    private fun getTermSessionClient() : TerminalSessionClient
    {
        val weakActivityReference = WeakReference(this)
        return object : TerminalSessionClient {
            override fun onTextChanged(changedSession: TerminalSession) {
                runOnUiThread {
                    weakActivityReference.get()?.mTermView?.onScreenUpdated()
                }
            }

            override fun onTitleChanged(updatedSession: TerminalSession) {  }

            override fun onSessionFinished(finishedSession: TerminalSession) {
                runOnUiThread{
                    weakActivityReference.get()?.mTermView?.let {
                        KeyboardUtils.hideSoftInput(it)
                        it.mTermSession?.finishIfRunning()
                    }
                    finish()
                }
            }

            override fun onCopyTextToClipboard(session: TerminalSession, text: String?) {
                ClipboardUtils.copyText(text)
            }

            override fun onPasteTextFromClipboard(session: TerminalSession?) {
                runOnUiThread{
                    val clip = ClipboardUtils.getText().toString()
                    if (clip.trim { it <= ' ' }
                            .isNotEmpty() && weakActivityReference.get()?.mTermView?.mEmulator != null) {
                        weakActivityReference.get()?.mTermView?.mEmulator?.paste(clip)
                    }
                }
            }

            override fun onBell(session: TerminalSession) { }

            override fun onColorsChanged(changedSession: TerminalSession) { }

            override fun onTerminalCursorStateChange(state: Boolean) { }

            override fun getTerminalCursorStyle(): Int {
                return TerminalEmulator.TERMINAL_CURSOR_STYLE_UNDERLINE
            }

            override fun logError(tag: String?, message: String?) {
                if (BuildConfig.DEBUG && message != null) {
                    Log.e(tag, message)
                }
            }

            override fun logWarn(tag: String?, message: String?) {
                if (BuildConfig.DEBUG && message != null) {
                    Log.w(tag, message)
                }
            }

            override fun logInfo(tag: String?, message: String?) {
                if (BuildConfig.DEBUG && message != null) {
                    Log.i(tag, message)
                }
            }

            override fun logDebug(tag: String?, message: String?) {
                if (BuildConfig.DEBUG && message != null) {
                    Log.d(tag, message)
                }
            }

            override fun logVerbose(tag: String?, message: String?) {
                if (BuildConfig.DEBUG && message != null) {
                    Log.v(tag, message)
                }
            }

            override fun logStackTraceWithMessage(
                tag: String?,
                message: String?,
                e: Exception?
            ) {
                if (BuildConfig.DEBUG) {
                    Log.e(tag, message + "\n" + Log.getStackTraceString(e))
                }
            }

            override fun logStackTrace(tag: String?, e: Exception?) {
                if (BuildConfig.DEBUG) {
                    Log.e(tag, Log.getStackTraceString(e))
                }
            }

        }
    }
    override fun onEmulatorSet() { }
    companion object {
        private const val TAG = "TermActivity"
    }
}
/*
H=$PATH:/data/app/~~Hfk1Sq2A4XNtLfCsC9OZSQ==/github.psicodes.ktxpy-oxz5xUgjGGvDM-ewPK-G0A==/lib/arm64 && export PYTHONHOME=/data/user/0/github.psicodes.ktxpy/files/files/usr && /data/app/~~Hfk1Sq2A4XNtLfCsC9OZSQ==/github.psicodes.ktxpy-oxz5xUgjGGvDM-ewPK-G0A==/lib/arm64 && export LD_LIBRARY_PATH="$LD_LIBRARY_PATH:" && export LD_LIBRARY_PATH="$LD_LIBRARY_PATH/data/user/0/github.psicodes.ktxpy/files/files/usr/lib" && clear && libpython3.so /data/user/0/github.psicodes.ktxpy/files/pythonFiles/ok.py && echo '[Enter to Exit]' && read junk && exit
 */