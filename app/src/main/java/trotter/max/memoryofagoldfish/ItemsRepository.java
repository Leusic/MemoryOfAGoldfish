package trotter.max.memoryofagoldfish;

import android.content.Context;
import android.content.ContextWrapper;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.util.Log;
import android.widget.ImageView;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;

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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;

public class ItemsRepository {

    private static ItemsRepository sItemsRepository;
    private Context mApplicationContext;

    private MediatorLiveData<ArrayList<Item>> mItems;
    private LiveData<Item> mSelectedItem;

    private VolleyItemListRetriever mRemoteItemList;
    private int index = 0;

    private ItemsRepository(Context pApplicationContext) {
        this.mApplicationContext = pApplicationContext;
        mItems = new MediatorLiveData<>();
        ArrayList<String> urls = new ArrayList<String>();
        urls.add("https://goparker.com/600096/memory/goldfish/index.json");
        urls.add("https://goparker.com/600096/memory/elephant/index.json");
        mRemoteItemList = new VolleyItemListRetriever(urls, pApplicationContext);
    }

    public static ItemsRepository getInstance(Context pApplicationContext) {
        if (sItemsRepository == null ){
            sItemsRepository = new ItemsRepository(pApplicationContext);
        }
        return sItemsRepository;
    }

    public interface VolleyJSONObjectResponse {
        void onResponse(JSONObject pObject, String pTag);

        void onError(VolleyError pError, String pTag);
    }

    public interface VolleyItemImageResponse {
        void onResponse(Bitmap pImage, Item pItem);

        void onError(VolleyError error, String pTag);
    }

    public LiveData<Item> getItem(int pItemIndex) {
        LiveData<Item> transformedItem = Transformations.switchMap(mItems, items -> {
            MutableLiveData<Item> itemData = new MutableLiveData<Item>();
            Item item = items.get(pItemIndex);
            itemData.setValue(item);
            //checks if the image is available in local files and if not runs loadImage to retrieve it from the webpage.
            for(int i = 0; i < item.getImageUrls().size(); i++){
                if(!loadImageLocally(Uri.parse(item.getImageUrls().get(i)).getLastPathSegment(), itemData)) {
                    loadImage(item.getImageUrls().get(i), itemData);
                }
            }
            return itemData;
        });
        mSelectedItem = transformedItem;
        return mSelectedItem;
    }

    public LiveData<ArrayList<Item>> getItems(){
        LiveData<ArrayList<Item>> remoteData = mRemoteItemList.getItems();
        mItems.addSource(remoteData, value -> mItems.setValue(value));
        LiveData<ArrayList<Item>> localData = loadIndexLocally("index.json");
        mItems.addSource(localData, value -> mItems.setValue(value));
        return mItems;
    }

    public void saveIndexLocally(JSONObject pIndexObject, String pFilename) {
        ContextWrapper contextWrapper = new ContextWrapper(mApplicationContext);
        OutputStreamWriter outputStreamWriter = null;
        try {
            outputStreamWriter = new OutputStreamWriter(
                    contextWrapper.openFileOutput(pFilename, Context.MODE_PRIVATE));
            outputStreamWriter.write(pIndexObject.toString());
            outputStreamWriter.flush();
            outputStreamWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private LiveData<ArrayList<Item>> loadIndexLocally(String pFilename) {
        JSONObject indexObject = null;
        MutableLiveData<ArrayList<Item>> mutableItems = new MutableLiveData<ArrayList<Item>>();
        try{
            InputStream inputStream = mApplicationContext.openFileInput(pFilename);

            if (inputStream != null) {
                InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                String receiveString = "";
                StringBuilder stringBuilder = new StringBuilder();

                while((receiveString = bufferedReader.readLine()) != null ) {
                    stringBuilder.append(receiveString);
                }

                inputStream.close();
                String builtString = stringBuilder.toString();
                indexObject = new JSONObject(builtString);
            }
        }
        catch(FileNotFoundException e){
            Log.e("JSONLoading", "File not found: " + e.toString());
        }
        catch(IOException e){
            Log.e("JSONLoading", "Can not read file: " + e.toString());
        }
        catch(JSONException e){
            Log.e("JSONLoading", "json error: " + e.toString());
        }
        if(indexObject != null) {
            ArrayList<Item> items = parseJSONResponse(indexObject);
            mutableItems.setValue(items);
        }

        return mutableItems;
    }

    public void saveImageLocally(Bitmap pBitmap, String pFilename) {
        ContextWrapper contextWrapper = new ContextWrapper(mApplicationContext);
        File directory = contextWrapper.getDir("itemImages", Context.MODE_PRIVATE);
        File file = new File (directory, pFilename);
        if(!file.exists()) {
            FileOutputStream fileOutputStream = null;
            try {
                fileOutputStream = new FileOutputStream(file);
                pBitmap.compress(Bitmap.CompressFormat.PNG, 100, fileOutputStream);
                fileOutputStream.flush();
                fileOutputStream.close();
            }
            catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public boolean loadImageLocally(String pFilename,MutableLiveData<Item> pItemData) {
        boolean loaded = false;
        ContextWrapper contextWrapper = new ContextWrapper(mApplicationContext);
        File directory = contextWrapper.getDir("itemImages", Context.MODE_PRIVATE);
        File file = new File(directory, pFilename);
        if (file.exists()) {
            FileInputStream fileInputStream = null;
            try {
                fileInputStream = new FileInputStream(file);
                Bitmap bitmap = BitmapFactory.decodeStream(fileInputStream);
                Item item = pItemData.getValue();
                item.addImage(bitmap);
                pItemData.setValue(item);

                fileInputStream.close();
                loaded = true;
            }
            catch(IOException e) {
                e.printStackTrace();
            }
        }
        return loaded;
    }

    public void loadImage(String pUrl, MutableLiveData<Item> pItemData) {
        RequestQueue queue = Volley.newRequestQueue(mApplicationContext);
        final MutableLiveData<Item> mutableItem = pItemData;

        ImageRequest imageRequest = new ImageRequest(
                pUrl, new Response.Listener<Bitmap>() {
            @Override
            public void onResponse(Bitmap bitmap) {
                Item item = mutableItem.getValue();
                item.addImage(bitmap);
                saveImageLocally(bitmap, Uri.parse(pUrl).getLastPathSegment());
                mutableItem.setValue(item);
            }
        },0,0,
        ImageView.ScaleType.CENTER_CROP,
        Bitmap.Config.RGB_565,
        new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {
                String errorResponse = "That didn't work";
            }
        });
        queue.add(imageRequest);
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
        for(int i = 0; i < JSONimageUrls.length(); i++){
            imageUrls.add(JSONimageUrls.getJSONObject(i).toString());
        }
        String tileBackUrl = pItemObject.getString("TileBack");

        Item item = new Item(Name, imageUrls, tileBackUrl);
        return item;
    }


}
