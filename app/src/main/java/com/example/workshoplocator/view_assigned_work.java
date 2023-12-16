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
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class view_assigned_work extends AppCompatActivity implements AdapterView.OnItemClickListener {

    ListView l1;
    SharedPreferences sh;
    ArrayList<String> fname,Date,Type,rid,lati,longi;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_assigned_work);
        l1=findViewById(R.id.listview05);
        sh= PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

        String url = "http://" + sh.getString("ip", "") + ":5000/view_assigned_work";
        RequestQueue queue = Volley.newRequestQueue(view_assigned_work.this);
        StringRequest stringRequest = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                // Display the response string.
                Log.d("+++++++++++++++++", response);
                try {

                    JSONArray ar = new JSONArray(response);

                    fname = new ArrayList<>();
                    Date = new ArrayList<>();
                    Type = new ArrayList<>();
                    rid = new ArrayList<>();
                    lati = new ArrayList<>();
                    longi = new ArrayList<>();

                    for (int i = 0; i < ar.length(); i++) {
                        JSONObject jo = ar.getJSONObject(i);
                        fname.add(jo.getString("Firstname")+jo.getString("Lastname"));
                        Date.add(jo.getString("Date"));
                        Type.add(jo.getString("Type"));
                        rid.add(jo.getString("rid"));
                        lati.add(jo.getString("Latitude"));
                        longi.add(jo.getString("Logitude"));

                    }

                    // ArrayAdapter<String> ad=new ArrayAdapter<>(Home.this,android.R.layout.simple_list_item_1,name);
                    //lv.setAdapter(ad);

                    l1.setAdapter(new custom3(view_assigned_work.this, fname, Date, Type));
                    l1.setOnItemClickListener(view_assigned_work.this);

                } catch (Exception e) {
                    Log.d("=========", e.toString());
                }


            }

        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

                Toast.makeText(view_assigned_work.this, "err" + error, Toast.LENGTH_SHORT).show();
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

        AlertDialog.Builder ald=new AlertDialog.Builder(view_assigned_work.this);
        ald.setTitle("Options")
                .setPositiveButton(" Track ", new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface arg0, int arg1) {
                        try
                        {


                            Intent intent = new Intent(Intent.ACTION_VIEW,
                                    Uri.parse("http://maps.google.com/maps?q="+lati.get(i)+","+longi.get(i)));
                            startActivity(intent);


                        }
                        catch(Exception e)
                        {
                            Toast.makeText(getApplicationContext(),e+"",Toast.LENGTH_LONG).show();
                        }

                    }
                })
                .setNegativeButton(" Close ", new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface arg0, int arg1) {


                    }
                });

        AlertDialog al=ald.create();
        al.show();

    }
    @Override
    public void onBackPressed() {
        Intent i=new Intent(getApplicationContext(),Login.class);
        startActivity(i);
        super.onBackPressed();
    }
}