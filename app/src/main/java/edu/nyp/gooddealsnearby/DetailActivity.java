package edu.nyp.gooddealsnearby;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONObject;

import java.io.InputStream;
import java.net.URLEncoder;

public class DetailActivity extends AppCompatActivity  {
    LocationObj location;
    ImageView image;
    TextView store;
    TextView period;
    TextView detail;
    TextView promo;
    Button sBTN;

    public final String HOST = getResources().getString(R.string.url);

    public static final String DIR = "gooddealsnearby";
    private static final String MAP_VIEW_BUNDLE_KEY = "MapViewBundleKey";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
        location =(LocationObj) getIntent().getExtras().get("location");
        image = (ImageView)findViewById(R.id.simage);
        store = (TextView) findViewById(R.id.store);
        promo = (TextView)findViewById(R.id.promo);
        period = (TextView) findViewById(R.id.period);
        detail = (TextView) findViewById(R.id.detail);
        sBTN = (Button)findViewById(R.id.sBTN);
        store.setText(location.name);
        promo.setText(location.promotion_title);
        period.setText(location.start_date+" to "+location.end_date);
        detail.setText(location.desc);



        new DownloadImageTask(image).execute("http://"+HOST+"/"+DIR+"/"+location.image);
    }

    public void onClick(View view) {

        Intent intent = new Intent(this, StoreActivity.class);
        intent.putExtra("location", location);
        startActivity(intent);
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
        }
    }


}
