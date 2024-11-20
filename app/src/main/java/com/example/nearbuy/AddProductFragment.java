package com.example.nearbuy;

import android.os.Bundle;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.Toolbar;
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
import android.widget.TextView;
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

    private EditText productTitle, productPrice, productViewType, productSubtitle,  productDescription;

    private String imageUrl1, imageUrl2, imageUrl3;
    private ImageView imagePreview1, imagePreview2, imagePreview3;
    private Spinner citySpinner, categorySpinner, productCODSpinner;
    private Button addProductButton;
    private ProgressBar progressBar;
    private FirebaseFirestore firebaseFirestore;
    private FirebaseAuth firebaseAuth;
    private String selectedCity, selectedCategory, selectedCOD;

    private String address, phone;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_add_product, container, false);


        Toolbar toolbar = view.findViewById(R.id.toolbar);

        // Set toolbar title
        toolbar.setTitle("ADD Product");

        // Handle back button functionality
        toolbar.setNavigationIcon(R.drawable.back_icon); // Use your back arrow icon drawable
        toolbar.setNavigationOnClickListener(v -> requireActivity().getOnBackPressedDispatcher().onBackPressed());

        // Get the arguments bundle
        if (getArguments() != null) {
            address= getArguments().getString("address");
            phone= getArguments().getString("phone");// Replace "key" with the same key used when passing
            // Use the value as needed
        }


        // Initialize UI elements
        productTitle = view.findViewById(R.id.product_title);
        productPrice = view.findViewById(R.id.product_price);
        productViewType = view.findViewById(R.id.product_view_type);
        productSubtitle = view.findViewById(R.id.product_subtitle);
        categorySpinner = view.findViewById(R.id.product_category);
        productCODSpinner = view.findViewById(R.id.product_cod);
        productDescription = view.findViewById(R.id.product_description);
        imagePreview1 = view.findViewById(R.id.image_preview_1);
        imagePreview2 = view.findViewById(R.id.image_preview_2);
        imagePreview3 = view.findViewById(R.id.image_preview_3);
        citySpinner = view.findViewById(R.id.city_spinner);
        addProductButton = view.findViewById(R.id.add_product_button);
        progressBar = view.findViewById(R.id.progress_bar);
        imagePreview1.setOnClickListener(view1 -> showImageDialog(view1));
        imagePreview2.setOnClickListener(view2 -> showImageDialog(view2));
        imagePreview3.setOnClickListener(view3 -> showImageDialog(view3));


        productViewType.setOnClickListener(v -> {
            // Create the BottomSheetDialogFragment and set the listener
            ViewTypeBottomSheetFragment fragment = new ViewTypeBottomSheetFragment();
            fragment.setOnViewTypeSelectedListener(viewType -> {
                // Update the TextView with the selected view type
                productViewType.setText(viewType);
            });

            // Show the BottomSheetDialogFragment using getChildFragmentManager()
            fragment.show(getChildFragmentManager(), fragment.getTag());
        });





        // Initialize Firebase
        firebaseFirestore = FirebaseFirestore.getInstance();
        firebaseAuth = FirebaseAuth.getInstance();

        // Setup City Spinner
        setupCitySpinner();
        setupCategorySpinner();
        setupCODSpinner();

        // Add Product Button Click
        addProductButton.setOnClickListener(v -> addProduct());


        return view;
    }

    // Declare the three strings to store the image URLs


    public void showImageDialog(View view) {
        // Open dialog based on the clicked image view
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Enter Image URL");

        // Input field for the image URL
        final EditText input = new EditText(getContext());
        input.setHint("Enter image URL");
        builder.setView(input);

        builder.setPositiveButton("OK", (dialog, which) -> {
            String imageUrl = input.getText().toString();
            if (!imageUrl.isEmpty()) {
                // Load image into ImageView and store the URL in the respective string
                ImageView imageView = (ImageView) view; // The clicked ImageView
                loadImageFromUrl(imageUrl, imageView);

                // Identify which image was clicked and store the URL accordingly
                if (view.getId() == R.id.image_preview_1) {
                    imageUrl1 = imageUrl; // Store the URL for the first image
                } else if (view.getId() == R.id.image_preview_2) {
                    imageUrl2 = imageUrl; // Store the URL for the second image
                } else if (view.getId() == R.id.image_preview_3) {
                    imageUrl3 = imageUrl; // Store the URL for the third image
                }
            }
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());
        builder.create().show();
    }

    // Method to load the image from the URL
    private void loadImageFromUrl(String imageUrl, ImageView imageView) {
        Glide.with(getContext())
                .load(imageUrl)
                .placeholder(R.drawable.placeholder) // Show a placeholder while loading
                .into(imageView);
    }



    private void setupCitySpinner() {
        // Populate city spinner with a list of cities (could be static or dynamic)
        List<String> cities = Arrays.asList("Guwahati", "Mumbai", "Delhi", "Bangalore", "Chennai", "Kolkata");
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

    private void setupCategorySpinner() {
        // Populate city spinner with a list of cities (could be static or dynamic)
        List<String> categories = Arrays.asList("HOME", "ELECTRONICS", "FASHION", "APPLIANCES", "BOOKS", "FURNITURE", "MOBILE","SHOES","SPORTS", "TOYS");
        ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, categories);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        categorySpinner.setAdapter(adapter);
        categorySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                selectedCategory = categories.get(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {}
        });
    }

    private void setupCODSpinner() {
        // Populate city spinner with a list of cities (could be static or dynamic)
        List<String> codList = Arrays.asList("COD Available", "COD Not Available");
        ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, codList);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        productCODSpinner.setAdapter(adapter);
        productCODSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                selectedCOD = codList.get(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {}
        });
    }


    private void addProduct() {
        String title = productTitle.getText().toString().trim();
        String price = productPrice.getText().toString().trim();
        Long viewType;
        if (productViewType.getText().toString().trim().equals("Horizontal Product View")){
            viewType = 2L;
        }else {
            viewType = 3L;
        }

        String subtitle = productSubtitle.getText().toString().trim();
        String description = productDescription.getText().toString().trim();

        if (title.isEmpty() || price.isEmpty() ||  subtitle.isEmpty() ||description.isEmpty()) {
            Toast.makeText(getContext(), "Please provide all fields and at least 3 images", Toast.LENGTH_SHORT).show();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        String userId = firebaseAuth.getCurrentUser().getUid();

        // Save product to PRODUCTS collection
        String categoryName = selectedCategory; // Get selected category name from Spinner
        String cityName = selectedCity; // Get selected city from Spinner
        String productId = firebaseFirestore.collection("PRODUCTS").document().getId(); // Generate random ID for product

        // Prepare product data
        Map<String, Object> productData = new HashMap<>();
        productData.put("product_title", title);
        productData.put("product_price", price);
        productData.put("view_type", viewType);
        productData.put("product_description", description);
        productData.put("product_subtitle", subtitle);
        productData.put("cod", selectedCOD);
        productData.put("product_image_1", imageUrl1);
        productData.put("product_image_2", imageUrl2);
        productData.put("product_image_3", imageUrl3);
        productData.put("city", selectedCity);
        productData.put("phone", phone);
        productData.put("address", address);



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
