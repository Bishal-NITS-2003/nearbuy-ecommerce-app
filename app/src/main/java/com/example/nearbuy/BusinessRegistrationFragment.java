package com.example.nearbuy;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;


public class BusinessRegistrationFragment extends Fragment {


    private FrameLayout parentFrameLayout;

    private  String userId;

    private EditText businessName, businessPhone;
    private EditText businessAddress;


    private ImageView back;

    private Button registerBusinessBtn;
    private ProgressBar progressBar;

    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore firebaseFirestore;

    public BusinessRegistrationFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_business_registration, container, false);

        parentFrameLayout = requireActivity().findViewById(R.id.register_business_framelayout);


        businessName = view.findViewById(R.id.business_name);
        businessPhone = view.findViewById(R.id.business_phone);
        businessAddress = view.findViewById(R.id.business_address);

        registerBusinessBtn = view.findViewById(R.id.become_seller_button);
        progressBar = view.findViewById(R.id.business_registration_progressbar);

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();
        userId = firebaseAuth.getUid();

        back = view.findViewById(R.id.business_registration_back_btn);

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);


        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent mainIntent = new Intent(getActivity(), MainActivity.class);
                startActivity(mainIntent);
                getActivity().finish();
            }
        });



        registerBusinessBtn.setOnClickListener(v -> saveBusinessDetails());
    }


    private void saveBusinessDetails() {
        progressBar.setVisibility(View.VISIBLE);
        String name = businessName.getText().toString().trim();
        String phone = businessPhone.getText().toString().trim();
        String address = businessAddress.getText().toString().trim();

        if (name.isEmpty() || address.isEmpty()) {
            Toast.makeText(getContext(), "Please fill all fields", Toast.LENGTH_SHORT).show();
            progressBar.setVisibility(View.INVISIBLE);
            return;
        }

        // Update Firestore
        long noOfPrpducts= 0L;
        Map<String, Object> businessDetails = new HashMap<>();
        businessDetails.put("business_name", name);
        businessDetails.put("business_address", address);
        businessDetails.put("no_of_products", noOfPrpducts);
        businessDetails.put("phone", phone);

        // Update `USERS` document
        firebaseFirestore.collection("USERS").document(userId)
                .update("isSeller", true)
                .addOnSuccessListener(unused -> {
                    // Add `MY_BUSINESS` document under `USER_DATA`
                    firebaseFirestore.collection("USERS")
                            .document(userId)
                            .collection("USER_DATA")
                            .document("MY_BUSINESS")
                            .set(businessDetails)
                            .addOnSuccessListener(unused2 -> Toast.makeText(getContext(), "Business Registered!", Toast.LENGTH_SHORT).show())
                            .addOnFailureListener(e -> Toast.makeText(getContext(), "Failed to save business details", Toast.LENGTH_SHORT).show());
                    progressBar.setVisibility(View.INVISIBLE);
                    setFragment(new BusinessDetailsFragment());
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Failed to update user info", Toast.LENGTH_SHORT).show();
                    progressBar.setVisibility(View.INVISIBLE);
                });
    }


    private void setFragment(Fragment fragment) {
        FragmentTransaction fragmentTransaction = requireActivity().getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(parentFrameLayout.getId(), fragment);
        fragmentTransaction.setCustomAnimations(R.anim.slide_from_left, R.anim.slideout_from_right);
        fragmentTransaction.commit();
    }




}