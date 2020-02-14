package com.example.a5einitiatetracker;

import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class SplashActivity extends AppCompatActivity {

    private Handler myHandler = new Handler();
    final Runnable r = new Runnable() {
        @Override
        public void run() {
            gotoMainActivity();
        }
    };
    private boolean isConnected = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        myHandler.postDelayed(r, 500);
        if (connected()) gotoMainActivity();
        else closeApp();
    }

    private void gotoMainActivity(){
        Intent intent = new Intent(getBaseContext(), MainActivity.class);
        startActivity(intent);
        finish();
    }

    private boolean connected(){
        ConnectivityManager connectivityManager = (ConnectivityManager)getSystemService(CONNECTIVITY_SERVICE);
        Network[] networks = connectivityManager.getAllNetworks();
        NetworkInfo networkInfo;
        for (Network mNetwork : networks){
            networkInfo = connectivityManager.getNetworkInfo(mNetwork);
            if ((networkInfo.getState().equals(NetworkInfo.State.CONNECTED))) {
                Toast.makeText(SplashActivity.this,"@string/connected", Toast.LENGTH_LONG);
                isConnected = true;
            }
        }
        return isConnected;
    }

    private void closeApp(){}
}
