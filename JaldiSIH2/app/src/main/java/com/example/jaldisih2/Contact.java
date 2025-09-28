package com.example.jaldisih2;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class Contact extends AppCompatActivity {

    // Declare TextViews and Buttons
    private TextView pumpHouseNameText;
    private TextView phoneText;
    private TextView emailText;
    private TextView timingText;
    private Button phoneButton;
    private Button emailButton;

    // Declare variables for Firebase data
    private String pumpHouseName;
    private String phoneNumber;
    private String email;
    private String timing;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact);

        // Initialize TextViews and Buttons
        pumpHouseNameText = findViewById(R.id.pumpHouseNameText);
        phoneText = findViewById(R.id.phoneText);
        emailText = findViewById(R.id.emailText);
        timingText = findViewById(R.id.timingText);
        phoneButton = findViewById(R.id.phoneButton);
        emailButton = findViewById(R.id.emailButton);

        // Firebase reference
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference contactRef = database.getReference("pump_house_info");

        // Add a listener to retrieve data
        contactRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                // Retrieve data from Firebase and handle null values
                try {
                    pumpHouseName = dataSnapshot.child("name").getValue(String.class);
                    phoneNumber = dataSnapshot.child("phone").getValue(String.class);
                    email = dataSnapshot.child("email").getValue(String.class);
                    timing = dataSnapshot.child("timing").getValue(String.class);

                    Log.d("ContactActivity", "Pump House Name: " + pumpHouseName);
                    Log.d("ContactActivity", "Phone: " + phoneNumber);
                    Log.d("ContactActivity", "Email: " + email);
                    Log.d("ContactActivity", "Timing: " + timing);

                    // Set the TextViews with the retrieved data (checking null values)
                    pumpHouseNameText.setText(pumpHouseName != null ? pumpHouseName : "Not available");
                    phoneText.setText(phoneNumber != null ? phoneNumber : "Not available");
                    emailText.setText(email != null ? email : "Not available");
                    timingText.setText(timing != null ? timing : "Not available");

                } catch (Exception e) {
                    Log.e("ContactActivity", "Error retrieving data", e);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Log the error if any issue occurs
                Log.e("ContactActivity", "Database error: " + databaseError.getMessage());
            }
        });

        // Set the Phone Button Click listener
        phoneButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (phoneNumber != null && !phoneNumber.isEmpty()) {
                    String phone = "tel:" + phoneNumber;
                    Intent intent = new Intent(Intent.ACTION_DIAL, Uri.parse(phone));
                    startActivity(intent);
                } else {
                    Log.e("ContactActivity", "Phone number is not available.");
                }
            }
        });

        // Set the Email Button Click listener
        emailButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (email != null && !email.isEmpty()) {
                    Intent intent = new Intent(Intent.ACTION_SENDTO);
                    intent.setData(Uri.parse("mailto:" + email));
                    startActivity(intent);
                } else {
                    Log.e("ContactActivity", "Email address is not available.");
                }
            }
        });
    }
}