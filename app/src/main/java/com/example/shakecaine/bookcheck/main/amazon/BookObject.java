package com.example.shakecaine.bookcheck.main.amazon;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import lombok.Getter;
import lombok.Setter;

/**
 * Created by Shakecaine on 2016-06-14.
 */
@Getter
@Setter
public class BookObject {
    String url;
    String id;
    String imageUrl;
    String imageLargeUrl;
    String buyUrl;
    String title;
    String author;
    String reviewUrl;
    String price;
    ArrayList<Map<String,String>> commentsList;

    public BookObject(){ }
}
