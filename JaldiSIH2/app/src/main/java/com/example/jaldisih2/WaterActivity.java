package com.example.jaldisih2;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.database.DatabaseError;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;


import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;



public class WaterActivity extends AppCompatActivity {
    private ConstraintLayout mainLayout;
    ArrayList<String[]>transactions;
    String username;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_water);

        mainLayout = findViewById(R.id.contentlayout);
        transactions = new ArrayList<>(); // Initialize the ArrayList here

        Intent intent = getIntent();
        username = intent.getStringExtra("username");

        DatabaseReference mDatabaseRef = FirebaseDatabase.getInstance().getReference("tickets");
        mDatabaseRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                try {
                    transactions.clear();
                    for (DataSnapshot childSnapshot : snapshot.getChildren()) {
                        String key = childSnapshot.getKey();
                        String user=key.substring(0, key.length() - 5);
                        if(user.equals(username)){
                            String status=childSnapshot.child("status").getValue(String.class);
                            if(status.equals("Completed")) {
                                String[] store = new String[4];
                                store[0] = childSnapshot.child("timestamp").getValue(String.class);
                                store[1] = childSnapshot.child("complaintType").getValue(String.class);
                                Long bill=childSnapshot.child("bill").getValue(Long.class);
                                Long admin_bill=childSnapshot.child("admin_bill").getValue(Long.class);
                                Long serviceFee = childSnapshot.child("service_bill").getValue(Long.class);
                                Long inventoryFee = bill-admin_bill;
                                store[2] = serviceFee != null ? String.valueOf(serviceFee) : "0";
                                store[3] = inventoryFee != null ? String.valueOf(inventoryFee) : "0";
                                transactions.add(store);
                            }
                        }
                    }

                    // Sort transactions by date
                    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault());
                    Collections.sort(transactions, new Comparator<String[]>() {
                        @Override
                        public int compare(String[] o1, String[] o2) {
                            try {
                                Date date1 = dateFormat.parse(o1[0]);
                                Date date2 = dateFormat.parse(o2[0]);
                                return date1.compareTo(date2);
                            } catch (ParseException e) {
                                e.printStackTrace();
                                return 0;
                            }
                        }
                    });

                    // Process sorted transactions
                    for (String[] data : transactions) {
                        addBillView(data[0], data[1], data[2],data[3]);
                    }
                } catch (Exception e) {
                    Log.e("WaterActivity", "Error: " + e.getMessage(), e);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("WaterActivity", "Database error: " + error.getMessage(), error.toException());
            }
        });
    }

    private void addBillView(String date, String complain,String service,String inventory) {
        int totalAmount = Integer.parseInt(service) + Integer.parseInt(inventory);
        String amount=String.valueOf(totalAmount);
        ConstraintLayout newBillLayout = new ConstraintLayout(this);
        newBillLayout.setId(View.generateViewId());

        // Set layout parameters
        ConstraintLayout.LayoutParams layoutParams = new ConstraintLayout.LayoutParams(
                ConstraintLayout.LayoutParams.MATCH_PARENT,
                ConstraintLayout.LayoutParams.WRAP_CONTENT
        );
        layoutParams.setMargins(dpToPx(10), dpToPx(30), dpToPx(10), 0);
        newBillLayout.setLayoutParams(layoutParams);

        // Set padding
        int padding = dpToPx(10);
        newBillLayout.setPadding(padding, padding, padding, padding);

        // Set background
        newBillLayout.setBackgroundResource(R.drawable.circular_vera_colour);
        newBillLayout.setTranslationZ(dpToPx(15));


        TextView dateText = createTextView(date);
        dateText.setId(View.generateViewId());
        TextView complainText = createTextView(complain);
        complainText.setId(View.generateViewId());
        TextView amountText = createTextView(amount);
        amountText.setId(View.generateViewId());


        Button downloadButton = new Button(this);
        downloadButton.setBackgroundResource(R.drawable.download_icon);
        downloadButton.setText("");
        int buttonSize = (int)(25 * getResources().getDisplayMetrics().density);
        downloadButton.setLayoutParams(new ConstraintLayout.LayoutParams(buttonSize, buttonSize));
        downloadButton.setId(View.generateViewId());

        newBillLayout.addView(dateText);
        newBillLayout.addView(complainText);
        newBillLayout.addView(amountText);
        newBillLayout.addView(downloadButton);

        mainLayout.addView(newBillLayout);

        // Set constraints for the views
        ConstraintSet constraintSet = new ConstraintSet();
        constraintSet.clone(newBillLayout);

        constraintSet.connect(dateText.getId(), ConstraintSet.START, newBillLayout.getId(), ConstraintSet.START, 0);
        constraintSet.connect(dateText.getId(), ConstraintSet.TOP, newBillLayout.getId(), ConstraintSet.TOP, 0);

        constraintSet.connect(complainText.getId(), ConstraintSet.START, dateText.getId(), ConstraintSet.END, dpToPx(16));
        constraintSet.connect(complainText.getId(), ConstraintSet.TOP, newBillLayout.getId(), ConstraintSet.TOP, 0);

        constraintSet.connect(amountText.getId(), ConstraintSet.START, complainText.getId(), ConstraintSet.END, dpToPx(16));
        constraintSet.connect(amountText.getId(), ConstraintSet.TOP, newBillLayout.getId(), ConstraintSet.TOP, 0);

        constraintSet.connect(downloadButton.getId(), ConstraintSet.END, newBillLayout.getId(), ConstraintSet.END, 0);
        constraintSet.connect(downloadButton.getId(), ConstraintSet.TOP, newBillLayout.getId(), ConstraintSet.TOP, 0);

        if (mainLayout.getChildCount() > 1) {
            ConstraintLayout previousLayout = (ConstraintLayout) mainLayout.getChildAt(mainLayout.getChildCount() - 2);
            previousLayout.post(new Runnable() {
                @Override
                public void run() {
                    ConstraintSet constraintSet1 = new ConstraintSet();
                    constraintSet1.clone(mainLayout);

                    // Connect the new layout below the previous layout
                    constraintSet1.connect(newBillLayout.getId(), ConstraintSet.TOP, previousLayout.getId(), ConstraintSet.BOTTOM, dpToPx(20));
                    constraintSet1.applyTo(mainLayout);
                }
            });
        } else {
            ConstraintSet constraintSet1 = new ConstraintSet();
            constraintSet1.clone(mainLayout);
            constraintSet1.connect(newBillLayout.getId(), ConstraintSet.TOP, mainLayout.getId(), ConstraintSet.TOP, dpToPx(30));
            constraintSet1.applyTo(mainLayout);
        }


        constraintSet.applyTo(newBillLayout);
        final String finalDate = date;
        final String finalComplain = complain;
        final String finalserviceAmount=service;
        final String finalinventoryAmount=inventory;

        downloadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean result = createTextFile(finalDate, finalComplain, finalserviceAmount,finalinventoryAmount);
                if (result) {
                    Toast.makeText(WaterActivity.this, "File saved successfully!", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(WaterActivity.this, "Failed to save file.", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private TextView createTextView(String text) {
        TextView textView = new TextView(this);
        textView.setText(text);
        textView.setTextColor(getResources().getColor(android.R.color.black));
        textView.setTextSize(13);
        return textView;
    }
    private int dpToPx(int dp) {
        return (int) (dp * getResources().getDisplayMetrics().density);
    }
    private boolean createTextFile(String date, String complain, String service,String inventory) {
        try {
            // Get the file directory (using external storage)
            File path = getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS);
            // Create the file if it doesn't exist
            String fileName="bill"+date+".pdf";
            File file = new File(path,fileName);
            Log.d("WaterActivity", "Saving file to: " + file.getAbsolutePath());
            Document document=new Document();
            PdfWriter.getInstance(document, new FileOutputStream(file));
            document.open();

            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 20, BaseColor.BLUE);
            Paragraph title = new Paragraph("Invoice", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            document.add(title);

            // Add Date
            Font normalFont = FontFactory.getFont(FontFactory.HELVETICA, 12, BaseColor.BLACK);
            Font boldFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 15, BaseColor.BLACK);
            Paragraph pdate = new Paragraph("Date: " + date, normalFont);
            pdate.setAlignment(Element.ALIGN_RIGHT);
            document.add(pdate);

            // Add Billing From and Billing To
            document.add(new Paragraph("\nBilling From:", boldFont));
            document.add(new Paragraph("Ministry of JalSakthi\nGovernment of India\n", normalFont));

            document.add(new Paragraph("Billing To:", boldFont));
            document.add(new Paragraph(username,normalFont));

            // Add a Table for Description and Amount
            PdfPTable table = new PdfPTable(2);
            table.setWidthPercentage(100);
            table.setSpacingBefore(10f);

            // Set column widths
            float[] columnWidths = {2f, 1f};
            table.setWidths(columnWidths);

            // Add Table Header
            PdfPCell descriptionHeader = new PdfPCell(new Phrase("Description", normalFont));
            descriptionHeader.setBackgroundColor(BaseColor.LIGHT_GRAY);
            descriptionHeader.setHorizontalAlignment(Element.ALIGN_CENTER);
            descriptionHeader.setPadding(5);

            PdfPCell amountHeader = new PdfPCell(new Phrase("Amount", normalFont));
            amountHeader.setBackgroundColor(BaseColor.LIGHT_GRAY);
            amountHeader.setHorizontalAlignment(Element.ALIGN_CENTER);
            amountHeader.setPadding(5);

            table.addCell(descriptionHeader);
            table.addCell(amountHeader);

            // Add Table Rows
            table.addCell(new PdfPCell(new Phrase("1) Service Fee", normalFont)));
            table.addCell(new PdfPCell(new Phrase("Rs."+service, normalFont)));

            table.addCell(new PdfPCell(new Phrase("2) Inventory Fee", normalFont)));
            table.addCell(new PdfPCell(new Phrase("Rs."+inventory, normalFont)));

            // Grand Total Row
            PdfPCell totalCell = new PdfPCell(new Phrase("Grand Total", normalFont));
            totalCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
            totalCell.setPadding(5);
            table.addCell(totalCell);
            String total=String.valueOf(Integer.parseInt(service)+Integer.parseInt(inventory));
            PdfPCell totalAmountCell = new PdfPCell(new Phrase("Rs."+total, normalFont));
            totalAmountCell.setHorizontalAlignment(Element.ALIGN_CENTER);
            totalAmountCell.setPadding(5);
            table.addCell(totalAmountCell);

            // Add Table to Document
            document.add(table);

            // Add Footer
            Paragraph footer = new Paragraph("\nThank you for your business!", normalFont);
            footer.setAlignment(Element.ALIGN_CENTER);
            document.add(footer);

            document.close();

            return true;  // File saved successfully

        } catch (IOException e) {
            e.printStackTrace();
            return false; // Failed to save file
        }catch(Exception e){
            e.printStackTrace();
            return false;
        }
    }
}