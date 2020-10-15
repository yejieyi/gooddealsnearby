package edu.nyp.gooddealsnearby;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
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
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONObject;
import org.json.JSONStringer;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

import static android.content.Context.MODE_PRIVATE;

public class ProfileFragment  extends Fragment implements OnTaskCompleted, View.OnClickListener {

    private final int PICK_IMAGE_ID = 1;
    ImageButton profile;
    Button confirmBTN = null;
    String username;
    String phone;
    String email;
    String msgType;
    String password;
    String cpassword;
    String id;
    String status;
    String photo;
    TextView UsernameText = null;
    EditText PhoneText = null;
    EditText EmailText = null;
    EditText PasswordText = null;
    EditText cPasswordText = null;
    public static final String HOST = "192.168.1.122";

    public static final String DIR = "gooddealsnearby";

    private static final String REQ_UPLOAD = "1001";
    private static final String REQ_DOWNLOAD = "1002";

    private static final String TAG = MainActivity.class.getSimpleName();
    public String SERVER = "http://"+HOST+"/"+DIR+"/saveImage.php",
            timestamp;
    private View mView;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        mView = inflater.inflate(R.layout.fragment_profile, null);
        profile =(ImageButton)mView.findViewById( R.id.logoIV);
        profile.setOnClickListener(this);
        confirmBTN = (Button) mView.findViewById(R.id.registerBTN);
        confirmBTN.setOnClickListener(this);
        retrievePreferences();
        UsernameText = (TextView) mView.findViewById(R.id.usernameET);
        PhoneText = (EditText) mView.findViewById(R.id.phoneET);
        EmailText = (EditText) mView.findViewById(R.id.emailET);
        PasswordText = (EditText) mView.findViewById(R.id.pwET);
        cPasswordText = (EditText) mView.findViewById(R.id.cpassET);
        UsernameText.setText(username);
        PhoneText.setText(phone);
        EmailText.setText(email);
        new DownloadImageTask(profile) .execute("http://"+HOST+"/"+DIR+"/"+photo);
        return mView;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.registerBTN:
                onConfirmClicked();
                break;
            case R.id.logoIV:
                pickImg();
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

    public void pickImg() {
        Intent chooseImageIntent = ImagePicker.getPickImageIntent(getContext());
        startActivityForResult(chooseImageIntent, PICK_IMAGE_ID);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch(requestCode) {
            case PICK_IMAGE_ID:
                Bitmap bitmap = ImagePicker.getImageFromResult(getContext(), resultCode, data);
                // TODO use bitmap
                if (bitmap!=null){
                    profile.setImageBitmap(bitmap);
                }


                break;
            default:
                super.onActivityResult(requestCode, resultCode, data);
                break;
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

    public String convertToJSON() {
        JSONStringer jsonText = new JSONStringer();
        try {

            jsonText.object();
            jsonText.key("type");
            jsonText.value(msgType);
            jsonText.key("username");
            jsonText.value(username);
            jsonText.key("phone");
            jsonText.value(phone);
            jsonText.key("email");
            jsonText.value(email);
            jsonText.key("password");
            jsonText.value(password);
            jsonText.key("photo");
            jsonText.value(photo);
            jsonText.key("id");
            jsonText.value(id);
            jsonText.endObject();

        } catch (Exception e) {
            e.printStackTrace();
        }
        return jsonText.toString();
    }

    public void onConfirmClicked() {
        PhoneText = (EditText) mView.findViewById(R.id.phoneET);
        EmailText = (EditText) mView.findViewById(R.id.emailET);
        PasswordText = (EditText) mView.findViewById(R.id.pwET);
        cPasswordText = (EditText) mView.findViewById(R.id.cpassET);
        phone = PhoneText.getText().toString();
        email = EmailText.getText().toString();
        if ( !PasswordText.getText().toString().equals("")){
            password = PasswordText.getText().toString();
        }
        cpassword = cPasswordText.getText().toString();
        msgType = REQ_UPLOAD;
        if (PasswordText.getText().toString().equals(cpassword)){
            Bitmap image = ((BitmapDrawable) profile.getDrawable()).getBitmap();
            if (image!=null){
                //execute the async task and upload the image to server
                new Upload(image,"IMG_"+username).execute();
                photo = "pic/IMG_"+username+".jpg";
            }
            else{
                photo="";
                String jsonString = convertToJSON();
                // call AsynTask to perform network operation on separate thread
                HttpAsyncTask task = new HttpAsyncTask(this);
                task.execute("http://"+HOST+"/"+DIR+"/updateProfile.php",
                        jsonString);
                Log.d("lol","onclick");
            }
        }
        else{
            Toast.makeText(getContext(), "Password does not match", Toast.LENGTH_SHORT).show();
        }





    }



    public void retrieveFromJSON(String message) {
        try {
            JSONObject jsonObject = new JSONObject(message);
            msgType = jsonObject.getString("type");
            if (msgType.equals(REQ_DOWNLOAD)){
                username = jsonObject.getString("username");
                phone = jsonObject.getString("phone");
                email = jsonObject.getString("email");
                password = jsonObject.getString("password");
                cpassword = jsonObject.getString("cpassword");
                photo = jsonObject.getString("photo");
                id = jsonObject.getString("id");

            } else if (msgType.equals(REQ_UPLOAD)) {
                id = jsonObject.getString("id");
            }
            status = jsonObject.getString("status");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onTaskCompleted(String response) {
        // retrieve response information from JSON
        retrieveFromJSON(response);
        // if response is from upload request
        if (msgType.equals(REQ_UPLOAD)){
            if (response.equals("Did not work!")||response.equals("")){
                Toast.makeText(getContext(),"Profile update failed,\n please try again later!",Toast.LENGTH_LONG).show();
            }
            else{
                saveAsPreferences();
                Toast.makeText(getContext(),"Profile updated\n successfully!",Toast.LENGTH_LONG).show();

            }
        }
        // if response if from download request
        else if (msgType.equals(REQ_DOWNLOAD)) {

        }
    }



    private void saveAsPreferences() {
        SharedPreferences prefs = getActivity().getSharedPreferences("profile",MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("username", username);
        editor.putString("phone",phone);
        editor.putString("email",email);
        editor.putString("photo",photo);
        editor.putString("id",id);
        editor.commit();
    }
    private String hashMapToUrl(HashMap<String, String> params) throws UnsupportedEncodingException {
        StringBuilder result = new StringBuilder();
        boolean first = true;
        for(Map.Entry<String, String> entry : params.entrySet()){
            if (first)
                first = false;
            else
                result.append("&");

            result.append(URLEncoder.encode(entry.getKey(), "UTF-8"));
            result.append("=");
            result.append(URLEncoder.encode(entry.getValue(), "UTF-8"));
        }

        return result.toString();
    }

    //async task to upload image
    private class Upload extends AsyncTask<Void,Void,String> {
        private Bitmap image;
        private String name;

        public Upload(Bitmap image, String name) {
            this.image = image;
            this.name = name;
        }

        @Override
        protected String doInBackground(Void... params) {
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            //compress the image to jpg format
            image.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
            /*
             * encode image to base64 so that it can be picked by saveImage.php file
             * */
            String encodeImage = Base64.encodeToString(byteArrayOutputStream.toByteArray(), Base64.DEFAULT);

            //generate hashMap to store encodedImage and the name
            HashMap<String, String> detail = new HashMap<>();
            detail.put("name", name);
            detail.put("image", encodeImage);

            try {
                //convert this HashMap to encodedUrl to send to php file
                String dataToSend = hashMapToUrl(detail);
                //make a Http request and send data to saveImage.php file
                String response = Request.post(SERVER, dataToSend);

                //return the response
                return response;

            } catch (Exception e) {
                e.printStackTrace();
                Log.e(TAG, "ERROR  " + e);
                return null;
            }
        }


        @Override
        protected void onPostExecute(String s) {
            //show image uploaded
            if (s != null) {
//                Toast.makeText(getApplicationContext(), "Image Uploaded", Toast.LENGTH_SHORT).show();
                String jsonString = convertToJSON();
                // call AsynTask to perform network operation on separate thread
                HttpAsyncTask task = new HttpAsyncTask(ProfileFragment.this);
                task.execute("http://"+HOST+"/"+DIR+"/updateProfile.php",
                        jsonString);
                Log.d("lol","onclick");
            } else {
                Toast.makeText(getContext(), "Image failed", Toast.LENGTH_SHORT).show();
            }

        }
    }
}
