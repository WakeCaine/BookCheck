package com.example.shakecaine.bookcheck.main.amazon.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.shakecaine.bookcheck.R;
import com.example.shakecaine.bookcheck.main.amazon.BookObject;
import com.example.shakecaine.bookcheck.main.amazon.Parser;
import com.squareup.picasso.Picasso;

import org.w3c.dom.NodeList;

/**
 * Created by Shakecaine on 2016-06-15.
 */
public class AmazonAdapter extends BaseAdapter {

    Context mContext;
    LayoutInflater mInflater;
    NodeList mNodeList;
    Parser parser;

    public AmazonAdapter(Context context, LayoutInflater inflater){
        this.mContext = context;
        this.mInflater = inflater;
        this.parser = new Parser();
        mNodeList = null;
    }

    @Override public int getCount() {
        if(mNodeList == null){
            return 0;
        } else {
            return mNodeList.getLength();
        }
    }

    @Override public Object getItem(int position) {
        return parser.getSearchObject(mNodeList,position);
    }

    @Override public long getItemId(int position) {
        return position;
    }

    @Override public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;

        if(convertView == null){
            convertView = mInflater.inflate(R.layout.row_book, null);
            holder = new ViewHolder();
            holder.thumbnailImageView = (ImageView) convertView.findViewById(R.id.img_thumbnail);
            holder.titleTextView = (TextView) convertView.findViewById(R.id.text_title);
            holder.authorTextView = (TextView) convertView.findViewById(R.id.text_author);

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        BookObject bookObject = (BookObject) getItem(position);
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
        holder.authorTextView.setText(authorName);

        return convertView;
    }

    public void updateData(NodeList nodeList) {
        // update the adapter's dataset
        this.mNodeList = nodeList;
        notifyDataSetChanged();
    }

    private static class ViewHolder{
        public ImageView thumbnailImageView;
        public TextView titleTextView;
        public TextView authorTextView;
    }
}
