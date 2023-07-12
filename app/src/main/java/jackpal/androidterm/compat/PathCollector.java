package jackpal.androidterm.compat;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.wildzeus.pythonktx.BuildConfig;

import java.io.File;
import java.text.Collator;
import java.util.Arrays;
import java.util.Locale;


/* NOTE: refactored broadcast functionality from Term.java
 * Applications that target Android 8.0 (Oreo, API Level 26) or higher no
 * longer receive implicit broadcasts registered in their manifest.
 * Broadcast registered at run-time are excluded but we would like
 * to receive paths from all application not only from running.
 * TODO: pending removal of deprecated path collection based on broadcasts.
 */
@Deprecated
public class PathCollector {
    // NOTE: use termoneplus development broadcasts!
    private static final String ACTION_PATH_APPEND_BROADCAST = BuildConfig.APPLICATION_ID + ".broadcast.APPEND_TO_PATH";
    private static final String ACTION_PATH_PREPEND_BROADCAST = BuildConfig.APPLICATION_ID + ".broadcast.PREPEND_TO_PATH";
    private static final String PERMISSION_PATH_APPEND_BROADCAST = BuildConfig.APPLICATION_ID + ".permission.APPEND_TO_PATH";
    private static final String PERMISSION_PATH_PREPEND_BROADCAST = BuildConfig.APPLICATION_ID + ".permission.PREPEND_TO_PATH";

    private final PathSettings settings;
    private int pending;
    private OnPathsReceivedListener callback;

    public PathCollector(AppCompatActivity context) {
        settings = new PathSettings(context);
        pending = 0;
        BroadcastReceiver receiver = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                if (action == null) return;

                String path = makePathFromBundle(getResultExtras(false));
                switch (action) {
                    case ACTION_PATH_PREPEND_BROADCAST:
                        settings.setPrependPath(path);
                        break;
                    case ACTION_PATH_APPEND_BROADCAST:
                        settings.setAppendPath(path);
                        break;
                    default:
                        return;
                }
                --pending;

                if (pending <= 0 && callback != null)
                    callback.onPathsReceived();
            }
        };

        ++pending;
        Intent broadcast = new Intent(ACTION_PATH_APPEND_BROADCAST);
        broadcast.addFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
        context.sendOrderedBroadcast(broadcast, PERMISSION_PATH_APPEND_BROADCAST,
                receiver, null, AppCompatActivity.RESULT_OK, null, null);

        ++pending;
        broadcast = new Intent(broadcast);
        broadcast.setAction(ACTION_PATH_PREPEND_BROADCAST);
        context.sendOrderedBroadcast(broadcast, PERMISSION_PATH_PREPEND_BROADCAST,
                receiver, null, AppCompatActivity.RESULT_OK, null, null);
    }

    private static String makePathFromBundle(Bundle extras) {
        if (extras == null || extras.size() == 0)
            return "";

        String[] keys = new String[extras.size()];
        keys = extras.keySet().toArray(keys);
        Collator collator = Collator.getInstance(Locale.US);
        Arrays.sort(keys, collator);

        StringBuilder path = new StringBuilder();
        for (String key : keys) {
            String dir = extras.getString(key);
            if (dir != null && !dir.equals("")) {
                path.append(dir);
                path.append(File.pathSeparator);
            }
        }

        return path.substring(0, path.length() - 1);
    }

    public void extractPreferences(SharedPreferences prefs) {
        settings.extractPreferences(prefs);
    }

    public void setOnPathsReceivedListener(OnPathsReceivedListener listener) {
        callback = listener;
    }

    public interface OnPathsReceivedListener {
        void onPathsReceived();
    }
}
