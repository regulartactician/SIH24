package com.example.jaldisih2;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class signup extends AppCompatActivity {

    private DatabaseReference databaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        // Initialize Firebase database reference
        databaseReference = FirebaseDatabase.getInstance().getReference("user_info");

        EditText username = findViewById(R.id.un_edt);
        EditText password = findViewById(R.id.pw_edt);
        Button submit = findViewById(R.id.sub_btn);

        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String un = username.getText().toString().trim();
                String pw = password.getText().toString().trim();

                // Validation for empty fields
                if (TextUtils.isEmpty(un)) {
                    Toast.makeText(signup.this, "Please enter a username", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (TextUtils.isEmpty(pw)) {
                    Toast.makeText(signup.this, "Please enter a password", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Password length check
                if (pw.length() < 8) {
                    Toast.makeText(signup.this, "Password must be at least 8 characters long", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Check if the username already exists
                checkIfUsernameExists(un, pw);
            }
        });
    }

    // Check if the username already exists in Firebase
    private void checkIfUsernameExists(final String username, final String password) {
        databaseReference.child(username).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    // Username already exists
                    Toast.makeText(signup.this, "Username already exists, please choose another", Toast.LENGTH_SHORT).show();
                } else {
                    // Username does not exist, proceed to store the credentials
                    saveUserCredentials(username, password);
                    Intent intent = new Intent(signup.this, MainActivity.class);  // Assuming HomeActivity is the next screen
                    startActivity(intent);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(signup.this, "Database error: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Save the new user's credentials in Firebase Realtime Database
    public class User {
        public String username;
        public String password;

        // Default constructor is required for Firebase
        public User() {}

        public User(String username, String password) {
            this.username = username;
            this.password = password;
        }
    }

    private void saveUserCredentials(String username, String password) {
        // Create a User object with the credentials
        User user = new User(username, password);

        // Save the user object under 'user_info/username'
        databaseReference.child(username).setValue(user)  // Corrected path
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(signup.this, "Signup successful! User added to database", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(signup.this, "Failed to upload data", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}