package com.example.nearbuy;

import android.os.Bundle;
import android.view.Window;
import android.widget.FrameLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class BecomeSellerActivity extends AppCompatActivity {

    private FrameLayout frameLayout;
    private Window window;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_become_seller);

        frameLayout = findViewById(R.id.register_business_framelayout);

        window = this.getWindow();
        window.setStatusBarColor(this.getResources().getColor(R.color.colorPrimary, this.getTheme()));

        // Check if the user is already a seller
        checkSellerStatusAndSetFragment();
    }

    private void checkSellerStatusAndSetFragment() {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid(); // Get current user ID
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("USERS").document(userId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        DocumentSnapshot document = task.getResult();
                        boolean isSeller = document.getBoolean("isSeller") != null && document.getBoolean("isSeller");

                        // Set the appropriate fragment based on seller status
                        if (isSeller) {
                            setDefaultFragment(new BusinessDetailsFragment());
                        } else {
                            setDefaultFragment(new BusinessRegistrationFragment());
                        }
                    } else {
                        // Default to Business Registration Fragment in case of failure
                        setDefaultFragment(new BusinessRegistrationFragment());
                    }
                })
                .addOnFailureListener(e -> {
                    // Handle any errors (optional)
                    setDefaultFragment(new BusinessRegistrationFragment());
                });
    }

    private void setDefaultFragment(Fragment fragment) {
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(frameLayout.getId(), fragment);
        fragmentTransaction.commit();
    }
}
