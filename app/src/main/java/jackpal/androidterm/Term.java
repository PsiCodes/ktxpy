/*
 * Copyright (C) 2007 The Android Open Source Project
 * Copyright (C) 2017-2022 Roumen Petrov.  All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package jackpal.androidterm;

import android.annotation.SuppressLint;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.snackbar.Snackbar;
import com.termoneplus.AppCompatActivity;
import com.Application;
import com.termoneplus.Permissions;
import com.termoneplus.TermActionBar;
import com.termoneplus.compat.SoftInputCompat;
import com.termoneplus.utils.ConsoleStartupScript;
import com.termoneplus.utils.SimpleClipboardManager;
import com.termoneplus.utils.WakeLock;
import com.termoneplus.utils.WrapOpenURL;

import java.io.IOException;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.preference.PreferenceManager;
import jackpal.androidterm.compat.PathCollector;
import jackpal.androidterm.emulatorview.EmulatorView;
import jackpal.androidterm.emulatorview.TermSession;
import jackpal.androidterm.emulatorview.UpdateCallback;
import jackpal.androidterm.emulatorview.compat.KeycodeConstants;
import jackpal.androidterm.util.SessionList;
import jackpal.androidterm.util.TermSettings;

import com.wildzeus.pythonktx.R;

/**
 * A terminal emulator activity.
 */
public class Term extends AppCompatActivity
        implements UpdateCallback, SharedPreferences.OnSharedPreferenceChangeListener {
    /**
     * The name of the ViewFlipper in the resources.
     */
    private static final int VIEW_FLIPPER = R.id.view_flipper;

    private final ActivityResultLauncher<Intent> request_choose_window =
            registerForActivityResult(
                    new ActivityResultContracts.StartActivityForResult(),
                    result -> onRequestChooseWindow(result.getResultCode(), result.getData())
            );

    /**
     * The ViewFlipper which holds the collection of EmulatorView widgets.
     */
    private TermViewFlipper mViewFlipper;
    private SessionList mTermSessions;
    private TermSettings mSettings;
    private PathCollector path_collector;
    private boolean mAlreadyStarted = false;
    private boolean mStopServiceOnFinish = false;
    private Intent TSIntent;
    private int onResumeSelectWindow = -1;
    private WifiManager.WifiLock mWifiLock;
    private boolean path_collected;
    private TermService mTermService;
    private TermActionBar mActionBar;
    private int mActionBarMode;
    private boolean mHaveFullHwKeyboard = false;
    /**
     * Should we use keyboard shortcuts?
     */
    private boolean mUseKeyboardShortcuts;
    /**
     * Intercepts keys before the view/terminal gets it.
     */
    private final View.OnKeyListener mKeyListener = new View.OnKeyListener() {
        public boolean onKey(View v, int keyCode, KeyEvent event) {
            return backkeyInterceptor(keyCode, event) || keyboardShortcuts(keyCode, event);
        }

        /**
         * Keyboard shortcuts (tab management, paste)
         */
        private boolean keyboardShortcuts(int keyCode, KeyEvent event) {
            if (event.getAction() != KeyEvent.ACTION_DOWN) {
                return false;
            }
            if (!mUseKeyboardShortcuts) {
                return false;
            }
            boolean isCtrlPressed = (event.getMetaState() & KeycodeConstants.META_CTRL_ON) != 0;
            boolean isShiftPressed = (event.getMetaState() & KeycodeConstants.META_SHIFT_ON) != 0;

            if (keyCode == KeycodeConstants.KEYCODE_TAB && isCtrlPressed) {
                if (isShiftPressed) {
                    mViewFlipper.showPrevious();
                } else {
                    mViewFlipper.showNext();
                }

                return true;
            } else if (keyCode == KeycodeConstants.KEYCODE_N && isCtrlPressed && isShiftPressed) {
                doCreateNewWindow();

                return true;
            } else if (keyCode == KeycodeConstants.KEYCODE_V && isCtrlPressed && isShiftPressed) {
                doPaste();

                return true;
            } else {
                return false;
            }
        }

        /**
         * Make sure the back button always leaves the application.
         */
        private boolean backkeyInterceptor(int keyCode, KeyEvent event) {
            if (keyCode == KeyEvent.KEYCODE_BACK && mActionBarMode == TermSettings.ACTION_BAR_MODE_HIDES && mActionBar.isShowing()) {
                /* We need to intercept the key event before the view sees it,
                   otherwise the view will handle it before we get it */
                onKeyUp(keyCode, event);
                return true;
            } else {
                return false;
            }
        }
    };
    private ServiceConnection mTSConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder service) {
            Log.i(Application.APP_TAG, "Bound to TermService");
            TermService.TSBinder binder = (TermService.TSBinder) service;
            mTermService = binder.getService();
            populateSessions();
        }

        public void onServiceDisconnected(ComponentName arg0) {
            mTermService = null;
        }
    };
    private Handler mHandler;

    protected static TermSession createTermSession(
            Context context,
            TermSettings settings,
            String extraCommand) throws IOException {

        String initialCommand = settings.getShell();

        GenericTermSession session = new ShellTermSession(settings, initialCommand);
        session.setProcessExitMessage(context.getString(R.string.process_exit_message));
        return session;
    }
    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        Application.settings.parsePreference(this, sharedPreferences, key);

        if (key.equals(getString(R.string.key_shellrc_preference))) {
            String value = sharedPreferences.getString(key, null);
            ConsoleStartupScript.write(mSettings.getHomePath(), value);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.remove(key);
            editor.apply();
        }

        if (key.equals(getString(R.string.key_home_path_preference))) {
            String value = sharedPreferences.getString(key, null);
            ConsoleStartupScript.rename(mSettings.getHomePath(), value);
            mSettings.setHomePath(value);
        }

        mSettings.readPrefs(this, sharedPreferences);
        path_collector.extractPreferences(sharedPreferences);
    }

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);

        Log.v(Application.APP_TAG, "onCreate");
        mHandler = new Handler(getMainLooper());

        if (icicle == null)
            onNewIntent(getIntent());

        mSettings = new TermSettings(this);

        mActionBarMode = mSettings.actionBarMode();

        path_collected = false;
        path_collector = new PathCollector(this);
        path_collector.setOnPathsReceivedListener(() -> {
            path_collected = true;
            populateSessions();
        });

        PreferenceManager.getDefaultSharedPreferences(this)
                .registerOnSharedPreferenceChangeListener(this);

        TSIntent = new Intent(this, TermService.class);
        mActionBar = TermActionBar.setTermContentView(this,
                mActionBarMode == TermSettings.ACTION_BAR_MODE_HIDES);
        mActionBar.setOnItemSelectedListener(position -> {
            int oldPosition = mViewFlipper.getDisplayedChild();
            if (position == oldPosition) return;

            if (position >= mViewFlipper.getChildCount()) {
                TermSession session = mTermService.getSession(position);
                mViewFlipper.addView(createEmulatorView(session));
            }
            mViewFlipper.setDisplayedChild(position);
            if (mActionBarMode == TermSettings.ACTION_BAR_MODE_HIDES)
                mActionBar.hide();
        });
        //mActionBar.setOnNavigationItemSelectedListener(this::onNavigationItemSelected);

        mViewFlipper = findViewById(VIEW_FLIPPER);

        Context app = getApplicationContext();

        WakeLock.create(this);

        WifiManager wm = (WifiManager) app.getSystemService(Context.WIFI_SERVICE);
        mWifiLock = wm.createWifiLock(WifiManager.WIFI_MODE_FULL_HIGH_PERF, Application.APP_TAG);

        mHaveFullHwKeyboard = checkHaveFullHwKeyboard(getResources().getConfiguration());

        updatePrefs();
        requestStoragePermission();
        mAlreadyStarted = true;
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (!bindService(TSIntent, mTSConnection, BIND_AUTO_CREATE)) {
            throw new IllegalStateException("Failed to bind to TermService!");
        }
    }

    @Override
    public void onBackPressed() {
    }

    private synchronized void populateSessions() {
        if (mTermService == null) return;
        if (!path_collected) return;

        if (mTermService.getSessionCount() == 0) {
            try {
                mTermService.addSession(createTermSession());
            } catch (IOException e) {
                Toast.makeText(getApplicationContext(),
                        "Failed to start terminal session", Toast.LENGTH_LONG).show();
                finish();
                return;
            }
        }

        mTermSessions = mTermService.getSessions();
        mTermSessions.addCallback(this);

        populateViewFlipper();
        populateWindowList();
    }

    private void populateViewFlipper() {
        for (TermSession session : mTermSessions) {
            EmulatorView view = createEmulatorView(session);
            mViewFlipper.addView(view);
        }

        updatePrefs();

        if (onResumeSelectWindow >= 0) {
            onResumeSelectWindow = Math.min(onResumeSelectWindow, mViewFlipper.getChildCount() - 1);
            mViewFlipper.setDisplayedChild(onResumeSelectWindow);
            onResumeSelectWindow = -1;
        }
        mViewFlipper.onResume();
    }

    private void populateWindowList() {
    }
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
           doCloseWindow();
        }
        return true;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        PreferenceManager.getDefaultSharedPreferences(this)
                .unregisterOnSharedPreferenceChangeListener(this);
        if (mStopServiceOnFinish) {
            stopService(TSIntent);
        }
        mTermService = null;
        mTSConnection = null;
        WakeLock.release();
        if (mWifiLock.isHeld()) {
            mWifiLock.release();
        }
        EmulatorView view = getCurrentEmulatorView();
        if (view == null) return;
        mViewFlipper.removeView(view);
        TermSession session = view.getTermSession();
        session.finish();
        mTermService=null;
        finish();

    }

    private TermSession createTermSession() throws IOException {
        return createTermSession(this, mSettings, null);
    }

    private TermView createEmulatorView(TermSession session) {
        DisplayMetrics metrics = getResources().getDisplayMetrics();
        TermView emulatorView = new TermView(this, session, metrics);
        mActionBar.lockDrawer(true);
        emulatorView.setExtGestureListener(new EmulatorViewGestureListener(emulatorView));
        emulatorView.setOnKeyListener(mKeyListener);
        emulatorView.setOnToggleSelectingTextListener(
               () -> mActionBar.lockDrawer(emulatorView.getSelectingText()));
        registerForContextMenu(emulatorView);

        return emulatorView;
    }

    protected TermSession getCurrentTermSession() {
        if (mTermService == null) return null;

        return mTermService.getSession(mViewFlipper.getDisplayedChild());
    }

    protected EmulatorView getCurrentEmulatorView() {
        return (EmulatorView) mViewFlipper.getCurrentView();
    }

    protected void updatePrefs() {
        mUseKeyboardShortcuts = mSettings.getUseKeyboardShortcutsFlag();

        mViewFlipper.updatePrefs(mSettings);

        DisplayMetrics metrics = getResources().getDisplayMetrics();
        for (View v : mViewFlipper) {
            ((EmulatorView) v).setDensity(metrics);
            ((TermView) v).updatePrefs(mSettings);
        }

        if (mTermSessions != null) {
            for (TermSession session : mTermSessions) {
                ((GenericTermSession) session).updatePrefs(mSettings);
            }
        }

        {
            int flag = FullScreenCompat.update(this);
            if (flag < 0) {
                // Cannot switch to/from fullscreen after starting activity.
                restart(R.string.restart_statusbar_change);
                return;
            }
            mViewFlipper.setFullScreen(flag > 0);
        }

        if (mActionBarMode != mSettings.actionBarMode()) {
            if (mAlreadyStarted) {
                // Can't switch to new layout after
                // starting the activity.
                restart(R.string.restart_actionbar_change);
                return;
            } else {
                if (mActionBarMode == TermSettings.ACTION_BAR_MODE_HIDES) {
                    mActionBar.hide();
                }
            }
        }

        @TermSettings.Orientation
        int orientation = mSettings.getScreenOrientation();
        int o = 0;
        if (orientation == TermSettings.ORIENTATION_UNSPECIFIED) {
            o = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED;
        } else if (orientation == TermSettings.ORIENTATION_LANDSCAPE) {
            o = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;
        } else if (orientation == TermSettings.ORIENTATION_PORTRAIT) {
            o = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
        } else {
            /* Shouldn't be happened. */
        }
        setRequestedOrientation(o);
    }

    @Override
    public void onPause() {
        super.onPause();

        /* Explicitly close the input method
           Otherwise, the soft keyboard could cover up whatever activity takes
           our place */
        final IBinder token = mViewFlipper.getWindowToken();
        new Thread() {
            @Override
            public void run() {
                InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(token, 0);
            }
        }.start();
    }

    @Override
    protected void onStop() {
        mViewFlipper.onPause();
        if (mTermSessions != null) {
            mTermSessions.removeCallback(this);
        }

        mViewFlipper.removeAllViews();

        unbindService(mTSConnection);

        super.onStop();
    }

    private boolean checkHaveFullHwKeyboard(Configuration c) {
        return (c.keyboard == Configuration.KEYBOARD_QWERTY) &&
                (c.hardKeyboardHidden == Configuration.HARDKEYBOARDHIDDEN_NO);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        mHaveFullHwKeyboard = checkHaveFullHwKeyboard(newConfig);

        EmulatorView v = (EmulatorView) mViewFlipper.getCurrentView();
        if (v != null) {
            v.updateSize(false);
        }
    }
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        /* NOTE: Resource IDs will be non-final in Android Gradle Plugin version 5.0,
           avoid using them in switch case statements */
        if (id == R.id.nav_action_help)
            WrapOpenURL.launch(this, R.string.help_url);
        else if (id == R.id.nav_send_email)
            doEmailTranscript();
        else
            return false;
        return true;
    }

    private void doCreateNewWindow() {
        if (mTermService == null) {
            Log.w(Application.APP_TAG, "Couldn't create new window because mTermService == null");
            return;
        }

        try {
            TermSession session = createTermSession();

            mTermService.addSession(session);

            TermView view = createEmulatorView(session);
            view.updatePrefs(mSettings);

            mViewFlipper.addView(view);
            mViewFlipper.setDisplayedChild(mViewFlipper.getChildCount() - 1);
        } catch (IOException e) {
            Toast.makeText(getApplicationContext(),
                    "Failed to create a session", Toast.LENGTH_SHORT).show();
        }
    }

    private void confirmCloseWindow() {
        final AlertDialog.Builder b = new AlertDialog.Builder(this);
        b.setIcon(android.R.drawable.ic_dialog_alert);
        b.setMessage(R.string.confirm_window_close_message);
        b.setPositiveButton(android.R.string.ok, (dialog, id) -> {
            dialog.dismiss();
            mHandler.post(this::doCloseWindow);
        });
        b.setNegativeButton(android.R.string.cancel, null);
        b.show();
    }

    private void doCloseWindow() {
        if (mTermService == null) return;

        EmulatorView view = getCurrentEmulatorView();
        if (view == null) return;

        view.onPause();
        mViewFlipper.removeView(view);
        TermSession session = view.getTermSession();
        if (session != null) session.finish();

        if (mTermService.getSessionCount() > 0)
            mViewFlipper.showNext();
    }

    private void onRequestChooseWindow(int resultCode, @Nullable Intent data) {
        if (resultCode == RESULT_OK && data != null) {
            int position = data.getIntExtra(Application.ARGUMENT_WINDOW_ID, -2);
            if (position >= 0) {
                // Switch windows after session list is in sync, not here
                onResumeSelectWindow = position;
            } else if (position == -1) {
                // NOTE do not create new windows (view) here as launch of a
                // activity cleans indirectly view flipper - see method onStop.
                // Create only new session and then on service connection view
                // flipper and etc. will be updated...
                //doCreateNewWindow();
                if (mTermService != null) {
                    try {
                        TermSession session = createTermSession();
                        mTermService.addSession(session);
                        onResumeSelectWindow = mTermService.getSessionCount() - 1;
                    } catch (IOException e) {
                        Toast.makeText(this.getApplicationContext(),
                                "Failed to create a session", Toast.LENGTH_SHORT).show();
                        onResumeSelectWindow = -1;
                    }
                } else
                    onResumeSelectWindow = -1;
            }
        } else {
            // Close the activity if user closed all sessions
            // TODO the left path will be invoked when nothing happened, but this Activity was destroyed!
            if (mTermService == null || mTermService.getSessionCount() == 0) {
                mStopServiceOnFinish = true;
                finish();
            }
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        if ((intent.getFlags() & Intent.FLAG_ACTIVITY_LAUNCHED_FROM_HISTORY) != 0) {
            // Don't repeat action if intent comes from history
            return;
        }

        String action = intent.getAction();
        if (TextUtils.isEmpty(action) ||
                /* not from application */
                !intent.getComponent().getPackageName().equals(Application.ID)) {
            return;
        }

        // huge number simply opens new window
        // TODO: add a way to restrict max number of windows per caller (possibly via reusing BoundSession)
        switch (action) {
            case Application.ACTION_OPEN_NEW_WINDOW:
                onResumeSelectWindow = Integer.MAX_VALUE;
                break;
            case Application.ACTION_SWITCH_WINDOW:
                int target = intent.getIntExtra(Application.ARGUMENT_TARGET_WINDOW, -1);
                if (target >= 0) {
                    onResumeSelectWindow = target;
                }
                break;
        }
        super.onNewIntent(intent);
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_BACK:
                if (mActionBarMode == TermSettings.ACTION_BAR_MODE_HIDES && mActionBar.isShowing()) {
                    mActionBar.hide();
                    return true;
                }
                switch (mSettings.getBackKeyAction()) {
                    case TermSettings.BACK_KEY_STOPS_SERVICE:
                        mStopServiceOnFinish = true;
                    case TermSettings.BACK_KEY_CLOSES_ACTIVITY:
                        finish();
                        return true;
                    case TermSettings.BACK_KEY_CLOSES_WINDOW:
                        doCloseWindow();
                        return true;
                    default:
                        return false;
                }
            case KeyEvent.KEYCODE_MENU:
                if (!mActionBar.isShowing()) {
                    mActionBar.show();
                    return true;
                } else {
                    return super.onKeyUp(keyCode, event);
                }
            default:
                return super.onKeyUp(keyCode, event);
        }
    }

    // Called when the list of sessions changes
    public void onUpdate() {
        if (mTermService == null) return;

        if (mTermService.getSessionCount() == 0) {
            mStopServiceOnFinish = true;
            finish();
            return;
        }

        SessionList sessions = mTermService.getSessions();
        if (sessions.size() < mViewFlipper.getChildCount()) {
            for (int i = 0; i < mViewFlipper.getChildCount(); ++i) {
                EmulatorView v = (EmulatorView) mViewFlipper.getChildAt(i);
                if (!sessions.contains(v.getTermSession())) {
                    v.onPause();
                    mViewFlipper.removeView(v);
                    --i;
                }
            }
        }
    }

    private void requestStoragePermission() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M /*API Level 23*/) return;

        if (Permissions.permissionExternalStorage(this))
            return;

        Permissions.requestExternalStorage(this, mViewFlipper, Permissions.REQUEST_EXTERNAL_STORAGE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        System.err.println("TRACE Term.onRequestPermissionsResult()  requestCode: " + requestCode);
        switch (requestCode) {
            case Permissions.REQUEST_EXTERNAL_STORAGE: {
                if (Permissions.isPermissionGranted(grantResults)) {
                    Snackbar.make(mViewFlipper,
                            R.string.message_external_storage_granted,
                            Snackbar.LENGTH_SHORT)
                            .show();
                } else {
                    Snackbar.make(mViewFlipper,
                            R.string.message_external_storage_not_granted,
                            Snackbar.LENGTH_SHORT)
                            .show();
                }
                return;
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    protected boolean canPaste() {
        return canPaste(new SimpleClipboardManager(this));
    }

    private boolean canPaste(SimpleClipboardManager clip) {
        return clip.hasText();
    }


    private void doResetTerminal() {
        TermSession session = getCurrentTermSession();
        if (session == null) return;
        session.reset();
    }

    private void doEmailTranscript() {
        TermSession session = getCurrentTermSession();
        if (session == null) return;

        // Don't really want to supply an address, but
        // currently it's required, otherwise nobody
        // wants to handle the intent.
        String addr = "user@example.com";
        Intent intent =
                new Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:"
                        + addr));

        String subject = getString(R.string.email_transcript_subject);
        String title = session.getTitle();
        if (title != null) {
            subject = subject + " - " + title;
        }
        intent.putExtra(Intent.EXTRA_SUBJECT, subject);
        intent.putExtra(Intent.EXTRA_TEXT,
                session.getTranscriptText().trim());
        try {
            startActivity(Intent.createChooser(intent,
                    getString(R.string.email_transcript_chooser_title)));
        } catch (ActivityNotFoundException e) {
            Toast.makeText(getApplicationContext(),
                    R.string.email_transcript_no_email_activity_found,
                    Toast.LENGTH_LONG).show();
        }
    }

    protected void doCopyAll() {
        TermSession session = getCurrentTermSession();
        if (session == null) return;

        SimpleClipboardManager clip = new SimpleClipboardManager(this);
        clip.setText(session.getTranscriptText().trim());
    }

    protected void doPaste() {
        TermSession session = getCurrentTermSession();
        if (session == null) return;

        SimpleClipboardManager clip = new SimpleClipboardManager(this);
        if (!canPaste(clip)) return;

        CharSequence paste = clip.getText();
        if (TextUtils.isEmpty(paste)) return;

        session.write(paste.toString());
    }
    private String formatMessage(int keyId, int disabledKeyId,
                                 Resources r, int arrayId,
                                 int enabledId,
                                 int disabledId, String regex) {
        if (keyId == disabledKeyId) {
            return r.getString(disabledId);
        }
        String[] keyNames = r.getStringArray(arrayId);
        String keyName = keyNames[keyId];
        String template = r.getString(enabledId);
        return template.replaceAll(regex, keyName);
    }

    private void doToggleSoftKeyboard() {
        SoftInputCompat.toggle(getCurrentEmulatorView());
    }

    private void doToggleSoftKeyboard(View view) {
        SoftInputCompat.toggle(view);
    }

    private void doToggleWakeLock() {
        WakeLock.toggle(this);
        invalidateOptionsMenu();
    }

    private void doToggleWifiLock() {
        if (mWifiLock.isHeld()) {
            mWifiLock.release();
        } else {
            mWifiLock.acquire();
        }
        invalidateOptionsMenu();
    }

    private void doUIToggle(int x, int y, int width, int height) {
        View view = getCurrentEmulatorView();
        switch (mActionBarMode) {
            case TermSettings.ACTION_BAR_MODE_ALWAYS_VISIBLE:
                if (!mHaveFullHwKeyboard) {
                    doToggleSoftKeyboard(view);
                }
                break;
            case TermSettings.ACTION_BAR_MODE_HIDES:
                if (mHaveFullHwKeyboard || y < height / 2) {
                    mActionBar.doToggleActionBar();
                } else {
                    doToggleSoftKeyboard(view);
                }
                break;
        }
        view.requestFocus();
    }

    private void synchronizeActionBar() {
        int position = mViewFlipper.getDisplayedChild();
        mActionBar.setSelection(position);
    }

    private static class FullScreenCompat {
        private static int update(Term activity) {
            return Compat1.update(activity);
        }

        private static class Compat1 {
            private final static int FULLSCREEN = WindowManager.LayoutParams.FLAG_FULLSCREEN;

            private static int update(Term activity) {
                Window win = activity.getWindow();
                WindowManager.LayoutParams params = win.getAttributes();
                int desired = activity.mSettings.showStatusBar() ? 0 : FULLSCREEN;
                if (desired != (params.flags & FULLSCREEN)) {
                    if (activity.mAlreadyStarted) return -1;
                    win.setFlags(desired, FULLSCREEN);
                }
                return (params.flags & FULLSCREEN) != 0 ? 1 : 0;
            }
        }
    }
    private class EmulatorViewGestureListener extends SimpleOnGestureListener {
        private final EmulatorView view;

        public EmulatorViewGestureListener(EmulatorView view) {
            this.view = view;
        }

        @Override
        public boolean onSingleTapUp(MotionEvent e) {
            // Let the EmulatorView handle taps if mouse tracking is active
            if (view.isMouseTrackingActive()) return false;

            //Check for link at tap location
            String link = view.getURLat(e.getX(), e.getY());
            if (link != null)
                WrapOpenURL.INSTANCE.launch(Term.this, link);
            else
                doUIToggle((int) e.getX(), (int) e.getY(), view.getVisibleWidth(), view.getVisibleHeight());
            return true;
        }

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            float absVelocityX = Math.abs(velocityX);
            float absVelocityY = Math.abs(velocityY);
            if (absVelocityX > Math.max(1000.0f, 2.0 * absVelocityY)) {
                // Assume user wanted side to side movement
                if (velocityX > 0) {
                    // Left to right swipe -- previous window
                    mViewFlipper.showPrevious();
                } else {
                    // Right to left swipe -- next window
                    mViewFlipper.showNext();
                }
                return true;
            } else {
                return false;
            }
        }
    }
}