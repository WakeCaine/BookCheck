package com.example.shakecaine.bookcheck.main.async;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;

import com.example.shakecaine.bookcheck.main.amazon.BookObject;
import com.google.gson.Gson;

import java.util.List;

/**
 * Created by Shakecaine on 2016-06-21.
 */
public class UpdateBookList extends AsyncTask< List<BookObject>, Void, String> {
    ProgressDialog dialog;
    Context context;
    int listSize;

    public UpdateBookList(Context context, int listSize, ProgressDialog dialog){
        this.listSize = listSize;
        this.context = context;
        this.dialog = dialog;
    }

    @Override protected String doInBackground(List<BookObject>... params) {
        List<BookObject> list = (List<BookObject>) params[0];
        if(list == null)
            return null;

        if(listSize != list.size()) {
            SharedPreferences mPrefs = context.getSharedPreferences("prefs", Context.MODE_PRIVATE);
            SharedPreferences.Editor prefsEditor = mPrefs.edit();
            Gson gson = new Gson();
            String json = gson.toJson(list);
            prefsEditor.putString("BookList", json);
            prefsEditor.commit();
        }
        return null;
    }

    @Override protected void onPostExecute(String lol){
        this.dialog.dismiss();
    }
}
