package edu.nyp.gooddealsnearby;

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import org.json.JSONObject;
import org.json.JSONStringer;

public class LoginActivity extends AppCompatActivity implements OnTaskCompleted  {
    String password;
    String username;
    EditText PasswordText = null;
    EditText UsernameText = null;
    String msgType;
    String phone;
    String email;
    String photo;
    String id;
    String status="";
    private static final String REQ_UPLOAD = "1001";
    private static final String REQ_DOWNLOAD = "1002";
    public static final String HOST = "192.168.1.122";

    public static final String DIR = "gooddealsnearby";

    @Override
    protected void onResume() {
        super.onResume();
        retrievePreferences();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
    }


    public void startReg(View v){
        Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
        startActivity(intent);
    }

    private void retrievePreferences() {
        SharedPreferences prefs = getSharedPreferences("profile",MODE_PRIVATE);
        if (prefs.contains("username")){
            String username = prefs.getString("username","");
            Toast.makeText(getApplicationContext(),"Welcome Back!\n"+username,Toast.LENGTH_LONG).show();
            startActivity(new Intent(LoginActivity.this, MainActivity.class));
            this.finish();
        }


    }



    public void onLogin(View view) {
        UsernameText = (EditText) findViewById(R.id.usernameET);
        PasswordText = (EditText) findViewById(R.id.passwordET);
        password = PasswordText.getText().toString();
        username = UsernameText.getText().toString();
        msgType = REQ_DOWNLOAD;
        String jsonString = convertToJSON();
        // call AsynTask to perform network operation on separate thread
        HttpAsyncTask task = new HttpAsyncTask(this);
        task.execute("http://"+HOST+"/"+DIR+"/loginProfile.php",
                jsonString);
        Log.d("lol","onclick");
    }

    public String convertToJSON() {
        JSONStringer jsonText = new JSONStringer();
        try {

            jsonText.object();
            jsonText.key("type");
            jsonText.value(msgType);
            jsonText.key("username");
            jsonText.value(username);
            jsonText.key("password");
            jsonText.value(password);
            jsonText.endObject();

        } catch (Exception e) {
            e.printStackTrace();
        }
        return jsonText.toString();
    }

    public void retrieveFromJSON(String message) {
        try {
            if (message!=null){
                JSONObject jsonObject = new JSONObject(message);
                msgType = jsonObject.getString("type");
                status = jsonObject.getString("status");
                if (msgType.equals(REQ_DOWNLOAD)){
                    if (status.equals("OK")){
                        username = jsonObject.getString("username");
                        phone = jsonObject.getString("phone");
                        email = jsonObject.getString("email");
                        password = jsonObject.getString("password");
                        photo = jsonObject.getString("photo");
                        id = jsonObject.getString("id");
                    }
                } else if (msgType.equals(REQ_UPLOAD)) {
                    id = jsonObject.getString("id");
                }

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
        if (msgType.equals(REQ_DOWNLOAD)){
            if (response.equals("Did not work!")||response.equals("")||status.equals("NOK")){
                Toast.makeText(getApplicationContext(),"Log in failed,\n please check your username and password!",Toast.LENGTH_LONG).show();
            }
            else{
                saveAsPreferences();
                startActivity(new Intent(LoginActivity.this,MainActivity.class));
                Toast.makeText(getApplicationContext(),"Login successfully!",Toast.LENGTH_LONG).show();
                this.finish();
            }
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
}
