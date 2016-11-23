package me.kareluo.ui;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.widget.CheckedTextView;
import android.widget.LinearLayout;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by felix on 16/11/19.
 */
public class OptionMenuView extends LinearLayout implements PopLayout.OnBulgeChangeCallback, View.OnClickListener {
    private static final String TAG = "OptionMenuView";

    private int mBulgeSize = 0;

    private boolean mNeedLayout = false;

    private List<OptionMenu> mOptionMenus;

    private OnOptionMenuClickListener mMenuClickListener;

    private int mLeftPadding, mTopPadding, mRightPadding, mBottomPadding;

    public OptionMenuView(Context context) {
        this(context, null, 0);
    }

    public OptionMenuView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public OptionMenuView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initialize(context, attrs, defStyleAttr);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public OptionMenuView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        initialize(context, attrs, defStyleAttr);
    }

    private void initialize(Context context, AttributeSet attrs, int defStyleAttr) {
        mOptionMenus = new ArrayList<>();
    }

    public void setOptionMenus(List<OptionMenu> optionMenus) {
        mOptionMenus.clear();
        if (optionMenus != null) {
            mOptionMenus.addAll(optionMenus);
        }
        notifyMenusChange();
    }

    public List<OptionMenu> getOptionMenus() {
        return mOptionMenus;
    }

    @Override
    public void setOrientation(int orientation) {
        mNeedLayout = getOrientation() != orientation;
        super.setOrientation(orientation);
    }

    @Override
    public void requestLayout() {
        if (mNeedLayout) {
            mNeedLayout = false;
            resetPadding();
            int width = LayoutParams.MATCH_PARENT;
            int leftMargin = 0, topMargin = 1;
            if (getOrientation() == HORIZONTAL) {
                width = LayoutParams.WRAP_CONTENT;
                leftMargin = 1;
                topMargin = 0;
            }
            int count = getChildCount();
            for (int i = 0; i < count; i++) {
                View view = getChildAt(i);
                LayoutParams params = (LayoutParams) view.getLayoutParams();
                if (params != null) {
                    params.width = width;
                    params.topMargin = topMargin;
                    params.leftMargin = leftMargin;
                }
            }
            if (count > 0) {
                View view = getChildAt(0);
                LayoutParams params = (LayoutParams) view.getLayoutParams();
                if (params != null) {
                    params.leftMargin = params.topMargin = 0;
                }
            }
        }
        super.requestLayout();
    }

    public void inflate(int menuRes, Menu menu) {
        MenuInflater inflater = new MenuInflater(getContext());
        inflater.inflate(menuRes, menu);
        mOptionMenus.clear();
        int size = menu.size();
        for (int i = 0; i < size; i++) {
            mOptionMenus.add(new OptionMenu(menu.getItem(i)));
        }
        notifyMenusChange();
    }

    public void notifyMenusChange() {
        adjustChildCount(mOptionMenus.size());
        int size = Math.min(mOptionMenus.size(), getChildCount());
        for (int i = 0; i < size; i++) {
            CheckedTextView textView = (CheckedTextView) getChildAt(i);
            textView.setTag(String.valueOf(i));
            mOptionMenus.get(i).validate(textView);
        }
    }

    private void adjustChildCount(int targetCount) {
        int count = getChildCount();
        if (count < targetCount) {
            for (int i = targetCount - count; i > 0; i--) {
                addView(newMenuItemView());
            }
        } else {
            for (int i = targetCount; i < count; i++) {
                View view = getChildAt(i);
                view.setVisibility(GONE);
            }
        }
        if (targetCount != count) {
            resetPadding();
        }
    }

    private OptionItemView newMenuItemView() {
        OptionItemView itemView = (OptionItemView) inflate(getContext(), R.layout.layout_menu_item, null);
        LayoutParams params = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        if (getOrientation() == HORIZONTAL) {
            if (getChildCount() > 0) {
                params.leftMargin = 1;
            } else {
                mLeftPadding = itemView.getPaddingLeft();
                mTopPadding = itemView.getPaddingTop();
                mRightPadding = itemView.getPaddingRight();
                mBottomPadding = itemView.getPaddingBottom();
            }
        } else {
            if (getChildCount() > 0) {
                params.topMargin = 1;
                params.width = LayoutParams.MATCH_PARENT;
            } else {
                mLeftPadding = itemView.getPaddingLeft();
                mTopPadding = itemView.getPaddingTop();
                mRightPadding = itemView.getPaddingRight();
                mBottomPadding = itemView.getPaddingBottom();
            }
        }
        itemView.setLayoutParams(params);
        itemView.setOnClickListener(this);
        return itemView;
    }

    private void resetPadding() {
        int count = Math.min(getChildCount(), mOptionMenus.size());
        if (count > 0) {
            if (count == 1) {
                // 只有一个时设置四周边界
                getChildAt(0).setPadding(mLeftPadding + mBulgeSize, mTopPadding + mBulgeSize,
                        mRightPadding + mBulgeSize, mBottomPadding + mBulgeSize);
            } else {
                View first = getChildAt(0);
                View last = getChildAt(count - 1);
                if (getOrientation() == HORIZONTAL) {
                    first.setPadding(mLeftPadding + mBulgeSize, mTopPadding + mBulgeSize,
                            mRightPadding, mBottomPadding + mBulgeSize);

                    last.setPadding(mLeftPadding, mTopPadding + mBulgeSize,
                            mRightPadding + mBulgeSize, mBottomPadding + mBulgeSize);

                    for (int i = 1; i < count - 1; i++) {
                        getChildAt(i).setPadding(mLeftPadding, mTopPadding + mBulgeSize,
                                mRightPadding, mBottomPadding + mBulgeSize);
                    }
                } else {
                    first.setPadding(mLeftPadding + mBulgeSize, mTopPadding + mBulgeSize,
                            mRightPadding + mBulgeSize, mBottomPadding);
                    last.setPadding(mLeftPadding + mBulgeSize, mTopPadding,
                            mRightPadding + mBulgeSize, mBottomPadding + mBulgeSize);
                    for (int i = 1; i < count - 1; i++) {
                        getChildAt(i).setPadding(mLeftPadding + mBulgeSize, mTopPadding,
                                mRightPadding + mBulgeSize, mBottomPadding);
                    }
                }
            }
        }
    }

    @Override
    public void onBulgeChanged(int site, int size) {
        if (size != mBulgeSize) {
            mBulgeSize = size;
            resetPadding();
        }
    }

    public void setOnOptionMenuClickListener(OnOptionMenuClickListener listener) {
        mMenuClickListener = listener;
    }

    @Override
    public void onClick(View v) {
        if (v.getTag() != null) {
            try {
                int position = Integer.parseInt((String) v.getTag());
                if (mMenuClickListener != null && position >= 0 && position < mOptionMenus.size()) {
                    mMenuClickListener.onOptionMenuClick(position, mOptionMenus.get(position));
                }
            } catch (Exception e) {
                Log.d(TAG, e.getMessage(), e);
            }
        }
    }

    public interface OnOptionMenuClickListener {

        boolean onOptionMenuClick(int position, OptionMenu menu);
    }
}