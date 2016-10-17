package com.example.shakecaine.bookcheck.main.amazon;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Shakecaine on 2016-06-14.
 */
public class UrlParameterHandler {
    public static UrlParameterHandler paramHandler;
    private UrlParameterHandler() {}


    public static synchronized UrlParameterHandler getInstance(){
        if(paramHandler==null){
            paramHandler=new UrlParameterHandler();
            return paramHandler;
        }
        return paramHandler;
    }

    public  Map<String,String> buildMapForItemSearch(String searchString){
        Map<String, String> params = new HashMap<String, String>();
        params.put("Service", "AWSECommerceService");
        params.put("Operation", "ItemSearch");
        params.put("SearchIndex", "Books");
        params.put("Keywords", searchString);
        params.put("ResponseGroup", "Images,ItemAttributes,ItemIds,Medium,Offers,Reviews,Small");
        params.put("Sort", "price");
        params.put("RelationshipType", "AuthorityTitle");
        return params;
    }
}
