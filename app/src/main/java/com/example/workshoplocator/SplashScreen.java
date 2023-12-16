package com.example.workshoplocator;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.widget.ImageView;

public class SplashScreen extends AppCompatActivity {


    Handler mHandler;
    Runnable mRunnable;

    ImageView im;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);


//        im = findViewById(R.id.imageView3);
//
//        im.setBackground(getDrawable(R.drawable.logo1));


        mHandler = new Handler();
        mRunnable = new Runnable() {
            @Override
            public void run() {
                Intent intent = new Intent(SplashScreen.this,MainActivity.class);
                startActivity(intent);
                finish();
            }
        };

        // its trigger runnable after 4000 millisecond.
        mHandler.postDelayed(mRunnable,1000);



    }
}