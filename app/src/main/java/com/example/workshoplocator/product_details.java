package com.example.workshoplocator;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.StrictMode;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
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

public class product_details extends AppCompatActivity {

    TextView t1,t2,t3,t4;
    EditText e1;
    Button b1,b2;
    ImageView i1;
    SharedPreferences sh;
    String quantity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_details);
        t1=findViewById(R.id.textView33);
        t2=findViewById(R.id.textView34);
        t3=findViewById(R.id.textView36);
        t4=findViewById(R.id.textView38);
        e1=findViewById(R.id.editTextTextPersonName15);
        b1=findViewById(R.id.button16);
        b2=findViewById(R.id.button17);
        i1=findViewById(R.id.imageView);
        sh= PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

        t1.setText(getIntent().getStringExtra("pname"));
        t2.setText(getIntent().getStringExtra("vechile_type"));
        t3.setText(getIntent().getStringExtra("Amount"));
        t4.setText(getIntent().getStringExtra("Details"));


        java.net.URL thumb_u;
        try {

            //thumb_u = new java.net.URL("http://192.168.43.57:5000/static/photo/flyer.jpg");

            thumb_u = new java.net.URL("http://"+sh.getString("ip","")+":5000/static/upload/"+getIntent().getStringExtra("photo"));
            Drawable thumb_d = Drawable.createFromStream(thumb_u.openStream(), "src");
            i1.setImageDrawable(thumb_d);

        }
        catch (Exception e)
        {
            Log.d("errsssssssssssss",""+e);
        }


        if (android.os.Build.VERSION.SDK_INT > 9) {
            StrictMode.ThreadPolicy policy =
                    new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }


        b1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(getApplicationContext(),view_product_buy.class);
                startActivity(i);
            }
        });

        b2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                quantity=e1.getText().toString();

                int qty = Integer.parseInt(quantity);

                int stock = Integer.parseInt(getIntent().getStringExtra("Stock"));

                if (qty>stock)
                {
                    Toast.makeText(product_details.this, "Quantity exceeded", Toast.LENGTH_SHORT).show();
                }

                else {

                    int amt = Integer.parseInt(getIntent().getStringExtra("Amount"));

                    int price = qty * amt;

                    Intent i = new Intent(getApplicationContext(), PaymentActivity.class);
                    Toast.makeText(product_details.this, "price" + price, Toast.LENGTH_SHORT).show();
                    i.putExtra("p", price + "");
                    i.putExtra("qty", quantity);
                    i.putExtra("pid", getIntent().getStringExtra("pid"));
                    startActivity(i);
                }


//                RequestQueue queue = Volley.newRequestQueue(product_details.this);
//                String url = "http://" + sh.getString("ip", "") + ":5000/viewproducts";
//
//                // Request a string response from the provided URL.
//                StringRequest stringRequest = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
//                    @Override
//                    public void onResponse(String response) {
//                        // Display the response string.
//                        Log.d("+++++++++++++++++", response);
//                        try {
//                            JSONObject json = new JSONObject(response);
//                            String res = json.getString("task");
//
//                            if (res.equalsIgnoreCase("valid")) {
//                                Toast.makeText(product_details.this, "registered successfully ", Toast.LENGTH_SHORT).show();
//
//                                Intent ik = new Intent(getApplicationContext(), first_login.class);
//                                startActivity(ik);
//
//                            } else {
//
//                                Toast.makeText(product_details.this, "please try again", Toast.LENGTH_SHORT).show();
//
//                            }
//                        } catch (JSONException e) {
//                            e.printStackTrace();
//                        }
//
//
//                    }
//                }, new Response.ErrorListener() {
//                    @Override
//                    public void onErrorResponse(VolleyError error) {
//
//
//                        Toast.makeText(getApplicationContext(), "Error" + error, Toast.LENGTH_LONG).show();
//                    }
//                }) {
//                    @Override
//                    protected Map<String, String> getParams() {
//                        Map<String, String> params = new HashMap<String, String>();
//
//                        params.put("quantity", quantity);
//
//
//                        params.put("lid", sh.getString("lid", ""));
//
//
//                        return params;
//                    }
//                };
//                queue.add(stringRequest);
            }
        });

    }
    @Override
    public void onBackPressed() {
        Intent i=new Intent(getApplicationContext(),view_product_buy.class);
        startActivity(i);
        super.onBackPressed();
    }
}