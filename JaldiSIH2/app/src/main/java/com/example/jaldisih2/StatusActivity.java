package com.example.jaldisih2;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class StatusActivity extends AppCompatActivity {
    private ListView ticketListView;
    private TextView ticketDetailsTextView;
    private ArrayList<String> ticketList = new ArrayList<>();
    private ArrayAdapter<String> adapter;
    private DatabaseReference ticketsRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_status);

        // Initialize views
        ticketListView = findViewById(R.id.ticketListView);
        ticketDetailsTextView = findViewById(R.id.ticketDetailsTextView);
        ticketDetailsTextView.setVisibility(View.GONE);

        // Get the username of the signed-in user passed from HomeActivity
        String signedInUsername = getIntent().getStringExtra("username");

        // Initialize Firebase database reference to the tickets node
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        ticketsRef = database.getReference("tickets");

        // Fetch tickets for the signed-in user
        ticketsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                ticketList.clear();

                for (DataSnapshot ticketSnapshot : dataSnapshot.getChildren()) {
                    String ticketID = ticketSnapshot.getKey();
                    String username = ticketSnapshot.child("username").getValue(String.class);

                    // Check if the ticket belongs to the signed-in user
                    if (signedInUsername != null && signedInUsername.equals(username)) {
                        ticketList.add(ticketID); // Add ticket ID to the list
                    }
                }

                // Update the ListView adapter
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e("StatusActivity", "Database error: " + databaseError.getMessage());
            }
        });

        // Set up the custom ListView adapter
        adapter = new ArrayAdapter<String>(this, R.layout.list_item_ticket, ticketList) {
            @NonNull
            @Override
            public View getView(int position, View convertView, @NonNull ViewGroup parent) {
                if (convertView == null) {
                    convertView = LayoutInflater.from(getContext()).inflate(R.layout.list_item_ticket, parent, false);
                }

                TextView typeTextView = convertView.findViewById(R.id.complaintTypeTextView);
                TextView descTextView = convertView.findViewById(R.id.complaintDescriptionTextView);
                TextView timeTextView = convertView.findViewById(R.id.complaintTimestampTextView);

                String ticketID = ticketList.get(position);
                DatabaseReference ticketRef = ticketsRef.child(ticketID);

                ticketRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        String complaintType = snapshot.child("complaintType").getValue(String.class);
                        String complaintDescription = snapshot.child("complaintDescription").getValue(String.class);
                        String timestamp = snapshot.child("timestamp").getValue(String.class);

                        typeTextView.setText(complaintType);
                        descTextView.setText(complaintDescription);
                        timeTextView.setText(timestamp);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.e("StatusActivity", "Database error: " + error.getMessage());
                    }
                });

                return convertView;
            }
        };
        ticketListView.setAdapter(adapter);

        // Handle ticket list item clicks
        ticketListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String selectedTicket = ticketList.get(position);

                // Fetch and display details of the selected ticket
                ticketsRef.child(selectedTicket).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            StringBuilder detailsBuilder = new StringBuilder();

                            detailsBuilder.append("Complaint Type: ").append(snapshot.child("complaintType").getValue(String.class)).append("\n");
                            detailsBuilder.append("Description: ").append(snapshot.child("complaintDescription").getValue(String.class)).append("\n");
                            detailsBuilder.append("Plumber: ").append(snapshot.child("plumber").getValue(String.class)).append("\n");
                            detailsBuilder.append("Status: ").append(snapshot.child("status").getValue(String.class)).append("\n");
                            detailsBuilder.append("Latitude: ").append(snapshot.child("latitude").getValue(Double.class)).append("\n");
                            detailsBuilder.append("Longitude: ").append(snapshot.child("longitude").getValue(Double.class)).append("\n");
                            detailsBuilder.append("Timestamp: ").append(snapshot.child("timestamp").getValue(String.class)).append("\n");

                            ticketDetailsTextView.setText(detailsBuilder.toString());
                            ticketDetailsTextView.setVisibility(View.VISIBLE);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.e("StatusActivity", "Database error: " + error.getMessage());
                    }
                });
            }
        });
    }
}
