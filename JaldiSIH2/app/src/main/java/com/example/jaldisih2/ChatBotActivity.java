package com.example.jaldisih2;


import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.util.Log;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.ArrayList;
import android.widget.Toast;
import android.speech.tts.TextToSpeech.Engine;
import android.speech.tts.TextToSpeech;
import android.speech.tts.TextToSpeech.OnInitListener;


import com.example.jaldisih2.api.HuggingFaceAPI;
import com.example.jaldisih2.api.InputData;
import com.example.jaldisih2.api.RetrofitClient;
import com.google.gson.Gson;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Locale;

public class ChatBotActivity extends AppCompatActivity {
    private Button requestButton;
    private EditText requestText;
    private EditText responseText;
    private Button responseButton;
    private TextToSpeech textToSpeech;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chatbot);
        requestButton=findViewById(R.id.requestButton);
        requestText=findViewById(R.id.requestText);
        responseText=findViewById(R.id.responseText);
        responseButton=findViewById(R.id.responseButton);
        requestButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String query=requestText.getText().toString();
                if(query.length()!=0){
                    HuggingFaceAPI api = RetrofitClient.getClient().create(HuggingFaceAPI.class);

                    // Input data
                    InputData inputData = new InputData(query);

                    // Make API call
                    Call<Object> call = api.getInference(inputData);
                    call.enqueue(new Callback<Object>() {
                        @Override
                        public void onResponse(Call<Object> call, Response<Object> response) {
                            if (response.isSuccessful()) {
                                try {
                                    // Parse the JSON response
                                    ArrayList<?> objectResponse = (ArrayList<?>) response.body();
                                    JSONObject jsonObject=new JSONObject(new Gson().toJson(objectResponse.get(0)));
                                    String generatedText =jsonObject.getString("generated_text");

                                    // Log and display the response
                                    Log.d("API Response", generatedText);

                                    // Optionally, display the response in a TextView
                                    responseText.setText(generatedText);
                                } catch (Exception e) {
                                    Log.e("JSON Parsing Error", e.getMessage());
                                }
                            } else {
                                Log.e("API Error", "Error code: " + response.code());
                            }
                        }

                        @Override
                        public void onFailure(Call<Object> call, Throwable t) {
                            Toast.makeText(ChatBotActivity.this, "Failure: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
                }
                else{
                    Toast.makeText(ChatBotActivity.this, "Please enter a query", Toast.LENGTH_SHORT).show();
                }
            }
        });
        textToSpeech = new TextToSpeech(this, new OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status == TextToSpeech.SUCCESS) {
                    // Set language for TTS
                    int langResult = textToSpeech.setLanguage(Locale.US);
                    if (langResult == TextToSpeech.LANG_MISSING_DATA
                            || langResult == TextToSpeech.LANG_NOT_SUPPORTED) {
                        Log.e("TTS", "Language is not supported or missing data.");
                    } else {
                        Log.d("TTS", "TextToSpeech initialized successfully.");
                    }
                } else {
                    Log.e("TTS", "Initialization failed.");
                }
            }
        });

        responseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String msg=responseText.getText().toString();
                if(msg.length()!=0){
                        speakText(msg);
                }

            }
        });

    }
    public void speakText(String text) {
        if (textToSpeech != null) {
            textToSpeech.speak(text, TextToSpeech.QUEUE_FLUSH, null, null);
        }
    }
    @Override
    protected void onDestroy() {
        // Shutdown TTS when the activity is destroyed
        if (textToSpeech != null) {
            textToSpeech.stop();
            textToSpeech.shutdown();
        }
        super.onDestroy();
    }

}
