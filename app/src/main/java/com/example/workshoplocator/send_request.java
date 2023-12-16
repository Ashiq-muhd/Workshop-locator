package com.example.workshoplocator;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class send_request extends AppCompatActivity {

    EditText e1;
    Button b1;
    SharedPreferences sh;
    String Whathappened;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_send_request);
        e1=findViewById(R.id.editTextTextPersonName5);
        b1=findViewById(R.id.button24);
        sh= PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

        b1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Whathappened= e1.getText().toString();
                RequestQueue queue = Volley.newRequestQueue(send_request.this);
                String url = "http://" + sh.getString("ip", "") + ":5000/sendreqtowrksp";

                // Request a string response from the provided URL.
                StringRequest stringRequest = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        // Display the response string.
                        Log.d("+++++++++++++++++", response);
                        try {
                            JSONObject json = new JSONObject(response);
                            String res = json.getString("task");

                            if (res.equalsIgnoreCase("success")) {
                                Toast.makeText(send_request.this, "Successfully Sended ", Toast.LENGTH_SHORT).show();

                                Intent ik = new Intent(getApplicationContext(), User_home.class);
                                startActivity(ik);

                            } else {

                                Toast.makeText(send_request.this, "please try again", Toast.LENGTH_SHORT).show();

                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }


                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {


                        Toast.makeText(getApplicationContext(), "Error" + error, Toast.LENGTH_LONG).show();
                    }
                }) {
                    @Override
                    protected Map<String, String> getParams() {
                        Map<String, String> params = new HashMap<String, String>();

                        params.put("Whathappened", Whathappened);


                        params.put("wsid", sh.getString("wsid", ""));
                        params.put("lid", sh.getString("lid", ""));


                        return params;
                    }
                };
                queue.add(stringRequest);
            }
        });

    }
}