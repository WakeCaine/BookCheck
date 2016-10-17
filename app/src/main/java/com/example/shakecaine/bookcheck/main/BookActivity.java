package com.example.shakecaine.bookcheck.main;

import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.example.shakecaine.OCR.services.TextExtractService;
import com.example.shakecaine.bookcheck.R;
import com.example.shakecaine.bookcheck.main.amazon.SearchAmazon;
import com.example.shakecaine.bookcheck.main.amazon.adapter.AmazonAdapterRecycled;
import java.util.ArrayList;

public class BookActivity extends AppCompatActivity implements View.OnClickListener {

    SharedPreferences mSharedPreferences;
    TextView mainTextView;
    Button mainButton;
    EditText mainEditText;
    //ListView mainListView;
    RecyclerView mainListView;
    //AmazonAdapter mAmazonAdapter;
    AmazonAdapterRecycled mAmazonAdapter;
    ArrayList mNameList = new ArrayList<>();
    ProgressDialog mDialog;
    TextExtractService textExtractService;

    private void queryBooksAmazon(String searchString){
        mDialog.show();
        new SearchAmazon().searchForBook(searchString, mDialog, mAmazonAdapter, textExtractService, false);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_test);
        textExtractService = new TextExtractService();
        // 1. Access the TextView defined in layout XML
        // and then set its text
        mainTextView = (TextView) findViewById(R.id.main_textview);
        mainButton = (Button) findViewById(R.id.main_button);
        mainEditText = (EditText) findViewById(R.id.main_edittext);
        mainListView = (RecyclerView) findViewById(R.id.main_listview);
        mainButton.setOnClickListener(this);
        mainButton.setOnClickListener(this);

        mAmazonAdapter = new AmazonAdapterRecycled(this, getLayoutInflater());
        mainListView.setAdapter(mAmazonAdapter);
        mainListView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override public void onClick(View v) {
        mDialog = new ProgressDialog(this);
        mDialog.setMessage("Searching for Book");
        mDialog.setCancelable(false);
        queryBooksAmazon(mainEditText.getText().toString());
        mainTextView.setText("Searching for " + mainEditText.getText().toString() + "!");
        mNameList.add(mainEditText.getText().toString());
    }

    @Override public void onPause(){
        mAmazonAdapter.updateSharedData();
        super.onPause();
    }
}
