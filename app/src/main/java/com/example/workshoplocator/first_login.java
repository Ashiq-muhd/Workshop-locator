package com.example.workshoplocator;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
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

public class first_login extends AppCompatActivity {

    EditText e1,e2;
    Button b1,b2;
    SharedPreferences sh;
    String uname,password;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_first_login);
        e1=findViewById(R.id.editTextTextPersonName16);
        e2=findViewById(R.id.editTextTextPersonName17);
        b1=findViewById(R.id.button22);
        b2=findViewById(R.id.button23);
        sh= PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

        b1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                uname=e1.getText().toString();
                password=e2.getText().toString();

                if (uname.equalsIgnoreCase(""))
                {
                    e1.setError("Required");
                    e1.requestFocus();
                }
                else if (password.equalsIgnoreCase(""))
                {
                    e2.setError("Required");
                    e2.requestFocus();
                }
                else {

                    RequestQueue queue = Volley.newRequestQueue(first_login.this);
                    String url = "http://" + sh.getString("ip", "") + ":5000/login";
                    // Request a string response from the provided URL.
                    StringRequest stringRequest = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            // Display the response string.
                            Log.d("+++++++++++++++++", response);
                            try {
                                JSONObject json = new JSONObject(response);
                                String res = json.getString("task");

                                if (res.equalsIgnoreCase("invalid")) {
                                    Toast.makeText(getApplicationContext(), res, Toast.LENGTH_LONG).show();
                                } else {
                                    String typ = json.getString("type");
                                    String id = json.getString("lid");

                                    if (typ.equalsIgnoreCase("mechanic")) {
                                        Toast.makeText(getApplicationContext(), "mechanic", Toast.LENGTH_LONG).show();
//                                    Intent ik=new Intent(getApplicationContext(),LocationService.class);
//                                    startService(ik);
                                        SharedPreferences.Editor ed = sh.edit();
                                        ed.putString("lid", id);
                                        ed.commit();

                                        Intent ik = new Intent(getApplicationContext(), LocationService.class);
                                        startService(ik);

                                        startActivity(new Intent(getApplicationContext(), Login.class));
                                    } else if (typ.equalsIgnoreCase("user")) {
                                        Toast.makeText(getApplicationContext(), "user", Toast.LENGTH_LONG).show();
                                        Intent ik = new Intent(getApplicationContext(), LocationService.class);
                                        startService(ik);
                                        SharedPreferences.Editor ed = sh.edit();
                                        ed.putString("lid", id);
                                        ed.commit();

//                                    Intent i = new Intent(getApplicationContext(),LocationService.class);
//                                    startService(i);

                                        startActivity(new Intent(getApplicationContext(), User_home.class));
//                                    Intent i = new Intent(getApplicationContext(),LocationService.class);
//                                    startService(i);

                                    } else {
                                        SharedPreferences.Editor ed = sh.edit();
                                        Toast.makeText(getApplicationContext(), "INVALID!!!!!", Toast.LENGTH_LONG).show();

                                        startActivity(new Intent(getApplicationContext(), first_login.class));
                                    }
                                }
                            } catch (JSONException e) {
                                Toast.makeText(getApplicationContext(), "exp" + e, Toast.LENGTH_LONG).show();
                                e.printStackTrace();
                            }
                        }
                    }, new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {

                            Toast.makeText(getApplicationContext(), "Error" + error, Toast.LENGTH_LONG).show();
                        }
                    }) {
                        @NonNull
                        @Override
                        protected Map<String, String> getParams() {
                            Map<String, String> params = new HashMap<String, String>();
                            params.put("uname", uname);
                            params.put("password", password);
                            return params;
                        }
                    };
                    // Add the request to the RequestQueue.
                    queue.add(stringRequest);
                }
            }
        });

        b2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(getApplicationContext(),user_reg.class);
                startActivity(i);
            }
        });

    }

    public void onBackPressed() {
        // TODO Auto-generated method stub
        AlertDialog.Builder ald=new AlertDialog.Builder(first_login.this);
        ald.setTitle("Do you want to Exit")
                .setPositiveButton(" YES ", new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface arg0, int arg1) {
                        Intent in=new Intent(Intent.ACTION_MAIN);
                        in.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        in.addCategory(Intent.CATEGORY_HOME);
                        startActivity(in);
                    }
                })
                .setNegativeButton(" NO ", new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface arg0, int arg1) {

                    }
                });

        AlertDialog al=ald.create();
        al.show();

    }


}