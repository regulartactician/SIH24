package com.example.jaldisih2;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.app.AppCompatActivity;


import com.google.android.gms.wallet.PaymentsClient;
import com.google.android.gms.wallet.Wallet;
import com.google.android.gms.wallet.WalletConstants;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.razorpay.Checkout;
import com.razorpay.PaymentResultListener;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Bills extends AppCompatActivity implements PaymentResultListener {
    private RecyclerView recyclerView;
    private BillsAdapter adapter;
    private List<BillItem> billItemList;
    private int totalCost = 0; // To store the computed total cost

    public void startPayment(String samount) {
        int amount = Math.round(Float.parseFloat(samount) * 100); // Convert to paisa
        Checkout checkout = new Checkout();
        checkout.setKeyID("rzp_test_dwytKOlOlHkzYH");
        checkout.setImage(R.drawable.ic_feature_icon_1);
        JSONObject object = new JSONObject();
        try {
            object.put("name", "Jaldi Bill Services");
            object.put("description", "Test Payment");
            object.put("theme.color", "#3399cc"); // Set theme color
            object.put("currency", "INR");
            object.put("amount", amount);
            object.put("prefill.contact", "+91 9080237017");
            object.put("prefill.email", "zenovations.media@gmail.com");
            checkout.open(Bills.this, object);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bills);

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Initialize the bill items list and add the header row
        billItemList = new ArrayList<>();
        billItemList.add(new BillItem("Slno", "Service Name", "Date", "Cost")); // Header row

        // Get the intent and extract the username
        Intent intent = getIntent();
        String username = intent.getStringExtra("username");

        // GPay button setup
        RelativeLayout btn = findViewById(R.id.googlePayButton);
        Checkout.preload(Bills.this);

        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startPayment(String.valueOf(totalCost)); // Start payment with total cost
            }
        });

        // Access the Firebase Realtime Database
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference userBillsRef = database.getReference("user_info").child(username).child("bills");

        // Add a ValueEventListener to fetch data from Firebase
        userBillsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                billItemList.clear(); // Clear the list before adding new data
                //billItemList.add(new BillItem("Slno", "Service Name", "Date", "Cost")); // Add header again
                //billItemList.add(new BillItem("1", "Service charge", "11/12/2024", "100")); // Add header again

                totalCost = 0; // Reset total cost
                int slno = 1;
                for (DataSnapshot billSnapshot : dataSnapshot.getChildren()) {
                    // Assuming each bill entry is an array of [slno, service name, date, cost]
                    List<String> billData = (List<String>) billSnapshot.getValue();
                    if (billData != null && billData.size() == 4) {
                        String serviceName = billData.get(1);
                        String date = billData.get(2);
                        String cost = billData.get(3);

                        // Remove '$' symbol and parse the cost
                        try {
                            String numericCost = cost.trim();
                            totalCost += Integer.parseInt(numericCost); // Add to total cost
                        } catch (NumberFormatException e) {
                            Log.e("Bills", "Error parsing cost: " + cost, e);
                        }

                        // Add the retrieved bill item to the list
                        billItemList.add(new BillItem(String.valueOf(slno), serviceName, date, cost));
                        slno++;
                    }
                }

                // Notify the adapter of the data changes
                adapter.notifyDataSetChanged();

                // Log the total cost for debugging
                Log.d("Bills", "Total Cost: " + totalCost);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e("Bills", "Error fetching data from Firebase", databaseError.toException());
            }
        });

        // Set up the adapter
        adapter = new BillsAdapter(billItemList);
        recyclerView.setAdapter(adapter);
    }

    @Override
    public void onPaymentSuccess(String s) {
        Toast.makeText(this, "Payment Success!", Toast.LENGTH_SHORT).show();

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        Intent intent = getIntent();
        String username = intent.getStringExtra("username"); // Retrieve username from the intent

        // Get the current timestamp and format it
        long currentTimeMillis = System.currentTimeMillis();
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy-HH:mm", java.util.Locale.getDefault());
        String formattedDate = sdf.format(new Date(currentTimeMillis)); // Format the timestamp

        DatabaseReference userBillsRef = database.getReference("user_info").child(username).child("bills");

        // Iterate over all bill items except the header
        for (int i = 1; i < billItemList.size(); i++) { // Skip the header row at index 0
            BillItem item = billItemList.get(i);

            // Split the "Service Name" field to extract the service ID
            String serviceName = item.getServiceName();
            String[] parts = serviceName.split(" ");
            if (parts.length > 0) {
                String serviceID = parts[0]; // Extract the service ID (first part)

                // Update the status in Firebase
                DatabaseReference statusRef = database.getReference("tickets").child(serviceID).child("status");
                statusRef.setValue("Complain Closed").addOnSuccessListener(aVoid -> {
                    Log.d("Bills", "Status updated successfully for service ID: " + serviceID);
                }).addOnFailureListener(e -> {
                    Log.e("Bills", "Failed to update status for service ID: " + serviceID, e);
                });

                // Record the transaction
                String transactionStatement = username + "-" + formattedDate + "-" + totalCost + "-" + serviceID;
                DatabaseReference transactionRef = database.getReference("transactions").push(); // Create a new unique entry
                transactionRef.setValue(transactionStatement).addOnSuccessListener(aVoid -> {
                    DatabaseReference userBillsRef1 = database.getReference("user_info").child(username).child("bills");
                    userBillsRef1.removeValue();
                    Log.d("Bills", "Transaction recorded: " + transactionStatement);
                }).addOnFailureListener(e -> {
                    Log.e("Bills", "Failed to record transaction: " + transactionStatement, e);
                });

                // Remove the bill entry from the user's bills node
//                userBillsRef.child(String.valueOf(i - 1)).removeValue().addOnSuccessListener(aVoid -> {
//                    Log.d("Bills", "Bill entry removed for index: " + (i - 1));
//                }).addOnFailureListener(e -> {
//                    Log.e("Bills", "Failed to remove bill entry for index: " + (i - 1), e);
//                });
            } else {
                Log.e("Bills", "Service Name format is invalid: " + serviceName);
            }
        }

        // Clear the local billItemList and notify the adapter
        billItemList.clear();
        billItemList.add(new BillItem("Slno", "Service Name", "Date", "Cost")); // Add header row again
        adapter.notifyDataSetChanged();

        Log.d("Bills", "Payment success handling complete.");
    }





    @Override
    public void onPaymentError(int i, String s) {
        Toast.makeText(this, "Payment Failed!", Toast.LENGTH_SHORT).show();
    }
}
