package com.tomspter.translator.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.tomspter.translator.R;
import com.tomspter.translator.interfaze.OnRecyclerViewOnClickListener;
import com.tomspter.translator.model.NotebookMarkItem;

import java.util.ArrayList;


public class NotebookMarkItemAdapter extends RecyclerView.Adapter<NotebookMarkItemAdapter.ItemViewHolder> {

    private final Context context;
    private final LayoutInflater inflater;
    private ArrayList<NotebookMarkItem> list;
    private OnRecyclerViewOnClickListener mListener;

    public NotebookMarkItemAdapter(@NonNull Context context, ArrayList<NotebookMarkItem> list){
        this.context = context;
        this.inflater = LayoutInflater.from(context);
        this.list = list;
    }

    /**
     * 生成为每个Item inflater出一个View，返回一个ViewHolder，负责每个子项布局
     * @param parent
     * @param viewType
     * @return
     */
    @Override
    public ItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ItemViewHolder(inflater.inflate(R.layout.notebook_mark_item,parent,false),mListener);
    }

    /**
     * 为每个holder绑定数据
     * @param holder
     * @param position
     */
    @Override
    public void onBindViewHolder(final ItemViewHolder holder, final int position) {
        NotebookMarkItem item = list.get(position);
        holder.tvOutput.setText(item.getInput() + "\n" + item.getOutput());
    }

    /**
     * 返回条目个数
     * @return
     */
    @Override
    public int getItemCount() {
        return list.size();
    }

    public void setItemClickListener(OnRecyclerViewOnClickListener listener){
        this.mListener = listener;
    }

    /**
     * 静态内部类VH
     */
    public class ItemViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        TextView tvOutput;
        ImageView ivMarkStar;
        ImageView ivCopy;
        ImageView ivShare;

        OnRecyclerViewOnClickListener listener;

        public ItemViewHolder(View itemView, final OnRecyclerViewOnClickListener listener) {
            super(itemView);

            tvOutput = (TextView) itemView.findViewById(R.id.text_view_output);
            ivMarkStar = (ImageView) itemView.findViewById(R.id.image_view_mark_star);
            ivCopy = (ImageView) itemView.findViewById(R.id.image_view_copy);
            ivShare = (ImageView) itemView.findViewById(R.id.image_view_share);

            this.listener = listener;
            itemView.setOnClickListener(this);

            ivMarkStar.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    listener.OnSubViewClick(ivMarkStar,getLayoutPosition());
                }
            });

            ivCopy.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    listener.OnSubViewClick(ivCopy,getLayoutPosition());
                }
            });

            ivShare.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    listener.OnSubViewClick(ivShare,getLayoutPosition());
                }
            });

        }

        @Override
        public void onClick(View view) {
            if (listener != null){
                listener.OnItemClick(view,getLayoutPosition());
            }
        }
    }
}
//RecyclerView(A flexible view for providing a limited window into a large data set.)
//RecyclerView->ListView
//使用RecyclerView时候，必须指定一个适配器Adapter和一个布局管理器LayoutManager。
//实现方法
//① 创建Adapter：创建一个继承RecyclerView.Adapter<VH>的Adapter类（VH是ViewHolder的类名）
//② 创建ViewHolder：在Adapter中创建一个继承RecyclerView.ViewHolder的静态内部类，记为VH。ViewHolder的实现和ListView的ViewHolder实现几乎一样。
//③ 在Adapter中实现3个方法

