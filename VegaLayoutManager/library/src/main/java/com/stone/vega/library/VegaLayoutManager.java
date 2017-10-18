package com.stone.vega.library;

import android.graphics.Rect;
import android.support.v4.util.ArrayMap;
import android.support.v7.widget.RecyclerView;
import android.util.SparseArray;
import android.util.SparseBooleanArray;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by xmuSistone on 2017/9/20.
 */
public class VegaLayoutManager extends RecyclerView.LayoutManager {

    private int scroll = 0;
    private SparseArray<Rect> locationRects = new SparseArray<>();
    private SparseBooleanArray attachedItems = new SparseBooleanArray();
    private ArrayMap<Integer, Integer> viewTypeHeightMap = new ArrayMap<>();

    private boolean needSnap = false;
    private boolean firstLayoutChildren = true;
    private int lastDy = 0;
    private int maxScroll = -1;
    private RecyclerView.Adapter adapter;
    private RecyclerView.Recycler recycler;

    @Override
    public RecyclerView.LayoutParams generateDefaultLayoutParams() {
        return new RecyclerView.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
    }

    /**
     * notifyDataSetChanged时会调用此方法
     */
    @Override
    public void onItemsChanged(RecyclerView recyclerView) {
        super.onItemsChanged(recyclerView);
        if (null != recycler) {
            buildLocationRects();
            layoutItemsOnScroll();
        }
    }

    @Override
    public void onLayoutChildren(RecyclerView.Recycler recycler, RecyclerView.State state) {
        this.recycler = recycler; // 二话不说，先把recycler保存了
        if (!firstLayoutChildren || adapter == null || getItemCount() <= 0 || state.isPreLayout()) {
            return;
        }
        firstLayoutChildren = false;

        buildLocationRects();

        // 先回收放到缓存，后面会再次统一layout
        detachAndScrapAttachedViews(recycler);
        layoutItemsOnCreate(recycler);
    }

    private void buildLocationRects() {
        viewTypeHeightMap.clear();
        locationRects.clear();
        attachedItems.clear();

        int tempPosition = getPaddingTop();
        int itemCount = getItemCount();
        for (int i = 0; i < itemCount; i++) {
            // 1. 先计算出itemWidth和itemHeight
            int viewType = adapter.getItemViewType(i);
            int itemHeight;
            if (viewTypeHeightMap.containsKey(viewType)) {
                itemHeight = viewTypeHeightMap.get(viewType);
            } else {
                View itemView = recycler.getViewForPosition(i);
                addView(itemView);
                measureChildWithMargins(itemView, View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
                itemHeight = getDecoratedMeasuredHeight(itemView);
                viewTypeHeightMap.put(viewType, itemHeight);
            }

            // 2. 组装Rect并保存
            Rect rect = new Rect();
            rect.left = getPaddingLeft();
            rect.top = tempPosition;
            rect.right = getWidth() - getPaddingRight();
            rect.bottom = rect.top + itemHeight;
            locationRects.put(i, rect);
            attachedItems.put(i, false);
            tempPosition = tempPosition + itemHeight;
        }

        computeMaxScroll();
    }

    @Override
    public void onAdapterChanged(RecyclerView.Adapter oldAdapter, RecyclerView.Adapter newAdapter) {
        this.adapter = newAdapter;
        if (null != recycler) {
            buildLocationRects();
            layoutItemsOnScroll();
        }
        super.onAdapterChanged(oldAdapter, newAdapter);
    }

    /**
     * 对外提供接口，找到第一个可视view的index
     */
    public int findFirstVisibleItemPosition() {
        int count = locationRects.size();
        Rect displayRect = new Rect(0, scroll, getWidth(), getHeight() + scroll);
        for (int i = 0; i < count; i++) {
            if (Rect.intersects(displayRect, locationRects.get(i)) &&
                    attachedItems.get(i)) {
                return i;
            }
        }
        return 0;
    }

    /**
     * 计算可滑动的最大值
     */
    private void computeMaxScroll() {
        maxScroll = locationRects.get(locationRects.size() - 1).bottom - getHeight();
        if (maxScroll < 0) {
            maxScroll = 0;
            return;
        }

        int itemCount = getItemCount();
        int screenFilledHeight = 0;
        for (int i = itemCount - 1; i >= 0; i--) {
            Rect rect = locationRects.get(i);
            screenFilledHeight = screenFilledHeight + (rect.bottom - rect.top);
            if (screenFilledHeight > getHeight()) {
                int extraSnapHeight = getHeight() - (screenFilledHeight - (rect.bottom - rect.top));
                maxScroll = maxScroll + extraSnapHeight;
                break;
            }
        }
    }

    /**
     * 初始化的时候，layout子View
     */
    private void layoutItemsOnCreate(RecyclerView.Recycler recycler) {
        int itemCount = getItemCount();
        for (int i = 0; i < itemCount; i++) {
            View childView = recycler.getViewForPosition(i);
            addView(childView);
            measureChildWithMargins(childView, View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
            layoutItem(childView, locationRects.get(i));
            attachedItems.put(i, true);
            childView.setPivotY(0);
            childView.setPivotX(childView.getMeasuredWidth() / 2);

            if (locationRects.get(i).top > getHeight()) {
                break;
            }
        }
    }


    /**
     * 初始化的时候，layout子View
     */
    private void layoutItemsOnScroll() {
        int childCount = getChildCount();
        // 1. 已经在屏幕上显示的child
        int itemCount = getItemCount();
        Rect displayRect = new Rect(0, scroll, getWidth(), getHeight() + scroll);
        int firstVisiblePosition = -1;
        int lastVisiblePosition = -1;
        for (int i = childCount - 1; i >= 0; i--) {
            View child = getChildAt(i);
            if (child == null) {
                continue;
            }
            int position = getPosition(child);
            if (!Rect.intersects(displayRect, locationRects.get(position))) {
                // 回收滑出屏幕的View
                removeAndRecycleView(child, recycler);
                attachedItems.put(position, false);
            } else {
                // Item还在显示区域内，更新滑动后Item的位置
                if (lastVisiblePosition < 0) {
                    lastVisiblePosition = position;
                }

                if (firstVisiblePosition < 0) {
                    firstVisiblePosition = position;
                } else {
                    firstVisiblePosition = Math.min(firstVisiblePosition, position);
                }

                layoutItem(child, locationRects.get(position)); //更新Item位置
            }
        }

        // 2. 复用View处理
        if (firstVisiblePosition > 0) {
            // 往前搜索复用
            for (int i = firstVisiblePosition - 1; i >= 0; i--) {
                if (Rect.intersects(displayRect, locationRects.get(i)) &&
                        !attachedItems.get(i)) {
                    reuseItemOnSroll(i, true);
                } else {
                    break;
                }
            }
        }
        // 往后搜索复用
        for (int i = lastVisiblePosition + 1; i < itemCount; i++) {
            if (Rect.intersects(displayRect, locationRects.get(i)) &&
                    !attachedItems.get(i)) {
                reuseItemOnSroll(i, false);
            } else {
                break;
            }
        }
    }

    /**
     * 复用position对应的View
     */
    private void reuseItemOnSroll(int position, boolean addViewFromTop) {
        View scrap = recycler.getViewForPosition(position);
        measureChildWithMargins(scrap, 0, 0);
        scrap.setPivotY(0);
        scrap.setPivotX(scrap.getMeasuredWidth() / 2);

        if (addViewFromTop) {
            addView(scrap, 0);
        } else {
            addView(scrap);
        }
        // 将这个Item布局出来
        layoutItem(scrap, locationRects.get(position));
        attachedItems.put(position, true);
    }


    private void layoutItem(View child, Rect rect) {
        int topDistance = scroll - rect.top;
        int layoutTop, layoutBottom;
        int itemHeight = rect.bottom - rect.top;
        if (topDistance < itemHeight && topDistance >= 0) {
            float rate1 = (float) topDistance / itemHeight;
            float rate2 = 1 - rate1 * rate1 / 3;
            float rate3 = 1 - rate1 * rate1;
            child.setScaleX(rate2);
            child.setScaleY(rate2);
            child.setAlpha(rate3);
            layoutTop = 0;
            layoutBottom = itemHeight;
        } else {
            child.setScaleX(1);
            child.setScaleY(1);
            child.setAlpha(1);

            layoutTop = rect.top - scroll;
            layoutBottom = rect.bottom - scroll;
        }
        layoutDecorated(child, rect.left, layoutTop, rect.right, layoutBottom);
    }

    @Override
    public boolean canScrollVertically() {
        return true;
    }

    @Override
    public int scrollVerticallyBy(int dy, RecyclerView.Recycler recycler, RecyclerView.State state) {
        if (getItemCount() == 0 || dy == 0) {
            return 0;
        }
        int travel = dy;
        if (dy + scroll < 0) {
            travel = -scroll;
        } else if (dy + scroll > maxScroll) {
            travel = maxScroll - scroll;
        }
        scroll += travel; //累计偏移量
        lastDy = dy;
        if (!state.isPreLayout() && getChildCount() > 0) {
            layoutItemsOnScroll();
        }

        return travel;
    }

    @Override
    public void onAttachedToWindow(RecyclerView view) {
        super.onAttachedToWindow(view);
        new StartSnapHelper().attachToRecyclerView(view);
    }

    @Override
    public void onScrollStateChanged(int state) {
        if (state == RecyclerView.SCROLL_STATE_DRAGGING) {
            needSnap = true;
        }
        super.onScrollStateChanged(state);
    }

    public int getSnapHeight() {
        if (!needSnap) {
            return 0;
        }
        needSnap = false;

        Rect displayRect = new Rect(0, scroll, getWidth(), getHeight() + scroll);
        int itemCount = getItemCount();
        for (int i = 0; i < itemCount; i++) {
            Rect itemRect = locationRects.get(i);
            if (displayRect.intersect(itemRect)) {

                if (lastDy > 0) {
                    // scroll变大，属于列表往下走，往下找下一个为snapView
                    if (i < itemCount - 1) {
                        Rect nextRect = locationRects.get(i + 1);
                        return nextRect.top - displayRect.top;
                    }
                }
                return itemRect.top - displayRect.top;
            }
        }
        return 0;
    }

    public View findSnapView() {
        if (getChildCount() > 0) {
            return getChildAt(0);
        }
        return null;
    }
}
