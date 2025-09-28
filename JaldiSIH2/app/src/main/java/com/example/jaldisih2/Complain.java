package com.example.jaldisih2;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Random;

public class Complain extends AppCompatActivity {

    private FirebaseDatabase mDatabase;
    private DatabaseReference mDatabaseRef;
    private FusedLocationProviderClient fusedLocationClient;

    private Spinner complaintTypeSpinner;
    private EditText otherComplaintTypeEditText, complaintDescriptionEditText;
    private TextView billTextView; // To display the bill
    private Button raiseButton, refreshButton; // Buttons for raising and refreshing

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_complain);

        Intent intent = getIntent();
        String username = intent.getStringExtra("username");

        // Initialize Firebase
        mDatabase = FirebaseDatabase.getInstance();
        mDatabaseRef = mDatabase.getReference("tickets"); // Path to tickets

        // Initialize Location Client
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        // UI Elements
        complaintTypeSpinner = findViewById(R.id.complaintTypeSpinner);
        otherComplaintTypeEditText = findViewById(R.id.otherComplaintTypeEditText);
        complaintDescriptionEditText = findViewById(R.id.complaintDescriptionEditText);
        billTextView = findViewById(R.id.billTextView); // Bill TextView
        raiseButton = findViewById(R.id.raiseButton);
        refreshButton = findViewById(R.id.refreshButton);

        // Initialize bill display
        billTextView.setText("Bill: ₹0");

        // Refresh button to fetch and update the bill
        refreshButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fetchAndUpdateBill(username);
            }
        });

        // Raise complaint button
        raiseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String complaintDescription = complaintDescriptionEditText.getText().toString();
                String complaintType = getComplaintType();

                // Check location permissions
                if (ActivityCompat.checkSelfPermission(Complain.this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                        ActivityCompat.checkSelfPermission(Complain.this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(Complain.this, new String[]{
                            android.Manifest.permission.ACCESS_FINE_LOCATION,
                            android.Manifest.permission.ACCESS_COARSE_LOCATION}, 1);
                    return;
                }

                // Get the last location
                fusedLocationClient.getLastLocation()
                        .addOnSuccessListener(Complain.this, new OnSuccessListener<Location>() {
                            @Override
                            public void onSuccess(Location location) {
                                if (location != null) {
                                    double latitude = location.getLatitude();
                                    double longitude = location.getLongitude();

                                    // Generate unique ticket ID
                                    String ticketId = generateTicketId(username);

                                    // Get the current timestamp
                                    String timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());

                                    // Create a ticket object
                                    Ticket ticket = new Ticket(username, timestamp, latitude, longitude, complaintType, complaintDescription);

                                    // Upload ticket to Firebase
                                    mDatabaseRef.child(ticketId).setValue(ticket).addOnCompleteListener(task -> {
                                        if (task.isSuccessful()) {
                                            Toast.makeText(Complain.this, "Complaint Raised Successfully", Toast.LENGTH_SHORT).show();

                                            // Fetch and update the latest bill immediately
                                            fetchAndUpdateBill(username);

                                        } else {
                                            Toast.makeText(Complain.this, "Failed to Raise Complaint", Toast.LENGTH_SHORT).show();
                                        }
                                    });
                                }
                            }
                        });
            }
        });
    }

    // Fetch and update bill from Firebase
    private void fetchAndUpdateBill(String username) {
        mDatabaseRef.orderByChild("username").equalTo(username)
                .addListenerForSingleValueEvent(new com.google.firebase.database.ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        int totalBill = 0;

                        for (DataSnapshot ticketSnapshot : dataSnapshot.getChildren()) {
                            Integer bill = ticketSnapshot.child("bill").getValue(Integer.class);
                            if (bill != null) {
                                totalBill += bill; // Sum up all bills
                            }
                        }

                        // Update the bill display
                        billTextView.setText("Bill: ₹" + totalBill);
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Toast.makeText(Complain.this, "Failed to fetch bill: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private String getComplaintType() {
        if (complaintTypeSpinner.getSelectedItem().toString().equals("Other")) {
            return otherComplaintTypeEditText.getText().toString();
        } else {
            return complaintTypeSpinner.getSelectedItem().toString();
        }
    }

    private String generateTicketId(String username) {
        int randomNumber = new Random().nextInt(90000) + 10000; // Generate 5-digit random number
        return username + randomNumber;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Location permission granted", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Location permission is required to raise a complaint", Toast.LENGTH_SHORT).show();
            }
        }
    }

    // Ticket class
    public static class Ticket {
        public String username;
        public String timestamp;
        public String status;
        public double latitude;
        public double longitude;
        public String complaintType;
        public String complaintDescription;
        public String plumber;
        public int bill;

        public Ticket(String username, String timestamp, double latitude, double longitude, String complaintType, String complaintDescription) {
            this.username = username;
            this.timestamp = timestamp;
            this.status = "Processing";
            this.latitude = latitude;
            this.longitude = longitude;
            this.complaintType = complaintType;
            this.complaintDescription = complaintDescription;
            this.plumber = "None";
            this.bill = 0; // Default bill
        }
    }
}
