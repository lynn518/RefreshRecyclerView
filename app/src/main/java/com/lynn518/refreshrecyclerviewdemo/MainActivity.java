package com.lynn518.refreshrecyclerviewdemo;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.lynn518.refreshrecyclerview.RefreshRecyclerView;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    public static final String TAG = "MainActivity";
    private static final int DATA_POSITION_TOP = 0;
    private static final int DATA_POSITION_BOTTOM = 1;
    private RefreshRecyclerView rv;
    private List<String> data = new ArrayList<>();
    private int mCount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        rv = (RefreshRecyclerView) findViewById(R.id.rv);
        rv.setOnPullRefreshListener(new RefreshRecyclerView.OnPullRefreshListener() {
            @Override
            public void onPullRefresh() {
                Log.d(TAG, "onPullRefresh: ");
                rv.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        generateData(1, DATA_POSITION_TOP, "下拉刷新");
                        rv.notifyRefreshOkAndDataChange(1);
                    }
                }, 1000);
            }
        });
        rv.setAdapter(new RefreshRecyclerView.RefreshAdapter(new StringBaseAdapter(this, data), initFootView(), initEmptyView()));
        rv.setOnLoadMoreListener(new RefreshRecyclerView.OnLoadMoreListener() {
            @Override
            public void onLoadMore() {
                rv.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        generateData(1, DATA_POSITION_BOTTOM, "自动加载");
                        rv.notifyLoadMoreOkAndDataChange(1);
                    }
                }, 500);

            }
        });


        rv.setOnItemClickListener(new RefreshRecyclerView.OnItemClickListener() {
            @Override
            public void onItemClick(RecyclerView.ViewHolder holder, int position) {
                String s = data.get(position);
                Toast.makeText(MainActivity.this, "onItemClick: position " + position + " , data : " + s, Toast.LENGTH_SHORT).show();

            }
        });


        rv.setOnItemLongClickListener(new RefreshRecyclerView.OnItemLongClickListener() {
            @Override
            public void onItemLongClick(RecyclerView.ViewHolder holder, int position) {
                String s = data.get(position);
                Log.d(TAG, "onItemLongClick: position " + position + " , data : " + s);
            }
        });


    }

    /**
     * 初始化没有时候数据时的视图
     *
     * @return
     */
    private View initEmptyView() {
        LinearLayout linearLayout = new LinearLayout(this);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 1770);
        layoutParams.gravity = Gravity.CENTER;
        linearLayout.setLayoutParams(layoutParams);
        View view = LayoutInflater.from(this).inflate(R.layout.empty, null);
        linearLayout.addView(view, layoutParams);
        return linearLayout;
    }

    /**
     * 生成数据
     *
     * @param count
     * @param dataPosition
     * @param content
     */
    private void generateData(int count, int dataPosition, String content) {
        for (int i = 0; i < count; i++) {
            if (dataPosition == DATA_POSITION_TOP) {
                data.add(0, "测试数据 : " + content + mCount);
            } else {
                data.add("测试数据 : " + content + mCount);
            }
            mCount++;
        }
    }


    /**
     * 初始化上拉加载更多视图
     *
     * @return
     */
    private View initFootView() {
        LinearLayout linearLayout = new LinearLayout(this);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        layoutParams.gravity = Gravity.CENTER;
        linearLayout.setLayoutParams(layoutParams);
        View view = LayoutInflater.from(this).inflate(R.layout.footer, null);
        linearLayout.addView(view, layoutParams);
        return linearLayout;
    }


}
