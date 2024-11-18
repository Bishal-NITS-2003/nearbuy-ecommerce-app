package com.example.nearbuy;


import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.Menu;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.core.view.GravityCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;


public class MainActivity extends AppCompatActivity {

    private static final int HOME_FRAGMENT = 0;
    private static final int WISHLIST_FRAGMENT = 1;
    private static final int ACCOUNT_FRAGMENT = 2;

    private FrameLayout frameLayout;

    private TextView fullname, email;
    private ImageView actionBarLogo;
    private int currentFragment = -1;
    private NavigationView navigationView;
    private FirebaseUser currentUser;

    public static DrawerLayout drawer;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        actionBarLogo = findViewById(R.id.action_bar_logo);
        setSupportActionBar(toolbar);
        toolbar.setTitleTextColor(getResources().getColor(R.color.white));

        navigationView = findViewById(R.id.nav_view);

        fullname=navigationView.getHeaderView(0).findViewById(R.id.main_fullname);
        email=navigationView.getHeaderView(0).findViewById(R.id.main_email);




        drawer =findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this,drawer,toolbar,R.string.navigation_drawer_open,R.string.navigation_drawer_close);
        toggle.getDrawerArrowDrawable().setColor(getResources().getColor(R.color.black));
        drawer.addDrawerListener(toggle);
        toggle.syncState();



        navigationView.getMenu().getItem(1).setChecked(true);

        frameLayout = findViewById(R.id.main_frame_layout);



        setFragment(new HomeFragment(), HOME_FRAGMENT);


        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            MenuItem menuItem;
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                drawer.closeDrawers();
                menuItem=item;
                if(currentUser != null) {
                    drawer.addDrawerListener(new DrawerLayout.SimpleDrawerListener() {
                        @Override
                        public void onDrawerClosed(View drawerView) {
                            super.onDrawerClosed(drawerView);
                            int id = menuItem.getItemId();
                            if (id == R.id.nav_my_home) {
                                actionBarLogo.setVisibility(View.VISIBLE);
                                invalidateOptionsMenu();
                                setFragment(new HomeFragment(), HOME_FRAGMENT);
                                //navigationView.getMenu().getItem(0).setChecked(true);
                            } else if (id == R.id.nav_my_reward) {
//                                gotoFragment("My Rewards", new MyRewardsFragment(), REWARDS_FRAGMENT);
                            } else if (id == R.id.nav_my_wishlist) {
//                                gotoFragment("My Wishlist", new MyWishlistFragment(), WISHLIST_FRAGMENT);
                            } else if (id == R.id.nav_my_account) {
//                                gotoFragment("My Account", new MyAccountFragment(), MYACCOUNT_FRAGMENT);
                            } else if (id == R.id.nav_my_seller) {
                                FirebaseFirestore db = FirebaseFirestore.getInstance();
                                String userId = FirebaseAuth.getInstance().getCurrentUser().getUid(); // Ensure the user is authenticated

                                db.collection("USERS").document(userId)
                                        .get()
                                        .addOnCompleteListener(task -> {
                                            if (task.isSuccessful()) {
                                                DocumentSnapshot document = task.getResult();
                                                if (document != null && document.exists()) {
                                                    boolean isSeller = document.getBoolean("isSeller") != null && document.getBoolean("isSeller");

                                                    // Dynamically update the navigation menu title
                                                    MenuItem menuItem = navigationView.getMenu().findItem(R.id.nav_my_seller);
                                                    if (isSeller) {
                                                        menuItem.setTitle("My Business");
                                                    } else {
                                                        menuItem.setTitle("Become a Seller");
                                                    }

                                                    // Start the activity
                                                    Intent intent = new Intent(MainActivity.this, BecomeSellerActivity.class);
                                                    startActivity(intent);
                                                } else {
                                                    Toast.makeText(MainActivity.this, "User data not found!", Toast.LENGTH_SHORT).show();
                                                }
                                            } else {
                                                Toast.makeText(MainActivity.this, "Failed to fetch data: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                            }
                                        });
                            } else if (id == R.id.nav_sign_out) {
                                FirebaseAuth.getInstance().signOut();
                                DBqueries.clearData();
                                DBqueries.email=null;  //my code
                                startActivity(new Intent(MainActivity.this,RegisterActivity.class));
                                finish();
                            }
                            drawer.removeDrawerListener(this);
                        }
                    });
                    return true;
                }
                else {
                    Intent mainintent = new Intent(MainActivity.this, RegisterActivity.class);
                    startActivity(mainintent);
                    finish();
                    return false;
                }

            }
        });



    }


    @Override
    protected void onStart() {
        super.onStart();
        currentUser= FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null){
            navigationView.getMenu().getItem(navigationView.getMenu().size() -1).setEnabled(false);
        }else{
            FirebaseFirestore.getInstance().collection("USERS").document(currentUser.getUid())
                    .get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                            if (task.isSuccessful()){

                                DBqueries.fullname = task.getResult().getString("fullname");
                                DBqueries.email = task.getResult().getString("email");

                                fullname.setText(DBqueries.fullname);
                                email.setText(DBqueries.email);


                            }else{
                                String error = task.getException().getMessage();
                                Toast.makeText(MainActivity.this, error, Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
            navigationView.getMenu().getItem(navigationView.getMenu().size() -1).setEnabled(true);
        }
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer =(DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)){
            drawer.closeDrawer(GravityCompat.START);
        }else {
            if (currentFragment == HOME_FRAGMENT) {
                currentFragment = -1;
                super.onBackPressed();
            }else {
                actionBarLogo.setVisibility(View.VISIBLE);
                invalidateOptionsMenu();
                setFragment(new HomeFragment(),HOME_FRAGMENT);
                navigationView.getMenu().getItem(1).setChecked(true);
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        if (currentFragment == HOME_FRAGMENT) {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
            getMenuInflater().inflate(R.menu.main, menu);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        int id = item.getItemId();

        if (id == R.id.main_search_icon){
            //todo:search
            return true;
        }else if (id == R.id.main_notification_icon){
            //todo:notification
            return true;

        }else if (id == R.id.main_cart_icon) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void gotoFragment(String title,Fragment fragment,int fragmentNo){
        actionBarLogo.setVisibility(View.GONE);
        getSupportActionBar().setDisplayShowTitleEnabled(true);
        getSupportActionBar().setTitle(title);
        invalidateOptionsMenu();
        setFragment(fragment,fragmentNo);
    }



    private void setFragment(Fragment fragment, int fragmentNo){
        if (fragmentNo != currentFragment) {
            currentFragment = fragmentNo;
            FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
            fragmentTransaction.setCustomAnimations(R.anim.fade_in,R.anim.fade_out);
            fragmentTransaction.replace(frameLayout.getId(), fragment);
            fragmentTransaction.commit();
        }
    }

}