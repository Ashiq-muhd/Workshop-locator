package com.example.workshoplocator;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
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

public class user_reg extends AppCompatActivity {

    EditText e1,e2,e4,e5,e6,e7,e8,e9,e10;;
    Button b1;
    SharedPreferences sh;
    String fname,lname,gender,place,post,pin,email,phone,uname,password;
    RadioButton r1,r2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_reg);
        e1=findViewById(R.id.editTextTextPersonName3);
        e2=findViewById(R.id.editTextTextPersonName4);
        r1=findViewById(R.id.radioButton);
        r2=findViewById(R.id.radioButton2);
        e4=findViewById(R.id.editTextTextPersonName6);
        e5=findViewById(R.id.editTextTextPersonName7);
        e6=findViewById(R.id.editTextTextPersonName8);
        e7=findViewById(R.id.editTextTextPersonName9);
        e8=findViewById(R.id.editTextTextPersonName10);
        e9=findViewById(R.id.editTextTextPersonName11);
        e10=findViewById(R.id.editTextTextPersonName12);
        b1=findViewById(R.id.button8);
        sh= PreferenceManager.getDefaultSharedPreferences(getApplicationContext());


        b1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                fname = e1.getText().toString();
                lname = e2.getText().toString();
                place = e4.getText().toString();
                post = e5.getText().toString();
                pin = e6.getText().toString();
                email = e7.getText().toString();
                phone = e8.getText().toString();
                uname = e9.getText().toString();
                password = e10.getText().toString();
                if (r1.isChecked()) {
                    gender = r1.getText().toString();

                } else {
                    gender = r2.getText().toString();
                }

                if (fname.equalsIgnoreCase("")) {
                    e1.setError("Enter first name");
                } else if (lname.equalsIgnoreCase("")) {
                    e2.setError("Enter last name");
                }
//                else if(age.equalsIgnoreCase(""))
//                {
//                    e3.setError("Enter age");
//                }

                else if (phone.equalsIgnoreCase("")) {
                    e8.setError("Enter Your Phone No");
                } else if (phone.length() < 10) {
                    e8.setError("Minimum 10 nos required");
                    e8.requestFocus();
                } else if (email.equalsIgnoreCase("")) {
                    e7.setError("Enter Your Email");
                } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                    e7.setError("Enter Valid Email");
                    e7.requestFocus();
                } else if (place.equalsIgnoreCase("")) {
                    e4.setError("Enter Your Place");
                } else if (post.equalsIgnoreCase("")) {
                    e4.setError("Enter Your post");
                } else if (pin.equalsIgnoreCase("")) {
                    e6.setError("Enter Your Pin");
                } else if (pin.length() != 6) {
                    e6.setError("invalid pin");
                    e6.requestFocus();
                } else if (uname.equalsIgnoreCase("")) {
                    e9.setError("Enter Your username");
                } else if (password.equalsIgnoreCase("")) {
                    e10.setError("Enter Your password");
                } else {


                    RequestQueue queue = Volley.newRequestQueue(user_reg.this);
                    String url = "http://" + sh.getString("ip", "") + ":5000/reguser";

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
                                    Toast.makeText(user_reg.this, "registered successfully ", Toast.LENGTH_SHORT).show();

                                    Intent ik = new Intent(getApplicationContext(), first_login.class);
                                    startActivity(ik);

                                } else {

                                    Toast.makeText(user_reg.this, "please try again", Toast.LENGTH_SHORT).show();

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

                            params.put("fname", fname);
                            params.put("lname", lname);
                            params.put("place", place);
                            params.put("post", post);
                            params.put("pin", pin);
                            params.put("gender", gender);
                            params.put("phone", phone);
                            params.put("email", email);
                            params.put("username", uname);
                            params.put("password", password);


                            return params;
                        }
                    };
                    queue.add(stringRequest);


                }
            }
        });

    }
}