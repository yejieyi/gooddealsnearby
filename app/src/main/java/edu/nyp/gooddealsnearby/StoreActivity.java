package edu.nyp.gooddealsnearby;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v4.app.ActivityCompat;
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
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONStringer;

import java.io.InputStream;
import java.net.URLEncoder;
import java.sql.Date;
import java.text.SimpleDateFormat;

public class StoreActivity extends AppCompatActivity implements OnMapReadyCallback, OnTaskCompleted, View.OnClickListener {

    LocationObj location;
    MapView mapView;
    private GoogleMap gmap;
    private static final String MAP_VIEW_BUNDLE_KEY = "MapViewBundleKey";
    public static final String HOST = "10.27.198.88";

    public static final String DIR = "gooddealsnearby";
    ImageView sImage;
    TextView store;
    TextView rate;
    TextView lTXT;
    TextView state;
    Button call;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_store);
        location = (LocationObj) getIntent().getExtras().get("location");
        sImage = (ImageView) findViewById(R.id.simage);
        store = (TextView) findViewById(R.id.store);
        rate = (TextView) findViewById(R.id.rate);
        state = (TextView) findViewById(R.id.state);
        store.setText(location.name);
        lTXT = (TextView) findViewById(R.id.location);
        call = (Button) findViewById(R.id.call);
        new DownloadImageTask(sImage).execute("http://" + HOST + "/" + DIR + "/" + location.image);
        Bundle mapViewBundle = null;
        if (savedInstanceState != null) {
            mapViewBundle = savedInstanceState.getBundle(MAP_VIEW_BUNDLE_KEY);
        }

        mapView = findViewById(R.id.map_view);
        mapView.onCreate(mapViewBundle);
        mapView.getMapAsync(this);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        Bundle mapViewBundle = outState.getBundle(MAP_VIEW_BUNDLE_KEY);
        if (mapViewBundle == null) {
            mapViewBundle = new Bundle();
            outState.putBundle(MAP_VIEW_BUNDLE_KEY, mapViewBundle);
        }

        mapView.onSaveInstanceState(mapViewBundle);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        LatLng sydney = new LatLng(location.lat, location.lon);
        gmap = googleMap;
        gmap.addMarker(new MarkerOptions().position(sydney)
                .title(location.name).visible(true)).showInfoWindow();
        gmap.moveCamera(CameraUpdateFactory.newLatLng(sydney));
        gmap.animateCamera(CameraUpdateFactory.zoomTo(12));

        try {
            HttpAsyncTask task = new HttpAsyncTask(this);

            String jsonString = convertToJSON();
            task.execute("http://" + HOST + "/" + DIR + "/getdetails.php",
                    jsonString);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String convertToJSON() {
        JSONStringer jsonText = new JSONStringer();
        try {

            jsonText.object();
            jsonText.key("placeid");
            jsonText.value(location.placeid);
            jsonText.endObject();

        } catch (Exception e) {
            e.printStackTrace();
        }
        return jsonText.toString();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    protected void onPause() {
        mapView.onPause();
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        mapView.onDestroy();
        super.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }

    @Override
    public void onClick(View view) {
        call((String) call.getText());
    }

    private static final int REQUEST_PHONE_CALL = 1;

    public void call(String s) {
        if (ActivityCompat.checkSelfPermission(this,
                android.Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                    this,
                    new String[]{Manifest.permission.CALL_PHONE},
                    REQUEST_PHONE_CALL);
            return;
        }
        Intent callIntent = new Intent(Intent.ACTION_CALL);
        callIntent.setData(Uri.parse(s));
        startActivity(callIntent);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case REQUEST_PHONE_CALL: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Intent callIntent = new Intent(Intent.ACTION_CALL);
                    callIntent.setData(Uri.parse((String) call.getText()));
                    if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                        // TODO: Consider calling
                        //    ActivityCompat#requestPermissions
                        // here to request the missing permissions, and then overriding
                        //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                        //                                          int[] grantResults)
                        // to handle the case where the user grants the permission. See the documentation
                        // for ActivityCompat#requestPermissions for more details.
                        return;
                    }
                    startActivity(callIntent);
                }
            }
        }
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
    @Override
    public void onTaskCompleted(String response) {
        // retrieve response information from JSON
        retrieveFromJSON(response);

    }

    public void retrieveFromJSON(String message) {
        try {
            JSONObject jsonObject = new JSONObject(message);
            JSONObject infoObject = jsonObject.getJSONObject("result");
            JSONObject oObject = new JSONObject();
            if (infoObject.has("opening_hours")){

                oObject = infoObject.getJSONObject("opening_hours");
            }
            if (infoObject.has("rating")&&infoObject.getString("rating")!=""){
                rate.setText("Rating: "+infoObject.getString("rating"));
            }
            if (infoObject.has("vicinity")){
                lTXT.setText(infoObject.getString("vicinity"));
            }
            else{
                lTXT.setText(location.location);
            }
            if (infoObject.has("formatted_phone_number")){
                call.setText("tel:"+infoObject.getString("formatted_phone_number").replaceAll("\\s",""));
                call.setOnClickListener(this);
            }
            if (oObject!=null){

                if (oObject.getBoolean("open_now")){
                    state.setText("Open Now");
                }
                else{

                    state.setText("Closed");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
