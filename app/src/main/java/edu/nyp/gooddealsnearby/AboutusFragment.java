package edu.nyp.gooddealsnearby;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;


public class AboutusFragment extends Fragment implements View.OnClickListener {
    Button button_call;
    Button button_email;
    private View mView;
    ImageView logo;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        mView = inflater.inflate(R.layout.fragment_aboutus, null);
        // Inflate the layout for this fragment
        button_call = (Button) mView.findViewById(R.id.contact);
        button_email = (Button) mView.findViewById(R.id.email);
        button_call.setOnClickListener(this);
        button_email.setOnClickListener(this);
        logo = (ImageView)mView.findViewById(R.id.logoIV);
        logo.setImageResource(R.drawable.logo);
        return mView;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.contact:
                call("93464494");
                break;
            case R.id.email:
                Intent emailIntent = new Intent(Intent.ACTION_SENDTO);
                emailIntent.setData(Uri.parse("mailto: sunny_ye@outlook.com"));
                emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Feedback of gooddealsnearby app");
                startActivity(Intent.createChooser(emailIntent, "Send feedback"));
                break;

        }
    }

    private static final int REQUEST_PHONE_CALL = 1;
    public void call(String s) {
        if (ActivityCompat.checkSelfPermission(getContext(),
                Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                    getActivity(),
                    new String[]{Manifest.permission.CALL_PHONE},
                    REQUEST_PHONE_CALL);
            return;
        }
        Intent callIntent = new Intent(Intent.ACTION_CALL);
        callIntent.setData(Uri.parse("tel:"+s));
        startActivity(callIntent);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case REQUEST_PHONE_CALL : {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Intent callIntent = new Intent(Intent.ACTION_CALL);
                    callIntent.setData(Uri.parse("tel:93464494"));
                    startActivity(callIntent);
                }
            }
        }
    }
}
