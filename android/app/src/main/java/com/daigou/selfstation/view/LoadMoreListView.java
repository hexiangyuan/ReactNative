package com.daigou.selfstation.view;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.widget.AbsListView;
import android.widget.ListView;

/**
 * Created by 65grouper on 15/10/14.
 * 自定义的上拉加载的listView
 */
public class LoadMoreListView extends ListView implements AbsListView.OnScrollListener {
    int lastItem;
    int totalItemCount;
    boolean isLoading;
    OnLoadingListener onLoadingListener;

    @Override
    protected void dispatchDraw(Canvas canvas) {
        try {
            super.dispatchDraw(canvas);
        } catch (IndexOutOfBoundsException e) {
            Log.e("Exception", e.getMessage().toString());
        }
    }
    public interface OnLoadingListener {
        void onLoad();
    }

    public LoadMoreListView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView(context);
    }

    public LoadMoreListView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView(context);
    }

    public LoadMoreListView(Context context) {
        super(context);
        initView(context);
    }

    public void initView(Context context) {
        LayoutInflater inflater = LayoutInflater.from(context);
        setOnScrollListener(this);

    }

    /**
     *
     * @param view
     * @param scrollState
     */
    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {
        switch (scrollState) {
            case OnScrollListener.SCROLL_STATE_IDLE:

                if (totalItemCount - getHeaderViewsCount() > 0 &&
                        view.getLastVisiblePosition() == (view.getCount() - 1)) {
                    // 如果滚动到底部
                    if (!isLoading) {
                        isLoading = true;
                        onLoadingListener.onLoad();
                    }
                }
                break;
            case OnScrollListener.SCROLL_STATE_TOUCH_SCROLL:// 正在滚动
                break;
            case OnScrollListener.SCROLL_STATE_FLING:
                break;
        }
    }

    public void setOnLoadingListener(OnLoadingListener onLoadingListener) {
        this.onLoadingListener = onLoadingListener;
    }

    public void completeLoad() {
        isLoading = false;
    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
        lastItem = firstVisibleItem + visibleItemCount;
        this.totalItemCount = totalItemCount;
    }
}
