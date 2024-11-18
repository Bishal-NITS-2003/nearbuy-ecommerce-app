package com.example.nearbuy;

import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.text.TextUtils;
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

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.SignInMethodQueryResult;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SignupFragment extends Fragment {

    private TextView alreadyHaveAnAccount;
    private FrameLayout parentFrameLayout;

    private EditText email;
    private EditText phone;
    private EditText fullname;
    private EditText password;
    private EditText confirmPassword;

    private ImageView back;

    private Button signUpBtn;
    private ProgressBar progressBar;

    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore firebaseFirestore;

    private final String emailPattern = "[a-zA-Z0-9._-]+@[a-z]+\\.[a-z]+";

    public SignupFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_signup, container, false);

        parentFrameLayout = requireActivity().findViewById(R.id.register_framelayout);
        alreadyHaveAnAccount = view.findViewById(R.id.tv_already_have_an_account);

        email = view.findViewById(R.id.sign_up_email);
        phone = view.findViewById(R.id.phone);
        fullname = view.findViewById(R.id.sign_up_full_name);
        password = view.findViewById(R.id.sign_up_password);
        confirmPassword = view.findViewById(R.id.sign_up_confirm_password);

        signUpBtn = view.findViewById(R.id.sign_up_button);
        progressBar = view.findViewById(R.id.sign_up_progressbar);

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();

        back = view.findViewById(R.id.sign_up_back_btn);

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        alreadyHaveAnAccount.setOnClickListener(v -> setFragment(new SigninFragment()));
        back.setOnClickListener(v -> setFragment(new SigninFragment()));

        TextWatcher inputWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                checkInputs();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        };

        email.addTextChangedListener(inputWatcher);
        phone.addTextChangedListener(inputWatcher);
        fullname.addTextChangedListener(inputWatcher);
        password.addTextChangedListener(inputWatcher);
        confirmPassword.addTextChangedListener(inputWatcher);

        signUpBtn.setOnClickListener(v -> checkEmailAndPassword());
    }

    private void setFragment(Fragment fragment) {
        FragmentTransaction fragmentTransaction = requireActivity().getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(parentFrameLayout.getId(), fragment);
        fragmentTransaction.setCustomAnimations(R.anim.slide_from_left, R.anim.slideout_from_right);
        fragmentTransaction.commit();
    }

    private void checkInputs() {
        if (!TextUtils.isEmpty(email.getText()) && email.getText().toString().matches(emailPattern)) {
            if (!TextUtils.isEmpty(phone.getText()) && phone.length() == 10) {
                if (!TextUtils.isEmpty(fullname.getText())) {
                    if (!TextUtils.isEmpty(password.getText()) && password.length() >= 8) {
                        if (!TextUtils.isEmpty(confirmPassword.getText())) {
                            signUpBtn.setEnabled(true);
                            signUpBtn.setTextColor(Color.rgb(255, 255, 255));
                            return;
                        }
                    }
                }
            }
        }
        signUpBtn.setEnabled(false);
        signUpBtn.setTextColor(Color.argb(50, 255, 255, 255));
    }

    private void checkEmailAndPassword() {
        if (password.getText().toString().equals(confirmPassword.getText().toString())) {
            progressBar.setVisibility(View.VISIBLE);
            signUpBtn.setEnabled(false);
            signUpBtn.setTextColor(Color.argb(50, 255, 255, 255));

            firebaseAuth.fetchSignInMethodsForEmail(email.getText().toString())
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            List<String> signInMethods = task.getResult().getSignInMethods();
                            if (signInMethods == null || signInMethods.isEmpty()) {
                                createAccount();
                            } else {
                                progressBar.setVisibility(View.INVISIBLE);
                                email.setError("Email already exists!");
                                signUpBtn.setEnabled(true);
                                signUpBtn.setTextColor(Color.rgb(255, 255, 255));
                            }
                        } else {
                            progressBar.setVisibility(View.INVISIBLE);
                            Toast.makeText(getActivity(), "Error: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        } else {
            confirmPassword.setError("Passwords do not match!");
        }
    }

    private void createAccount() {
        firebaseAuth.createUserWithEmailAndPassword(email.getText().toString(), password.getText().toString())
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        saveUserData();
                    } else {
                        progressBar.setVisibility(View.INVISIBLE);
                        Toast.makeText(getActivity(), "Error: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        signUpBtn.setEnabled(true);
                        signUpBtn.setTextColor(Color.rgb(255, 255, 255));
                    }
                });
    }

    private void saveUserData() {
        Map<String, Object> userData = new HashMap<>();
        userData.put("fullname", fullname.getText().toString());
        userData.put("email", email.getText().toString());
        userData.put("phone", phone.getText().toString());
        userData.put("isSeller", false);

        firebaseFirestore.collection("USERS").document(firebaseAuth.getUid())
                .set(userData)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        setupUserCollections();
                    } else {
                        progressBar.setVisibility(View.INVISIBLE);
                        Toast.makeText(getActivity(), "Error saving data: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void setupUserCollections() {
        CollectionReference userDataReference = firebaseFirestore.collection("USERS")
                .document(firebaseAuth.getUid()).collection("USER_DATA");

        String[] documentNames = {"MY_WISHLIST", "MY_REWARDS", "MY_BUSINESS"};
        Map<String, Object> fields = new HashMap<>();
        fields.put("list_size", 0);

        for (String documentName : documentNames) {
            userDataReference.document(documentName).set(fields)
                    .addOnCompleteListener(task -> {
                        if (!task.isSuccessful()) {
                            progressBar.setVisibility(View.INVISIBLE);
                            Toast.makeText(getActivity(), "Error setting up collections", Toast.LENGTH_SHORT).show();
                        }else{
                            Intent mainIntent = new Intent(getActivity(), MainActivity.class);
                            startActivity(mainIntent);
                            getActivity().finish();
                        }
                    });
        }
    }
}
