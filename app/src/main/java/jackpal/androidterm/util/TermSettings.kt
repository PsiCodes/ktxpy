package jackpal.androidterm.util

import android.content.Context
import android.content.SharedPreferences
import android.content.res.Resources
import android.view.KeyEvent
import androidx.annotation.IntDef
import androidx.preference.PreferenceManager
import com.termoneplus.Settings
import jackpal.androidterm.util.TermSettings
import com.wildzeus.pythonktx.R
import jackpal.androidterm.emulatorview.ColorScheme
import java.lang.NumberFormatException
import java.lang.annotation.Retention
import java.lang.annotation.RetentionPolicy

/**
 * Terminal emulator settings
 */
class TermSettings(context: Context) {
    private var mPrefs: SharedPreferences? = null
    private var mStatusBar = 0
    private var mActionBarMode = 0

    @get:Orientation
    @Orientation
    var screenOrientation = 0
        private set
    var screenCalcMethod = 0
        private set
    var cursorStyle = 0
        private set
    var cursorBlink = 0
        private set
    var fontSize = 0
        private set
    private var mColorId = 0
    private var mUTF8ByDefault = false
    var backKeyAction = 0
        private set
    var controlKeyId = 0
        private set
    var fnKeyId = 0
        private set
    private var mUseCookedIME = 0
    var shell: String? = null
        private set
    var failsafeShell: String? = null
        private set
    var termType: String? = null
        private set
    private var mCloseOnExit = false
    var homePath: String? = null
    var altSendsEscFlag = false
        private set
    var mouseTrackingFlag = false
        private set
    var useKeyboardShortcutsFlag = false
        private set
    annotation class Orientation

    init {
        readDefaultPrefs(context.resources)
        readPrefs(context, PreferenceManager.getDefaultSharedPreferences(context))
    }

    private fun readDefaultPrefs(res: Resources) {
        mStatusBar = res.getString(R.string.pref_statusbar_default).toInt()
        mActionBarMode = res.getInteger(R.integer.pref_actionbar_default)
        screenOrientation = res.getInteger(R.integer.pref_orientation_default)
        screenCalcMethod = res.getInteger(R.integer.pref_sizecalc_default)
        cursorStyle = res.getString(R.string.pref_cursorstyle_default).toInt()
        cursorBlink = res.getString(R.string.pref_cursorblink_default).toInt()
        fontSize = 9
        mColorId = res.getString(R.string.pref_color_default).toInt()
        mUTF8ByDefault = res.getBoolean(R.bool.pref_utf8_by_default_default)
        backKeyAction = res.getString(R.string.pref_backaction_default).toInt()
        controlKeyId = res.getString(R.string.pref_controlkey_default).toInt()
        fnKeyId = res.getString(R.string.pref_fnkey_default).toInt()
        mUseCookedIME = res.getString(R.string.pref_ime_default).toInt()
        failsafeShell = res.getString(R.string.pref_shell_default)
        shell = failsafeShell
        termType = res.getString(R.string.pref_termtype_default)
        mCloseOnExit = res.getBoolean(R.bool.pref_close_window_on_process_exit_default)
        // the mHomePath default is set dynamically in readPrefs()
        altSendsEscFlag = res.getBoolean(R.bool.pref_alt_sends_esc_default)
        mouseTrackingFlag = res.getBoolean(R.bool.pref_mouse_tracking_default)
        useKeyboardShortcutsFlag = res.getBoolean(R.bool.pref_use_keyboard_shortcuts_default)
    }

    fun readPrefs(context: Context, prefs: SharedPreferences?) {
        mPrefs = prefs
        mStatusBar = readIntPref("statusbar", mStatusBar, 1)
        mActionBarMode = readIntPref("actionbar", mActionBarMode, ACTION_BAR_MODE_MAX)
        screenOrientation = readIntPref("orientation", screenOrientation, 2)
        screenCalcMethod = readIntPref("sizecalc", screenCalcMethod, 1)
        // mCursorStyle = readIntPref(CURSORSTYLE_KEY, mCursorStyle, 2);
        // mCursorBlink = readIntPref(CURSORBLINK_KEY, mCursorBlink, 1);
        fontSize = readIntPref("fontsize", fontSize, 288)
        mColorId = readIntPref("color", mColorId, Settings.color_schemes.size - 1)
        mUTF8ByDefault = readBooleanPref("utf8_by_default", mUTF8ByDefault)
        backKeyAction = readIntPref("backaction", backKeyAction, BACK_KEY_MAX)
        controlKeyId = readIntPref(
            "controlkey", controlKeyId,
            CONTROL_KEY_SCHEMES.size - 1
        )
        fnKeyId = readIntPref(
            "fnkey", fnKeyId,
            FN_KEY_SCHEMES.size - 1
        )
        mUseCookedIME = readIntPref("ime", mUseCookedIME, 1)
        shell = readStringPref("shell", shell)
        termType = readStringPref("termtype", termType)
        mCloseOnExit = readBooleanPref("close_window_on_process_exit", mCloseOnExit)
        homePath = readStringPref(context.getString(R.string.key_home_path_preference), homePath)
        altSendsEscFlag = readBooleanPref("alt_sends_esc", altSendsEscFlag)
        mouseTrackingFlag = readBooleanPref("mouse_tracking", mouseTrackingFlag)
        useKeyboardShortcutsFlag = readBooleanPref(
            "use_keyboard_shortcuts",
            useKeyboardShortcutsFlag
        )
        mPrefs = null // we leak a Context if we hold on to this
    }

    private fun readIntPref(key: String, defaultValue: Int, maxValue: Int): Int {
        var `val`: Int
        `val` = try {
            mPrefs!!.getString(key, Integer.toString(defaultValue))!!.toInt()
        } catch (e: NumberFormatException) {
            defaultValue
        }
        `val` = Math.max(0, Math.min(`val`, maxValue))
        return `val`
    }

    private fun readStringPref(key: String, defaultValue: String?): String? {
        return mPrefs!!.getString(key, defaultValue)
    }

    private fun readBooleanPref(key: String, defaultValue: Boolean): Boolean {
        return mPrefs!!.getBoolean(key, defaultValue)
    }

    fun showStatusBar(): Boolean {
        return mStatusBar != 0
    }

    fun actionBarMode(): Int {
        return mActionBarMode
    }

    val colorScheme: ColorScheme
        get() = Settings.color_schemes[mColorId]

    fun defaultToUTF8Mode(): Boolean {
        return mUTF8ByDefault
    }

    fun backKeySendsCharacter(): Boolean {
        return backKeyAction >= BACK_KEY_SENDS_ESC
    }

    val backKeyCharacter: Int
        get() = when (backKeyAction) {
            BACK_KEY_SENDS_ESC -> 27
            BACK_KEY_SENDS_TAB -> 9
            else -> 0
        }
    val controlKeyCode: Int
        get() = CONTROL_KEY_SCHEMES[controlKeyId]
    val fnKeyCode: Int
        get() = FN_KEY_SCHEMES[fnKeyId]

    fun useCookedIME(): Boolean {
        return mUseCookedIME != 0
    }

    fun closeWindowOnProcessExit(): Boolean {
        return mCloseOnExit
    }

    companion object {
        const val ACTION_BAR_MODE_ALWAYS_VISIBLE = 1
        const val ACTION_BAR_MODE_HIDES = 2
        private const val ACTION_BAR_MODE_MAX = 2
        const val ORIENTATION_UNSPECIFIED = 0
        const val ORIENTATION_LANDSCAPE = 1
        const val ORIENTATION_PORTRAIT = 2

        /** An integer not in the range of real key codes.  */
        const val KEYCODE_NONE = -1
        const val CONTROL_KEY_ID_NONE = 7
        val CONTROL_KEY_SCHEMES = intArrayOf(
            KeyEvent.KEYCODE_DPAD_CENTER,
            KeyEvent.KEYCODE_AT,
            KeyEvent.KEYCODE_ALT_LEFT,
            KeyEvent.KEYCODE_ALT_RIGHT,
            KeyEvent.KEYCODE_VOLUME_UP,
            KeyEvent.KEYCODE_VOLUME_DOWN,
            KeyEvent.KEYCODE_CAMERA,
            KEYCODE_NONE
        )
        const val FN_KEY_ID_NONE = 7
        val FN_KEY_SCHEMES = intArrayOf(
            KeyEvent.KEYCODE_DPAD_CENTER,
            KeyEvent.KEYCODE_AT,
            KeyEvent.KEYCODE_ALT_LEFT,
            KeyEvent.KEYCODE_ALT_RIGHT,
            KeyEvent.KEYCODE_VOLUME_UP,
            KeyEvent.KEYCODE_VOLUME_DOWN,
            KeyEvent.KEYCODE_CAMERA,
            KEYCODE_NONE
        )
        const val BACK_KEY_STOPS_SERVICE = 0
        const val BACK_KEY_CLOSES_WINDOW = 1
        const val BACK_KEY_CLOSES_ACTIVITY = 2
        const val BACK_KEY_SENDS_ESC = 3
        const val BACK_KEY_SENDS_TAB = 4
        private const val BACK_KEY_MAX = 4
    }
}