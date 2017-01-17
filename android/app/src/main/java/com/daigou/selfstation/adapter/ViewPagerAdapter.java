package com.daigou.selfstation.adapter;

import android.support.v4.view.PagerAdapter;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

/**
 * Created by DaZhuang on 15/10/21.
 */
public class ViewPagerAdapter extends PagerAdapter {
    private List<View> views;
    public ViewPagerAdapter(List<View> views) {
        this.views = views;
    }
    @Override
    public int getCount() {
        return views.size();
    }

    @Override
    public boolean isViewFromObject(View arg0, Object arg1) {
        return arg0 == arg1;
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        (container).addView(views.get(position));
        return views.get(position);
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        (container).removeView(views.get(position));
    }

    @Override
    public CharSequence getPageTitle(int position) {
        switch (position) {
            case 0:
                return "incoming";
            case 1:
                return "arrived";
            case 2:
                return "completed";
            default:
                return "";
        }
    }
}