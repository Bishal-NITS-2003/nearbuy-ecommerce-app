package com.example.nearbuy;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AddProductFragment extends Fragment {

    private EditText productTitle, productPrice, productViewType, productSubtitle, productCategory, productCOD,  productDescription, imageUrl1, imageUrl2, imageUrl3;
    private ImageView imagePreview1, imagePreview2, imagePreview3;
    private Spinner citySpinner;
    private Button addProductButton;
    private ProgressBar progressBar;
    private FirebaseFirestore firebaseFirestore;
    private FirebaseAuth firebaseAuth;
    private String selectedCity;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_add_product, container, false);

        // Initialize UI elements
        productTitle = view.findViewById(R.id.product_title);
        productPrice = view.findViewById(R.id.product_price);
        productViewType = view.findViewById(R.id.product_view_type);
        productSubtitle = view.findViewById(R.id.product_subtitle);
        productCategory = view.findViewById(R.id.product_category);
        productCOD = view.findViewById(R.id.product_cod);
        productDescription = view.findViewById(R.id.product_description);
        imageUrl1 = view.findViewById(R.id.image_url_1);
        imageUrl2 = view.findViewById(R.id.image_url_2);
        imageUrl3 = view.findViewById(R.id.image_url_3);
        imagePreview1 = view.findViewById(R.id.image_preview_1);
        imagePreview2 = view.findViewById(R.id.image_preview_2);
        imagePreview3 = view.findViewById(R.id.image_preview_3);
        citySpinner = view.findViewById(R.id.city_spinner);
        addProductButton = view.findViewById(R.id.add_product_button);
        progressBar = view.findViewById(R.id.progress_bar);

        // Initialize Firebase
        firebaseFirestore = FirebaseFirestore.getInstance();
        firebaseAuth = FirebaseAuth.getInstance();

        // Setup City Spinner
        setupCitySpinner();

        // Add Product Button Click
        addProductButton.setOnClickListener(v -> addProduct());

        // Image URL TextWatchers for preview
        setupImageUrlPreview(imageUrl1, imagePreview1);
        setupImageUrlPreview(imageUrl2, imagePreview2);
        setupImageUrlPreview(imageUrl3, imagePreview3);

        return view;
    }

    private void setupCitySpinner() {
        // Populate city spinner with a list of cities (could be static or dynamic)
        List<String> cities = Arrays.asList("Mumbai", "Delhi", "Bangalore", "Chennai", "Kolkata","Guwahati");
        ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, cities);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        citySpinner.setAdapter(adapter);
        citySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                selectedCity = cities.get(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {}
        });
    }

    private void setupImageUrlPreview(EditText imageUrlInput, ImageView imagePreview) {
        imageUrlInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence charSequence, int start, int before, int count) {
                String url = charSequence.toString().trim();
                if (!url.isEmpty()) {
                    // Display the image as the user types the URL
                    Glide.with(getContext())
                            .load(url)
                            .into(imagePreview);
                } else {
                    imagePreview.setImageResource(0); // Reset the image preview
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {}
        });
    }

    private void addProduct() {
        String title = productTitle.getText().toString().trim();
        String price = productPrice.getText().toString().trim();
        String viewType = productViewType.getText().toString().trim();
        String subtitle = productSubtitle.getText().toString().trim();
        String selectedCategoryName = productCategory.getText().toString().trim();
        String cod = productCOD.getText().toString().trim();
        String description = productDescription.getText().toString().trim();
        String image1 = imageUrl1.getText().toString().trim();
        String image2 = imageUrl2.getText().toString().trim();
        String image3 = imageUrl3.getText().toString().trim();

        if (title.isEmpty() || price.isEmpty() ||  subtitle.isEmpty() ||description.isEmpty() || image1.isEmpty() || image2.isEmpty() || image3.isEmpty()) {
            Toast.makeText(getContext(), "Please provide all fields and at least 3 images", Toast.LENGTH_SHORT).show();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        String userId = firebaseAuth.getCurrentUser().getUid();

        // Save product to PRODUCTS collection
        String categoryName = selectedCategoryName; // Get selected category name from Spinner
        String cityName = selectedCity; // Get selected city from Spinner
        String productId = firebaseFirestore.collection("PRODUCTS").document().getId(); // Generate random ID for product

        // Prepare product data
        Map<String, Object> productData = new HashMap<>();
        productData.put("product_title", title);
        productData.put("product_price", price);
        productData.put("view_type", viewType);
        productData.put("product_description", description);
        productData.put("product_subtitle", subtitle);
        productData.put("cod", cod);
        productData.put("product_image_1", image1);
        productData.put("product_image_2", image2);
        productData.put("product_image_3", image3);
        productData.put("city", selectedCity);



        firebaseFirestore.collection("PRODUCTS")
                .document("aqGT4ZvNfP7CIrME1kPu") // Use category name as the document ID
                .collection(categoryName) // Sub-collection for products under the category
                .document(productId) // Auto-generated product ID
                .set(productData) // Save product data
                .addOnSuccessListener(aVoid -> {
                    // Access the MY_BUSINESS document to dynamically assign product_id_x
                    DocumentReference myBusinessRef = firebaseFirestore.collection("USERS")
                            .document(userId)
                            .collection("USER_DATA")
                            .document("MY_BUSINESS");

                    myBusinessRef.get().addOnSuccessListener(documentSnapshot -> {
                        Map<String, Object> businessData = new HashMap<>();
                        int currentProductCount = 0;

                        if (documentSnapshot.exists() && documentSnapshot.contains("no_of_products")) {
                            currentProductCount = documentSnapshot.getLong("no_of_products").intValue();
                        }

                        // Increment product count
                        currentProductCount++;
                        businessData.put("no_of_products", currentProductCount); // Update product count
                        businessData.put("product_id_" + currentProductCount, productId); // Save the product ID with the sequential number

                        // Update the MY_BUSINESS document
                        myBusinessRef.update(businessData)
                                .addOnSuccessListener(aVoid1 -> {
                                    progressBar.setVisibility(View.INVISIBLE);
                                    Toast.makeText(getContext(), "Product added successfully", Toast.LENGTH_SHORT).show();
                                    getActivity().getSupportFragmentManager().popBackStack(); // Go back to the previous fragment
                                })
                                .addOnFailureListener(e -> {
                                    progressBar.setVisibility(View.INVISIBLE);
                                    Toast.makeText(getContext(), "Failed to update MY_BUSINESS", Toast.LENGTH_SHORT).show();
                                });
                    }).addOnFailureListener(e -> {
                        progressBar.setVisibility(View.INVISIBLE);
                        Toast.makeText(getContext(), "Failed to access MY_BUSINESS", Toast.LENGTH_SHORT).show();
                    });
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.INVISIBLE);
                    Toast.makeText(getContext(), "Failed to add product", Toast.LENGTH_SHORT).show();
                });

    }
}
