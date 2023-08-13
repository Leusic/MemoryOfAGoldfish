package trotter.max.memoryofagoldfish;

import android.content.Context;
import android.content.ContextWrapper;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.util.Log;
import android.widget.ImageView;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class VolleyItemListRetriever implements ItemsRepository.VolleyJSONObjectResponse, ItemsRepository.VolleyItemImageResponse {
    private ArrayList<String> mUrls;
    private int currentIndex;
    private MutableLiveData<ArrayList<Item>> mItemsData;
    private ArrayList<Item> mItems;
    private RequestQueue mQueue;
    private boolean initialised = false;

    public VolleyItemListRetriever(ArrayList<String> pUrls, Context pContext){
        currentIndex = 0;
        mUrls = pUrls;
        mQueue = Volley.newRequestQueue(pContext);
        mItems = new ArrayList<Item>();
    }

    public LiveData<ArrayList<Item>> getItems() {
        mItemsData = new MutableLiveData<ArrayList<Item>>();
        for(int i = 0; i < mUrls.size(); i++) {
            CustomJSONObjectRequest request = new CustomJSONObjectRequest(
                    Request.Method.GET, mUrls.get(i), null, "ItemListJSON", this);
            mQueue.add(request.getJsonObjectRequest());
            if(!initialised){
                mItems.add(new Item("PLACEHOLDER", new ArrayList<String>(), "PLACEHOLDER_URL"));
            }
        }
        initialised = true;
        return mItemsData;
    }

    @Override
    public void onResponse(JSONObject pObject, String pTag) {
        Log.i("VolleyItemListRetriever", pTag);
        ArrayList<Item> parsedResponse = parseJSONResponse(pObject);
        if(mItems.get(currentIndex).getImages().size() <= 40){
            mItems.set(currentIndex, parsedResponse.get(0));
        }
        mItemsData.setValue(mItems);
        currentIndex++;
    }

    public void onResponse(Bitmap pImage, Item pItem) {
        Log.i("VolleyItemListRetriever", "Image retrieved for:" + pItem.getName());
        if(pItem.getTileBackImage() != null){
            pItem.addImage(pImage);
        }
        if(pItem.getTileBackImage() == null){
            pItem.setTileBackImage(pImage);
        }
        mItemsData.setValue(mItems);
    }

    @Override
    public void onError(VolleyError pError, String pTag) {
        Log.e("VolleyItemListRetriever", pTag);
    }

    private ArrayList<Item> parseJSONResponse(JSONObject pResponse) {
        ArrayList<Item> items = new ArrayList<>();
        try {
            Item item = parseJSONItem(pResponse);
            items.add(item);
        }
        catch(JSONException e){
            e.printStackTrace();
        }
        return items;
    }

    private Item parseJSONItem(JSONObject pItemObject) throws JSONException {
        String Name = pItemObject.getString("Name");
        JSONArray JSONimageUrls = pItemObject.getJSONArray("PictureSet");
        ArrayList<String> imageUrls = new ArrayList<String>();
        if(currentIndex >= mUrls.size()){
            currentIndex = 0;
        }
        String urlPrefix = mUrls.get(currentIndex).split("index.json")[0];
        for(int i = 0; i < JSONimageUrls.length(); i++){
            imageUrls.add(urlPrefix + JSONimageUrls.getString(i));
        }
        String tileBackUrl = urlPrefix + pItemObject.getString("TileBack");

        Item item = new Item(Name, imageUrls, tileBackUrl);
        CustomItemImageRequest tileBackImageRequest = new CustomItemImageRequest(
                item.getTileBackUrl(),
                item,
                this);
        mQueue.add(tileBackImageRequest.getImageRequest());
        for(int i = 0; i < JSONimageUrls.length(); i++) {
            CustomItemImageRequest itemImageRequest = new CustomItemImageRequest(
                    item.getImageUrls().get(i),
                    item,
                    this);
            mQueue.add(itemImageRequest.getImageRequest());
        }
        return item;
        }

    public class CustomJSONObjectRequest implements Response.Listener<JSONObject>, Response.ErrorListener {

        private ItemsRepository.VolleyJSONObjectResponse mVolleyJSONObjectResponse;
        private String mTag;
        private JsonObjectRequest mJsonObjectRequest;

        public JsonObjectRequest getJsonObjectRequest() {
            return mJsonObjectRequest;
        }

        public CustomJSONObjectRequest(int pMethod, String pUrl, JSONObject pJsonObject, String pTag, ItemsRepository.VolleyJSONObjectResponse pVolleyJSONObjectResponse) {
            this.mVolleyJSONObjectResponse = pVolleyJSONObjectResponse;
            this.mTag = pTag;
            mJsonObjectRequest = new JsonObjectRequest(pMethod, pUrl, pJsonObject, this, this);
        }

        @Override
        public void onResponse(JSONObject pResponse) {
            mVolleyJSONObjectResponse.onResponse(pResponse, mTag);
        }

        @Override
        public void onErrorResponse(VolleyError pError) {
            mVolleyJSONObjectResponse.onError(pError, mTag);
        }

    }

    public class CustomItemImageRequest implements Response.Listener<Bitmap>, Response.ErrorListener {
        private ItemsRepository.VolleyItemImageResponse mVolleyItemImageResponse;
        private Item mItem;
        private ImageRequest mImageRequest;

        public CustomItemImageRequest(String pUrl, Item pItem, ItemsRepository.VolleyItemImageResponse pVolleyItemImageResponse){
            mVolleyItemImageResponse = pVolleyItemImageResponse;
            mItem = pItem;
            mImageRequest = new ImageRequest(pUrl, this, 0, 0, ImageView.ScaleType.CENTER_CROP, Bitmap.Config.RGB_565, this);
        }

        public ImageRequest getImageRequest() {
            return mImageRequest;
        }

        @Override
        public void onResponse(Bitmap pResponse) {
            mVolleyItemImageResponse.onResponse(pResponse, mItem);
        }

        @Override
        public void onErrorResponse(VolleyError pError) {
            mVolleyItemImageResponse.onError(pError, mItem.getName());
        }
    }
}
