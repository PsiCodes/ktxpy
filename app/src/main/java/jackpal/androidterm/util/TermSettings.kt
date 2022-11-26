package jackpal.androidterm.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.view.KeyEvent;
import com.termoneplus.Settings;
import com.wildzeus.pythonktx.R;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import androidx.annotation.IntDef;
import androidx.preference.PreferenceManager;
import jackpal.androidterm.emulatorview.ColorScheme;


/**
 * Terminal emulator settings
 */
public class TermSettings {
    private SharedPreferences mPrefs;

    private int mStatusBar;
    private int mActionBarMode;
    @Orientation
    private int mOrientation;
    private int mSizeCalc;
    private int mCursorStyle;
    private int mCursorBlink;
    private int mFontSize;
    private int mColorId;
    private boolean mUTF8ByDefault;
    private int mBackKeyAction;
    private int mControlKeyId;
    private int mFnKeyId;
    private int mUseCookedIME;
    private String mShell;
    private String mFailsafeShell;
    private String mTermType;
    private boolean mCloseOnExit;
    private String mHomePath;

    private boolean mAltSendsEsc;

    private boolean mMouseTracking;

    private boolean mUseKeyboardShortcuts;

    public static final int ACTION_BAR_MODE_ALWAYS_VISIBLE = 1;
    public static final int ACTION_BAR_MODE_HIDES = 2;
    private static final int ACTION_BAR_MODE_MAX = 2;

    @Retention(RetentionPolicy.SOURCE)
    @IntDef({ORIENTATION_UNSPECIFIED, ORIENTATION_LANDSCAPE, ORIENTATION_PORTRAIT})
    public @interface Orientation {}
    public static final int ORIENTATION_UNSPECIFIED = 0;
    public static final int ORIENTATION_LANDSCAPE = 1;
    public static final int ORIENTATION_PORTRAIT = 2;

    /** An integer not in the range of real key codes. */
    public static final int KEYCODE_NONE = -1;

    public static final int CONTROL_KEY_ID_NONE = 7;
    public static final int[] CONTROL_KEY_SCHEMES = {
        KeyEvent.KEYCODE_DPAD_CENTER,
        KeyEvent.KEYCODE_AT,
        KeyEvent.KEYCODE_ALT_LEFT,
        KeyEvent.KEYCODE_ALT_RIGHT,
        KeyEvent.KEYCODE_VOLUME_UP,
        KeyEvent.KEYCODE_VOLUME_DOWN,
        KeyEvent.KEYCODE_CAMERA,
        KEYCODE_NONE
    };

    public static final int FN_KEY_ID_NONE = 7;
    public static final int[] FN_KEY_SCHEMES = {
        KeyEvent.KEYCODE_DPAD_CENTER,
        KeyEvent.KEYCODE_AT,
        KeyEvent.KEYCODE_ALT_LEFT,
        KeyEvent.KEYCODE_ALT_RIGHT,
        KeyEvent.KEYCODE_VOLUME_UP,
        KeyEvent.KEYCODE_VOLUME_DOWN,
        KeyEvent.KEYCODE_CAMERA,
        KEYCODE_NONE
    };

    public static final int BACK_KEY_STOPS_SERVICE = 0;
    public static final int BACK_KEY_CLOSES_WINDOW = 1;
    public static final int BACK_KEY_CLOSES_ACTIVITY = 2;
    public static final int BACK_KEY_SENDS_ESC = 3;
    public static final int BACK_KEY_SENDS_TAB = 4;
    private static final int BACK_KEY_MAX = 4;

    public TermSettings(Context context) {
        readDefaultPrefs(context.getResources());
        readPrefs(context, PreferenceManager.getDefaultSharedPreferences(context));
    }

    private void readDefaultPrefs(Resources res) {
        mStatusBar = Integer.parseInt(res.getString(R.string.pref_statusbar_default));
        mActionBarMode = res.getInteger(R.integer.pref_actionbar_default);
        mOrientation = res.getInteger(R.integer.pref_orientation_default);
        mSizeCalc = res.getInteger(R.integer.pref_sizecalc_default);
        mCursorStyle = Integer.parseInt(res.getString(R.string.pref_cursorstyle_default));
        mCursorBlink = Integer.parseInt(res.getString(R.string.pref_cursorblink_default));
        mFontSize = 9;
        mColorId = Integer.parseInt(res.getString(R.string.pref_color_default));
        mUTF8ByDefault = res.getBoolean(R.bool.pref_utf8_by_default_default);
        mBackKeyAction = Integer.parseInt(res.getString(R.string.pref_backaction_default));
        mControlKeyId = Integer.parseInt(res.getString(R.string.pref_controlkey_default));
        mFnKeyId = Integer.parseInt(res.getString(R.string.pref_fnkey_default));
        mUseCookedIME = Integer.parseInt(res.getString(R.string.pref_ime_default));
        mFailsafeShell = res.getString(R.string.pref_shell_default);
        mShell = mFailsafeShell;
        mTermType = res.getString(R.string.pref_termtype_default);
        mCloseOnExit = res.getBoolean(R.bool.pref_close_window_on_process_exit_default);
        // the mHomePath default is set dynamically in readPrefs()
        mAltSendsEsc = res.getBoolean(R.bool.pref_alt_sends_esc_default);
        mMouseTracking = res.getBoolean(R.bool.pref_mouse_tracking_default);
        mUseKeyboardShortcuts = res.getBoolean(R.bool.pref_use_keyboard_shortcuts_default);
    }

    public void readPrefs(Context context, SharedPreferences prefs) {
        mPrefs = prefs;
        mStatusBar = readIntPref("statusbar", mStatusBar, 1);
        mActionBarMode = readIntPref("actionbar", mActionBarMode, ACTION_BAR_MODE_MAX);
        mOrientation = readIntPref("orientation", mOrientation, 2);
        mSizeCalc = readIntPref("sizecalc", mSizeCalc, 1);
        // mCursorStyle = readIntPref(CURSORSTYLE_KEY, mCursorStyle, 2);
        // mCursorBlink = readIntPref(CURSORBLINK_KEY, mCursorBlink, 1);
        mFontSize = readIntPref("fontsize", mFontSize, 288);
        mColorId = readIntPref("color", mColorId, Settings.color_schemes.length - 1);
        mUTF8ByDefault = readBooleanPref("utf8_by_default", mUTF8ByDefault);
        mBackKeyAction = readIntPref("backaction", mBackKeyAction, BACK_KEY_MAX);
        mControlKeyId = readIntPref("controlkey", mControlKeyId,
                CONTROL_KEY_SCHEMES.length - 1);
        mFnKeyId = readIntPref("fnkey", mFnKeyId,
                FN_KEY_SCHEMES.length - 1);
        mUseCookedIME = readIntPref("ime", mUseCookedIME, 1);
        mShell = readStringPref("shell", mShell);
        mTermType = readStringPref("termtype", mTermType);
        mCloseOnExit = readBooleanPref("close_window_on_process_exit", mCloseOnExit);
        mHomePath = readStringPref(context.getString(R.string.key_home_path_preference), mHomePath);
        mAltSendsEsc = readBooleanPref("alt_sends_esc", mAltSendsEsc);
        mMouseTracking = readBooleanPref("mouse_tracking", mMouseTracking);
        mUseKeyboardShortcuts = readBooleanPref("use_keyboard_shortcuts",
                mUseKeyboardShortcuts);
        mPrefs = null;  // we leak a Context if we hold on to this
    }

    private int readIntPref(String key, int defaultValue, int maxValue) {
        int val;
        try {
            val = Integer.parseInt(
                mPrefs.getString(key, Integer.toString(defaultValue)));
        } catch (NumberFormatException e) {
            val = defaultValue;
        }
        val = Math.max(0, Math.min(val, maxValue));
        return val;
    }

    private String readStringPref(String key, String defaultValue) {
        return mPrefs.getString(key, defaultValue);
    }

    private boolean readBooleanPref(String key, boolean defaultValue) {
        return mPrefs.getBoolean(key, defaultValue);
    }

    public boolean showStatusBar() {
        return (mStatusBar != 0);
    }

    public int actionBarMode() {
        return mActionBarMode;
    }

    @Orientation
    public int getScreenOrientation() {
        return mOrientation;
    }

    public int getScreenCalcMethod() {
        return mSizeCalc;
    }

    public int getCursorStyle() {
        return mCursorStyle;
    }

    public int getCursorBlink() {
        return mCursorBlink;
    }

    public int getFontSize() {
        return mFontSize;
    }

    public ColorScheme getColorScheme() {
        return Settings.color_schemes[mColorId];
    }

    public boolean defaultToUTF8Mode() {
        return mUTF8ByDefault;
    }

    public int getBackKeyAction() {
        return mBackKeyAction;
    }

    public boolean backKeySendsCharacter() {
        return mBackKeyAction >= BACK_KEY_SENDS_ESC;
    }

    public boolean getAltSendsEscFlag() {
        return mAltSendsEsc;
    }

    public boolean getMouseTrackingFlag() {
        return mMouseTracking;
    }

    public boolean getUseKeyboardShortcutsFlag() {
        return mUseKeyboardShortcuts;
    }

    public int getBackKeyCharacter() {
        switch (mBackKeyAction) {
            case BACK_KEY_SENDS_ESC: return 27;
            case BACK_KEY_SENDS_TAB: return 9;
            default: return 0;
        }
    }

    public int getControlKeyId() {
        return mControlKeyId;
    }

    public int getFnKeyId() {
        return mFnKeyId;
    }

    public int getControlKeyCode() {
        return CONTROL_KEY_SCHEMES[mControlKeyId];
    }

    public int getFnKeyCode() {
        return FN_KEY_SCHEMES[mFnKeyId];
    }

    public boolean useCookedIME() {
        return (mUseCookedIME != 0);
    }

    public String getShell() {
        return mShell;
    }

    public String getFailsafeShell() {
        return mFailsafeShell;
    }

    public String getTermType() {
        return mTermType;
    }

    public boolean closeWindowOnProcessExit() {
        return mCloseOnExit;
    }

    public void setHomePath(String homePath) {
        mHomePath = homePath;
    }

    public String getHomePath() {
        return mHomePath;
    }
}
