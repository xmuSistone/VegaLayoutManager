/
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package com.stone.vega.library;

import android.graphics.Rect;
import android.util.SparseArray;
import android.util.SparseBooleanArray;
import android.view.View;
import androidx.collection.ArrayMap;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.RecyclerView.Adapter;
import androidx.recyclerview.widget.RecyclerView.LayoutManager;
import androidx.recyclerview.widget.RecyclerView.LayoutParams;
import androidx.recyclerview.widget.RecyclerView.Recycler;
import androidx.recyclerview.widget.RecyclerView.State;

public class VegaLayoutManager extends LayoutManager {
    private int scroll = 0;
    private SparseArray<Rect> locationRects = new SparseArray();
    private SparseBooleanArray attachedItems = new SparseBooleanArray();
    private ArrayMap<Integer, Integer> viewTypeHeightMap = new ArrayMap();
    private boolean needSnap = false;
    private int lastDy = 0;
    private int maxScroll = -1;
    private Adapter adapter;
    private Recycler recycler;

    public VegaLayoutManager() {
    }

    public LayoutParams generateDefaultLayoutParams() {
        return new LayoutParams(-2, -2);
    }

    public void onAdapterChanged(Adapter oldAdapter, Adapter newAdapter) {
        super.onAdapterChanged(oldAdapter, newAdapter);
        this.adapter = newAdapter;
    }

    public void onLayoutChildren(Recycler recycler, State state) {
        this.recycler = recycler;
        if (!state.isPreLayout()) {
            this.buildLocationRects();
            this.detachAndScrapAttachedViews(recycler);
            this.layoutItemsOnCreate(recycler);
        }
    }

    private void buildLocationRects() {
        this.locationRects.clear();
        this.attachedItems.clear();
        int tempPosition = this.getPaddingTop();
        int itemCount = this.getItemCount();

        for(int i = 0; i < itemCount; ++i) {
            int viewType = this.adapter.getItemViewType(i);
            int itemHeight;
            if (this.viewTypeHeightMap.containsKey(viewType)) {
                itemHeight = (Integer)this.viewTypeHeightMap.get(viewType);
            } else {
                View itemView = this.recycler.getViewForPosition(i);
                this.addView(itemView);
                this.measureChildWithMargins(itemView, 0, 0);
                itemHeight = this.getDecoratedMeasuredHeight(itemView);
                this.viewTypeHeightMap.put(viewType, itemHeight);
            }

            Rect rect = new Rect();
            rect.left = this.getPaddingLeft();
            rect.top = tempPosition;
            rect.right = this.getWidth() - this.getPaddingRight();
            rect.bottom = rect.top + itemHeight;
            this.locationRects.put(i, rect);
            this.attachedItems.put(i, false);
            tempPosition += itemHeight;
        }

        if (itemCount == 0) {
            this.maxScroll = 0;
        } else {
            this.computeMaxScroll();
        }

    }

    public int findFirstVisibleItemPosition() {
        int count = this.locationRects.size();
        Rect displayRect = new Rect(0, this.scroll, this.getWidth(), this.getHeight() + this.scroll);

        for(int i = 0; i < count; ++i) {
            if (Rect.intersects(displayRect, (Rect)this.locationRects.get(i)) && this.attachedItems.get(i)) {
                return i;
            }
        }

        return 0;
    }

    private void computeMaxScroll() {
        this.maxScroll = ((Rect)this.locationRects.get(this.locationRects.size() - 1)).bottom - this.getHeight();
        if (this.maxScroll < 0) {
            this.maxScroll = 0;
        } else {
            int itemCount = this.getItemCount();
            int screenFilledHeight = 0;

            for(int i = itemCount - 1; i >= 0; --i) {
                Rect rect = (Rect)this.locationRects.get(i);
                screenFilledHeight += rect.bottom - rect.top;
                if (screenFilledHeight > this.getHeight()) {
                    int extraSnapHeight = this.getHeight() - (screenFilledHeight - (rect.bottom - rect.top));
                    this.maxScroll += extraSnapHeight;
                    break;
                }
            }

        }
    }

    private void layoutItemsOnCreate(Recycler recycler) {
        int itemCount = this.getItemCount();
        Rect displayRect = new Rect(0, this.scroll, this.getWidth(), this.getHeight() + this.scroll);

        for(int i = 0; i < itemCount; ++i) {
            Rect thisRect = (Rect)this.locationRects.get(i);
            if (Rect.intersects(displayRect, thisRect)) {
                View childView = recycler.getViewForPosition(i);
                this.addView(childView);
                this.measureChildWithMargins(childView, 0, 0);
                this.layoutItem(childView, (Rect)this.locationRects.get(i));
                this.attachedItems.put(i, true);
                childView.setPivotY(0.0F);
                childView.setPivotX((float)(childView.getMeasuredWidth() / 2));
                if (thisRect.top - this.scroll > this.getHeight()) {
                    break;
                }
            }
        }

    }

    private void layoutItemsOnScroll() {
        int childCount = this.getChildCount();
        int itemCount = this.getItemCount();
        Rect displayRect = new Rect(0, this.scroll, this.getWidth(), this.getHeight() + this.scroll);
        int firstVisiblePosition = -1;
        int lastVisiblePosition = -1;

        int i;
        for(i = childCount - 1; i >= 0; --i) {
            View child = this.getChildAt(i);
            if (child != null) {
                int position = this.getPosition(child);
                if (!Rect.intersects(displayRect, (Rect)this.locationRects.get(position))) {
                    this.removeAndRecycleView(child, this.recycler);
                    this.attachedItems.put(position, false);
                } else {
                    if (lastVisiblePosition < 0) {
                        lastVisiblePosition = position;
                    }

                    if (firstVisiblePosition < 0) {
                        firstVisiblePosition = position;
                    } else {
                        firstVisiblePosition = Math.min(firstVisiblePosition, position);
                    }

                    this.layoutItem(child, (Rect)this.locationRects.get(position));
                }
            }
        }

        if (firstVisiblePosition > 0) {
            for(i = firstVisiblePosition - 1; i >= 0 && Rect.intersects(displayRect, (Rect)this.locationRects.get(i)) && !this.attachedItems.get(i); --i) {
                this.reuseItemOnSroll(i, true);
            }
        }

        for(i = lastVisiblePosition + 1; i < itemCount && Rect.intersects(displayRect, (Rect)this.locationRects.get(i)) && !this.attachedItems.get(i); ++i) {
            this.reuseItemOnSroll(i, false);
        }

    }

    private void reuseItemOnSroll(int position, boolean addViewFromTop) {
        View scrap = this.recycler.getViewForPosition(position);
        this.measureChildWithMargins(scrap, 0, 0);
        scrap.setPivotY(0.0F);
        scrap.setPivotX((float)(scrap.getMeasuredWidth() / 2));
        if (addViewFromTop) {
            this.addView(scrap, 0);
        } else {
            this.addView(scrap);
        }

        this.layoutItem(scrap, (Rect)this.locationRects.get(position));
        this.attachedItems.put(position, true);
    }

    private void layoutItem(View child, Rect rect) {
        int topDistance = this.scroll - rect.top;
        int itemHeight = rect.bottom - rect.top;
        int layoutTop;
        int layoutBottom;
        if (topDistance < itemHeight && topDistance > 0) {
            float rate1 = (float)topDistance / (float)itemHeight;
            float rate2 = 1.0F - rate1 * rate1 / 3.0F;
            float rate3 = 1.0F - rate1 * rate1;
            child.setScaleX(rate2);
            child.setScaleY(rate2);
            child.setAlpha(rate3);
            layoutTop = 0;
            layoutBottom = itemHeight;
        } else {
            child.setScaleX(1.0F);
            child.setScaleY(1.0F);
            child.setAlpha(1.0F);
            layoutTop = rect.top - this.scroll;
            layoutBottom = rect.bottom - this.scroll;
        }

        this.layoutDecorated(child, rect.left, layoutTop, rect.right, layoutBottom);
    }

    public boolean canScrollVertically() {
        return true;
    }

    public int scrollVerticallyBy(int dy, Recycler recycler, State state) {
        if (this.getItemCount() != 0 && dy != 0) {
            int travel = dy;
            if (dy + this.scroll < 0) {
                travel = -this.scroll;
            } else if (dy + this.scroll > this.maxScroll) {
                travel = this.maxScroll - this.scroll;
            }

            this.scroll += travel;
            this.lastDy = dy;
            if (!state.isPreLayout() && this.getChildCount() > 0) {
                this.layoutItemsOnScroll();
            }

            return travel;
        } else {
            return 0;
        }
    }

    public void onAttachedToWindow(RecyclerView view) {
        super.onAttachedToWindow(view);
        (new StartSnapHelper()).attachToRecyclerView(view);
    }

    public void onScrollStateChanged(int state) {
        if (state == 1) {
            this.needSnap = true;
        }

        super.onScrollStateChanged(state);
    }

    public int getSnapHeight() {
        if (!this.needSnap) {
            return 0;
        } else {
            this.needSnap = false;
            Rect displayRect = new Rect(0, this.scroll, this.getWidth(), this.getHeight() + this.scroll);
            int itemCount = this.getItemCount();

            for(int i = 0; i < itemCount; ++i) {
                Rect itemRect = (Rect)this.locationRects.get(i);
                if (displayRect.intersect(itemRect)) {
                    if (this.lastDy > 0 && i < itemCount - 1) {
                        Rect nextRect = (Rect)this.locationRects.get(i + 1);
                        return nextRect.top - displayRect.top;
                    }

                    return itemRect.top - displayRect.top;
                }
            }

            return 0;
        }
    }

    public View findSnapView() {
        return this.getChildCount() > 0 ? this.getChildAt(0) : null;
    }
}
