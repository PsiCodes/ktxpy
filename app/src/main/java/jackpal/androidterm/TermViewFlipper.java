package jackpal.androidterm;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.widget.Toast;
import android.widget.ViewFlipper;
import java.util.Iterator;
import java.util.LinkedList;
import com.wildzeus.pythonktx.R;

import androidx.annotation.NonNull;
import jackpal.androidterm.emulatorview.EmulatorView;
import jackpal.androidterm.emulatorview.TermSession;
import jackpal.androidterm.emulatorview.UpdateCallback;
import jackpal.androidterm.util.TermSettings;


public class TermViewFlipper extends ViewFlipper implements Iterable<View> {
    final Rect win = new Rect();
    final Rect vis = new Rect();

    private Context context;
    private Toast mToast;
    private LinkedList<UpdateCallback> callbacks;

    private int mCurWidth;
    private int mCurHeight;
    private LayoutParams mChildParams;
    private boolean mRedoLayout = false;
    private boolean resize_on_measure = false;
    private boolean full_screen = false;

    class ViewFlipperIterator implements Iterator<View> {
        int pos = 0;

        public boolean hasNext() {
            return (pos < getChildCount());
        }

        public View next() {
            return getChildAt(pos++);
        }

        public void remove() {
            throw new UnsupportedOperationException();
        }
    }

    public TermViewFlipper(Context context) {
        super(context);
        commonConstructor(context);
    }

    public TermViewFlipper(Context context, AttributeSet attrs) {
        super(context, attrs);
        commonConstructor(context);
    }

    private void commonConstructor(Context context) {
        this.context = context;
        callbacks = new LinkedList<>();
        mChildParams = new LayoutParams(0, 0, Gravity.TOP | Gravity.LEFT);
    }

    public void updatePrefs(TermSettings settings) {
        setBackgroundColor(settings.getColorScheme().getBackColor());
        resize_on_measure = (settings.getScreenCalcMethod() == 1);
    }

    @NonNull
    public Iterator<View> iterator() {
        return new ViewFlipperIterator();
    }

    public void addCallback(UpdateCallback callback) {
        callbacks.add(callback);
    }

    public void removeCallback(UpdateCallback callback) {
        callbacks.remove(callback);
    }

    private void notifyChange() {
        for (UpdateCallback callback : callbacks) {
            callback.onUpdate();
        }
    }

    public void onPause() {
        pauseCurrentView();
    }

    public void onResume() {
        resumeCurrentView();
    }

    public void pauseCurrentView() {
        EmulatorView view = (EmulatorView) getCurrentView();
        if (view == null) {
            return;
        }
        view.onPause();
    }

    public void resumeCurrentView() {
        EmulatorView view = (EmulatorView) getCurrentView();
        if (view == null) {
            return;
        }
        view.onResume();
        view.requestFocus();
    }

    private void showTitle() {
        if (getChildCount() == 0) {
            return;
        }

        EmulatorView view = (EmulatorView) getCurrentView();
        if (view == null) {
            return;
        }
        TermSession session = view.getTermSession();
        if (session == null) {
            return;
        }

        String title = context.getString(R.string.window_title, getDisplayedChild() + 1);
        if (session instanceof GenericTermSession) {
            title = ((GenericTermSession) session).getTitle(title);
        }

        if (mToast == null) {
            mToast = Toast.makeText(context.getApplicationContext(),
                    title, Toast.LENGTH_SHORT);
            mToast.setGravity(Gravity.CENTER, 0, 0);
        } else {
            mToast.setText(title);
        }
        mToast.show();
    }

    @Override
    public void showPrevious() {
        pauseCurrentView();
        super.showPrevious();
        showTitle();
        resumeCurrentView();
        notifyChange();
    }

    @Override
    public void showNext() {
        pauseCurrentView();
        super.showNext();
        showTitle();
        resumeCurrentView();
        notifyChange();
    }

    @Override
    public void setDisplayedChild(int position) {
        pauseCurrentView();
        super.setDisplayedChild(position);
        showTitle();
        resumeCurrentView();
        notifyChange();
    }

    @Override
    public void addView(View v, int index) {
        super.addView(v, index, mChildParams);
    }

    @Override
    public void addView(View v) {
        super.addView(v, mChildParams);
    }

    /**
     * Called when the view changes size.
     * (Note: Not always called on Android < 2.2)
     */
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        if (!resizeOnMeasure()) return;

        /* Get rectangle representing visible area of this window (takes
           IME into account, but not other views in the layout) */
        getWindowVisibleDisplayFrame(win);

        /* Get rectangle representing visible area of this view, as seen by
           the activity (takes other views in the layout into account, but
           not space used by the IME) */
        getGlobalVisibleRect(vis);

        /* Work around bug in getWindowVisibleDisplayFrame on API < 10, and
           avoid a distracting height change as status bar hides otherwise. */
        /* Extra note:
           On some "new" devices front-camera is places in status bar. On those
           devices is useless to hide status bar as this does not add extra
           space to window. On such devices win.top always has a value. This
           does not look like defect as window top position is not changed.
           Bug or not on those devices using zero for win.top in calculations
           bellow resolves issue with partial display of action bar if status bar
           is hidden (full screen mode). */
        int win_top = win.top;
        if (full_screen) win_top = 0;

        int nw = win.width();
        int nh = win.height() - (vis.top - win_top);
        doSizeChanged(nw, nh);
    }

    /**
     * "Called during layout when the size of this view has changed."
     * NOTE: Not always called when screen is rotated, in landscape
     * orientation, when IME method(soft-keyboard) is shown hidden.
     */
    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        if (resizeOnMeasure()) return;

        doSizeChanged(w, h);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (mRedoLayout) {
            requestLayout();
            mRedoLayout = false;
        }
        super.onDraw(canvas);
    }

    private boolean resizeOnMeasure() {
        /* Note if "full screen" mode is activated event "size changed"
           is not triggered. */
        return full_screen || resize_on_measure;
    }

    private void doSizeChanged(int w, int h) {
        if ((w == mCurWidth) && (h == mCurHeight)) return;

        mChildParams.width = mCurWidth = w;
        mChildParams.height = mCurHeight = h;
        for (View v : this)
            updateViewLayout(v, mChildParams);

        mRedoLayout = true;

        EmulatorView currentView = (EmulatorView) getCurrentView();
        if (currentView != null)
            currentView.updateSize(false);
    }

    public void setFullScreen(boolean flag) {
        full_screen = flag;
    }
}
