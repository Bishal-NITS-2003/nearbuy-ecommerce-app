package com.example.nearbuy;

import android.content.Intent;
import android.media.Image;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

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
            FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
            transaction.replace(R.id.register_business_framelayout, new AddProductFragment());
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
                        String address = documentSnapshot.getString("business_address");

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
        productList.clear();
        productList.add(new WishlistModel("201","https://www.kurin.com/wp-content/uploads/placeholder-square.jpg","vccsbcs","hbjhbjb", "650","https://www.kurin.com/wp-content/uploads/placeholder-square.jpg"));
        productList.add(new WishlistModel("201","https://www.kurin.com/wp-content/uploads/placeholder-square.jpg","vccsbcs","hbjhbjb", "650","https://www.kurin.com/wp-content/uploads/placeholder-square.jpg"));
        productList.add(new WishlistModel("201","https://www.kurin.com/wp-content/uploads/placeholder-square.jpg","vccsbcs","hbjhbjb", "650","https://www.kurin.com/wp-content/uploads/placeholder-square.jpg"));
        productList.add(new WishlistModel("201","https://www.kurin.com/wp-content/uploads/placeholder-square.jpg","vccsbcs","hbjhbjb", "650","https://www.kurin.com/wp-content/uploads/placeholder-square.jpg"));
        productList.add(new WishlistModel("201","https://www.kurin.com/wp-content/uploads/placeholder-square.jpg","vccsbcs","hbjhbjb", "650","https://www.kurin.com/wp-content/uploads/placeholder-square.jpg"));
        productList.add(new WishlistModel("201","https://www.kurin.com/wp-content/uploads/placeholder-square.jpg","vccsbcs","hbjhbjb", "650","https://www.kurin.com/wp-content/uploads/placeholder-square.jpg"));
        productList.add(new WishlistModel("201","https://www.kurin.com/wp-content/uploads/placeholder-square.jpg","vccsbcs","hbjhbjb", "650","https://www.kurin.com/wp-content/uploads/placeholder-square.jpg"));
        productList.add(new WishlistModel("201","https://www.kurin.com/wp-content/uploads/placeholder-square.jpg","vccsbcs","hbjhbjb", "650","https://www.kurin.com/wp-content/uploads/placeholder-square.jpg"));
        progressBar.setVisibility(View.INVISIBLE);
        productAdapter.notifyDataSetChanged();
        productRecyclerView.setVisibility(View.VISIBLE);


//        firebaseFirestore.collection("USERS")
//                .document(userId)
//                .collection("USER_DATA")
//                .document("MY_BUSINESS")
//                .collection("PRODUCTS")
//                .get()
//                .addOnSuccessListener(queryDocumentSnapshots -> {
//                    productList.clear();
//                    productList.addAll(queryDocumentSnapshots.toObjects(ProductModel.class));
//                    productAdapter.notifyDataSetChanged();
//                })
//                .addOnFailureListener(e -> Toast.makeText(getContext(), "Failed to fetch products", Toast.LENGTH_SHORT).show());

}
}
