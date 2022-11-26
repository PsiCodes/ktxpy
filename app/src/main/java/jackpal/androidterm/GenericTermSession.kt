package jackpal.androidterm

import android.os.ParcelFileDescriptor
import android.support.v4.os.IResultReceiver._Parcel
import android.util.Log
import jackpal.androidterm.util.TermSettings
import jackpal.androidterm.emulatorview.TermSession
import jackpal.androidterm.GenericTermSession
import jackpal.androidterm.emulatorview.UpdateCallback
import com.termoneplus.TermIO
import java.io.IOException
import java.io.UnsupportedEncodingException
import java.lang.IllegalStateException

/**
 * A terminal session, consisting of a TerminalEmulator, a TranscriptScreen,
 * and the I/O streams used to talk to the process.
 */
internal open class GenericTermSession(
    public val mTermFd: ParcelFileDescriptor,
    settings: TermSettings,
    exitOnEOF: Boolean
) : TermSession(exitOnEOF) {
    private val createdAt: Long
    public val mTermFd1=mTermFd

    // A cookie which uniquely identifies this session.
    private var mHandle: String? = null
    private var mCloseWindowOnProcessExit = false
    private var mProcessExitMessage: String? = null

    init {
        termOut = ParcelFileDescriptor.AutoCloseOutputStream(mTermFd)
        termIn = ParcelFileDescriptor.AutoCloseInputStream(mTermFd)
        createdAt = System.currentTimeMillis()
        updatePrefs(settings)
    }

    fun updatePrefs(settings: TermSettings) {
        setColorScheme(settings.colorScheme)
        setDefaultUTF8Mode(settings.defaultToUTF8Mode())
        mCloseWindowOnProcessExit = settings.closeWindowOnProcessExit()
    }

    override fun initializeEmulator(columns: Int, rows: Int) {
        var columns = columns
        var rows = rows
        if (VTTEST_MODE) {
            columns = 80
            rows = 24
        }
        super.initializeEmulator(columns, rows)
        setPtyUTF8Mode()
        setUTF8ModeUpdateCallback { setPtyUTF8Mode() }
    }

    override fun updateSize(columns: Int, rows: Int) {
        var columns = columns
        var rows = rows
        if (VTTEST_MODE) {
            columns = 80
            rows = 24
        }
        // Inform the attached pty of our new size:
        setPtyWindowSize(rows, columns)
        super.updateSize(columns, rows)
    }

    /* XXX We should really get this ourselves from the resource bundle, but
       we cannot hold a context */
    fun setProcessExitMessage(message: String?) {
        mProcessExitMessage = message
    }

    override fun onProcessExit() {
        if (mCloseWindowOnProcessExit) {
            finish()
        } else if (mProcessExitMessage != null) {
            try {
                val msg = "\r\n[$mProcessExitMessage]".toByteArray(charset("UTF-8"))
                appendToEmulator(msg, 0, msg.size)
                notifyUpdate()
            } catch (e: UnsupportedEncodingException) {
                // Never happens
            }
        }
    }

    override fun finish() {
        try {
            mTermFd.close()
        } catch (e: IOException) {
            // ok
        }
        super.finish()
    }

    /**
     * Gets the terminal session's title.  Unlike the superclass's getTitle(),
     * if the title is null or an empty string, the provided default title will
     * be returned instead.
     *
     * @param defaultTitle The default title to use if this session's title is
     * unset or an empty string.
     */
    fun getTitle(defaultTitle: String): String {
        val title = title
        return if (title != null && title.length > 0) {
            title
        } else {
            defaultTitle
        }
    }

    var handle: String?
        get() = mHandle
        set(handle) {
            check(mHandle == null) { "Cannot change handle once set" }
            mHandle = handle
        }

    override fun toString(): String {
        return javaClass.simpleName + '(' + createdAt + ',' + mHandle + ')'
    }

    /**
     * Set the widow size for a given pty. Allows programs
     * connected to the pty learn how large their screen is.
     */
    private fun setPtyWindowSize(row: Int, col: Int) {
        // If the tty goes away too quickly, this may get called after it's descriptor is closed
        if (!mTermFd.fileDescriptor.valid()) return
        try {
            TermIO.setWindowSize(mTermFd, row, col)
        } catch (e: IOException) {
            Log.e("exec", "Failed to set window size: " + e.message)
            if (isFailFast) throw IllegalStateException(e)
        }
    }

    /**
     * Set or clear UTF-8 mode for a given pty.  Used by the terminal driver
     * to implement correct erase behavior in cooked mode (Linux >= 2.6.4).
     */
    private fun setPtyUTF8Mode() {
        // If the tty goes away too quickly, this may get called after it's descriptor is closed
        if (!mTermFd.fileDescriptor.valid()) return
        try {
            TermIO.setUTF8Input(mTermFd, utF8Mode)
        } catch (e: IOException) {
            Log.e("exec", "Failed to set UTF mode: " + e.message)
            if (isFailFast) throw IllegalStateException(e)
        }
    }

    /**
     * @return true, if failing to operate on file descriptor deserves an exception (never the case for ATE own shell)
     */
    open val isFailFast: Boolean
        get() = false

    companion object {
        //** Set to true to force into 80 x 24 for testing with vttest. */
        private const val VTTEST_MODE = false
        const val PROCESS_EXIT_FINISHES_SESSION = 0
        const val PROCESS_EXIT_DISPLAYS_MESSAGE = 1
    }
}