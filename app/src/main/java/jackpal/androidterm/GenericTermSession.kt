package jackpal.androidterm;

import java.io.*;

import android.os.ParcelFileDescriptor;
import android.util.Log;

import com.termoneplus.TermIO;

import jackpal.androidterm.emulatorview.TermSession;
import jackpal.androidterm.util.TermSettings;


/**
 * A terminal session, consisting of a TerminalEmulator, a TranscriptScreen,
 * and the I/O streams used to talk to the process.
 */
class GenericTermSession extends TermSession {
    //** Set to true to force into 80 x 24 for testing with vttest. */
    private static final boolean VTTEST_MODE = false;

    private final long createdAt;

    // A cookie which uniquely identifies this session.
    private String mHandle;

    final ParcelFileDescriptor mTermFd;

    public static final int PROCESS_EXIT_FINISHES_SESSION = 0;
    public static final int PROCESS_EXIT_DISPLAYS_MESSAGE = 1;

    private boolean mCloseWindowOnProcessExit;
    private String mProcessExitMessage;

    GenericTermSession(ParcelFileDescriptor mTermFd, TermSettings settings, boolean exitOnEOF) {
        super(exitOnEOF);

        this.mTermFd = mTermFd;
        setTermOut(new ParcelFileDescriptor.AutoCloseOutputStream(mTermFd));
        setTermIn(new ParcelFileDescriptor.AutoCloseInputStream(mTermFd));

        this.createdAt = System.currentTimeMillis();

        updatePrefs(settings);
    }

    public void updatePrefs(TermSettings settings) {
        setColorScheme(settings.getColorScheme());
        setDefaultUTF8Mode(settings.defaultToUTF8Mode());
        mCloseWindowOnProcessExit = settings.closeWindowOnProcessExit();
    }

    @Override
    public void initializeEmulator(int columns, int rows) {
        if (VTTEST_MODE) {
            columns = 80;
            rows = 24;
        }
        super.initializeEmulator(columns, rows);

        setPtyUTF8Mode();
        setUTF8ModeUpdateCallback(this::setPtyUTF8Mode);
    }

    @Override
    public void updateSize(int columns, int rows) {
        if (VTTEST_MODE) {
            columns = 80;
            rows = 24;
        }
        // Inform the attached pty of our new size:
        setPtyWindowSize(rows, columns);
        super.updateSize(columns, rows);
    }

    /* XXX We should really get this ourselves from the resource bundle, but
       we cannot hold a context */
    public void setProcessExitMessage(String message) {
        mProcessExitMessage = message;
    }

    @Override
    protected void onProcessExit() {
        if (mCloseWindowOnProcessExit) {
            finish();
        } else if (mProcessExitMessage != null) {
            try {
                byte[] msg = ("\r\n[" + mProcessExitMessage + "]").getBytes("UTF-8");
                appendToEmulator(msg, 0, msg.length);
                notifyUpdate();
            } catch (UnsupportedEncodingException e) {
                // Never happens
            }
        }
    }

    @Override
    public void finish() {
        try {
            mTermFd.close();
        } catch (IOException e) {
            // ok
        }

        super.finish();
    }

    /**
     * Gets the terminal session's title.  Unlike the superclass's getTitle(),
     * if the title is null or an empty string, the provided default title will
     * be returned instead.
     *
     * @param defaultTitle The default title to use if this session's title is
     *     unset or an empty string.
     */
    public String getTitle(String defaultTitle) {
        String title = getTitle();
        if (title != null && title.length() > 0) {
            return title;
        } else {
            return defaultTitle;
        }
    }

    public void setHandle(String handle) {
        if (mHandle != null) {
            throw new IllegalStateException("Cannot change handle once set");
        }
        mHandle = handle;
    }

    public String getHandle() {
        return mHandle;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + '(' + createdAt + ',' + mHandle + ')';
    }

    /**
     * Set the widow size for a given pty. Allows programs
     * connected to the pty learn how large their screen is.
     */
    private void setPtyWindowSize(int row, int col) {
        // If the tty goes away too quickly, this may get called after it's descriptor is closed
        if (!mTermFd.getFileDescriptor().valid())
            return;

        try {
            TermIO.setWindowSize(mTermFd, row, col);
        } catch (IOException e) {
            Log.e("exec", "Failed to set window size: " + e.getMessage());

            if (isFailFast())
                throw new IllegalStateException(e);
        }
    }

    /**
     * Set or clear UTF-8 mode for a given pty.  Used by the terminal driver
     * to implement correct erase behavior in cooked mode (Linux >= 2.6.4).
     */
    private void setPtyUTF8Mode() {
        // If the tty goes away too quickly, this may get called after it's descriptor is closed
        if (!mTermFd.getFileDescriptor().valid())
            return;

        try {
            TermIO.setUTF8Input(mTermFd, getUTF8Mode());
        } catch (IOException e) {
            Log.e("exec", "Failed to set UTF mode: " + e.getMessage());

            if (isFailFast())
                throw new IllegalStateException(e);
        }
    }

    /**
     * @return true, if failing to operate on file descriptor deserves an exception (never the case for ATE own shell)
     */
    boolean isFailFast() {
        return false;
    }
}
