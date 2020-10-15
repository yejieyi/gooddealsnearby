package edu.nyp.gooddealsnearby;

import android.Manifest;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingRequest;
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

public class MainActivity extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener, GoogleApiClient.ConnectionCallbacks, LocationListener,
        ResultCallback<Status>,OnTaskCompleted {

    final int PERMISSIONS_REQUEST_LOCATION = 1001;
    private static final String TAG = MainActivity.class.getSimpleName();
    final int UPDATE_PREFERRED_INTERVAL = 2000;
    final int UPDATE_FASTEST_INTERVAL = 1000;
    final int UPDATE_PRIORITY = LocationRequest.PRIORITY_HIGH_ACCURACY;
    boolean mRequestingLocationUpdates = false;
    Location mLastLocation;
    final String tag = getClass().getSimpleName();
    GoogleApiClient mGoogleApiClient;
    double latitude = 0;
    double longitude = 0;
    ArrayList<LocationObj> locations = new ArrayList<LocationObj>();
    public static final String HOST = "192.168.1.122";

    public static final String DIR = "gooddealsnearby";
    int count =0;


    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            Fragment fragment = null;
            switch (item.getItemId()) {
                case R.id.navigation_home:
                    fragment = new ExploreFragment();
                    break;
                case R.id.navigation_dashboard:
                    fragment = new MyFragment();
                    break;
                case R.id.navigation_notifications:
                    fragment = new AboutusFragment();
                    break;
            }
            return loadFragment(fragment);
        }
    };

    private static final String NOTIFICATION_MSG = "NOTIFICATION MSG";
    // Create a Intent send by the notification
    public static Intent makeNotificationIntent(Context context, String msg) {
        Intent intent = new Intent( context, MainActivity.class );
        intent.putExtra( NOTIFICATION_MSG, msg );
        return intent;
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

    public String convertToJSON() {
        JSONStringer jsonText = new JSONStringer();
        try {

            jsonText.object();
            jsonText.key("type");
            jsonText.value("1001");
            jsonText.key("lat");
            jsonText.value(latitude);
            jsonText.key("lon");
            jsonText.value(longitude);
            jsonText.key("option");
            jsonText.value("all");
            jsonText.endObject();

        } catch (Exception e) {
            e.printStackTrace();
        }
        return jsonText.toString();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Log.d(TAG, "lol");
        loadFragment(new ExploreFragment());
        if (mGoogleApiClient == null){
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }


        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);


    }

    @Override
    public void onTaskCompleted(String response) {
        // retrieve response information from JSON
        retrieveFromJSON(response);
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
                startGeofence();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
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
                mLocationRequest.setInterval(UPDATE_PREFERRED_INTERVAL);
                mLocationRequest.setFastestInterval(UPDATE_FASTEST_INTERVAL);
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
                count ++;
                latitude = mLastLocation.getLatitude();
                longitude = mLastLocation.getLongitude();
                if (count==1) {
                    HttpAsyncTask task = new HttpAsyncTask(this);

                    String jsonString = convertToJSON();
                    task.execute("http://" + HOST + "/" + DIR + "/promotion.php",
                            jsonString);
                }
//                Toast.makeText(getApplicationContext(),"Lat:"+latitude+",lon:"+longitude,Toast.LENGTH_LONG).show();
            }

        } catch (SecurityException se){
            se.printStackTrace();
        }

    }

    @Override
    protected void onResume() {
        super.onResume();
        retrievePreferences();
        if (!mGoogleApiClient.isConnected()) {
            mGoogleApiClient.connect();
        }


    }

    private void retrievePreferences() {
        SharedPreferences prefs = getSharedPreferences("profile", MODE_PRIVATE);
        if (prefs.contains("username")) {
            String username = prefs.getString("username", "");
//            Toast.makeText(getApplicationContext(),"Welcome Back!\n"+username,Toast.LENGTH_LONG).show();
        } else {
            startActivity(new Intent(MainActivity.this, LoginActivity.class));
        }

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
        Log.d(tag, "Google Play Services connected");
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
        Log.d(tag, "Google Play Services suspended");
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.d(tag, "Google Play Services failed");
    }

    private boolean loadFragment(Fragment fragment) {
            if (fragment != null) {
                getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.fragment_container, fragment)
                        .commit();
                Bundle bundle = new Bundle();
                bundle.putDouble("lat", latitude);
                bundle.putDouble("lon", longitude);
                fragment.setArguments(bundle);
                return true;
            }
            return false;


    }

    private void startGeofence() {
        Log.i(TAG, "startGeofence()");
        for (int i=0;i<locations.size();i++){
            Geofence geofence = createGeofence( locations.get(i).latlon, GEOFENCE_RADIUS,locations.get(i).name );
            GeofencingRequest geofenceRequest = createGeofenceRequest( geofence );
            addGeofence( geofenceRequest );
        }

    }

    private static final long GEO_DURATION = 60 * 60 * 1000;
    private static final float GEOFENCE_RADIUS = 500.0f; // in meters

    // Create a Geofence
    private Geofence createGeofence(LatLng latLng, float radius,String name ) {
        Log.d(TAG, "createGeofence");
        return new Geofence.Builder()
                .setRequestId(name)
                .setCircularRegion( latLng.latitude, latLng.longitude, radius)
                .setExpirationDuration( GEO_DURATION )
                .setTransitionTypes( Geofence.GEOFENCE_TRANSITION_ENTER
                        | Geofence.GEOFENCE_TRANSITION_EXIT )
                .build();
    }

    // Create a Geofence Request
    private GeofencingRequest createGeofenceRequest( Geofence geofence ) {
        Log.d(TAG, "createGeofenceRequest");
        return new GeofencingRequest.Builder()
                .setInitialTrigger( GeofencingRequest.INITIAL_TRIGGER_ENTER )
                .addGeofence( geofence )
                .build();
    }

    private PendingIntent geoFencePendingIntent;
    private final int GEOFENCE_REQ_CODE = 0;
    private PendingIntent createGeofencePendingIntent() {
        Log.d(TAG, "createGeofencePendingIntent");
        if ( geoFencePendingIntent != null )
            return geoFencePendingIntent;

        Intent intent = new Intent( this, GeofenceTrasitionService.class);
        return PendingIntent.getService(
                this, GEOFENCE_REQ_CODE, intent, PendingIntent.FLAG_UPDATE_CURRENT );
    }

    // Add the created GeofenceRequest to the device's monitoring list
    private void addGeofence(GeofencingRequest request) {
        Log.d(TAG, "addGeofence");
        if (checkPermission())
            LocationServices.GeofencingApi.addGeofences(
                    mGoogleApiClient,
                    request,
                    createGeofencePendingIntent()
            ).setResultCallback(this);
    }

    private boolean checkPermission() {
        Log.d(TAG, "checkPermission()");
        // Ask for permission if it wasn't granted yet
        return (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED );
    }

    @Override
    public void onResult(@NonNull Status status) {
        Log.i(TAG, "onResult: " + status);
        if ( status.isSuccess() ) {
            Log.d(TAG,"success");
        } else {
            // inform about fail
        }
    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {

    }
}
