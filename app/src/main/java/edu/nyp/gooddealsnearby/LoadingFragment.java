package edu.nyp.gooddealsnearby;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TableLayout;

import java.util.ArrayList;


public class LoadingFragment extends Fragment {

    final String TAG = getClass().getSimpleName();
    View mView;
    private ProgressBar pgsBar;




    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mView =  inflater.inflate(R.layout.fragment_loading, null);
        pgsBar = (ProgressBar)mView.findViewById(R.id.pBar);
        pgsBar.setVisibility(mView.VISIBLE);
        return mView;

    }
}
