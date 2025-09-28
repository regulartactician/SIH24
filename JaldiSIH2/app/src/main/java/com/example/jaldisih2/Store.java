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

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

import org.w3c.dom.Text;

public class Store extends AppCompatActivity {
    private FirebaseDatabase mDatabase;
    private DatabaseReference mDatabaseRef;
    private FusedLocationProviderClient fusedLocationClient;
    private TextView storenameText, ownernameText, addressText, phoneText, emailText;
    private Button phoneButton, emailButton, previousButton, nextButton;
    private ArrayList<String[]> storeDetails;
    private int index = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_store);

        // Firebase setup
        mDatabase = FirebaseDatabase.getInstance();
        mDatabaseRef = mDatabase.getReference("store_details");
        storeDetails = new ArrayList<>();

        // Initialize Views
        storenameText = findViewById(R.id.storenametext);
        ownernameText = findViewById(R.id.ownernametext);
        addressText = findViewById(R.id.addresstext);
        phoneText = findViewById(R.id.phonetext);
        emailText = findViewById(R.id.emailtext);
        phoneButton = findViewById(R.id.phonebutton);
        emailButton = findViewById(R.id.emailbutton);
        previousButton = findViewById(R.id.previousbutton);
        nextButton = findViewById(R.id.nextbutton);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        // Load store data from Firebase
        loadDataFromFirebase();

        // Set up button click listeners
        previousButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                showPreviousStore();
            }
        });
        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                showNextStore();
            }
        });

        phoneButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                    String phone = "tel:" + phoneText.getText();
                    Intent intent = new Intent(Intent.ACTION_DIAL, Uri.parse(phone));
                    startActivity(intent);
            }
        });

        // Set the Email Button Click listener
        emailButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                    Intent intent = new Intent(Intent.ACTION_SENDTO);
                    intent.setData(Uri.parse("mailto:" + emailText.getText()));
                    startActivity(intent);
            }
        });
    }

    private void loadDataFromFirebase() {
        mDatabaseRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                try {
                    storeDetails.clear();
                    for (DataSnapshot data : snapshot.getChildren()) {
                        String[] store = new String[7];
                        store[0] = String.valueOf(data.child("id").getValue(Integer.class));
                        store[1] = data.child("name").getValue(String.class);
                        store[2] = data.child("owner").getValue(String.class);
                        store[3] = data.child("address").getValue(String.class);
                        store[4] = data.child("phone").getValue(String.class);
                        store[5] = data.child("email").getValue(String.class);
                        store[6] = data.child("coordinate").getValue(String.class);
                        storeDetails.add(store);
                    }
                    if (!storeDetails.isEmpty()) {
                        index = 0;
                        updateUI(storeDetails.get(index));
                    }
                } catch (Exception e) {
                    Log.e("StoreActivity", "Error: " + e);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("StoreActivity", "Database error: " + error.getMessage());
            }
        });
    }
    private void updateUI(String[] store) {
        storenameText.setText(store[1]);
        ownernameText.setText(store[2]);
        addressText.setText(store[3]);
        phoneText.setText(store[4]);
        emailText.setText(store[5]);

        // Enable/Disable navigation buttons
        if(index==0){
            previousButton.setVisibility(View.INVISIBLE);
        }else{
            previousButton.setVisibility(View.VISIBLE);
        }
        if(index==storeDetails.size()-1){
            nextButton.setVisibility(View.INVISIBLE);
        }else{
            nextButton.setVisibility(View.VISIBLE);
        }
    }
    private void showPreviousStore() {
        if (index > 0) {
            index--;
            updateUI(storeDetails.get(index));
        }
    }
    private void showNextStore() {
        if (index < storeDetails.size() - 1) {
            index++;
            updateUI(storeDetails.get(index));
        }
    }
}