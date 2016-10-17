package com.example.shakecaine.bookcheck.main.amazon.adapter;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
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
import com.example.shakecaine.bookcheck.main.amazon.Parser;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.squareup.picasso.Picasso;

import org.w3c.dom.NodeList;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import lombok.Getter;

/**
 * Created by Shakecaine on 2016-06-19.
 */
public class AmazonAdapterRecycled extends RecyclerView.Adapter<AmazonAdapterRecycled.MyViewHolder>
{
    private static final String PREFS = "prefs";
    Context mContext;
    LayoutInflater mInflater;
    NodeList mNodeList;
    Parser parser;
    @Getter public List<BookObject> bookObjectList;
    int bookListCount = 0;

    public AmazonAdapterRecycled(Context context, LayoutInflater inflater){
        this.mContext = context;
        this.mInflater = inflater;
        this.parser = new Parser();
        mNodeList = null;

        String JSONString = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getString("BookList", null);
        Type type = new TypeToken< List <  BookObject >>() {}.getType();
        bookObjectList = new Gson().fromJson(JSONString, type);
        bookListCount = bookObjectList!= null ? bookObjectList.size() : 0;
    }

    @Override public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {


        View convertView  = mInflater.from(parent.getContext()).inflate(R.layout.row_book, parent, false);
        MyViewHolder holder = new MyViewHolder(convertView, bookObjectList ,new MyViewHolder.IMyViewHolderClicks(){
            public void onMe(int position) {
                //JSONObject jsonObject = (JSONObject) mJSONAdapter.getItem(position);
                BookObject bookObject = (BookObject) parser.getSearchObject(mNodeList,position);
                //String coverID = jsonObject.optString("cover_i","");
                String imageUrl = bookObject.getImageLargeUrl() != null ? bookObject.getImageLargeUrl() : "";

                Intent detailIntent = new Intent(mContext, DetailActivity.class);
                //detailIntent.putExtra("coverID",coverID);
                detailIntent.putExtra("imageURL", imageUrl);
                detailIntent.putExtra("author", bookObject.getAuthor().length() > 0 ? bookObject.getAuthor() : "");
                detailIntent.putExtra("textTitle", bookObject.getTitle().length() > 50 ? bookObject.getTitle().substring(0,49) : bookObject.getTitle());
                detailIntent.putExtra("reviewURL", bookObject.getReviewUrl());
                detailIntent.putExtra("productURL", bookObject.getBuyUrl());
                detailIntent.putExtra("price", bookObject.getPrice());

                mContext.startActivity(detailIntent);
            }

            @Override public void onStar(View view, List<BookObject> bookObjectList, int position) {
                ImageView im = (ImageView) view;
                BookObject bookObject = (BookObject) parser.getSearchObject(mNodeList,position);
                String backgroundImageName = String.valueOf(im.getTag());
                if (backgroundImageName.equals("star_off"))  // here "bg" is the tag that you set previously
                {
                    bookObjectList.add(bookObject);
                    im.setImageResource(android.R.drawable.btn_star_big_on);
                    im.setTag("star_on");
                }
                else
                {
                    for(BookObject book : bookObjectList){
                        if (bookObject.getTitle().equals(book.getTitle()) && bookObject.getAuthor().equals(book.getAuthor())) {
                            bookObjectList.remove(book);
                            break;
                        }
                    }
                    im.setTag("star_off");
                    im.setImageResource(android.R.drawable.star_off);
                }
            }
        });
        convertView.setTag(holder);

        return holder;
    }

    @Override public void onBindViewHolder(MyViewHolder holder, int position) {
        BookObject bookObject = (BookObject) parser.getSearchObject(mNodeList,position);
        String imageURL = bookObject.getImageUrl();
        if(imageURL != null){
            Picasso.with(mContext).load(imageURL).placeholder(R.drawable.ic_books).into(holder.thumbnailImageView);
        } else {
            holder.thumbnailImageView.setImageResource(R.drawable.ic_books);
        }

        String bookTitle = "";

        if(bookObject.getTitle() != null){
            bookTitle = bookObject.getTitle();
        }

        holder.titleTextView.setText(bookTitle);
        for(BookObject book : bookObjectList) {
            if(bookObject.getTitle().equals(book.getTitle()) && bookObject.getAuthor().equals(book.getAuthor())) {
                holder.starView.setTag("star_on");
                holder.starView.setImageResource(android.R.drawable.btn_star_big_on);
                break;
            } else {
                holder.starView.setTag("star_off");
                holder.starView.setImageResource(android.R.drawable.star_off);
            }
        }
        //holder.authorTextView.setText(authorName);
    }

    @Override public int getItemCount() {
        if(mNodeList == null){
            return 0;
        } else {
            return mNodeList.getLength();
        }
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        public ImageView thumbnailImageView;
        public TextView titleTextView;
        public TextView authorTextView;
        public IMyViewHolderClicks mListener;
        public ImageView starView;
        public List<BookObject> bookObjectList;
        public TextView reviewLink;

        public MyViewHolder(View itemView, List<BookObject> bookObjectList, IMyViewHolderClicks listener) {
            super(itemView);
            this.bookObjectList = bookObjectList;
            this.mListener = listener;
            thumbnailImageView = (ImageView) itemView.findViewById(R.id.img_thumbnail);
            titleTextView = (TextView) itemView.findViewById(R.id.text_title);
            starView = (ImageView) itemView.findViewById(R.id.starView);
            authorTextView = (TextView) itemView.findViewById(R.id.text_author);
            thumbnailImageView.setOnClickListener(this);
            titleTextView.setOnClickListener(this);
            starView.setOnClickListener(this);
            starView.setTag("star_off");
            //authorTextView.setOnClickListener(this);
        }

        @Override public void onClick(View view){
            if(!(view instanceof ImageView))
                mListener.onMe(this.getAdapterPosition());
            else {
                if(view != thumbnailImageView)
                    mListener.onStar(view, this.bookObjectList , this.getAdapterPosition());
                else
                    mListener.onMe(this.getAdapterPosition());
            }

        }

        public static interface IMyViewHolderClicks {
            public void onMe(int position);
            public void onStar(View view, List<BookObject> bookObjectList, int position);
        }
    }

    public void updateData(NodeList nodeList) {
        // update the adapter's dataset
        this.mNodeList = nodeList;
        notifyDataSetChanged();
    }

    public void updateSharedData(){
        if(bookObjectList != null)
            if(bookListCount != bookObjectList.size()) {
                SharedPreferences mPrefs = mContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
                SharedPreferences.Editor prefsEditor = mPrefs.edit();
                Gson gson = new Gson();
                String json = gson.toJson(this.bookObjectList);
                prefsEditor.putString("BookList", json);
                prefsEditor.commit();
            }
    }
}
