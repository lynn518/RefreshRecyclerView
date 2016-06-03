package com.lynn518.refreshrecyclerview;

import android.content.Context;
import android.support.v4.view.GestureDetectorCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.LinearLayout;

/**
 * 基于RecyclerView和SwipeRefreshLayout自定义RefreshRecyclerView,只适用于VERTICAL{@link LinearLayoutManager}
 * 实现的功能:
 * 1.下拉刷新(刷新完成后自动滚动至第一个item)
 * 2.上拉加载更多(刷新完成后自动滚动至新添加的数据的第一个item)
 * 3.条目点击监听
 * 4.条目长按监听
 * 5.自动刷新 {@link #pullRefresh()}
 * 6.当数据为空时,自动显示EmptyView {@link EmptyViewHolder}
 * 如果不需要下拉刷新功能,调用{@link #setPullRefreshEnable(boolean)}
 * 如果不需要上拉加载更多,{@link #setAdapter(RefreshAdapter)}中footerView参数给null就可以了
 *
 * @author lynn518(QQ:17949941 欢迎交流)
 */
public class RefreshRecyclerView extends LinearLayout {
    private static final String TAG = "RefreshRecyclerView";
    private RecyclerView mRecyclerView;
    private boolean isLoadingMore;
    private boolean isRefreshing;
    private boolean isPullRefreshEnable = true;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private OnItemClickListener onItemClickListener;
    private OnItemLongClickListener onItemLongClickListener;
    private OnLoadMoreListener onLoadMoreListener;
    private RefreshAdapter mAdapter;
    private Context mContext;
    private RecyclerView.OnScrollListener onScrollListener = new RecyclerView.OnScrollListener() {

        private int dy;
        private int lastVisibleItemPosition;

        @Override
        public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
            super.onScrolled(recyclerView, dx, dy);
            this.dy = dy;
        }

        @Override
        public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
            super.onScrollStateChanged(recyclerView, newState);
            LinearLayoutManager manager = (LinearLayoutManager) recyclerView.getLayoutManager();
            lastVisibleItemPosition = manager.findLastVisibleItemPosition();
            View footerView = mAdapter.getFooterView();
            if (!isLoadingMore && !isRefreshing && mAdapter.isLoadMoreEnable && !mAdapter.isEmptyStatus && lastVisibleItemPosition + 1 == mAdapter.getItemCount() &&
                    dy > 0 && newState == RecyclerView.SCROLL_STATE_IDLE && onLoadMoreListener != null) {
                if (footerView != null) {
                    footerView.setVisibility(View.VISIBLE);
                }
                isLoadingMore = true;
                onLoadMoreListener.onLoadMore();

            } else if (mAdapter.isLoadMoreEnable && footerView != null) {
                footerView.setVisibility(View.GONE);
            }
        }
    };
    private GestureDetectorCompat mGestureDetector;
    //    private ItemTouchHelper mItemTouchHelper;//可以实现拖动效果
    private boolean hasSetTouchListener;
    private OnPullRefreshListener onPullRefreshListener;

    public RefreshRecyclerView(Context context) {
        this(context, null);
    }


    public RefreshRecyclerView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }


    public RefreshRecyclerView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.mContext = context;
        init();
    }

    /**
     * 初始化
     */
    private void init() {
        this.setOrientation(VERTICAL);
        mSwipeRefreshLayout = new SwipeRefreshLayout(mContext);
        mRecyclerView = new RecyclerView(mContext);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(mContext, LinearLayoutManager.VERTICAL, false));
        mSwipeRefreshLayout.addView(mRecyclerView);
        this.addView(mSwipeRefreshLayout);
    }

    /**
     * 设置监听器
     */
    private void setTouchListener() {
        if (!hasSetTouchListener) {
            hasSetTouchListener = true;
            mGestureDetector = new GestureDetectorCompat(mContext, new GestureDetector.SimpleOnGestureListener() {
                @Override
                public boolean onSingleTapUp(MotionEvent e) {
                    View child = mRecyclerView.findChildViewUnder(e.getX(), e.getY());
                    if (child != null) {
                        RecyclerView.ViewHolder vh = mRecyclerView.getChildViewHolder(child);
                        int childAdapterPosition = mRecyclerView.getChildAdapterPosition(child);
                        if (onItemClickListener != null && childAdapterPosition + 1 != mAdapter.getItemCount()) {
                            onItemClickListener.onItemClick(vh, childAdapterPosition);
                        }
                    }
                    return true;
                }

                @Override
                public void onLongPress(MotionEvent e) {
//                super.onLongPress(e);
                    View child = mRecyclerView.findChildViewUnder(e.getX(), e.getY());
                    if (child != null) {
                        RecyclerView.ViewHolder vh = mRecyclerView.getChildViewHolder(child);
                        int childAdapterPosition = mRecyclerView.getChildAdapterPosition(child);
                        if (onItemLongClickListener != null && childAdapterPosition + 1 != mAdapter.getItemCount()) {
                            onItemLongClickListener.onItemLongClick(vh, childAdapterPosition);
                        }
                    }

                }

            });
            mRecyclerView.addOnItemTouchListener(new RecyclerView.SimpleOnItemTouchListener() {
                @Override
                public void onTouchEvent(RecyclerView rv, MotionEvent e) {
                    super.onTouchEvent(rv, e);
                    mGestureDetector.onTouchEvent(e);
                }


                @Override
                public boolean onInterceptTouchEvent(RecyclerView rv, MotionEvent e) {
                    return mGestureDetector.onTouchEvent(e);
                }
            });
        }

    }

    /**
     * 设置下拉刷新监听
     *
     * @param listener
     * @return
     */
    public RefreshRecyclerView setOnPullRefreshListener(OnPullRefreshListener listener) {
        this.onPullRefreshListener = listener;
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if (isPullRefreshEnable && !isRefreshing && !isLoadingMore && onPullRefreshListener != null) {
                    isRefreshing = true;
                    onPullRefreshListener.onPullRefresh();
                }
            }
        });
        return this;
    }

    /**
     * 设置条目点击监听
     *
     * @param onItemClickListener
     * @return
     */
    public RefreshRecyclerView setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
        setTouchListener();
        return this;
    }

    /**
     * 设置条目长按监听
     *
     * @param onItemLongClickListener
     * @return
     */
    public RefreshRecyclerView setOnItemLongClickListener(OnItemLongClickListener onItemLongClickListener) {
        this.onItemLongClickListener = onItemLongClickListener;
        setTouchListener();
        return this;
    }

    /**
     * 设置加载更多监听
     *
     * @param onLoadMoreListener
     * @return
     */
    public RefreshRecyclerView setOnLoadMoreListener(OnLoadMoreListener onLoadMoreListener) {
        this.onLoadMoreListener = onLoadMoreListener;
        return this;
    }

    /**
     * 设置分割线
     *
     * @param itemDecoration
     */
    public void addItemDecoration(RecyclerView.ItemDecoration itemDecoration) {
        this.mRecyclerView.addItemDecoration(itemDecoration);
    }

    /**
     * 通知下拉刷新完成
     */
    public void notifyRefreshOkAndDataChange(int size) {
        if (size > 0) {
            int status = mAdapter.getStatus();
            if (status == RefreshAdapter.STATUS_EMPTY) {
                mAdapter.notifyItemRemoved(0);
                mAdapter.setStatus(RefreshAdapter.STATUS_NORMAL);
            }
            mAdapter.notifyItemRangeInserted(0, size);
            setSelection(0);
        }
        mSwipeRefreshLayout.setRefreshing(false);
        isRefreshing = false;
    }

    /**
     * 通知加载更多完成并刷新数据
     *
     * @param size 新增数据的数量
     */
    public void notifyLoadMoreOkAndDataChange(int size) {
        if (size > 0) {
            mAdapter.notifyItemRangeInserted(mAdapter.getItemCount() + 1, size);
            setSelection(mAdapter.getItemCount() - 1 - size);
        }
        mAdapter.notifyItemRemoved(mAdapter.getItemCount());
        isLoadingMore = false;
    }

    /**
     * 获取适配器
     *
     * @return
     */
    public RefreshAdapter getAdapter() {
        return mAdapter;
    }

    /**
     * 设置适配器
     *
     * @param adapter
     */
    public void setAdapter(RefreshAdapter adapter) {
        if (adapter == null) {
            try {
                throw new Exception("adapter is null");
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            this.mAdapter = adapter;
            mRecyclerView.addOnScrollListener(onScrollListener);
            mRecyclerView.setAdapter(adapter);
        }

    }

    /**
     * 设置是否可以下拉刷新
     *
     * @param pullRefreshEnable
     */
    public void setPullRefreshEnable(boolean pullRefreshEnable) {
        this.isPullRefreshEnable = pullRefreshEnable;
    }

    /**
     * 滚动RecyclerView到指定的位置
     *
     * @param position
     */
    public void setSelection(int position) {
        mRecyclerView.scrollToPosition(position);
    }

    /**
     * 设置条目动画
     *
     * @param animator
     */
    public void setItemAnimator(RecyclerView.ItemAnimator animator) {
        mRecyclerView.setItemAnimator(animator);
    }

    /**
     * 设置下拉刷新进度条的颜色
     *
     * @param colors
     */
    public void setColorSchemeColors(int... colors) {
        mSwipeRefreshLayout.setColorSchemeColors(colors);
    }

    /**
     * 下拉刷新
     */
    public void pullRefresh() {
        int measuredHeight = mSwipeRefreshLayout.getMeasuredHeight();
        Log.e(TAG, "pullRefresh: measuredHeight : " + measuredHeight);
        if (measuredHeight <= 0) {
            mSwipeRefreshLayout.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    mSwipeRefreshLayout.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                    if (isPullRefreshEnable && !isRefreshing && onPullRefreshListener != null) {
                        mSwipeRefreshLayout.setRefreshing(true);
                        onPullRefreshListener.onPullRefresh();
                        isRefreshing = true;
                    }
                }
            });
        } else {
            if (isPullRefreshEnable && !isRefreshing && onPullRefreshListener != null) {
                mSwipeRefreshLayout.setRefreshing(true);
                onPullRefreshListener.onPullRefresh();
                isRefreshing = true;
            }
        }
    }

    /**
     * 条目点击监听器
     */
    public interface OnItemClickListener {
        void onItemClick(RecyclerView.ViewHolder holder, int position);
    }


    /**
     * 条目长按监听器
     */
    public interface OnItemLongClickListener {
        void onItemLongClick(RecyclerView.ViewHolder holder, int position);
    }

    /**
     * 上拉加载更多监听器
     */
    public interface OnLoadMoreListener {
        void onLoadMore();
    }

    /**
     * 下拉刷新监听器
     */
    public interface OnPullRefreshListener {
        void onPullRefresh();
    }


    public static class RefreshAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
        public static final int STATUS_EMPTY = 1;
        public static final int STATUS_NORMAL = 0;
        private static final int TYPE_FOOTER = 0xFF;
        private static final int TYPE_EMPTY = 0xEE;
        private RecyclerView.Adapter mInternalAdapter;
        private RecyclerView.ViewHolder footerViewHolder;
        private View footerView;
        private boolean isLoadMoreEnable;
        private EmptyViewHolder emptyViewHolder;
        private boolean isEmptyStatus;
        private int status;


        public RefreshAdapter(RecyclerView.Adapter mInternalAdapter, View footerView, View emptyView) {
            if (mInternalAdapter == null) {
                try {
                    throw new Exception("adapter is null");
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return;
            }
            this.mInternalAdapter = mInternalAdapter;
            if (footerView != null) {
                footerView.setVisibility(GONE);
                this.footerView = footerView;
                isLoadMoreEnable = true;
                footerViewHolder = new FooterViewHolder(footerView);
            }
            if (emptyView != null) {
                emptyViewHolder = new EmptyViewHolder(emptyView);
            }
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            if (viewType == TYPE_EMPTY) {
                return emptyViewHolder;
            }
            if (viewType == TYPE_FOOTER) {
                return footerViewHolder;
            }
            return mInternalAdapter.onCreateViewHolder(parent, viewType);


        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            if (position < mInternalAdapter.getItemCount()) {
                mInternalAdapter.onBindViewHolder(holder, position);
            }

        }

        @Override
        public int getItemCount() {
            int count = mInternalAdapter.getItemCount();
            if (count == 0 && emptyViewHolder != null) {
                isEmptyStatus = true;
                status = STATUS_EMPTY;
                return 1;
            }
            isEmptyStatus = false;
            if (isLoadMoreEnable) {
                count++;
            }
            return count;
        }


        @Override
        public int getItemViewType(int position) {
            if (isEmptyStatus && mInternalAdapter.getItemCount() == 0) {
                return TYPE_EMPTY;
            }
            if (isLoadMoreEnable && position + 1 == getItemCount()) {
                return TYPE_FOOTER;
            }
            return mInternalAdapter.getItemViewType(position);
        }


        public int getStatus() {
            return status;
        }

        public RefreshAdapter setStatus(int status) {
            this.status = status;
            return this;
        }

        public View getFooterView() {
            return footerView;
        }
    }

    static class FooterViewHolder extends RecyclerView.ViewHolder {
        public FooterViewHolder(View itemView) {
            super(itemView);
        }
    }


    static class EmptyViewHolder extends RecyclerView.ViewHolder {
        public EmptyViewHolder(View itemView) {
            super(itemView);
        }
    }


}
