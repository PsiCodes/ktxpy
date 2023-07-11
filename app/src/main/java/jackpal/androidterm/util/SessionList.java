package jackpal.androidterm.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;

import jackpal.androidterm.emulatorview.TermSession;
import jackpal.androidterm.emulatorview.UpdateCallback;


/**
 * An ArrayList of TermSessions which allows users to register callbacks in
 * order to be notified when the list is changed.
 */
@SuppressWarnings("serial")
public class SessionList extends ArrayList<TermSession>
{
    final LinkedList<UpdateCallback> callbacks = new LinkedList<>();
    final LinkedList<UpdateCallback> titleChangedListeners = new LinkedList<>();
    final UpdateCallback mTitleChangedListener = this::notifyTitleChanged;

    public SessionList() {
        super();
    }

    public SessionList(int capacity) {
        super(capacity);
    }

    public void addCallback(UpdateCallback callback) {
        callbacks.add(callback);
        callback.onUpdate();
    }

    public boolean removeCallback(UpdateCallback callback) {
        return callbacks.remove(callback);
    }

    private void notifyChange() {
        for (UpdateCallback callback : callbacks) {
            callback.onUpdate();
        }
    }

    public void addTitleChangedListener(UpdateCallback listener) {
        titleChangedListeners.add(listener);
        listener.onUpdate();
    }

    public boolean removeTitleChangedListener(UpdateCallback listener) {
        return titleChangedListeners.remove(listener);
    }

    private void notifyTitleChanged() {
        for (UpdateCallback listener : titleChangedListeners) {
            listener.onUpdate();
        }
    }

    @Override
    public boolean add(TermSession object) {
        boolean result = super.add(object);
        object.setTitleChangedListener(mTitleChangedListener);
        notifyChange();
        return result;
    }

    @Override
    public void add(int index, TermSession object) {
        super.add(index, object);
        object.setTitleChangedListener(mTitleChangedListener);
        notifyChange();
    }

    @Override
    public boolean addAll(Collection <? extends TermSession> collection) {
        boolean result = super.addAll(collection);
        for (TermSession session : collection) {
            session.setTitleChangedListener(mTitleChangedListener);
        }
        notifyChange();
        return result;
    }

    @Override
    public boolean addAll(int index, Collection <? extends TermSession> collection) {
        boolean result = super.addAll(index, collection);
        for (TermSession session : collection) {
            session.setTitleChangedListener(mTitleChangedListener);
        }
        notifyChange();
        return result;
    }

    @Override
    public void clear() {
        for (TermSession session : this) {
            session.setTitleChangedListener(null);
        }
        super.clear();
        notifyChange();
    }

    @Override
    public TermSession remove(int index) {
        TermSession object = super.remove(index);
        if (object != null) {
            object.setTitleChangedListener(null);
            notifyChange();
        }
        return object;
    }

    @Override
    public boolean remove(Object object) {
        boolean result = super.remove(object);
        if (result && object instanceof TermSession) {
            ((TermSession) object).setTitleChangedListener(null);
            notifyChange();
        }
        return result;
    }

    @Override
    public TermSession set(int index, TermSession object) {
        TermSession old = super.set(index, object);
        object.setTitleChangedListener(mTitleChangedListener);
        if (old != null) {
            old.setTitleChangedListener(null);
        }
        notifyChange();
        return old;
    }
}
