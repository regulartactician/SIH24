package com.example.jaldisih2;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Button;
import android.widget.Toast;


public class HomeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = getIntent();

        setContentView(R.layout.activity_home);

        LinearLayout complain = (LinearLayout) findViewById(R.id.complain);
        LinearLayout status = (LinearLayout) findViewById(R.id.status);
        LinearLayout bills = (LinearLayout) findViewById(R.id.bills);
        LinearLayout shop = (LinearLayout) findViewById(R.id.shop);
        LinearLayout contact = (LinearLayout) findViewById(R.id.contact);
        LinearLayout fhtcreq = (LinearLayout) findViewById(R.id.maps);
        LinearLayout wateract = (LinearLayout) findViewById(R.id.wateractivity);
        LinearLayout nearby = (LinearLayout) findViewById(R.id.nearby);
        Button chatBot = findViewById(R.id.chat_bot);
        String message = intent.getStringExtra("username").toString();
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
        complain.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(HomeActivity.this,Complain.class);
                intent.putExtra("username",message);
                startActivity(intent);
            }
        });
        bills.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(HomeActivity.this,Bills.class);
                intent.putExtra("username",message);
                startActivity(intent);
            }
        });

        status.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(HomeActivity.this, StatusActivity.class);
                intent.putExtra("username", message); // Pass username
                startActivity(intent);
            }
        });

        contact.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(HomeActivity.this, Contact.class);
                startActivity(intent);
            }
        });
        nearby.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(HomeActivity.this, Store.class);
                startActivity(intent);
            }
        });

        wateract.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(HomeActivity.this, WaterActivity.class);
                intent.putExtra("username", message); // Pass username
                startActivity(intent);
            }
        });
        chatBot.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(HomeActivity.this, ChatBotActivity.class);
                intent.putExtra("username", message); // Pass username
                startActivity(intent);
            }
        });
        fhtcreq.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(HomeActivity.this, FhtcTap.class);
                intent.putExtra("username", message); // Pass username
                startActivity(intent);
            }
        });


    }
}