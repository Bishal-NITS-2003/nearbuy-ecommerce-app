package com.example.nearbuy;

import android.os.Bundle;
import android.view.KeyEvent;
import android.view.Window;
import android.widget.FrameLayout;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

public class RegisterActivity extends AppCompatActivity {

    private FrameLayout frameLayout;
    public static  boolean onResetPasswordFragment = false;
    public static boolean onOTPFragment = false;

    Window window;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);


        frameLayout = findViewById(R.id.register_framelayout);
        setDefaultFragment(new SigninFragment());

        window = this.getWindow();
        window.setStatusBarColor(this.getResources().getColor(R.color.colorPrimary,this.getTheme()));
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK){
            if (onResetPasswordFragment) {
                onResetPasswordFragment = false;
                setFragment(new SigninFragment());

                return  false;

            }
            if (onOTPFragment){
                onOTPFragment = false;
                setFragment(new SignupFragment());

                return false;
            }


        }


        return super.onKeyDown(keyCode, event);
    }

    private void setDefaultFragment(Fragment fragment) {
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(frameLayout.getId(),fragment) ;
        fragmentTransaction.commit();
    }

    private void setFragment(Fragment fragment) {
        FragmentTransaction fragmentTransaction =getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(frameLayout.getId(),fragment);
        fragmentTransaction.setCustomAnimations(android.R.anim.slide_in_left,android.R.anim.slide_out_right);
        fragmentTransaction.commit();
    }
}