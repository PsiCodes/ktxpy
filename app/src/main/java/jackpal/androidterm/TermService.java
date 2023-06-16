package jackpal.androidterm;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Binder;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.ParcelFileDescriptor;
import android.os.ResultReceiver;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;

import com.Application;
import com.termoneplus.TermActivity;
import com.termoneplus.services.CommandService;

import java.util.UUID;

import jackpal.androidterm.emulatorview.TermSession;
import jackpal.androidterm.libtermexec.v1.ITerminal;
import jackpal.androidterm.util.SessionList;
import jackpal.androidterm.util.TermSettings;
import com.wildzeus.pythonktx.R;

public class TermService extends Service {
    private static final int RUNNING_NOTIFICATION = 1;

    private final IBinder mTSBinder = new TSBinder();
    private final SessionList mTermSessions = new SessionList();
    private CommandService command_service;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        if (TermExec.SERVICE_ACTION_V1.equals(intent.getAction())) {
            Log.i("TermService", "Outside process called onBind()");

            return new RBinder();
        } else {
            Log.i("TermService", "Activity called onBind()");

            return mTSBinder;
        }
    }

    @Override
    public void onCreate() {
        /* Put the service in the foreground. */
        command_service = new CommandService(this);
        command_service.start();
        Log.d(Application.APP_TAG, "TermService started");
    }

    @Override
    public void onDestroy() {
        command_service.stop();
        for (TermSession session : mTermSessions){
            session.setFinishCallback(null);
            session.finish();
        }
        mTermSessions.clear();
        StopForeground.stop(this);
    }

    public int getSessionCount() {
        return mTermSessions.size();
    }

    public TermSession getSession(int index) {
        try {
            return mTermSessions.get(index);
        } catch (IndexOutOfBoundsException e) {
            return null;
        }
    }

    public SessionList getSessions() {
        return mTermSessions;
    }

    public void addSession(TermSession session) {
        addSession(session, this::onSessionFinish);
    }

    private void addSession(TermSession session, TermSession.FinishCallback callback) {
        mTermSessions.add(session);
        session.setFinishCallback(callback);
    }

    private void onSessionFinish(TermSession session) {
        mTermSessions.remove(session);
    }
    private static class ActivityPendingIntent {
        private static PendingIntent get(Context context, int requestCode, Intent intent, int flags) {
            flags |= PendingIntent.FLAG_IMMUTABLE;
            return PendingIntent.getActivity(context, requestCode, intent, flags);
        }
    }


    private static class StopForeground {
        private static void stop(Service service) {
            Compat24.stop(service);
        }

        private static class Compat24 {
            private static void stop(Service service) {
                service.stopForeground(STOP_FOREGROUND_REMOVE);
            }
        }

    }


    public class TSBinder extends Binder {
        public TermService getService() {
            Log.i("TermService", "Activity binding to service");
            return TermService.this;
        }
    }

    private final class RBinder extends ITerminal.Stub {
        @Override
        public IntentSender startSession(final ParcelFileDescriptor pseudoTerminalMultiplexerFd,
                                         final ResultReceiver callback) {
            final String sessionHandle = UUID.randomUUID().toString();

            // distinct Intent Uri and PendingIntent requestCode must be sufficient to avoid collisions
            final Intent switchIntent = new Intent()
                    .setClassName(Application.ID, Term.class.getName())
                    .setAction(Application.ACTION_OPEN_NEW_WINDOW)
                    .setData(Uri.parse(sessionHandle))
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    .putExtra(Application.ARGUMENT_TARGET_WINDOW, sessionHandle);

            final PendingIntent result = ActivityPendingIntent.get(getApplicationContext(), sessionHandle.hashCode(),
                    switchIntent, 0);

            final PackageManager pm = getPackageManager();
            final String[] pkgs = pm.getPackagesForUid(getCallingUid());
            if (pkgs == null || pkgs.length == 0)
                return null;

            for (String packageName : pkgs) {
                try {
                    final PackageInfo pkgInfo = pm.getPackageInfo(packageName, 0);

                    final ApplicationInfo appInfo = pkgInfo.applicationInfo;
                    if (appInfo == null)
                        continue;

                    final CharSequence label = pm.getApplicationLabel(appInfo);

                    if (!TextUtils.isEmpty(label)) {
                        final String niceName = label.toString();

                        new Handler(Looper.getMainLooper()).post(() -> {
                            GenericTermSession session = null;
                            try {
                                final TermSettings settings = new TermSettings(getApplicationContext());

                                session = new BoundSession(pseudoTerminalMultiplexerFd, settings, niceName);
                                session.setHandle(sessionHandle);
                                session.setTitle("");
                                session.initializeEmulator(80, 24);

                                addSession(session, new RBinderCleanupCallback(result, callback));
                            } catch (Exception whatWentWrong) {
                                Log.e("TermService", "Failed to bootstrap AIDL session: "
                                        + whatWentWrong.getMessage());

                                if (session != null)
                                    session.finish();
                            }
                        });

                        return result.getIntentSender();
                    }
                } catch (PackageManager.NameNotFoundException ignore) {
                }
            }

            return null;
        }
    }

    private final class RBinderCleanupCallback implements TermSession.FinishCallback {
        private final PendingIntent result;
        private final ResultReceiver callback;

        public RBinderCleanupCallback(PendingIntent result, ResultReceiver callback) {
            this.result = result;
            this.callback = callback;
        }

        @Override
        public void onSessionFinish(TermSession session) {
            result.cancel();

            callback.send(0, new Bundle());

            mTermSessions.remove(session);
        }
    }
}
