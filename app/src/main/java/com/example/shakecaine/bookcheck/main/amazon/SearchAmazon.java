package com.example.shakecaine.bookcheck.main.amazon;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.util.Log;


import com.example.shakecaine.OCR.services.TextExtractService;
import com.example.shakecaine.bookcheck.main.amazon.adapter.AmazonAdapterRecycled;

import org.w3c.dom.NodeList;
import org.w3c.dom.Text;

/**
 * Created by Shakecaine on 2016-06-14.
 */
public class SearchAmazon extends AsyncTask<String, Void, NodeList> {
    NodeList mNodeList = null;
    ProgressDialog dialog = null;
    AmazonAdapterRecycled mAmazonAdapter;
    TextExtractService textExtractService;
    String mainString;
    boolean order = true;
    boolean breakMe = false;
    boolean special = false;

    public SearchAmazon(){}

    protected void onPreExecute() {
        textExtractService = new TextExtractService();
    }

    @Override protected NodeList doInBackground(String... params) {
        String bookKeyWords = params[0];

        SignedRequestsHelper helper;
        try {
            helper = new SignedRequestsHelper();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

        String requestUrl = helper.sign(UrlParameterHandler.getInstance().buildMapForItemSearch(bookKeyWords));

        Log.d("BOOK", "Request URL= " + requestUrl);

        Parser p = new Parser();
        return p.getResponseNodeList(requestUrl);
    }

    protected void onPostExecute(NodeList nodeList) {
        breakMe = false;
        if(special == true) {
            if (textExtractService.getMainStringList().size() < 1 && order == true) {
                if (nodeList.getLength() < 1) {
                    breakMe = true;
                    new SearchAmazon().searchForBook(textExtractService.getMainWords(true), dialog, mAmazonAdapter, textExtractService, special);
                }
            } else if (textExtractService.getMainStringList().size() < 1 && textExtractService.getSubStringList().size() > 0 && order == true) {
                textExtractService.extractWords(mainString);
                breakMe = true;
                order = false;
                new SearchAmazon().searchForBook(textExtractService.getMainWords(false), dialog, mAmazonAdapter, textExtractService, special);
            } /*else if(textExtractService.getMainStringList().size() > 0 && order == false){
                if (nodeList.getLength() < 1) {
                    breakMe = true;
                    new SearchAmazon().searchForBook(textExtractService.getMainWords(true), dialog, mAmazonAdapter, textExtractService);
                }
            }*/
        }
        if(breakMe == false) {
            textExtractService.extractWords("");
            textExtractService.extractSubWords("");
            dialog.dismiss();
            this.mNodeList = nodeList;
            mAmazonAdapter.updateData(nodeList);
        }
    }

    public void searchForBook(String bookKeyWords, ProgressDialog mDialog, AmazonAdapterRecycled amazonAdapter, TextExtractService textExtractService, boolean special){
        this.dialog = mDialog;
        this.mAmazonAdapter = amazonAdapter;
        this.textExtractService = textExtractService;
        this.mainString = bookKeyWords;
        this.special = special;
        this.execute(bookKeyWords);
    }
}
