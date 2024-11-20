package com.example.nearbuy;

import android.content.Intent;
import android.media.Image;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class BusinessDetailsFragment extends Fragment {

    private RecyclerView productRecyclerView;
    private ImageView back;

    private ProgressBar progressBar;

    private TextView businessNameTextView;
    private TextView businessAddressTextView;


    private Button addProductButton;
    private List<WishlistModel> productList = new ArrayList<>();
    private WishlistAdapter productAdapter; // Create this adapter for RecyclerView
    private FirebaseFirestore firebaseFirestore;

    private String phone, address;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_business_details, container, false);

        progressBar = view.findViewById(R.id.business_details_progressbar);
        back= view.findViewById(R.id.business_details_back_btn);

        businessNameTextView = view.findViewById(R.id.business_details_name);
        businessAddressTextView = view.findViewById(R.id.business_details_address);

        productRecyclerView = view.findViewById(R.id.product_recycler_view);
        addProductButton = view.findViewById(R.id.add_product_button);
        firebaseFirestore = FirebaseFirestore.getInstance();

        // Setup RecyclerView
        productRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        productAdapter = new WishlistAdapter(productList,true);
        productRecyclerView.setAdapter(productAdapter);

        // Fetch Products
        fetchBusinessDetails();



        // Add Product Button Click
        addProductButton.setOnClickListener(v -> {
            AddProductFragment addProductFragment = new AddProductFragment();

            // Create a Bundle to pass data
            Bundle bundle = new Bundle();
            bundle.putString("address", address); // Replace "key" with your key and "value to pass" with your value
            bundle.putString("phone", phone);
            // Set the bundle as arguments for the fragment
            addProductFragment.setArguments(bundle);

            // Perform the fragment transaction
            FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
            transaction.replace(R.id.register_business_framelayout, addProductFragment);
            transaction.addToBackStack(null); // Add to back stack to navigate back
            transaction.commit();
        });

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent mainIntent = new Intent(getActivity(), MainActivity.class);
                startActivity(mainIntent);
                getActivity().finish();
            }
        });

        return view;
    }

    private void fetchBusinessDetails() {
        progressBar.setVisibility(View.VISIBLE);
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        // Fetch Business Details
        firebaseFirestore.collection("USERS")
                .document(userId)
                .collection("USER_DATA")
                .document("MY_BUSINESS")
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String businessName = documentSnapshot.getString("business_name");
                        address = documentSnapshot.getString("business_address");
                        phone = documentSnapshot.getString("phone");
                        addProductButton.setEnabled(true);
                        addProductButton.setBackgroundColor(getResources().getColor(R.color.colorPrimary));


                        // Display Business Name and Address
                        businessNameTextView.setText(businessName != null ? businessName : "N/A");
                        businessAddressTextView.setText(address != null ? address : "N/A");

//                         Fetch Products
                        fetchProducts(userId);
                    } else {
                        progressBar.setVisibility(View.VISIBLE);
                        Toast.makeText(getContext(), "Business details not found", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(getContext(), "Failed to fetch business details", Toast.LENGTH_SHORT).show());
    }


    private void fetchProducts(String userId) {
        productList.clear();  // Clear the current list

        // Fetch the number of products and the product IDs from the MY_BUSINESS document under the USER_DATA collection
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("USERS").document(userId).collection("USER_DATA").document("MY_BUSINESS")
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        // Get the number of products and the product IDs dynamically
                        int noOfProducts = documentSnapshot.getLong("no_of_products").intValue(); // Get the number of products

                        List<String> productIds = new ArrayList<>();
                        for (int i = 1; i <= noOfProducts; i++) {
                            String productIdField = "product_id_" + i;
                            String productId = documentSnapshot.getString(productIdField);
                            if (productId != null) {
                                productIds.add(productId); // Add each product ID to the list
                            }
                        }

                        // Fetch product details for each product ID
                        if (!productIds.isEmpty()) {
                            fetchProductDetails(productIds);
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.INVISIBLE);
                    Toast.makeText(getContext(), "Error fetching business data", Toast.LENGTH_SHORT).show();
                });
    }

    private void fetchProductDetails(List<String> productIds) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Loop over each productId to search in all categories
        for (String productId : productIds) {
            db.collection("PRODUCTS")
                    .document("aqGT4ZvNfP7CIrME1kPu")  // The document containing subcollections like HOME, ELECTRONICS, etc.
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            // Fetch all subcollections like HOME, ELECTRONICS, FASHION, etc.
                            List<String> subcollections = Arrays.asList("HOME", "ELECTRONICS", "FASHION", "APPLIANCES", "BOOKS", "FURNITURE", "MOBILE","SHOES","SPORTS", "TOYS");  // Add all your subcollections here

                            for (String subcollection : subcollections) {
                                db.collection("PRODUCTS")
                                        .document("aqGT4ZvNfP7CIrME1kPu")
                                        .collection(subcollection)  // Access the subcollection
                                        .document(productId)  // Access the document by productId
                                        .get()
                                        .addOnSuccessListener(productDoc -> {
                                            if (productDoc.exists()) {
                                                // Extract product details from the document
                                                String title = productDoc.getString("product_title");
                                                String description = productDoc.getString("product_subtitle");
                                                String price = productDoc.getString("product_price");
                                                String imageUrl = productDoc.getString("product_image_1");

                                                // Create WishlistModel object and add to the list
                                                WishlistModel product = new WishlistModel(productId, imageUrl, title, description, price, "");
                                                productList.add(product);

                                                // Notify the adapter that data has been updated
                                                productAdapter.notifyDataSetChanged();
                                            }
                                        })
                                        .addOnFailureListener(e -> {
                                            // Handle failure in getting product details from a specific category
                                            Log.e("fetchProductDetails", "Error fetching product from subcollection " + subcollection, e);
                                        });
                            }
                        }
                    })
                    .addOnFailureListener(e -> {
                        // Handle failure in fetching document from PRODUCTS
                        Log.e("fetchProductDetails", "Error fetching PRODUCTS document", e);
                    });
        }

        // Hide the progress bar and show the RecyclerView once data is fetched
        progressBar.setVisibility(View.INVISIBLE);
        productRecyclerView.setVisibility(View.VISIBLE);
    }


}
