package com.zhushenwudi.base.view;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import java.lang.ref.WeakReference;

class TopNiftyViewManager {
    private static final int MSG_TIMEOUT = 0;
    private static final int SHORT_DURATION_MS = 1500;
    private static final int LONG_DURATION_MS = 2750;
    private static TopNiftyViewManager sSnackbarManager;
    private final Object mLock = new Object();
    private SnackbarRecord mCurrentSnackbar;
    private SnackbarRecord mNextSnackbar;
    private final Handler mHandler = new Handler(Looper.getMainLooper(), new Handler.Callback() {
        public boolean handleMessage(Message message) {
            if (message.what == 0) {
                TopNiftyViewManager.this.handleTimeout((SnackbarRecord) message.obj);
                return true;
            }
            return false;
        }
    });

    private TopNiftyViewManager() {
    }

    static TopNiftyViewManager getInstance() {
        if (sSnackbarManager == null) {
            sSnackbarManager = new TopNiftyViewManager();
        }

        return sSnackbarManager;
    }

    public void show(int duration, Callback callback) {
        synchronized (this.mLock) {
            if (this.isCurrentSnackbar(callback)) {
                this.mCurrentSnackbar.duration = duration;
                this.mHandler.removeCallbacksAndMessages(this.mCurrentSnackbar);
                this.scheduleTimeoutLocked(this.mCurrentSnackbar);
            } else {
                if (this.isNextSnackbar(callback)) {
                    this.mNextSnackbar.duration = duration;
                } else {
                    this.mNextSnackbar = new SnackbarRecord(duration, callback);
                }

                if (this.mCurrentSnackbar == null || !this.cancelSnackbarLocked(this.mCurrentSnackbar)) {
                    this.mCurrentSnackbar = null;
                    this.showNextSnackbarLocked();
                }
            }
        }
    }

    public void dismiss(Callback callback) {
        synchronized (this.mLock) {
            if (this.isCurrentSnackbar(callback)) {
                this.cancelSnackbarLocked(this.mCurrentSnackbar);
            }

            if (this.isNextSnackbar(callback)) {
                this.cancelSnackbarLocked(this.mNextSnackbar);
            }

        }
    }

    public void onDismissed(Callback callback) {
        synchronized (this.mLock) {
            if (this.isCurrentSnackbar(callback)) {
                this.mCurrentSnackbar = null;
                if (this.mNextSnackbar != null) {
                    this.showNextSnackbarLocked();
                }
            }

        }
    }

    public void onShown(Callback callback) {
        synchronized (this.mLock) {
            if (this.isCurrentSnackbar(callback)) {
                this.scheduleTimeoutLocked(this.mCurrentSnackbar);
            }

        }
    }

    public void cancelTimeout(Callback callback) {
        synchronized (this.mLock) {
            if (this.isCurrentSnackbar(callback)) {
                this.mHandler.removeCallbacksAndMessages(this.mCurrentSnackbar);
            }

        }
    }

    public void restoreTimeout(Callback callback) {
        synchronized (this.mLock) {
            if (this.isCurrentSnackbar(callback)) {
                this.scheduleTimeoutLocked(this.mCurrentSnackbar);
            }

        }
    }

    private void showNextSnackbarLocked() {
        if (this.mNextSnackbar != null) {
            this.mCurrentSnackbar = this.mNextSnackbar;
            this.mNextSnackbar = null;
            Callback callback = this.mCurrentSnackbar.callback.get();
            if (callback != null) {
                callback.show();
            } else {
                this.mCurrentSnackbar = null;
            }
        }

    }

    private boolean cancelSnackbarLocked(SnackbarRecord record) {
        Callback callback = record.callback.get();
        if (callback != null) {
            callback.dismiss();
            return true;
        } else {
            return false;
        }
    }

    private boolean isCurrentSnackbar(Callback callback) {
        return this.mCurrentSnackbar != null && this.mCurrentSnackbar.isSnackbar(callback);
    }

    private boolean isNextSnackbar(Callback callback) {
        return this.mNextSnackbar != null && this.mNextSnackbar.isSnackbar(callback);
    }

    private void scheduleTimeoutLocked(SnackbarRecord r) {
        this.mHandler.removeCallbacksAndMessages(r);
        if (r.duration == 0) {
            return;
        }
        this.mHandler.sendMessageDelayed(Message.obtain(this.mHandler, 0, r), r.duration);
    }

    private void handleTimeout(SnackbarRecord record) {
        synchronized (this.mLock) {
            if (this.mCurrentSnackbar == record || this.mNextSnackbar == record) {
                this.cancelSnackbarLocked(record);
            }

        }
    }

    interface Callback {
        void show();

        void dismiss();
    }

    private static class SnackbarRecord {
        private final WeakReference<Callback> callback;
        private int duration;

        SnackbarRecord(int duration, Callback callback) {
            this.callback = new WeakReference(callback);
            this.duration = duration;
        }

        boolean isSnackbar(Callback callback) {
            return callback != null && this.callback.get() == callback;
        }
    }
}
