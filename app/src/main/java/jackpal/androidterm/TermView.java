package jackpal.androidterm;

import android.content.Context;
import android.graphics.Typeface;
import android.util.DisplayMetrics;

import jackpal.androidterm.emulatorview.EmulatorView;
import jackpal.androidterm.emulatorview.TermSession;
import jackpal.androidterm.util.TermSettings;


public class TermView extends EmulatorView {
    public TermView(Context context, TermSession session, DisplayMetrics metrics) {
        super(context, session, metrics);
    }

    public void updatePrefs(TermSettings settings) {
        setUseCookedIME(settings.useCookedIME());
        setBackKeyCharacter(settings.getBackKeyCharacter());
        setAltSendsEsc(settings.getAltSendsEscFlag());
        setControlKeyCode(settings.getControlKeyCode());
        setFnKeyCode(settings.getFnKeyCode());
        setTermType(settings.getTermType());
        setMouseTracking(settings.getMouseTrackingFlag());
        // call last as method invalidates terminal
        setPaintAttributes(Typeface.MONOSPACE,
                settings.getFontSize(), settings.getColorScheme());
    }

    @Override
    public String toString() {
        return getClass().toString() + '(' + getTermSession() + ')';
    }
}
