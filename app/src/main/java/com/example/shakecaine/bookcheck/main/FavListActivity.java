package com.example.shakecaine.bookcheck.main;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.example.shakecaine.bookcheck.R;
import com.example.shakecaine.bookcheck.main.adapter.FavBookAdapter;

public class FavListActivity extends AppCompatActivity {

    RecyclerView favListView;
    FavBookAdapter mFavAdapter;
    ProgressDialog mDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mDialog = new ProgressDialog(this);
        setContentView(R.layout.activity_fav_list);

        favListView = (RecyclerView) findViewById(R.id.fav_listview);
        mFavAdapter = new FavBookAdapter(this, getLayoutInflater());
        favListView.setAdapter(mFavAdapter);
        favListView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override protected void onPause(){
        mDialog.setMessage("Saving changes...");
        mDialog.show();
        mFavAdapter.updateSharedData(mDialog);
        super.onPause();
    }
}
