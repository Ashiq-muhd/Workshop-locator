package com.example.workshoplocator;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
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
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class view_nearby_wrksp extends AppCompatActivity implements AdapterView.OnItemClickListener {

    ListView l1;
    SharedPreferences sh;
    ArrayList<String> Workshopname,Place,Phone,Email,lati,longi,wwid;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_nearby_wrksp);
        l1=findViewById(R.id.listview06);
        sh= PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

        String url = "http://" + sh.getString("ip", "") + ":5000/nearest_mechanic";
        RequestQueue queue = Volley.newRequestQueue(view_nearby_wrksp.this);

        StringRequest stringRequest = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                // Display the response string.
                Log.d("+++++++++++++++++", response);
                try {

                    JSONArray ar = new JSONArray(response);

                    Workshopname = new ArrayList<>();
                    Place = new ArrayList<>();
                    Phone = new ArrayList<>();
                    Email = new ArrayList<>();
                    lati = new ArrayList<>();
                    longi = new ArrayList<>();
                    wwid = new ArrayList<>();


                    for (int i = 0; i < ar.length(); i++) {
                        JSONObject jo = ar.getJSONObject(i);
                        Workshopname.add(jo.getString("Workshopname"));
                        Place.add(jo.getString("Place"));
                        Phone.add(jo.getString("Phone"));
                        Email.add(jo.getString("Email"));
                        lati.add(jo.getString("Latitude"));
                        longi.add(jo.getString("Longitude"));
                        wwid.add(jo.getString("lid"));

                    }

                    // ArrayAdapter<String> ad=new ArrayAdapter<>(Home.this,android.R.layout.simple_list_item_1,name);
                    //lv.setAdapter(ad);

                    l1.setAdapter(new Custom_workshop(view_nearby_wrksp.this, Workshopname,Place,Phone,Email));
                    l1.setOnItemClickListener(view_nearby_wrksp.this);

                } catch (Exception e) {
                    Log.d("=========", e.toString());
                }


            }

        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

                Toast.makeText(view_nearby_wrksp.this, "err" + error, Toast.LENGTH_SHORT).show();
            }
        }) {
            @NonNull
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("lattitude",String.valueOf(LocationService.lati));
                params.put("longitude",String.valueOf(LocationService.longi));
//                params.put("lid", sh.getString("lid", ""));
//                params.put("lid", sh.getString("lid", ""));
                return params;
            }
        };
        queue.add(stringRequest);



    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        SharedPreferences.Editor ed=sh.edit();
        ed.putString("wsid",wwid.get(i));
        ed.commit();


        AlertDialog.Builder ald=new AlertDialog.Builder(view_nearby_wrksp.this);
        ald.setTitle("VIEW SERVICES OR TRACK WORKSHOP ")
                .setPositiveButton("TRACK  ", new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface arg0, int arg1) {
                        try {

                            Intent ik = new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.google.com/maps?q="+lati.get(i)+","+longi.get(i)));
                            startActivity(ik);

                        }
                        catch(Exception e)
                        {
                            Toast.makeText(getApplicationContext(),e+"",Toast.LENGTH_LONG).show();
                        }

                    }
                })
                .setNegativeButton("BOOK ", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
//                        RequestQueue queue = Volley.newRequestQueue(view_nearby_wrksp.this);
//                        String url = "http://" + sh.getString("ip", "") + ":5000/sendreq";
//
//                        // Request a string response from the provided URL.
//                        StringRequest stringRequest = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
//                            @Override
//                            public void onResponse(String response) {
//                                // Display the response string.
//                                Log.d("+++++++++++++++++", response);
//                                try {
//                                    JSONObject json = new JSONObject(response);
//                                    String res = json.getString("task");
//
//                                    if (res.equalsIgnoreCase("success")) {
//                                        Toast.makeText(view_nearby_wrksp.this, " booking done successfully :)", Toast.LENGTH_SHORT).show();

                                        Intent ik = new Intent(getApplicationContext(), send_request.class);
                                        startActivity(ik);

//                                    } else {
//
//                                        Toast.makeText(view_nearby_wrksp.this, "please try again", Toast.LENGTH_SHORT).show();
//
//                                    }
//                                } catch (JSONException e) {
//                                    e.printStackTrace();
//                                }
//
//
//                            }
//                        }, new Response.ErrorListener() {
//                            @Override
//                            public void onErrorResponse(VolleyError error) {
//
//
//                                Toast.makeText(getApplicationContext(), "Error" + error, Toast.LENGTH_LONG).show();
//                            }
//                        }) {
//                            @Override
//                            protected Map<String, String> getParams() {
//                                Map<String, String> params = new HashMap<String, String>();
//
//
//                                params.put("wid", wwid.get(i));
//
//                                params.put("lid", sh.getString("lid", ""));
//
//
//                                return params;
//                            }
//                        };
//                        queue.add(stringRequest);
                    }
                });


        AlertDialog al = ald.create();
        al.show();


    }
    @Override
    public void onBackPressed() {
        Intent i=new Intent(getApplicationContext(),User_home.class);
        startActivity(i);
        super.onBackPressed();
    }
}