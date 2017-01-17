package com.daigou.selfstation.listener;

import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;

/**
 * Created by 65grouper on 15/11/12.
 */
public class OnRcvScrollListener extends RecyclerView.OnScrollListener {
    public interface OnLoadListener {
        void onLoad();
    }

    private int[] lastPositions;
    private int lastVisibleItemPosition;
    private int currentScrollState = 0;
    private OnLoadListener onLoadListener;
    private boolean isCompleted = false;

    @Override
    public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
        super.onScrolled(recyclerView, dx, dy);
        RecyclerView.LayoutManager layoutManager = recyclerView.getLayoutManager();
        if (layoutManager instanceof LinearLayoutManager) {
            lastVisibleItemPosition = ((LinearLayoutManager) layoutManager).findLastVisibleItemPosition();
        } else if (layoutManager instanceof GridLayoutManager) {
            lastVisibleItemPosition = ((GridLayoutManager) layoutManager).findLastVisibleItemPosition();
        } else if (layoutManager instanceof StaggeredGridLayoutManager) {
            StaggeredGridLayoutManager staggeredGridLayoutManager
                    = ((StaggeredGridLayoutManager) layoutManager);
            if (lastPositions == null) {
                lastPositions = new int[staggeredGridLayoutManager.getSpanCount()];
            }
            staggeredGridLayoutManager.findLastVisibleItemPositions(lastPositions);
            lastVisibleItemPosition = findMax(lastPositions);
        } else {
            throw new RuntimeException("Unsupported layoutManager used.");
        }
    }

    @Override
    public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
        super.onScrollStateChanged(recyclerView, newState);
        currentScrollState = newState;
        RecyclerView.LayoutManager layoutManager = recyclerView.getLayoutManager();
        int visibleItemCount = layoutManager.getChildCount();
        int totalItemCount = layoutManager.getItemCount();
        if (visibleItemCount > 0 && currentScrollState == RecyclerView.SCROLL_STATE_IDLE &&
                (lastVisibleItemPosition >= totalItemCount - 1) && isCompleted == false) {
            onLoadListener.onLoad();
        }
    }

    public void setCompleted(boolean isCompleted) {
        this.isCompleted = isCompleted;
    }

    public void setOnLoadListener(OnLoadListener onLoadListener) {
        this.onLoadListener = onLoadListener;
    }

    private int findMax(int[] lastPositions) {
        int max = lastPositions[0];
        for (int value : lastPositions) {
            if (value > max) {
                max = value;
            }
        }
        return max;
    }
}
