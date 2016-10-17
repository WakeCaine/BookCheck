package com.example.shakecaine.bookcheck.main.amazon;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import android.util.Log;

/**
 * Created by Shakecaine on 2016-06-14.
 */
public class Parser {
    /** ---------------------  Search TAG --------------------- */
    private static final String KEY_ROOT="Items";
    private static final String KEY_REQUEST_ROOT="Request";
    private static final String KEY_REQUEST_CONTAINER="IsValid";
    private static final String KEY_ITEM="Item";
    private static final String KEY_ID="ASIN";
    private static final String KEY_ITEM_URL="DetailPageURL";
    private static final String KEY_IMAGE_ROOT="MediumImage";
    private static final String KEY_IMAGE_CONTAINER="URL";
    private static final String KEY_ITEM_ATTR_CONTAINER="ItemAttributes";
    private static final String KEY_ITEM_ATTR_TITLE="Title";

    private static final String VALUE_VALID_RESPONCE="True";

    //Tags
    //Items,Request,IsValid,Item,ASIN,DetailPageURL,MediumImage,URL,ItemAttributes,Title




    public NodeList getResponseNodeList(String service_url) {
        String searchResponce = this.getUrlContents(service_url);
        Log.i("url",""+ service_url);
        Log.i("responce",""+ searchResponce);
        org.w3c.dom.Document doc;
        NodeList items = null;
        if (searchResponce != null) {
            try {
                doc = this.getDomElement(searchResponce);
                items = doc.getElementsByTagName(KEY_ROOT);
                Element element=(Element)items.item(0);
                if(isResponceValid(element)){
                    items=doc.getElementsByTagName(KEY_ITEM);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return items;
    }

    public BookObject getSearchObject(NodeList list,int position){
        BookObject object=new BookObject();
        Element e=(Element)list.item(position);
        Log.d("BOOK", e.getTextContent());
        object.setUrl(this.getValue(e, KEY_ITEM_URL));
        Log.d("BOOK", "BOOK URL: " + object.getUrl());
        object.setBuyUrl(object.getUrl());
        object.setId(this.getValue(e, KEY_ID));
        Log.d("BOOK", "BOOK ID: " + object.getId());
        object.setImageUrl(this.getValue((Element) e.getElementsByTagName(KEY_IMAGE_ROOT).item(0),KEY_IMAGE_CONTAINER));
        object.setImageLargeUrl(this.getValue((Element) e.getElementsByTagName("LargeImage").item(0),KEY_IMAGE_CONTAINER));
        object.setTitle(this.getValue(e, KEY_ITEM_ATTR_TITLE));
        Log.d("BOOK", "BOOK TITLE: " + object.getTitle());

        if(e.getElementsByTagName("OfferSummary").item(0) != null) {
            Log.d("OFFERSUMMARY", e.getElementsByTagName("OfferSummary").item(0).getTextContent());
            if (((Element) e.getElementsByTagName("OfferSummary").item(0)) != null) {
                object.setPrice(this.getValue((Element) ((Element) e.getElementsByTagName("OfferSummary").item(0)).getElementsByTagName("LowestNewPrice").item(0), "FormattedPrice"));
            } else {
                object.setPrice("N/A");
            }
        } else {
            object.setPrice("N/A");
        }

        object.setAuthor(this.getValue((Element) e.getElementsByTagName("ItemAttributes").item(0),"Author"));
        String itemlink = this.getValue((Element) e.getElementsByTagName("CustomerReviews").item(0),"IFrameURL");
        Log.d("ITEMLINK", "ITEM: " + itemlink);
        object.setReviewUrl(this.getValue((Element) e.getElementsByTagName("CustomerReviews").item(0),"IFrameURL"));
        Log.d("REVIEWS", "REVIEWS URL: " + object.getReviewUrl());
        return object;
    }

    public boolean isResponceValid(Element element){
        NodeList nList=element.getElementsByTagName(KEY_REQUEST_ROOT);
        Element e=(Element)nList.item(0);
        if(getValue(e, KEY_REQUEST_CONTAINER).equals(VALUE_VALID_RESPONCE)){
            return true;
        }
        return false;
    }

    /** In app reused functions */

    private String getUrlContents(String theUrl) {
        StringBuilder content = new StringBuilder();
        try {
            URL url = new URL(theUrl);
            URLConnection urlConnection = url.openConnection();
            BufferedReader bufferedReader = new BufferedReader(
                    new InputStreamReader(urlConnection.getInputStream()), 8);
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                content.append(line + "");
            }
            bufferedReader.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return content.toString();
    }

    public org.w3c.dom.Document getDomElement(String xml) {
        org.w3c.dom.Document doc = null;
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        try {

            DocumentBuilder db = dbf.newDocumentBuilder();

            InputSource is = new InputSource();
            is.setCharacterStream(new StringReader(xml));
            doc = (org.w3c.dom.Document) db.parse(is);

        } catch (ParserConfigurationException e) {
            Log.e("Error: ", e.getMessage());
            return null;
        } catch (SAXException e) {
            Log.e("Error: ", e.getMessage());
            return null;
        } catch (IOException e) {
            Log.e("Error: ", e.getMessage());
            return null;
        }

        return doc;
    }

    public final String getElementValue(Node elem) {
        Node child;
        if (elem != null) {
            if (elem.hasChildNodes()) {
                for (child = elem.getFirstChild(); child != null; child = child
                        .getNextSibling()) {
                    if (child.getNodeType() == Node.TEXT_NODE
                            || (child.getNodeType() == Node.CDATA_SECTION_NODE)) {
                        return child.getNodeValue();
                    }
                }
            }
        }
        return "";
    }

    public String getValue(Element item, String str) {
        if(str != null && item != null) {
            NodeList n = item.getElementsByTagName(str);
            return this.getElementValue(n.item(0));
        } else {
            return null;
        }
    }

    public ArrayList<Map<String,String>> extractComments(String commentsIFrame){
        //Placeholder
        ArrayList<Map<String,String>> listComments = new ArrayList<>();
        Map<String,String> fuck = new HashMap<String, String>();
        fuck.put("Dupa dupa dupa dupaDupa dupa dupa dupaDupa dupa dupa dupaDupa dupa dupa dupa", "4/5");
        listComments.add(fuck);
        fuck = new HashMap<String, String>();
        fuck.put("Dupa dupa dupa dupa1Dupa dupa dupa dupa1Dupa dupa dupa dupa1Dupa dupa dupa dupa1", "3/5");
        listComments.add(fuck);
        ///////////////////////////////////////
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
                    commentMap.put(comment.text(), rating.get(counter).substring(0,1));
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
}
