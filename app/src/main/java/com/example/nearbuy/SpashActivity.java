package com.example.nearbuy;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.SystemClock;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class SpashActivity extends AppCompatActivity {

    private FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_spash);

        firebaseAuth = FirebaseAuth.getInstance();

        SystemClock.sleep(3000);


    }

    @Override
    protected void onStart() {
        super.onStart();

        FirebaseUser currentUser = firebaseAuth.getCurrentUser();

        if (currentUser ==null){
            Intent registerIntent = new Intent( SpashActivity.this,RegisterActivity.class);
            startActivity(registerIntent);
            finish();

        }else{
            Intent mainIntent = new Intent( SpashActivity.this,MainActivity.class);
            startActivity(mainIntent);
            finish();


        }
    }
}