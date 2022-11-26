package jackpal.androidterm;

import android.content.Intent;
import android.net.Uri;
import android.text.TextUtils;

import com.Application;
import com.termoneplus.RemoteActionActivity;
import com.termoneplus.TermActivity;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

import androidx.annotation.NonNull;
import jackpal.androidterm.emulatorview.TermSession;


public class RemoteInterface extends RemoteActionActivity {

    /**
     * Quote a string so it can be used as a parameter in bash and similar shells.
     */
    public static String quoteForBash(String s) {
        StringBuilder builder = new StringBuilder();
        String specialChars = "\"\\$`!";
        builder.append('"');
        int length = s.length();
        for (int i = 0; i < length; i++) {
            char c = s.charAt(i);
            if (specialChars.indexOf(c) >= 0) {
                builder.append('\\');
            }
            builder.append(c);
        }
        builder.append('"');
        return builder.toString();
    }

    @Override
    protected void processAction(@NonNull Intent intent, @NonNull String action) {
        if (Intent.ACTION_SEND.equals(action)) {
            /* "permission.RUN_SCRIPT" not required as this is merely opening a new window. */
            processSendAction(intent);
            return;
        }
        // Intent sender may not have permissions, ignore any extras
        openNewWindow(null);
    }

    private void processSendAction(@NonNull Intent intent) {
        if (intent.hasExtra(Intent.EXTRA_STREAM)) {
            Object extraStream = intent.getExtras().get(Intent.EXTRA_STREAM);
            if (extraStream instanceof Uri) {
                Uri uri = (Uri) extraStream;
                String scheme = uri.getScheme();
                if (TextUtils.isEmpty(scheme)) {
                    openNewWindow(null);
                    return;
                }
                switch (scheme) {
                    case "file": {
                        String path = uri.getPath();
                        File file = new File(path);
                        String dirPath = file.isDirectory() ? path : file.getParent();
                        openNewWindow("cd " + quoteForBash(dirPath));
                        return;
                    }
                }
            }
        }
        openNewWindow(null);
    }

    protected String openNewWindow(String iInitialCommand) {
        TermService service = getTermService();

        try {
            TermSession session = TermActivity.createTermSession(this, mSettings, iInitialCommand);

            service.addSession(session);

            String handle = UUID.randomUUID().toString();
            ((GenericTermSession) session).setHandle(handle);

            Intent intent = new Intent(this, TermActivity.class)
                    .setAction(Application.ACTION_OPEN_NEW_WINDOW)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);

            return handle;
        } catch (IOException e) {
            return null;
        }
    }

    protected String appendToWindow(String handle, String iInitialCommand) {
        TermService service = getTermService();

        // Find the target window
        GenericTermSession target = null;
        int index;
        for (index = 0; index < service.getSessionCount(); ++index) {
            GenericTermSession session = (GenericTermSession) service.getSession(index);
            String h = session.getHandle();
            if (h != null && h.equals(handle)) {
                target = session;
                break;
            }
        }

        if (target == null) {
            // Target window not found, open a new one
            return openNewWindow(iInitialCommand);
        }

        if (iInitialCommand != null) {
            target.write(iInitialCommand);
            target.write('\r');
        }

        Intent intent = new Intent(this, TermActivity.class)
                .setAction(Application.ACTION_SWITCH_WINDOW)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                .putExtra(Application.ARGUMENT_TARGET_WINDOW, index);
        startActivity(intent);

        return handle;
    }
}
