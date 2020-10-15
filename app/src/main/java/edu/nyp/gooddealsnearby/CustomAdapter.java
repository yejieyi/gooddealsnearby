package edu.nyp.gooddealsnearby;


import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.io.InputStream;
import java.util.ArrayList;

public class CustomAdapter extends BaseAdapter {
    public final String HOST = "10.27.198,88";

    public static final String DIR = "gooddealsnearby";
    Context context;
//    String Name[];
//    String SubItem[];
//    int flags[];
    ArrayList<LocationObj> locations;
    LayoutInflater inflter;
    String search;
    boolean done = false;

    public CustomAdapter(Context applicationContext, ArrayList<LocationObj> locations,String search) {
        this.context = applicationContext;
        this.locations = locations;
        this.search = search;
        inflter = (LayoutInflater.from(applicationContext));
    }

    @Override
    public int getCount() {
//        search = search.toLowerCase();
//        int count = 0;
//        for (int i=0;i<locations.size();i++){
//            if (locations.get(i).name.toLowerCase().contains(search)){
//                count ++;
//            }
//        }
//        Log.d("b",""+count);
        return locations.size();
    }

    @Override
    public Object getItem(int i) {
        return null;
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        view = inflter.inflate(R.layout.activity_list_item, null);
        search = search.toLowerCase();
            TextView item = (TextView) view.findViewById(R.id.item);
            TextView subitem = (TextView) view.findViewById(R.id.subitem);
            TextView distance = (TextView) view.findViewById(R.id.distance);
            ImageView image = (ImageView) view.findViewById(R.id.image);
            item.setText(locations.get(i).promotion_title);
            subitem.setText(locations.get(i).name);
            String d;
            if (locations.get(i).unit.equals("m")){
                d = Math.round(locations.get(i).distance)+locations.get(i).unit;}
            else{
                d = locations.get(i).distance+locations.get(i).unit;
            }
            distance.setText(d);
            Log.d("a",locations.get(i).name);
            Log.d("a",""+locations.get(i).distance+locations.get(i).unit);
        Picasso.with(context).load("http://"+HOST+"/"+DIR+"/"+locations.get(i).image).into(image);
//            new DownloadImageTask(image).execute("http://"+HOST+"/"+DIR+"/"+locations.get(i).image);
            return view;
    }

    private class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {
        ImageView bmImage;
        public DownloadImageTask(ImageView bmImage) {
            this.bmImage = bmImage;
        }

        protected Bitmap doInBackground(String... urls) {
            String urldisplay = urls[0];
            Bitmap mIcon11 = null;
            try {
                InputStream in = new java.net.URL(urldisplay).openStream();
                mIcon11 = BitmapFactory.decodeStream(in);
            } catch (Exception e) {
                Log.e("Error", e.getMessage());
                e.printStackTrace();
            }
            return mIcon11;
        }

        protected void onPostExecute(Bitmap result) {
            bmImage.setImageBitmap(result);
            done = true;
        }
    }


}
