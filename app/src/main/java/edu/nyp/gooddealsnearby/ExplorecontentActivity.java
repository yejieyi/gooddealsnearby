package edu.nyp.gooddealsnearby;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Typeface;
import android.location.Location;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;

import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONStringer;

import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Timer;
import java.util.TimerTask;

import static android.view.View.VISIBLE;

public class ExplorecontentActivity extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener, GoogleApiClient.ConnectionCallbacks,OnTaskCompleted,LocationListener, AdapterView.OnItemClickListener {

    final String TAG = getClass().getSimpleName();

    double currentLat=0;
    double currentLong=0;
    EditText searchET;
    String option;
//    RelativeLayout tblLocation;
    ArrayList<LocationObj> locations = new ArrayList<LocationObj>();
    ArrayList<LocationObj> slocations = new ArrayList<LocationObj>();

    private static final String REQ_UPLOAD = "1001";
    private static final String REQ_DOWNLOAD = "1002";
    public static final String HOST = "192.168.1.122";

    public static final String DIR = "gooddealsnearby";
    String msgType;
    ListView simpleList;
    String search="";
    final int PERMISSIONS_REQUEST_LOCATION = 1001;
    final int UPDATE_PREFERRED_INTERVAL = 2000;
    final int UPDATE_FASTEST_INTERVAL = 1000;
    final int UPDATE_PRIORITY = LocationRequest.PRIORITY_HIGH_ACCURACY;
    boolean mRequestingLocationUpdates = false;
    Location mLastLocation;
    GoogleApiClient mGoogleApiClient;
    SwipeRefreshLayout pullToRefresh;
    FrameLayout f;
    LinearLayout v;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_explorecontent);
        f = (FrameLayout)findViewById(R.id.fragment_container);
        v = (LinearLayout) findViewById(R.id.main);
        currentLat = getIntent().getExtras().getDouble("lat");
        currentLong = getIntent().getExtras().getDouble("lon");
        option = getIntent().getExtras().getString("option");
//        tblLocation = (RelativeLayout) findViewById(R.id.tblLocation);
        Log.d(TAG,currentLat+","+currentLong+","+option);
        searchET = (EditText)findViewById(R.id.searchET);
        pullToRefresh = findViewById(R.id.pullToRefresh);
        pullToRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                checkForPermission(); // your code
                pullToRefresh.setRefreshing(false);
            }
        });
        searchET.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View view, int keyCode, KeyEvent event) {
                if((event.getAction()==KeyEvent.ACTION_DOWN)&&
                        (keyCode==KeyEvent.KEYCODE_ENTER)){
                    search = searchET.getText().toString();
                    Log.d(TAG,search);
                    InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(searchET.getWindowToken(),0);
                    refreshTable();
                    return true;
                }
                return false;
            }
        });

        searchET.setSelected(false);
        if (mGoogleApiClient == null){
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }

        simpleList = (ListView)findViewById(R.id.ListView);
        simpleList.setOnItemClickListener(this);
        loadFragment(new LoadingFragment());


    }
    @Override
    protected void onStart() {
        mGoogleApiClient.connect();
        super.onStart();
    }

    @Override
    protected void onStop() {
        // Stop location update
        if (mRequestingLocationUpdates)
            stopLocationUpdates();

        mGoogleApiClient.disconnect();
        super.onStop();
    }

    @Override
    public void onResume() {
        super.onResume();
        msgType = REQ_DOWNLOAD;
        // call AsynTask to perform network operation on separate thread
        Log.d("lol","onclick");
        if (!mGoogleApiClient.isConnected()) {
            mGoogleApiClient.connect();
        }
    }

    @Override
    public void onTaskCompleted(String response) {
        // retrieve response information from JSON
        retrieveFromJSON(response);
        refreshTable();
    }
    public String convertToJSON() {
        JSONStringer jsonText = new JSONStringer();
        try {

            jsonText.object();
            jsonText.key("type");
            jsonText.value(msgType);
            jsonText.key("lat");
            jsonText.value(currentLat);
            jsonText.key("lon");
            jsonText.value(currentLong);
            jsonText.key("option");
            jsonText.value(option);
            jsonText.endObject();

        } catch (Exception e) {
            e.printStackTrace();
        }
        return jsonText.toString();
    }

    public void retrieveFromJSON(String message) {
        try {
//            JSONObject jsonObject = new JSONObject(message);
            JSONArray resultArray = new JSONArray(message);
            locations.clear();
            if (resultArray != null) {
                for (int i = 0; i < resultArray.length(); i++) {
                    JSONObject resultObj = resultArray.getJSONObject(i);
                    String id = resultObj.getString("id");;
                    String name = resultObj.getString("storeName");
                    String promotion_title = resultObj.getString("promotion_title");
                    String price = resultObj.getString("price");
                    String image = resultObj.getString("image");
                    double lat = resultObj.getDouble("lat");
                    double lon = resultObj.getDouble("lon");
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    Date start_date = Date.valueOf(resultObj.getString("start_date"));
                    Date end_date = Date.valueOf(resultObj.getString("end_date"));
                    String location = resultObj.getString("location");
                    double distance = resultObj.getDouble("distance");
                    double absdistance = resultObj.getDouble("distance");
                    String placeid = resultObj.getString("place_id");
                    String desc = resultObj.getString("description");
                    String unit;
                    if (distance>1000){
                        distance = Math.round(distance/1000.0);
                        unit = "km";
                    }
                    else{
                        distance = Math.round(distance);
                        unit = "m";
                    }
                    LatLng latlon = new LatLng(lat,lon);
                    locations.add(new LocationObj(id,name,promotion_title,price,image,latlon,start_date,end_date,location,distance,unit,absdistance,placeid,desc));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void refreshTable() {
        slocations.clear();
        Log.d(TAG, "refreshTable...");
        Collections.sort(locations, new Comparator<LocationObj>() {
            public int compare(LocationObj o1, LocationObj o2) {
                if (o1.absdistance>o2.absdistance)
                        return 1;
                else{
                    return -1;
                }
            }
        });
        for (int i=0;i<locations.size();i++){
            if (locations.get(i).name.toLowerCase().contains(search.toLowerCase())){
                slocations.add(locations.get(i));
            }
        }
        CustomAdapter customAdapter = new CustomAdapter(getApplicationContext(), slocations,search);
        simpleList.setAdapter(customAdapter);
        v.removeViewInLayout(f);



    }// refreshTable

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {

    }

    private void checkForPermission() {
        if (Build.VERSION.SDK_INT >= 23 &&
                ContextCompat.checkSelfPermission(this,
                        android.Manifest.permission.ACCESS_FINE_LOCATION)
                        != PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this,
                        android.Manifest.permission.ACCESS_COARSE_LOCATION)
                        != PackageManager.PERMISSION_GRANTED) {
            // Permission not granted yet when APL Level is at least 23
            ActivityCompat.requestPermissions(this, new String[]
                            {android.Manifest.permission.ACCESS_COARSE_LOCATION,
                                    android.Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSIONS_REQUEST_LOCATION);

        }
        else {
            // API Level is lower than 23 or Permission already granted
            startLocationUpdates();}
    }


    @Override
    public void onLocationChanged(Location location) {
        updateLocation();
    }


    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Log.d(TAG, "Google Play Services connected");
        if (!mRequestingLocationUpdates){
            // If not yet updating location
            checkForPermission();
        } else {
            // If updating location
            if (mGoogleApiClient.isConnected()){
                stopLocationUpdates();
            }
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.d(TAG, "Google Play Services suspended");
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.d(TAG, "Google Play Services failed");
    }
    private void stopLocationUpdates() {
        // remove updates on change of location
        LocationServices.FusedLocationApi.removeLocationUpdates(
                mGoogleApiClient, this);
        // update status
        mRequestingLocationUpdates = false;

    }

    private void startLocationUpdates() {
        try {
            // update last known location
            updateLocation();
            // request updates on change of location
            LocationRequest mLocationRequest = new LocationRequest();
//            mLocationRequest.setInterval(UPDATE_PREFERRED_INTERVAL);
//            mLocationRequest.setFastestInterval(UPDATE_FASTEST_INTERVAL);
            mLocationRequest.setPriority(UPDATE_PRIORITY);
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient,
                    mLocationRequest, this);
            // update status
            mRequestingLocationUpdates = true;
        } catch(SecurityException se){
            se.printStackTrace();
        }

    }

    private void updateLocation() {
        try {
            // Using FusedLocationAPI to obtain last known location
            mLastLocation = LocationServices.FusedLocationApi.getLastLocation(
                    mGoogleApiClient);

            if (mLastLocation != null) {
                currentLat = mLastLocation.getLatitude();
                currentLong = mLastLocation.getLongitude();

                HttpAsyncTask task = new HttpAsyncTask(this);

                String jsonString = convertToJSON();
                task.execute("http://"+HOST+"/"+DIR+"/promotion.php",
                        jsonString);
//                Toast.makeText(getApplicationContext(),"Lat:"+latitude+",lon:"+longitude,Toast.LENGTH_LONG).show();
            }

        } catch (SecurityException se){
            se.printStackTrace();
        }

    }

    private boolean loadFragment(Fragment fragment) {
        if (fragment != null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, fragment)
                    .commit();
            return true;
        }
        return false;


    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
        Log.i("HelloListView", "You clicked Item: " + id + " at position:" + position);
        Log.i("HelloListView", "You clicked Item: " + slocations.get(position).promotion_title);
        Intent intent = new Intent(this, DetailActivity.class);
        intent.putExtra("location", slocations.get(position));
        startActivity(intent);
    }
}
