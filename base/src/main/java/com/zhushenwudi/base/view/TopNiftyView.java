package com.zhushenwudi.base.view;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.core.view.ViewCompat;
import androidx.core.view.ViewPropertyAnimatorListenerAdapter;
import androidx.interpolator.view.animation.FastOutSlowInInterpolator;

import com.zhushenwudi.base.R;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

public class TopNiftyView {
    public static final int INFINITY_TIME = 0;
    public static final int LONG_TIME = 1500;
    public static final int SHORT_TIME = 2700;
    private final ViewGroup mParent;
    private final Context mContext;
    private final TopNiftyView.SnackbarLayout mView;
    private final TopNiftyViewManager.Callback mManagerCallback = new TopNiftyViewManager.Callback() {
        public void show() {
            TopNiftyView.sHandler.sendMessage(TopNiftyView.sHandler.obtainMessage(0, TopNiftyView.this));
        }

        public void dismiss() {
            TopNiftyView.sHandler.sendMessage(TopNiftyView.sHandler.obtainMessage(1, TopNiftyView.this));
        }
    };
    private static final Handler sHandler = new Handler(Looper.getMainLooper(), message -> {
        switch (message.what) {
            case 0:
                ((TopNiftyView) message.obj).showView();
                return true;
            case 1:
                ((TopNiftyView) message.obj).hideView();
                return true;
            default:
                return false;
        }
    });
    private int mDuration;

    TopNiftyView(ViewGroup parent) {
        this.mParent = parent;
        this.mContext = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(this.mContext);
        this.mView = (TopNiftyView.SnackbarLayout) inflater.inflate(R.layout.layout_snackbar, this.mParent, false);
    }

    /**
     * @param activity  The Activity
     * @param text      The msg to show
     * @param duration  notify time
     * @param marginTop margin to the top of main content
     * @return TopNiftyView
     */
    public static TopNiftyView make(Activity activity, CharSequence text, int duration, int marginTop) {
        TopNiftyView snackbar;
        final RelativeLayout layout = new RelativeLayout(activity);
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, dip2px(activity, marginTop + 80));
        layout.setLayoutParams(params);

        if (marginTop != 0) {
            final RelativeLayout layout1 = new RelativeLayout(activity);
            RelativeLayout.LayoutParams params1 = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, dip2px(activity, 80));
            params1.setMargins(0, dip2px(activity, marginTop), 0, 0); //距离上边actionBarHeight dp
            layout1.setLayoutParams(params1);
            layout1.setGravity(Gravity.CENTER_HORIZONTAL);
            layout.addView(layout1);
            activity.addContentView(layout, params);
            snackbar = new TopNiftyView(findSuitableParent(layout1));
        } else {
            activity.addContentView(layout, params);
            snackbar = new TopNiftyView(findSuitableParent(layout));
        }

        snackbar.setText(text);
        snackbar.setDuration(duration);
        return snackbar;
    }

    public static TopNiftyView make(Activity activity, int resId, int duration, int actionBarHeight) {
        return make(activity, activity.getResources().getText(resId), duration, actionBarHeight);
    }

    public ViewGroup.LayoutParams getLayout() {
        return mView.getLayoutParams();
    }

    private static int dip2px(Context context, float dpValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }

    @Nullable
    private static ViewGroup findSuitableParent(View view) {
        return (ViewGroup) view;
    }

    public TopNiftyView setAction(@StringRes int resId, OnClickListener listener) {
        return this.setAction(this.mContext.getText(resId), listener);
    }

    public TopNiftyView setImage(int image) {
        ImageView img = this.mView.getImageView();
        img.setVisibility(View.VISIBLE);
        img.setImageResource(image);
        return this;
    }

    public TopNiftyView setAction(CharSequence text, final OnClickListener listener) {
        TextView tv = this.mView.getActionView();
        if (!TextUtils.isEmpty(text) && listener != null) {
            tv.setVisibility(View.VISIBLE);
            tv.setText(text);
            tv.setOnClickListener(view -> {
                listener.onClick(view);
                TopNiftyView.this.dismiss();
            });
        } else {
            tv.setVisibility(View.GONE);
            tv.setOnClickListener(null);
        }
        return this;
    }

    public TopNiftyView setActionTextColor(ColorStateList colors) {
        TextView tv = this.mView.getActionView();
        tv.setTextColor(colors);
        return this;
    }

    public TopNiftyView setActionTextColor(int color) {
        TextView tv = this.mView.getActionView();
        tv.setTextColor(color);
        return this;
    }

    public TopNiftyView setTextColor(ColorStateList colors) {
        TextView tv = this.mView.getMessageView();
        tv.setTextColor(colors);
        return this;
    }

    public TopNiftyView setTextColor(int color) {
        TextView tv = this.mView.getMessageView();
        tv.setTextColor(color);
        return this;
    }

    public TopNiftyView setTextSize(int size) {
        TextView tv = this.mView.getMessageView();
        tv.setTextSize(size);
        return this;
    }

    public TopNiftyView setText(CharSequence message) {
        TextView tv = this.mView.getMessageView();
        tv.setText(message);
        return this;
    }

    public TopNiftyView setBackground(int color) {
        this.mView.setBackgroundColor(color);
        return this;
    }

    public TopNiftyView setBackground2(int res) {
        this.mView.setBackgroundResource(res);
        return this;
    }

    public TopNiftyView setText(@StringRes int resId) {
        return this.setText(this.mContext.getText(resId));
    }

    public int getDuration() {
        return this.mDuration;
    }

    public TopNiftyView setDuration(int duration) {
        this.mDuration = duration;
        return this;
    }

    public View getView() {
        return this.mView;
    }

    public void show() {
        TopNiftyViewManager.getInstance().show(this.mDuration, this.mManagerCallback);
    }

    public void dismiss() {
        TopNiftyViewManager.getInstance().dismiss(this.mManagerCallback);
    }

    final void showView() {
        if (this.mView.getParent() == null) {
            this.mParent.addView(this.mView);
        }
        if (ViewCompat.isLaidOut(this.mView)) {
            this.animateViewIn();
        } else {
            this.mView.setOnLayoutChangeListener((view, left, top, right, bottom) -> {
                TopNiftyView.this.animateViewIn();
                TopNiftyView.this.mView.setOnLayoutChangeListener(null);
            });
        }
    }

    private void animateViewIn() {
        ViewCompat.setTranslationY(this.mView, -(float) this.mView.getHeight());
        ViewCompat.animate(this.mView).translationY(0.0F).setInterpolator(new FastOutSlowInInterpolator()).setDuration(500L).setListener(new ViewPropertyAnimatorListenerAdapter() {
            public void onAnimationStart(View view) {
                TopNiftyView.this.mView.animateChildrenIn(70, 180);
            }

            public void onAnimationEnd(View view) {
                TopNiftyViewManager.getInstance().onShown(TopNiftyView.this.mManagerCallback);
            }
        }).start();
    }

    private void animateViewOut() {
        ViewCompat.animate(this.mView).translationY(-(float) this.mView.getHeight()).setInterpolator(new FastOutSlowInInterpolator()).setDuration(500L).setListener(new ViewPropertyAnimatorListenerAdapter() {
            public void onAnimationStart(View view) {
                TopNiftyView.this.mView.animateChildrenOut(0, 180);
            }

            public void onAnimationEnd(View view) {
                TopNiftyView.this.onViewHidden();
            }
        }).start();

    }

    final void hideView() {
        if (this.mView.getVisibility() == View.VISIBLE) {
            this.animateViewOut();
        } else {
            this.onViewHidden();
        }
    }

    private void onViewHidden() {
        this.mParent.removeView(this.mView);
        TopNiftyViewManager.getInstance().onDismissed(this.mManagerCallback);
    }


    @Retention(RetentionPolicy.SOURCE)
    public @interface Duration {
    }

    public static class SnackbarLayout extends LinearLayout {
        private TextView mMessageView;
        private TextView mActionView;
        private ImageView mImageView;
        private final int mMaxWidth;
        private final int mMaxInlineActionWidth;
        private TopNiftyView.SnackbarLayout.OnLayoutChangeListener mOnLayoutChangeListener;

        public SnackbarLayout(Context context) {
            this(context, null);
        }

        @SuppressLint("PrivateResource")
        public SnackbarLayout(Context context, AttributeSet attrs) {
            super(context, attrs);
            TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.SnackbarLayout);
            this.mMaxWidth = a.getDimensionPixelSize(R.styleable.SnackbarLayout_maxWidth, -1);
            this.mMaxInlineActionWidth = a.getDimensionPixelSize(R.styleable.SnackbarLayout_maxActionInlineWidth, -1);
            if (a.hasValue(R.styleable.SnackbarLayout_elevation)) {
                ViewCompat.setElevation(this, (float) a.getDimensionPixelSize(R.styleable.SnackbarLayout_elevation, 0));
            }

            a.recycle();
            this.setClickable(true);
            LayoutInflater.from(context).inflate(R.layout.layout_snackbar_include, this);
        }

        private static void updateTopBottomPadding(View view, int topPadding, int bottomPadding) {
            if (ViewCompat.isPaddingRelative(view)) {
                ViewCompat.setPaddingRelative(view, ViewCompat.getPaddingStart(view), topPadding, ViewCompat.getPaddingEnd(view), bottomPadding);
            } else {
                view.setPadding(view.getPaddingLeft(), topPadding, view.getPaddingRight(), bottomPadding);
            }
        }

        protected void onFinishInflate() {
            super.onFinishInflate();
            this.mMessageView = this.findViewById(R.id.snackbar_text);
            this.mActionView = this.findViewById(R.id.snackbar_action);
            this.mImageView = this.findViewById(R.id.image);
        }

        TextView getMessageView() {
            return this.mMessageView;
        }

        TextView getActionView() {
            return this.mActionView;
        }

        public ImageView getImageView() {
            return this.mImageView;
        }

        protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
            if (this.mMaxWidth > 0 && this.getMeasuredWidth() > this.mMaxWidth) {
                widthMeasureSpec = MeasureSpec.makeMeasureSpec(this.mMaxWidth, MeasureSpec.EXACTLY);
                super.onMeasure(widthMeasureSpec, heightMeasureSpec);
            }

            int multiLineVPadding = this.getResources().getDimensionPixelSize(R.dimen.snackbar_padding_vertical_2lines);
            int singleLineVPadding = this.getResources().getDimensionPixelSize(R.dimen.snackbar_padding_vertical);
            boolean isMultiLine = this.mMessageView.getLayout().getLineCount() > 1;
            boolean remeasure = false;
            if (isMultiLine && this.mMaxInlineActionWidth > 0 && this.mActionView.getMeasuredWidth() > this.mMaxInlineActionWidth) {
                if (this.updateViewsWithinLayout(1, multiLineVPadding, multiLineVPadding - singleLineVPadding)) {
                    remeasure = true;
                }
            } else {
                int messagePadding = isMultiLine ? multiLineVPadding : singleLineVPadding;
                if (this.updateViewsWithinLayout(0, messagePadding, messagePadding)) {
                    remeasure = true;
                }
            }

            if (remeasure) {
                super.onMeasure(widthMeasureSpec, heightMeasureSpec);
            }
        }

        void animateChildrenIn(int delay, int duration) {
            ViewCompat.setAlpha(this.mMessageView, 0.0F);
            ViewCompat.animate(this.mMessageView).alpha(1.0F).setDuration(duration).setStartDelay(delay).start();
            if (this.mActionView.getVisibility() == VISIBLE) {
                ViewCompat.setAlpha(this.mActionView, 0.0F);
                ViewCompat.animate(this.mActionView).alpha(1.0F).setDuration(duration).setStartDelay(delay).start();
            }

        }

        void animateChildrenOut(int delay, int duration) {
            ViewCompat.setAlpha(this.mMessageView, 1.0F);
            ViewCompat.animate(this.mMessageView).alpha(0.0F).setDuration(duration).setStartDelay(delay).start();
            if (this.mActionView.getVisibility() == VISIBLE) {
                ViewCompat.setAlpha(this.mActionView, 1.0F);
                ViewCompat.animate(this.mActionView).alpha(0.0F).setDuration(duration).setStartDelay(delay).start();
            }
        }

        protected void onLayout(boolean changed, int l, int t, int r, int b) {
            super.onLayout(changed, l, t, r, b);
            if (changed && this.mOnLayoutChangeListener != null) {
                this.mOnLayoutChangeListener.onLayoutChange(this, l, t, r, b);
            }
        }

        void setOnLayoutChangeListener(TopNiftyView.SnackbarLayout.OnLayoutChangeListener onLayoutChangeListener) {
            this.mOnLayoutChangeListener = onLayoutChangeListener;
        }

        private boolean updateViewsWithinLayout(int orientation, int messagePadTop, int messagePadBottom) {
            boolean changed = false;
            if (orientation != this.getOrientation()) {
                this.setOrientation(orientation);
                changed = true;
            }

            if (this.mMessageView.getPaddingTop() != messagePadTop || this.mMessageView.getPaddingBottom() != messagePadBottom) {
                updateTopBottomPadding(this.mMessageView, messagePadTop, messagePadBottom);
                changed = true;
            }

            return changed;
        }

        interface OnLayoutChangeListener {
            void onLayoutChange(View var1, int var2, int var3, int var4, int var5);
        }
    }
}
