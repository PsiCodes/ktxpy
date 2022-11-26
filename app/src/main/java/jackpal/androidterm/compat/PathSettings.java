package jackpal.androidterm.compat;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.text.TextUtils;
import com.wildzeus.pythonktx.R;

import java.io.File;

import androidx.preference.PreferenceManager;


/* NOTE: refactored path settings from TermSettings.java
 * TODO: pending removal as functionality does not support multiple entries.
 */
@Deprecated
public class PathSettings {
    private static String mPrependPath = null;
    private static String mAppendPath = null;

    // extracted from SharedPreferences
    private static boolean path_verify;


    public PathSettings(Context context) {
        Resources res = context.getResources();
        path_verify = res.getBoolean(R.bool.pref_verify_path_default);
        extractPreferences(PreferenceManager.getDefaultSharedPreferences(context));
    }

    public void extractPreferences(SharedPreferences prefs) {
        path_verify = prefs.getBoolean("verify_path", path_verify);
    }

    public static String getPrependPath() {
        return mPrependPath;
    }

    public void setPrependPath(String prependPath) {
        mPrependPath = prependPath;
    }

    public static String getAppendPath() {
        return mAppendPath;
    }

    public void setAppendPath(String appendPath) {
        mAppendPath = appendPath;
    }

    public static String buildPATH() {
        String path = System.getenv("PATH");
        if (path == null) path = "";
        path = extendPath(path);
        if (path_verify)
            path = preservePath(path);
        return path;
    }

    private static String extendPath(String path) {
        String s;

        s = getAppendPath();
        if (!TextUtils.isEmpty(s))
            path = path + File.pathSeparator + s;

        s = getPrependPath();
        if (!TextUtils.isEmpty(s))
            path = s + File.pathSeparator + path;

        return path;
    }

    private static String preservePath(String path) {
        String[] entries = path.split(File.pathSeparator);
        StringBuilder new_path = new StringBuilder(path.length());
        for (String entry : entries) {
            File dir = new File(entry);
            try {
                if (!dir.isDirectory()) continue;
            } catch (SecurityException ignore) {
                continue;
            }
            if (dir.canExecute()) {
                new_path.append(entry);
                new_path.append(File.pathSeparator);
            }
        }
        return new_path.substring(0, new_path.length() - 1);
    }
}
