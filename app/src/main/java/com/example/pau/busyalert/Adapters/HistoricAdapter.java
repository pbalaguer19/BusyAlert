package com.example.pau.busyalert.Adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.pau.busyalert.JavaClasses.HerokuLog;
import com.example.pau.busyalert.R;

import java.util.List;

/**
 * Created by pau on 26/5/17.
 */

public class HistoricAdapter extends RecyclerView.Adapter<HistoricAdapter.HistoricViewHolder> {

    private List<HerokuLog> historics;
    private int rowLayout;
    private Context context;


    public static class HistoricViewHolder extends RecyclerView.ViewHolder {
        LinearLayout historicsLayout;
        TextView action;
        TextView data;


        public HistoricViewHolder(View v) {
            super(v);
            historicsLayout = (LinearLayout) v.findViewById(R.id.historic_layout);
            action = (TextView) v.findViewById(R.id.action);
            data = (TextView) v.findViewById(R.id.data);
        }
    }

    public HistoricAdapter(List<HerokuLog> historics, int rowLayout, Context context) {
        this.historics = historics;
        this.rowLayout = rowLayout;
        this.context = context;
    }

    @Override
    public HistoricAdapter.HistoricViewHolder onCreateViewHolder(ViewGroup parent,
                                                            int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(rowLayout, parent, false);
        return new HistoricViewHolder(view);
    }


    @Override
    public void onBindViewHolder(HistoricAdapter.HistoricViewHolder holder, final int position) {
        holder.action.setText(historics.get(position).getAction());
        holder.data.setText(historics.get(position).getExtraData());
    }

    @Override
    public int getItemCount() {
        return historics.size();
    }
}
