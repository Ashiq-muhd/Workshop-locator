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

public class view_product_buy extends AppCompatActivity implements AdapterView.OnItemClickListener {

    ListView l1;
    SharedPreferences sh;
    ArrayList<String> pname,image,vcl_type,amount,details,quantity,wid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_product_buy);
        l1=findViewById(R.id.listview07);
        sh= PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

        String url = "http://" + sh.getString("ip", "") + ":5000/viewproducts";
        RequestQueue queue = Volley.newRequestQueue(view_product_buy.this);

        StringRequest stringRequest = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                // Display the response string.
                Log.d("+++++++++++++++++", response);
                try {

                    JSONArray ar = new JSONArray(response);

                    pname = new ArrayList<>();
                    image = new ArrayList<>();
                    wid = new ArrayList<>();
                    details = new ArrayList<>();
                    vcl_type = new ArrayList<>();
                    amount = new ArrayList<>();
                    quantity = new ArrayList<>();


                    for (int i = 0; i < ar.length(); i++) {
                        JSONObject jo = ar.getJSONObject(i);
                        pname.add(jo.getString("pname"));
                        image.add(jo.getString("photo"));
                        wid.add(jo.getString("pid"));
                        details.add(jo.getString("Details"));
                        vcl_type.add(jo.getString("vechile_type"));
                        amount.add(jo.getString("Amount"));
                        quantity.add(jo.getString("Stock"));

                    }

                    // ArrayAdapter<String> ad=new ArrayAdapter<>(Home.this,android.R.layout.simple_list_item_1,name);
                    //lv.setAdapter(ad);

                    l1.setAdapter(new custom3(view_product_buy.this, pname,image,quantity));
                    l1.setOnItemClickListener(view_product_buy.this);

                } catch (Exception e) {
                    Log.d("=========", e.toString());
                }


            }

        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

                Toast.makeText(view_product_buy.this, "err" + error, Toast.LENGTH_SHORT).show();
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

    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        Intent ik = new Intent(getApplicationContext(),product_details.class);
        ik.putExtra("pname",pname.get(i));
        ik.putExtra("photo",image.get(i));
        ik.putExtra("pid",wid.get(i));
        ik.putExtra("vechile_type",vcl_type.get(i));
        ik.putExtra("Amount",amount.get(i));
        ik.putExtra("Stock",quantity.get(i));
        ik.putExtra("Details",details.get(i));


        startActivity(ik);
    }
    @Override
    public void onBackPressed() {
        Intent i=new Intent(getApplicationContext(),workshop_list.class);
        startActivity(i);
        super.onBackPressed();
    }
}