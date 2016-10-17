package com.example.shakecaine.bookcheck.main;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.shakecaine.OCR.services.CameraService;
import com.example.shakecaine.OCR.services.PermissionService;
import com.example.shakecaine.bookcheck.R;
import com.example.shakecaine.bookcheck.main.adapter.CommentsAdapter;
import com.example.shakecaine.bookcheck.main.amazon.adapter.AmazonAdapterRecycled;
import com.googlecode.tesseract.android.TessBaseAPI;
import com.squareup.picasso.Picasso;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.opencv.core.Mat;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.photo.Photo;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DetailActivity extends AppCompatActivity implements View.OnClickListener {
    private static final String IMAGE_URL_BASE = "http://covers.openlibrary.org/b/id/";
    String mImageURL;
    public TextView commentText;
    public TextView ratingText;
    public TextView titleText;
    public TextView authorText;
    public TextView priceText;
    ImageView imageView;
    Button buyButton;

    RecyclerView mainListView;
    CommentsAdapter commentsAdapter;
    ProgressDialog mDialog;

    String productURL;

    List<Map<String,String>> commentsList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mDialog = new ProgressDialog(this);
        mDialog.setMessage("Extracting comments");
        mDialog.setCancelable(false);
        setContentView(R.layout.activity_detail);

        Bundle extras = this.getIntent().getExtras();

        String reviewURL = extras.getString("reviewURL");
        ExtractClass extract = new ExtractClass();
        commentsAdapter = new CommentsAdapter(this,getLayoutInflater(), null);
        extract.extractComments(reviewURL, mDialog, commentsAdapter );
        imageView = (ImageView) findViewById(R.id.img_cover);
        mainListView = (RecyclerView) findViewById(R.id.commentList);

        String imageURL = extras.getString("imageURL");
        productURL = extras.getString("productURL");
        String textTitle = extras.getString("textTitle");
        String textAuthor = extras.getString("author");
        String price = extras.getString("price");
        price = (price == null || price.equals("")) ? "N/A" : price;
        if (imageURL.length() > 0){
            mImageURL = imageURL;
            Picasso.with(this).load(mImageURL).placeholder(R.drawable.img_books_loading).into(imageView);
        }

        titleText = (TextView) findViewById(R.id.textTitle);
        titleText.setText(textTitle);
        priceText = (TextView) findViewById(R.id.priceText);
        priceText.setText(price);

        authorText = (TextView) findViewById(R.id.authorText);
        authorText.setText(textAuthor);
        mainListView.setAdapter(commentsAdapter);
        mainListView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        buyButton = (Button) findViewById(R.id.buy_button);
        buyButton.setOnClickListener(this);
        mDialog.show();
    }

    @Override public void onClick(View v) {
        if(v.equals(buyButton)){
            Intent browse = new Intent( Intent.ACTION_VIEW , Uri.parse( productURL ) );
            startActivity( browse );
        }
    }

    class ExtractClass extends AsyncTask<String, Void, List<Map<String,String>>> {
        ProgressDialog dialog = null;
        CommentsAdapter adapter;
        Bitmap bitmapHere;
        String textHere;

        @Override protected List<Map<String,String>> doInBackground(String... params) {
            String commentsIFrame = params[0];
            ArrayList<Map<String,String>> listComments1 = new ArrayList<>();
            if(commentsIFrame != null && !commentsIFrame.equals("")){
                try {
                    Document doc  = Jsoup.connect(commentsIFrame).get();
                    Log.d(this.toString(), "WHOLE DOCUMENT: " + doc.text());
                    Elements comments = doc.select("div.reviewText");
                    Log.d(this.toString(), "COMMENTS: " + comments.text());
                    Elements ratings = doc.select("img");
                    Log.d(this.toString(), "RATINGS: " + ratings.text());
                    List<String> rating = new ArrayList<>();
                    for(org.jsoup.nodes.Element ratingElement : ratings){
                        if(ratingElement.toString().contains("title")){
                            rating.add(ratingElement.attr("title"));
                        }
                    }
                    int counter = 0;
                    for(org.jsoup.nodes.Element comment : comments){
                        Map<String,String> commentMap = new HashMap<String, String>();
                        commentMap.put(comment.text(), rating.get(counter).substring(0,1) + "/5");
                        listComments1.add(commentMap);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }

                return listComments1;
            } else {
                return null;
            }
        }

        public void extractComments(String reviewURL, ProgressDialog mDialog, CommentsAdapter adapter  ){
            this.dialog = mDialog;
            this.adapter = adapter;
            this.execute(reviewURL);
        }

        protected void onPostExecute(List<Map<String,String>> list){
            adapter.updateData(list);
            dialog.dismiss();
        }
    }
}
