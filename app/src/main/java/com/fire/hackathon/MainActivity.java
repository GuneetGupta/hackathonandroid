package com.fire.hackathon;

import android.content.Intent;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.TileOverlayOptions;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Scanner;

public class MainActivity extends FragmentActivity implements OnMapReadyCallback {
    private Button report;

    private EditText ipee;

    GoogleMap map;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ipee = (EditText)findViewById(R.id.ipee);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        report = findViewById(R.id.report);
        report.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openreportact();
            }
        });
    }

    public void openreportact(){
        Intent intent = new Intent(this,Reportact.class);
        startActivity(intent);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {

        map = googleMap;
        addMarker(21,78,"Test",4 );

        GetLocation();

    }

    void addMarker(final double lat, final double lon, final String title, final int count){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                LatLng loc = new LatLng(lat,lon);
                Marker marker = map.addMarker(new MarkerOptions().position(loc).title(title).snippet("Count reported: "+count));
                markers.add(marker);
                //map.moveCamera(CameraUpdateFactory.newLatLng(loc));
            }
        });
    }

    void clearMap(){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                for(int i=0;i<markers.size();i++){
                    markers.get(i).remove();
                }
                markers.clear();
            }
        });
    }

    ArrayList<Marker> markers = new ArrayList<Marker>();

    void GetLocation(){
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    try {
                        OkHttpClient client = new OkHttpClient();
                        Request request = new Request.Builder()
                                .url("http://" + ipee.getText().toString() + ":5001/api/location")
                                .build();

                        Response response = client.newCall(request).execute();
                        String responseText = response.body().string();

                        JSONArray array = new JSONArray(responseText);
                        int n = array.length();
//                    Log.d("array",String.valueOf(n));
                        clearMap();
                        for (int i = 0; i < n; ++i) {
                            final JSONObject location = array.getJSONObject(i);
                            Iterator<String> keys = location.keys();

                            while(keys.hasNext()) {
                                String key = keys.next();
                                if (location.get(key) instanceof JSONObject) {
                                    Log.d("key",key);
                                }
                            }
//                            Log.d("key", location.keys<String>()[0]);
                            double lat = location.getDouble("latitude");
                            double lon = location.getDouble("longitude");
                            String title = location.getString("description");
                            int count = location.getInt("count");
                            addMarker(lat, lon, title,count);
                        }


                    } catch (Exception e) {
//                    Log.d("error",e.getMessage());
                        e.printStackTrace();
                    }
                    try {
                        Thread.sleep(10*1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                        break;
                    }
                }
            }
        });
        t.start();
    }



    private ArrayList<LatLng> readItems(int resource) throws JSONException {
        ArrayList<LatLng> list = new ArrayList<LatLng>();
        InputStream inputStream = getResources().openRawResource(resource);
        String json = new Scanner(inputStream).useDelimiter("\\A").next();
        JSONArray array = new JSONArray(json);
        for (int i = 0; i < array.length(); i++) {
            JSONObject object = array.getJSONObject(i);
            double lat = object.getDouble("lat");
            double lng = object.getDouble("lng");
            list.add(new LatLng(lat, lng));
        }
        return list;
    }

}
