package edu.nyp.gooddealsnearby;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.google.android.gms.maps.model.LatLng;

import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONStringer;

import java.net.URLEncoder;
import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.ArrayList;


public class ExploreFragment extends Fragment implements View.OnClickListener {

    final String TAG = getClass().getSimpleName();
    Button foodBTN = null;
    Button entBTN = null;
    Button shopBTN = null;
    Button hotelBTN = null;

    double currentLat=0;
    double currentLong=0;
    TableLayout tblLocation;
    ArrayList<LocationObj> locations = new ArrayList<LocationObj>();

    private static final String REQ_UPLOAD = "1001";
    private static final String REQ_DOWNLOAD = "1002";
    public static final String HOST = "192.168.1.122";

    public static final String DIR = "gooddealsnearby";
    String msgType;
    private View mView;
    String option = "";




    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        currentLat = getArguments().getDouble("lat");
        currentLong = getArguments().getDouble("lon");
        Log.d(TAG,"lat:"+currentLat+"lon"+currentLong);
        mView =  inflater.inflate(R.layout.fragment_explore, null);
        foodBTN = (Button)mView.findViewById(R.id.foodBTN);
        entBTN = (Button)mView.findViewById(R.id.entBTN);
        shopBTN = (Button)mView.findViewById(R.id.shopBTN);
        hotelBTN = (Button)mView.findViewById(R.id.hotelBTN);
        foodBTN.setOnClickListener(this);
        entBTN.setOnClickListener(this);
        shopBTN.setOnClickListener(this);
        hotelBTN.setOnClickListener(this);
        return mView;

    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.foodBTN:
                option = "food";
                break;
            case R.id.entBTN:
                option = "entertainment";
                break;
            case R.id.shopBTN:
                option = "shopping";
                break;
            case R.id.hotelBTN:
                option = "hotel";
                break;
        }
//        ExploreContentFragment fragment = new ExploreContentFragment();
//        FragmentManager fragmentManager = getFragmentManager();
//        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
//        fragmentTransaction.replace(R.id.fragment_container, fragment);
//        fragmentTransaction.commit();
        Intent intent = new Intent(getActivity(), ExplorecontentActivity.class);
        intent.putExtra("lat", currentLat);
        intent.putExtra("lon", currentLong);
        intent.putExtra("option",option);
        startActivity(intent);

    }
}
