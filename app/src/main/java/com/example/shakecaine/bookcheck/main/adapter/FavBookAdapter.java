package com.example.shakecaine.bookcheck.main.adapter;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.shakecaine.bookcheck.R;
import com.example.shakecaine.bookcheck.main.DetailActivity;
import com.example.shakecaine.bookcheck.main.amazon.BookObject;
import com.example.shakecaine.bookcheck.main.async.UpdateBookList;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.squareup.picasso.Picasso;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Shakecaine on 2016-06-19.
 */
public class FavBookAdapter extends RecyclerView.Adapter<FavBookAdapter.MyViewHolder>
{
    private static final String PREFS = "prefs";
    Context mContext;
    LayoutInflater mInflater;
    List<BookObject> bookObjectList;
    int listCount;

    public FavBookAdapter(Context context, LayoutInflater inflater){
        this.mContext = context;
        this.mInflater = inflater;
        bookObjectList = new ArrayList<BookObject>();

        String JSONString = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getString("BookList", null);
        Type type = new TypeToken< List <  BookObject >>() {}.getType();
        bookObjectList = new Gson().fromJson(JSONString, type);
        listCount = bookObjectList!= null ? bookObjectList.size() : 0;
    }

    @Override public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View convertView  = mInflater.from(parent.getContext()).inflate(R.layout.row_book, parent, false);
        MyViewHolder holder = new MyViewHolder(convertView, new FavBookAdapter.MyViewHolder.IMyViewHolderClicks(){
            public void onMe(int position) {
                //TODO: GO TO DETAIL ACTIVITY
                Toast.makeText(mContext,"GO TO DETAIL OF FAV BOOK", Toast.LENGTH_SHORT).show();
                Intent detailIntent = new Intent(mContext, DetailActivity.class);
                //detailIntent.putExtra("coverID",coverID);
                detailIntent.putExtra("imageURL", bookObjectList.get(position).getImageLargeUrl());
                detailIntent.putExtra("author", bookObjectList.get(position).getAuthor().length() > 0 ? bookObjectList.get(position).getAuthor() : "");
                detailIntent.putExtra("textTitle", bookObjectList.get(position).getTitle().length() > 40 ? bookObjectList.get(position).getTitle().substring(0,39) : bookObjectList.get(position).getTitle());
                detailIntent.putExtra("reviewURL", bookObjectList.get(position).getReviewUrl());
                detailIntent.putExtra("productURL", bookObjectList.get(position).getBuyUrl());
                detailIntent.putExtra("price", bookObjectList.get(position).getPrice());
                mContext.startActivity(detailIntent);
            }

            @Override public void onStar(View view, int position) {
                ImageView im = (ImageView) view;
                im.setImageResource(android.R.drawable.star_off);

                bookObjectList.remove(position);
                updateData(bookObjectList);
            }
        });
        convertView.setTag(holder);

        return holder;
    }

    @Override public void onBindViewHolder(MyViewHolder holder, int position) {
        BookObject bookObject = this.bookObjectList.get(position);

        String imageURL = bookObject.getImageUrl();
        if(imageURL != null){
            Picasso.with(mContext).load(imageURL).placeholder(R.drawable.ic_books).into(holder.thumbnailImageView);
        } else {
            holder.thumbnailImageView.setImageResource(R.drawable.ic_books);
        }

        String bookTitle = "";
        String authorName = "";

        if(bookObject.getTitle() != null){
            bookTitle = bookObject.getTitle();
        }

        if(bookObject.getAuthor() != null){
            authorName = bookObject.getAuthor();
        }

        holder.titleTextView.setText(bookTitle);
        holder.starView.setImageResource(android.R.drawable.btn_star_big_on);
        //holder.authorTextView.setText(authorName);
    }

    @Override public int getItemCount() {
        if(bookObjectList != null)
            return bookObjectList.size();
        else
            return listCount;
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        public ImageView thumbnailImageView;
        public TextView titleTextView;
        public TextView authorTextView;
        public IMyViewHolderClicks mListener;
        public ImageView starView;

        public MyViewHolder(View itemView, IMyViewHolderClicks listener) {
            super(itemView);
            this.mListener = listener;
            thumbnailImageView = (ImageView) itemView.findViewById(R.id.img_thumbnail);
            titleTextView = (TextView) itemView.findViewById(R.id.text_title);
            starView = (ImageView) itemView.findViewById(R.id.starView);
            thumbnailImageView.setOnClickListener(this);
            titleTextView.setOnClickListener(this);
            starView.setOnClickListener(this);
        }

        @Override public void onClick(View view){
            if(!(view instanceof ImageView))
                mListener.onMe(this.getAdapterPosition());
            else {
                if(view != thumbnailImageView)
                    mListener.onStar(view, this.getAdapterPosition());
                else
                    mListener.onMe(this.getAdapterPosition());
            }

        }

        public static interface IMyViewHolderClicks {
            public void onMe(int position);
            public void onStar(View view, int position);
        }
    }

    public void updateData(List<BookObject> bookList) {
        // update the adapter's dataset
        this.bookObjectList = bookList;
        notifyDataSetChanged();
    }

    public void updateSharedData(ProgressDialog mDialog){
        new UpdateBookList(mContext,listCount, mDialog).execute(bookObjectList);
    }
}
