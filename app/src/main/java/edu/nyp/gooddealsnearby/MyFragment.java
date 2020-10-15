package edu.nyp.gooddealsnearby;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import org.json.JSONObject;
import org.json.JSONStringer;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

import static android.content.Context.MODE_PRIVATE;

public class MyFragment extends Fragment implements View.OnClickListener {

    private final int PICK_IMAGE_ID = 1;
    ImageButton profile;
    Button confirmBTN = null;
    Button logoutBTN = null;
    String username;
    String phone;
    String email;
    String password;
    String id;
    String status;
    String photo;
    TextView UsernameText = null;
    TextView PhoneText = null;
    TextView EmailText = null;
    TextView PasswordText = null;
    TextView cPasswordText = null;
    public static final String HOST = "192.168.1.122";

    public static final String DIR = "gooddealsnearby";


    private static final String TAG = MainActivity.class.getSimpleName();
    public String SERVER = "http://"+HOST+"/"+DIR+"/saveImage.php",
            timestamp;
    private View mView;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        mView = inflater.inflate(R.layout.fragment_my, null);
        profile =(ImageButton)mView.findViewById( R.id.logoIV);
        profile.setOnClickListener(this);
        confirmBTN = (Button) mView.findViewById(R.id.registerBTN);
        confirmBTN.setOnClickListener(this);
        logoutBTN = (Button)mView.findViewById(R.id.logoutBTN);
        logoutBTN.setOnClickListener(this);
        retrievePreferences();
        UsernameText = (TextView) mView.findViewById(R.id.usernameET);
        PhoneText = (TextView) mView.findViewById(R.id.phoneET);
        EmailText = (TextView) mView.findViewById(R.id.emailET);
        PasswordText = (TextView) mView.findViewById(R.id.pwET);
        cPasswordText = (TextView) mView.findViewById(R.id.cpassET);
        UsernameText.setText(username);
        PhoneText.setText(phone);
        EmailText.setText(email);
//        Picasso.with(getContext()).load("http://"+HOST+"/"+DIR+"/"+photo).into(profile);
        new DownloadImageTask(profile) .execute("http://"+HOST+"/"+DIR+"/"+photo);
        return mView;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.registerBTN:
                Fragment nextFrag= new ProfileFragment();
                getActivity().getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, nextFrag,"findThisFragment")
                        .addToBackStack(null)
                        .commit();
                break;
            case R.id.logoutBTN:
                SharedPreferences preferences = getActivity().getSharedPreferences("profile", MODE_PRIVATE);
                preferences.edit().clear().commit();
                Toast.makeText(getContext(), "Log out successfully", Toast.LENGTH_SHORT).show();
                retrievePreferences();
                break;

        }
    }

    private class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {
        ImageButton bmImage;
        public DownloadImageTask(ImageButton bmImage) {
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





    private void retrievePreferences() {
        SharedPreferences prefs = getActivity().getSharedPreferences("profile",MODE_PRIVATE);
        if (prefs.contains("username")){
            username = prefs.getString("username","");
            email = prefs.getString("email","");
            phone = prefs.getString("phone","");
            password = prefs.getString("password","");
            photo = prefs.getString("photo","");
            id = prefs.getString("id","");
        }
        else{
            startActivity(new Intent(getActivity(), LoginActivity.class));
        }

    }









}
