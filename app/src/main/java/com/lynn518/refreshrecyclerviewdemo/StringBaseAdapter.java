package com.lynn518.refreshrecyclerviewdemo;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

/**
 * Created by ZL on 16/5/12.
 */
public class StringBaseAdapter extends RecyclerView.Adapter<StringBaseAdapter.ItemViewHolder> {
    public static final String TAG = StringBaseAdapter.class.getSimpleName();
    private Context context;
    private List<String> data;

    public StringBaseAdapter(Context context, List<String> data) {
        this.context = context;
        this.data = data;
    }

    @Override
    public ItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_rv, parent, false);
        return new ItemViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ItemViewHolder holder, int position) {
        holder.tv.setText(data.get(position));

    }

    public int getItemCount() {
        return data.size();
    }


    static class ItemViewHolder extends RecyclerView.ViewHolder {

        TextView tv;

        public ItemViewHolder(View itemView) {
            super(itemView);
            tv = (TextView) itemView.findViewById(R.id.tv);

        }


    }


}
