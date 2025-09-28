package com.example.jaldisih2;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class FhtcTap extends AppCompatActivity {

    private FusedLocationProviderClient fusedLocationClient;
    private DatabaseReference mDatabaseRef;

    private EditText phoneNumberEditText;
    private Button registerButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fhtctap);

        // Initialize Firebase
        mDatabaseRef = FirebaseDatabase.getInstance().getReference("users");

        // Initialize location services
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        // UI Elements
        phoneNumberEditText = findViewById(R.id.phoneNumberEditText);
        registerButton = findViewById(R.id.requestTapButton);

        // Get username from the intent
        Intent intent = getIntent();
        String username = intent.getStringExtra("username");

        // Register button logic
        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String phoneNumber = phoneNumberEditText.getText().toString();

                if (username == null || username.isEmpty() || phoneNumber.isEmpty()) {
                    Toast.makeText(FhtcTap.this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Check location permissions
                if (ActivityCompat.checkSelfPermission(FhtcTap.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                        ActivityCompat.checkSelfPermission(FhtcTap.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(FhtcTap.this,
                            new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 1);
                    return;
                }

                // Fetch user's location
                fusedLocationClient.getLastLocation().addOnSuccessListener(new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        if (location != null) {
                            double latitude = location.getLatitude();
                            double longitude = location.getLongitude();

                            // Create a user object and save to Firebase
                            User user = new User(username, phoneNumber, latitude, longitude, "pending"); // Assuming the default status is "pending"
                            mDatabaseRef.child(username).setValue(user).addOnCompleteListener(task -> {
                                if (task.isSuccessful()) {
                                    Toast.makeText(FhtcTap.this, "User registered successfully!", Toast.LENGTH_SHORT).show();
                                } else {
                                    Toast.makeText(FhtcTap.this, "Failed to register user", Toast.LENGTH_SHORT).show();
                                }
                            });
                        } else {
                            Toast.makeText(FhtcTap.this, "Unable to fetch location", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        });
    }

    // User class
    public static class User {
        public String username;
        public String phoneNumber;
        public double latitude;
        public double longitude;
        public String requestStatus;

        // Constructor
        public User(String username, String phoneNumber, double latitude, double longitude, String requestStatus) {
            this.username = username;
            this.phoneNumber = phoneNumber;
            this.latitude = latitude;
            this.longitude = longitude;
            this.requestStatus = requestStatus; // Added requestStatus
        }
    }
}
