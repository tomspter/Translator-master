package com.tomspter.translator.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.tomspter.translator.R;
import com.tomspter.translator.model.BingModel;

import java.util.ArrayList;

public class SampleAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    public final Context context;
    public final LayoutInflater inflater;
    public ArrayList<BingModel.Sample> samples;

    public SampleAdapter (@NonNull Context context, ArrayList<BingModel.Sample> samples) {
        this.context = context;
        this.inflater = LayoutInflater.from(context);
        this.samples = samples;
    }

    /**
     * 生成为每个Item inflater出一个View，返回一个ViewHolder，负责每个子项布局
     * @param parent
     * @param viewType
     * @return
     */
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new SampleViewHolder(inflater.inflate(R.layout.sample_item, parent, false));
    }

    /**
     * 为每个holder绑定数据
     * @param holder
     * @param position
     */
    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        String s = samples.get(position).getEng() + "\n" + samples.get(position).getChn();
        ((SampleViewHolder)holder).textView.setText(s);
    }

    /**
     * 返回条目个数
     * @return
     */
    @Override
    public int getItemCount() {
        return samples.size();
    }

    /**
     * 静态内部类VH
     */
    public class SampleViewHolder extends RecyclerView.ViewHolder {

        TextView textView;

        public SampleViewHolder(View itemView) {
            super(itemView);
            textView = (TextView) itemView.findViewById(R.id.textView);
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
