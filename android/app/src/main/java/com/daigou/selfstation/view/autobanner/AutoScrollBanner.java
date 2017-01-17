package com.daigou.selfstation.view.autobanner;

import android.content.Context;
import android.os.Handler;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.daigou.selfstation.R;
import com.facebook.drawee.view.SimpleDraweeView;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

/**
 * copy by ezbuy app
 */
public class AutoScrollBanner extends FrameLayout implements OnPageChangeListener {

    public static final int VIEWPAGER_COUNT = 10000;//伪无限制循环,将界面放置于一个很大的数额页码;
    /**
     * 我乱写的ID
     * set的Handler需要处理这个ID
     * 回调changePage()方法切换视图
     */
    public static final int HANDLER_MSG_ID = 122459093;
    private final int AUTO_SCROLL_TIME = 4000;
    private ArrayList<BannerBean> bannerList = new ArrayList<>();
    private ViewPager mViewpager;
    private LinearLayout pointGroup;
    private TextView tvDescriptions;
    private List<ImageView> imageList = new ArrayList<>();
    private List<View> viewList = new ArrayList<>();
    private int lastPointIndex;
    private boolean isAutoScroll = true;
    private MyPagerAdapter mBannerAdapter;
    private long before;
    private int scrollStatus;
    private int realSize = 0;
    private BaseAutoScrollViewAdapter adapter;
    // 自动滚动的消息机制
    private Timer timer;
    private Handler handler;
    private OnPageImageClickListener listener;

    public AutoScrollBanner(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView(context);
    }

    public AutoScrollBanner(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView(context);
    }

    public AutoScrollBanner(Context context) {
        super(context);
        initView(context);
    }

    public void setHandler(Handler handler) {
        this.handler = handler;
    }

    public void setOnPageImageClickListener(OnPageImageClickListener listener) {
        this.listener = listener;
    }

    public void setPointIsVisible(boolean isVisible) {
        if (pointGroup != null) {
            if (isVisible) {
                pointGroup.setVisibility(View.VISIBLE);
            } else {
                pointGroup.setVisibility(View.GONE);
            }
        }
    }

    public void setTitleIsVisible(boolean isVisible) {
        if (tvDescriptions != null) {
            if (isVisible) {
                tvDescriptions.setVisibility(View.VISIBLE);
            } else {
                tvDescriptions.setVisibility(View.GONE);
            }
        }
    }

    public void setAutoScroll(boolean autoScroll) {
        isAutoScroll = autoScroll;
    }

    public void setAdapter(BaseAutoScrollViewAdapter adapter) {
        this.adapter = adapter;
    }

    // 初始化控件
    private void initView(final Context context) {
        View.inflate(context, R.layout.view_auto_scroll_banner, this);
        mViewpager = (ViewPager) findViewById(R.id.viewpager);
        pointGroup = (LinearLayout) findViewById(R.id.ll_image_point);
        tvDescriptions = (TextView) findViewById(R.id.tv_image_desc);
        mViewpager.addOnPageChangeListener(this);
        startAutoScroll();
    }

    public void setBannerBeanList(List<BannerBean> list) {
        mBannerAdapter = new MyPagerAdapter(imageList);
        mViewpager.setAdapter(mBannerAdapter);
        bannerList.clear();
        pointGroup.removeAllViews();
        imageList.clear();
        if (list.size() > 0) {
            bannerList.addAll(list);
            initImageViewsData();
        }
    }

    public void setViewList(List<View> list) {
        viewList.clear();
        mViewpager.setAdapter(adapter);
        pointGroup.removeAllViews();
        if (list.size() > 0) {
            viewList.addAll(list);
            initViewsDate();
        }
    }

    public void notifyDataSetChanged() {
        if (mBannerAdapter != null) {
            mBannerAdapter.notifyDataSetChanged();
        }
    }

    private void initViewsDate() {
        if (viewList.size() < 4 && viewList.size() > 1) {
            viewList.addAll(viewList);
            realSize = viewList.size() / 2;
        } else {
            realSize = viewList.size();
        }
        initPoints(realSize);
        if (viewList.size() == 1) {
            isAutoScroll = false;
            adapter.notifyDataSetChanged();
            //设置当前的item为一个无限循环的中间值
            mViewpager.setCurrentItem(0);
        } else {
            isAutoScroll = true;
            adapter.notifyDataSetChanged();
            //设置当前的item为一个VIEWPAGER_COUNT无限循环的中间值
            mViewpager.setCurrentItem(VIEWPAGER_COUNT / 2 - VIEWPAGER_COUNT / 2 % realSize);
        }
        lastPointIndex = 0;
        pointGroup.getChildAt(0).setEnabled(true);//设置第一个点viewList.size
    }

    // 初始化ImageViews数据
    private void initImageViewsData() {
        if (bannerList.size() < 4 && bannerList.size() > 1) {
            bannerList.addAll(bannerList);
            initImageViews(bannerList.size());
            realSize = bannerList.size() / 2;
        } else {
            realSize = bannerList.size();
            initImageViews(realSize);
        }
        initPoints(realSize);
        if (bannerList.size() == 1) {
            isAutoScroll = false;
            mBannerAdapter.notifyDataSetChanged();
            //设置当前的item为一个无限循环的中间值
            mViewpager.setCurrentItem(0);
        } else {
            isAutoScroll = true;
            mBannerAdapter.notifyDataSetChanged();
            //设置当前的item为一个VIEWPAGER_COUNT无限循环的中间值
            mViewpager.setCurrentItem(VIEWPAGER_COUNT / 2 - VIEWPAGER_COUNT / 2 % realSize);
        }
        tvDescriptions.setText(bannerList.get(0).getTitle());// 设置第一个标题
        lastPointIndex = 0;
        pointGroup.getChildAt(0).setEnabled(true);//设置第一个点
    }

    private void initPoints(int size) {
        for (int i = 0; i < size; i++) {
            //设置下面指示点的指示个数
            ImageView point = new ImageView(getContext());
            point.setBackgroundResource(R.drawable.selector_point);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            params.setMargins(0, 3, 15, 3);
            point.setLayoutParams(params);
            point.setEnabled(false);
            pointGroup.addView(point);
        }
    }

    private void initImageViews(int size) {
        for (int i = 0; i < size; i++) {
            // 添加数据到ImageView集合中
            SimpleDraweeView simpleDraweeView = new SimpleDraweeView(getContext());
//            simpleDraweeView.setPlaceHolderColor(R.color.default_background);
            simpleDraweeView.setImageURI(bannerList.get(i).getImagPath());
            imageList.add(simpleDraweeView);
        }
    }

    /**
     * handler 处理时调用
     */
    public void changePage() {
        mViewpager.setCurrentItem(mViewpager.getCurrentItem() + 1);
    }

    public void startAutoScroll() {
        if (isAutoScroll && handler != null) {
            timer = new Timer();
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    //停止,并且实际间隔大雨定义时长,且开启自动滚动,有handler处理对象
                    if (scrollStatus == ViewPager.SCROLL_STATE_IDLE && System.currentTimeMillis() - before >= AUTO_SCROLL_TIME && getContext() != null && handler != null) {
                        handler.sendEmptyMessage(HANDLER_MSG_ID);
                    }
                }
            }, AUTO_SCROLL_TIME, AUTO_SCROLL_TIME);
        }
    }

    public void stopAutoScroll() {
        if (timer != null) {
            timer.cancel();
        }
    }

    /**
     * pager监听器
     */
    @Override
    public void onPageSelected(int position) {
        before = System.currentTimeMillis();
        if (realSize == 0) {
            return;
        }
        int realPosition = position % realSize;
        if (bannerList.size() > 0) {
            // 设置标题的改变
            tvDescriptions.setText(bannerList.get(realPosition).getTitle());
        }
        // 设置当前point的状态和前一个point的状态
        pointGroup.getChildAt(realPosition).setEnabled(true);
        pointGroup.getChildAt(lastPointIndex).setEnabled(false);
        // 将当前的arg0赋值给lastPointIndex
        lastPointIndex = realPosition;
    }

    @Override
    public void onPageScrollStateChanged(int state) {
        scrollStatus = state;
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

    }

    public interface OnPageImageClickListener {
        void OnPagerClick(int position, View view);
    }

    /**
     * ViewPager适配器
     */
    private class MyPagerAdapter<T> extends PagerAdapter {
        private List<View> list = new ArrayList<>();

        public MyPagerAdapter(List<View> list) {
            this.list = list;
        }

        @Override
        public int getCount() {
            if (list == null) {
                return 0;
            }
            if (list.size() == 1) {
                return 1;
            }
            return VIEWPAGER_COUNT;// 伪无限制循环,将界面放置于一个很大的数额页码;
        }

        @Override
        public boolean isViewFromObject(View arg0, Object arg1) {
            return arg0 == arg1;
        }

        @Override
        public Object instantiateItem(ViewGroup container, final int position) {
            View v = null;
            if (list != null && list.size() != 0) {
                if (list.size() == 1) {
                    v = list.get(0);
                } else {
                    v = list.get(position % list.size());
                }
                if (v.getParent() != null) {
                    ((ViewGroup) v.getParent()).removeView(v);
                }
                container.addView(v);
                v.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (listener != null) {
                            listener.OnPagerClick(position % realSize, v);
                        }
                    }
                });
            }
            return v;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
        }

    }
}
