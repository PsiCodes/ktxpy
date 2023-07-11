package jackpal.androidterm;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.ParcelFileDescriptor;
import android.util.Log;

import com.Application;
import com.termoneplus.Process;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import jackpal.androidterm.compat.PathSettings;
import jackpal.androidterm.util.TermSettings;


/**
 * A terminal session, controlling the process attached to the session (usually
 * a shell). It keeps track of process PID and destroys it's process group
 * upon stopping.
 */
public class ShellTermSession extends GenericTermSession {
    private static final int PROCESS_EXITED = 1;

    private final String mInitialCommand;
    private final int mProcId;
    private final Thread mWatcherThread;


    public ShellTermSession(TermSettings settings, String initialCommand) throws IOException {
        super(ParcelFileDescriptor.open(new File("/dev/ptmx"), ParcelFileDescriptor.MODE_READ_WRITE),
                settings, false);

        mInitialCommand = initialCommand;

        mProcId = createShellProcess(settings);
        final Handler handler = new ProcessHandler(this);
        mWatcherThread  = new Thread(() -> {
            Log.i(Application.APP_TAG, "waiting for: " + mProcId);
            int result = Process.waitExit(mProcId);
            Log.i(Application.APP_TAG, "subprocess exited: " + result);
            handler.sendMessage(handler.obtainMessage(PROCESS_EXITED, result));
        });
        mWatcherThread.setName("Process watcher");
    }

    @Override
    public void initializeEmulator(int columns, int rows) {
        super.initializeEmulator(columns, rows);

        mWatcherThread.start();
        sendInitialCommand(mInitialCommand);
    }

    private void sendInitialCommand(String initialCommand) {
        if (initialCommand.length() > 0) {
            write(initialCommand + '\r');
        }
    }

    private int createShellProcess(TermSettings settings) throws IOException {
        String shell = settings.getShell();

        ArrayList<String> argList = parse(shell);
        String arg0;
        String[] args;

        try {
            arg0 = argList.get(0);
            File file = new File(arg0);
            if (!file.exists()) {
                Log.e(Application.APP_TAG, "Shell " + arg0 + " not found!");
                throw new FileNotFoundException(arg0);
            } else if (!file.canExecute()) {
                Log.e(Application.APP_TAG, "Shell " + arg0 + " not executable!");
                throw new FileNotFoundException(arg0);
            }
            args = argList.toArray(new String[0]);
        } catch (Exception e) {
            argList = parse(settings.getFailsafeShell());
            arg0 = argList.get(0);
            args = argList.toArray(new String[0]);
        }

        Map<String, String> map = new HashMap<>(System.getenv());
        map.put("TERM", settings.getTermType());
        map.put("PATH", Application.xbindir.getPath() + File.pathSeparator + PathSettings.buildPATH());
        map.put("HOME", settings.getHomePath());
        map.put("TMPDIR", Application.getTmpPath());
        map.put("ENV", Application.getScriptFilePath());

        String[] env = new String[map.size()];
        int k = 0;
        for (Map.Entry<String, String> entry : map.entrySet())
            env[k++] = entry.getKey() + "=" + entry.getValue();

        return Process.createSubprocess(getMTermFd1(), arg0, args, env);
    }

    private ArrayList<String> parse(String cmd) {
        final int PLAIN = 0;
        final int WHITESPACE = 1;
        final int INQUOTE = 2;
        int state = WHITESPACE;
        ArrayList<String> result =  new ArrayList<>();
        int cmdLen = cmd.length();
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < cmdLen; i++) {
            char c = cmd.charAt(i);
            if (state == PLAIN) {
                if (Character.isWhitespace(c)) {
                    result.add(builder.toString());
                    builder.delete(0,builder.length());
                    state = WHITESPACE;
                } else if (c == '"') {
                    state = INQUOTE;
                } else {
                    builder.append(c);
                }
            } else if (state == WHITESPACE) {
                if (Character.isWhitespace(c)) {
                    // do nothing
                } else if (c == '"') {
                    state = INQUOTE;
                } else {
                    state = PLAIN;
                    builder.append(c);
                }
            } else if (state == INQUOTE) {
                if (c == '\\') {
                    if (i + 1 < cmdLen) {
                        i += 1;
                        builder.append(cmd.charAt(i));
                    }
                } else if (c == '"') {
                    state = PLAIN;
                } else {
                    builder.append(c);
                }
            }
        }
        if (builder.length() > 0) {
            result.add(builder.toString());
        }
        return result;
    }

    private void onProcessExit(int result) {
        onProcessExit();
    }

    @Override
    public void finish() {
        Process.finishChilds(mProcId);
        super.finish();
    }

    private static class ProcessHandler extends Handler {
        private final WeakReference<ShellTermSession> reference;

        ProcessHandler(ShellTermSession session) {
            super(Looper.getMainLooper());
            reference = new WeakReference<>(session);
        }

        @Override
        public void handleMessage(Message msg) {
            ShellTermSession session = reference.get();
            if (session == null) return;
            if (!session.isRunning()) return;

            if (msg.what == PROCESS_EXITED)
                session.onProcessExit((Integer) msg.obj);
        }
    }
}
