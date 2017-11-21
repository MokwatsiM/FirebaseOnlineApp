package com.example.mlab.androidpresencesystem;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

/**
 * Created by mLab on 2017/11/08.
 */

public class ListOnlineViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

    public TextView username;
    ItemClickListenener itemClickListenener;

    public ListOnlineViewHolder(View itemView) {
        super(itemView);

        username = itemView.findViewById(R.id.txtEmail);
        itemView.setOnClickListener(this);

    }

    public void setItemClickListenener(ItemClickListenener itemClickListenener) {
        this.itemClickListenener = itemClickListenener;
    }

    @Override
    public void onClick(View v) {
        itemClickListenener.onClick(v, getAdapterPosition());
    }
}
