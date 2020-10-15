package edu.nyp.gooddealsnearby;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.Toast;

import org.json.JSONObject;
import org.json.JSONStringer;

import java.io.ByteArrayOutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

import static android.provider.Telephony.Carriers.SERVER;

public class RegisterActivity extends AppCompatActivity implements OnTaskCompleted {
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
    String status="";
    String photo;
    EditText UsernameText = null;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        profile =(ImageButton)findViewById( R.id.logoIV);
        confirmBTN = (Button) findViewById(R.id.registerBTN);
    }

    public void pickImg(View view) {
        Intent chooseImageIntent = ImagePicker.getPickImageIntent(this);
        startActivityForResult(chooseImageIntent, PICK_IMAGE_ID);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch(requestCode) {
            case PICK_IMAGE_ID:
                Bitmap bitmap = ImagePicker.getImageFromResult(this, resultCode, data);
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

    public boolean isValidEmailAddress(String email) {
        String ePattern = "^[a-zA-Z0-9.!#$%&'*+/=?^_`{|}~-]+@((\\[[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\])|(([a-zA-Z\\-0-9]+\\.)+[a-zA-Z]{2,}))$";
        java.util.regex.Pattern p = java.util.regex.Pattern.compile(ePattern);
        java.util.regex.Matcher m = p.matcher(email);
        return m.matches();
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
//            jsonText.key("id");
//            jsonText.value(id);
            jsonText.endObject();

        } catch (Exception e) {
            e.printStackTrace();
        }
        return jsonText.toString();
    }

    public void onConfirmClicked(View view) {
        UsernameText = (EditText) findViewById(R.id.usernameET);
        PhoneText = (EditText) findViewById(R.id.phoneET);
        EmailText = (EditText) findViewById(R.id.emailET);
        PasswordText = (EditText) findViewById(R.id.pwET);
        cPasswordText = (EditText) findViewById(R.id.cpassET);
        username = UsernameText.getText().toString();
        phone = PhoneText.getText().toString();
        email = EmailText.getText().toString();
        password = PasswordText.getText().toString();
        cpassword = cPasswordText.getText().toString();
        msgType = REQ_UPLOAD;
        if (password.equals(cpassword)){
            if (isValidEmailAddress(email)){
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
                    task.execute("http://"+HOST+"/"+DIR+"/registerProfile.php",
                            jsonString);
                    Log.d("lol","onclick");
                }
            }
            else{
                Toast.makeText(getApplicationContext(), "Please enter valid email", Toast.LENGTH_SHORT).show();
            }

        }
        else{
            Toast.makeText(getApplicationContext(), "Password does not match", Toast.LENGTH_SHORT).show();
        }





    }



    public void retrieveFromJSON(String message) {
        try {

                JSONObject jsonObject = new JSONObject(message);
                msgType = jsonObject.getString("type");
                status = jsonObject.getString("status");
                Log.d(TAG,"current status:"+status);
                if (msgType.equals(REQ_DOWNLOAD)) {
                    username = jsonObject.getString("username");
                    phone = jsonObject.getString("phone");
                    email = jsonObject.getString("email");
                    password = jsonObject.getString("password");
                    cpassword = jsonObject.getString("cpassword");
                    photo = jsonObject.getString("photo");
                    id = jsonObject.getString("id");

                } else if (msgType.equals(REQ_UPLOAD)) {
                    username = jsonObject.getString("username");
                    phone = jsonObject.getString("phone");
                    email = jsonObject.getString("email");
                    password = jsonObject.getString("password");
                    photo = jsonObject.getString("photo");
                    id = jsonObject.getString("id");
                }

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
                Toast.makeText(getApplicationContext(),"Profile creation failed,\n please try again later!",Toast.LENGTH_LONG).show();
            }
            else if (status.equals("DUP")){
                Toast.makeText(getApplicationContext(),"Duplicated username,\n please try again later!",Toast.LENGTH_LONG).show();
            }
            else{
                saveAsPreferences();
                startActivity(new Intent(RegisterActivity.this,MainActivity.class));
                Toast.makeText(getApplicationContext(),"Profile created\n successfully!",Toast.LENGTH_LONG).show();
                this.finish();
            }
        }
        // if response if from download request
        else if (msgType.equals(REQ_DOWNLOAD)) {

        }
    }

    private void saveAsPreferences() {
        SharedPreferences prefs = getSharedPreferences("profile",MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("username", username);
        editor.putString("phone",phone);
        editor.putString("email",email);
        editor.putString("photo",photo);
        editor.putString("id",id);
        editor.putString("password",password);
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
            //compress the image to png format
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
                Log.d("hello",dataToSend);
                //make a Http request and send data to saveImage.php file
                String response = Request.post(SERVER
                        , dataToSend);

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
                HttpAsyncTask task = new HttpAsyncTask(RegisterActivity.this);
                task.execute("http://"+HOST+"/"+DIR+"/registerProfile.php",
                        jsonString);
                Log.d("lol","onclick");
            } else {
                Toast.makeText(getApplicationContext(), "Image failed", Toast.LENGTH_SHORT).show();
            }

        }
    }
}
