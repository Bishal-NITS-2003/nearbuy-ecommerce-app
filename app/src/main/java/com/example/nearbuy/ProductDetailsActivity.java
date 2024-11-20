package com.example.nearbuy;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.viewpager.widget.ViewPager;

import android.app.Dialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ProductDetailsActivity extends AppCompatActivity {

    private TextView productTitle, productPrice, productDescription, productDetails, tvcodIndicator, productAddress;

    private FirebaseFirestore firebaseFirestore;
    private FirebaseAuth firebaseAuth;

    private FirebaseUser currentUser;

    private DocumentSnapshot documentSnapshot;
    private ViewPager productImagesViewPager;
    private TabLayout viewpagerIndicator;

    private Dialog loadingDialog;

    public static boolean running_wishlist_querry=false;

    public static String productID;


    public static boolean ALREADY_ADDED_TO_WISHLIST = false;
    public static FloatingActionButton addToWishListBtn;

    private Button callBtn, whatsappBtn;

    private String phone, address, message;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_details);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            // Set the status bar color
            window.setStatusBarColor(getResources().getColor(R.color.colorPrimary));  // Replace with your color resource
        }

        // Initialize FirebaseAuth
        firebaseAuth = FirebaseAuth.getInstance();

        // Get the current user
        currentUser = firebaseAuth.getCurrentUser();



        productImagesViewPager = findViewById(R.id.product_images_view_pager);
        viewpagerIndicator = findViewById(R.id.view_pager_indicator);
        addToWishListBtn = findViewById(R.id.add_to_wishlist_button);
        callBtn = findViewById(R.id.call_btn);
        whatsappBtn = findViewById(R.id.whatsapp_btn);

        productTitle = findViewById(R.id.product_title);
        productAddress= findViewById(R.id.product_address);
        productPrice = findViewById(R.id.product_price);
        productDescription = findViewById(R.id.product_description);
        productDetails = findViewById(R.id.productDetails);

        tvcodIndicator = findViewById(R.id.tv_cod_indicator);
        firebaseFirestore = FirebaseFirestore.getInstance();

        //////////loading dialog

        loadingDialog = new Dialog(ProductDetailsActivity.this);
        loadingDialog.setContentView(R.layout.loading_progress_dialog);
        loadingDialog.setCancelable(false);
        loadingDialog.getWindow().setBackgroundDrawable(getDrawable(R.drawable.slider_background));
        loadingDialog.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        loadingDialog.show();

        //////////loading dialog

        final List<String> productImages = new ArrayList<>();


        productID = getIntent().getStringExtra("PRODUCT_ID");

        String[] collectionNames = {"HOME", "ELECTRONICS", "FASHION", "APPLIANCES", "BOOKS", "FURNITURE", "MOBILE","SHOES","SPORTS", "TOYS"};

        final boolean[] productFound = {false}; // Using an array as a mutable flag

        for (String collectionName : collectionNames) {
            if (productFound[0]) break; // Exit loop if product is found

            firebaseFirestore.collection("PRODUCTS").document("aqGT4ZvNfP7CIrME1kPu")
                    .collection(collectionName)
                    .document(productID)
                    .get()
                    .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentSnapshot> docTask) {
                            if (docTask.isSuccessful()) {
                                DocumentSnapshot productDoc = docTask.getResult();
                                if (productDoc.exists()) {
                                    productFound[0] = true; // Set flag to true when product is found

                                    // Populate UI with product details
                                    for (int x = 1; x <= 3; x++) {
                                        productImages.add(productDoc.get("product_image_" + x).toString());
                                    }
                                    ProductImagesAdapter productImagesAdapter = new ProductImagesAdapter(productImages);
                                    productImagesViewPager.setAdapter(productImagesAdapter);

                                    productTitle.setText(productDoc.get("product_title").toString());
                                    productPrice.setText("Rs." + productDoc.get("product_price").toString() + "/-");
                                    productDescription.setText(productDoc.get("product_subtitle").toString());
                                    productDetails.setText(productDoc.get("product_description").toString());
                                    tvcodIndicator.setText(productDoc.get("cod").toString());
                                    productAddress.setText(productDoc.get("address").toString());
                                    address= productDoc.get("address").toString();
                                    phone = productDoc.get("phone").toString();
                                    message= "Hello!\nI came across your product: "+productDoc.get("product_title").toString()+" on NearBuy.\nIs it available now?";


                                    // Wishlist handling
                                    if (currentUser != null) {
                                        if (DBqueries.wishList.size() == 0) {
                                            DBqueries.loadWishlist(ProductDetailsActivity.this, loadingDialog, false);
                                        } else {
                                            loadingDialog.dismiss();
                                        }
                                        if (DBqueries.wishList.contains(productID)) {
                                            ALREADY_ADDED_TO_WISHLIST = true;
                                            addToWishListBtn.setSupportImageTintList(getResources().getColorStateList(R.color.colorPrimary));
                                        } else {
                                            addToWishListBtn.setSupportImageTintList(ColorStateList.valueOf(Color.parseColor("#9e9e9e")));
                                            ALREADY_ADDED_TO_WISHLIST = false;
                                        }
                                    }
                                    loadingDialog.dismiss();
                                }
                            } else {
                                Toast.makeText(ProductDetailsActivity.this, "Error loading product.", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        }



viewpagerIndicator.setupWithViewPager(productImagesViewPager, true);


        whatsappBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                try {
                    // URL format for WhatsApp API
                    String url = "https://api.whatsapp.com/send?phone=91" + phone
                            + "&text=" + URLEncoder.encode(message, "UTF-8");

                    // Create intent
                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    intent.setData(Uri.parse(url));

                    // Check if WhatsApp is installed
                    startActivity(intent);

                } catch (Exception e) {
                    e.printStackTrace();
                    Toast.makeText(getApplicationContext(), "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                }
            }
        });


        callBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_DIAL);
                intent.setData(Uri.parse("tel:+91"+phone));
                startActivity(intent);
            }
        });






        addToWishListBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (currentUser == null) {
                    Intent mainIntent = new Intent(ProductDetailsActivity.this, RegisterActivity.class);
                    startActivity(mainIntent);
                    finish();
                } else {
                    if (!running_wishlist_querry) {
                        running_wishlist_querry = true;
                        if (ALREADY_ADDED_TO_WISHLIST) {
                            int index = DBqueries.wishList.indexOf(productID);
                            DBqueries.removeFromWishlist(index, ProductDetailsActivity.this);
                            ALREADY_ADDED_TO_WISHLIST = false;
                            addToWishListBtn.setSupportImageTintList(ColorStateList.valueOf(Color.parseColor("#9e9e9e")));
                        } else {
                            addToWishListBtn.setSupportImageTintList(getResources().getColorStateList(R.color.colorPrimary));

                            Map<String, Object> addProduct = new HashMap<>();
                            addProduct.put("product_ID_" + String.valueOf(DBqueries.wishList.size()), productID);
                            addProduct.put("list_size", (long) (DBqueries.wishList.size() + 1));

                            firebaseFirestore.collection("USERS").document(currentUser.getUid())
                                    .collection("USER_DATA").document("MY_WISHLIST")
                                    .update(addProduct)
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {

                                                if (DBqueries.wishList.size() == 0) {
                                                    DBqueries.wishlistModelList.add(new WishlistModel(
                                                            productID
                                                            ,documentSnapshot.get("product_image_1").toString()
                                                            , documentSnapshot.get("product_title").toString()
                                                            , documentSnapshot.get("product_description").toString()
                                                            , documentSnapshot.get("product_price").toString()
                                                            , ""
                                                    ));
                                                }

                                                ALREADY_ADDED_TO_WISHLIST = true;
                                                addToWishListBtn.setSupportImageTintList(getResources().getColorStateList(R.color.colorPrimary));
                                                DBqueries.wishList.add(productID);
                                                Toast.makeText(ProductDetailsActivity.this, "Added to Wishlist Successfully!", Toast.LENGTH_SHORT).show();

                                            } else {
                                                addToWishListBtn.setSupportImageTintList(ColorStateList.valueOf(Color.parseColor("#9e9e9e")));
                                                String err = task.getException().getMessage();
                                                Toast.makeText(ProductDetailsActivity.this, err, Toast.LENGTH_SHORT).show();
                                            }
                                            running_wishlist_querry = false;
                                        }
                                    });
                        }
                    }
                }

            }
        });
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.search_and_cart_icon, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        int id = item.getItemId();

        if (id == android.R.id.home){
            finish();
            return true;
        }else if (id == R.id.main_search_icon){
            //todo:search
            return true;

        }else if (id == R.id.main_cart_icon) {
            //todo:notification
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

}