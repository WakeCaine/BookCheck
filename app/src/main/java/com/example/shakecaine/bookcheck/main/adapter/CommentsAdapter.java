package com.example.shakecaine.bookcheck.main.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.shakecaine.bookcheck.R;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Shakecaine on 2016-07-26.
 */
public class CommentsAdapter extends RecyclerView.Adapter<CommentsAdapter.MyViewHolder>{
    private static final String PREFS = "prefs";
    Context mContext;
    LayoutInflater mInflater;
    List<Map<String, String>> commentsList;
    int listCount;

    public CommentsAdapter(Context context, LayoutInflater inflater, List<Map<String, String>> commentsList){
        this.mContext = context;
        this.mInflater = inflater;
        this.commentsList = commentsList;
        listCount = this.commentsList!= null ? this.commentsList.size() : 0;
    }

    @Override public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View convertView  = mInflater.from(parent.getContext()).inflate(R.layout.row_comment, parent, false);
        MyViewHolder holder = new MyViewHolder(convertView);
        convertView.setTag(holder);
        return holder;
    }

    @Override public void onBindViewHolder(MyViewHolder holder, int position) {
        String comment = this.commentsList.get(position).entrySet().iterator().next().getKey();
        String rating = this.commentsList.get(position).entrySet().iterator().next().getValue();
        holder.commentText.setText(comment);
        holder.ratingText.setText(rating);
    }

    @Override public int getItemCount() {
        if(this.commentsList != null)
            return this.commentsList.size();
        else
            return listCount;
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView commentText;
        public TextView ratingText;

        public MyViewHolder(View itemView) {
            super(itemView);
            commentText = (TextView) itemView.findViewById(R.id.commentText);
            ratingText = (TextView) itemView.findViewById(R.id.ratingText);
        }
    }

    public void updateData(List<Map<String, String>> commentsList) {
        // update the adapter's dataset
        this.commentsList = commentsList;
        notifyDataSetChanged();
    }


}
