package com.example.workshoplocator;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class send_feedback extends AppCompatActivity implements AdapterView.OnItemSelectedListener {

    EditText e1;
    Button b1;
    SharedPreferences sh;
    Spinner s1;
    String feedback;
    ArrayList<String> Workshopname,wid;
    String wwid;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_send_feedback);
        s1=findViewById(R.id.spinner2);
        e1=findViewById(R.id.editTextTextPersonName14);
        b1=findViewById(R.id.button21);
        sh= PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

        String url = "http://" + sh.getString("ip", "") + ":5000/viewworkshop";
        RequestQueue queue = Volley.newRequestQueue(send_feedback.this);

        StringRequest stringRequest = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                // Display the response string.
                Log.d("+++++++++++++++++", response);
                try {
//                    Toast.makeText(send_feedback.this, response+"", Toast.LENGTH_SHORT).show();

                    JSONArray ar = new JSONArray(response);

                    Workshopname = new ArrayList<>();
                    wid = new ArrayList<>();


                    for (int i = 0; i < ar.length(); i++) {
                        JSONObject jo = ar.getJSONObject(i);
                        Workshopname.add(jo.getString("Workshopname"));
                        wid.add(jo.getString("lid"));

                    }

                     ArrayAdapter<String> ad=new ArrayAdapter<>(send_feedback.this,android.R.layout.simple_list_item_1,Workshopname);
                    s1.setAdapter(ad);

//                    l1.setAdapter(new custom(supply.this, title, type, details,time));
                    s1.setOnItemSelectedListener(send_feedback.this);

                } catch (Exception e) {
                    Log.d("=========", e.toString());
                }


            }

        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

                Toast.makeText(send_feedback.this, "err" + error, Toast.LENGTH_SHORT).show();
            }
        }) {
            @NonNull
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
//                params.put("lid", sh.getString("lid", ""));
                return params;
            }
        };
        queue.add(stringRequest);



        b1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                feedback= e1.getText().toString();
                RequestQueue queue = Volley.newRequestQueue(send_feedback.this);
                String url = "http://" + sh.getString("ip", "") + ":5000/sendfeedback";

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
                                Toast.makeText(send_feedback.this, "Thanks for your valueable feedback :)", Toast.LENGTH_SHORT).show();

                                Intent ik = new Intent(getApplicationContext(), User_home.class);
                                startActivity(ik);

                            } else {

                                Toast.makeText(send_feedback.this, "please try again", Toast.LENGTH_SHORT).show();

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

                        params.put("feedback", feedback);
                        params.put("wid", wwid);

                        params.put("lid", sh.getString("lid", ""));


                        return params;
                    }
                };
                queue.add(stringRequest);

            }
        });
    }

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
        wwid=wid.get(i);
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }
    @Override
    public void onBackPressed() {
        Intent i=new Intent(getApplicationContext(),User_home.class);
        startActivity(i);
        super.onBackPressed();
    }
}