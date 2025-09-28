package com.example.jaldisih2;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.w3c.dom.Text;

public class MainActivity extends AppCompatActivity {

    private DatabaseReference databaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize Firebase database reference
        databaseReference = FirebaseDatabase.getInstance().getReference("user_info");

        EditText username = findViewById(R.id.un_edt);
        EditText password = findViewById(R.id.pw_edt);
        TextView header = findViewById(R.id.header);
        Button submit = findViewById(R.id.sub_btn);
        TextView signup = findViewById(R.id.signup);

        header.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(MainActivity.this, HomeActivity.class);
                i.putExtra("username",username.getText().toString());
                startActivity(i);
            }
        });

        // Redirect to Signup activity
        signup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, signup.class);
                intent.putExtra("username",username.getText().toString());
                // Start the signup activity
                startActivity(intent);
            }
        });

        // Login Button functionality
        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String un = username.getText().toString().trim();
                String pw = password.getText().toString().trim();

                // Validation for empty fields
                if (TextUtils.isEmpty(un)) {
                    Toast.makeText(MainActivity.this, "Please enter a username", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (TextUtils.isEmpty(pw)) {
                    Toast.makeText(MainActivity.this, "Please enter a password", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Check if the username exists and password matches
                loginUser(un, pw);
            }
        });
    }

    // Function to check if username exists and validate the password
    private void loginUser(String username, String password) {
        databaseReference.child(username).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    // User exists, check password
                    User user = dataSnapshot.getValue(User.class);
                    if (user != null && user.password.equals(password)) {
                        // Credentials are correct, proceed to next screen
                        Toast.makeText(MainActivity.this, "Login successful!", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(MainActivity.this, HomeActivity.class);  // Assuming HomeActivity is the next screen
                        intent.putExtra("username",username);
                        startActivity(intent);
                    } else {
                        // Incorrect password
                        Toast.makeText(MainActivity.this, "Incorrect password", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    // Username doesn't exist
                    Toast.makeText(MainActivity.this, "Username does not exist", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(MainActivity.this, "Database error: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    // User class to match the data structure in Firebase
    public static class User {
        public String username;
        public String password;

        // Default constructor for Firebase
        public User() {}

        public User(String username, String password) {
            this.username = username;
            this.password = password;
        }
    }
}
