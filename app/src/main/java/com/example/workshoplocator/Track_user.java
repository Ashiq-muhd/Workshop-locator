package com.example.workshoplocator;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Track_user extends AppCompatActivity implements AdapterView.OnItemClickListener {

    ListView l1;
    SharedPreferences sh;
    ArrayList<String> uname,Phone,lati,longi;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_track_user);
        l1=findViewById(R.id.listview03);
        sh= PreferenceManager.getDefaultSharedPreferences(getApplicationContext());


        String url = "http://" + sh.getString("ip", "") + ":5000/trackuser";
        RequestQueue queue = Volley.newRequestQueue(Track_user.this);

        StringRequest stringRequest = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                // Display the response string.
                Log.d("+++++++++++++++++", response);
                try {

                    JSONArray ar = new JSONArray(response);

                    uname = new ArrayList<>();
                    Phone = new ArrayList<>();
                    lati = new ArrayList<>();
                    longi = new ArrayList<>();


                    for (int i = 0; i < ar.length(); i++) {
                        JSONObject jo = ar.getJSONObject(i);
                        uname.add(jo.getString("Firstname")+jo.getString("Lastname"));
                        Phone.add(jo.getString("Phone"));
                        lati.add(jo.getString("Latitude"));
                        longi.add(jo.getString("Logitude"));

                    }

                    // ArrayAdapter<String> ad=new ArrayAdapter<>(Home.this,android.R.layout.simple_list_item_1,name);
                    //lv.setAdapter(ad);

                    l1.setAdapter(new custom2(Track_user.this,uname,Phone));
                    l1.setOnItemClickListener(Track_user.this);

                } catch (Exception e) {
                    Log.d("=========", e.toString());
                }


            }

        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

                Toast.makeText(Track_user.this, "err" + error, Toast.LENGTH_SHORT).show();
            }
        }) {
            @NonNull
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("lid", sh.getString("lid", ""));
                return params;
            }
        };
        queue.add(stringRequest);


    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        Intent ik = new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.google.com/maps?q="+lati.get(i)+","+longi.get(i)));
        startActivity(ik);
    }
    @Override
    public void onBackPressed() {
        Intent i=new Intent(getApplicationContext(),Login.class);
        startActivity(i);
        super.onBackPressed();
    }
}