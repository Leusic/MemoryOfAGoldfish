package trotter.max.memoryofagoldfish;

import android.graphics.Bitmap;

import java.util.ArrayList;

public class Item{
    private String mName;
    private ArrayList<Bitmap> mImages;
    private ArrayList<String> mImageUrls;
    private Bitmap mTileBackImage;
    private String mTileBackUrl;
    private int[] mTileBank;


    public Item(String pName, ArrayList<String> pImageUrls, String pTileBackUrl) {
        setName(pName);
        setImageUrls(pImageUrls);
        setTileBackUrl(pTileBackUrl);
        mImages = new ArrayList<Bitmap>();
        mTileBank = new int[20];
        for(int x: mTileBank){
            x = 2;
        }
    }

    public ArrayList<Bitmap> getImages() { return mImages;}
    public void setImages(ArrayList<Bitmap> pImages) {mImages = pImages;}
    public void addImage(Bitmap pImage) {mImages.add(pImage);}
    public String getName() { return mName;}
    public void setName(String pName) {mName = pName;}
    public ArrayList<String> getImageUrls() { return mImageUrls;}
    public void setImageUrls(ArrayList<String> pImageUrls) {mImageUrls = pImageUrls;}
    public Bitmap getTileBackImage() {return mTileBackImage;}
    public void setTileBackImage(Bitmap pTileBackImage) {mTileBackImage = pTileBackImage;}
    public String getTileBackUrl() {return mTileBackUrl;}
    public void setTileBackUrl(String pTileBackUrl) {mTileBackUrl = pTileBackUrl;}

}
