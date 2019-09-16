package com.fire.hackathon;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;

import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;

import android.location.LocationManager;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.support.v7.app.AlertDialog;

import com.google.android.gms.location.FusedLocationProviderClient;
//import com.google.android.gms.location.LocationListener;
//import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import com.squareup.okhttp.FormEncodingBuilder;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class Reportact extends AppCompatActivity {

    private Button capture;
    private Button submit;
    private EditText describe;
    String lat, lon;

    //TextView tv;
    EditText ip,lat_text,lon_text;

    Context context = this;
    private static final int CAMERA_REQUEST_CODE = 1;
    private ProgressDialog mProgress;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reportact);


        capture = (Button) findViewById(R.id.capture);
        submit = (Button) findViewById(R.id.submit);
        describe = (EditText) findViewById(R.id.describe);

        ip = (EditText)findViewById(R.id.ip);
        lat_text = (EditText)findViewById(R.id.lat_text);
        lon_text = (EditText)findViewById(R.id.lon_text);


        capture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                fetchLocation();
                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(intent, CAMERA_REQUEST_CODE);
            }
        });

        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {

                //fetchLocation();

                String text = describe.getText().toString();
                try {
                    if (describe.getText().length() > 10 && image != null) {
                        syncServer();
                        submit.setEnabled(false);
                        Toast.makeText(getApplicationContext(), "Sending data", Toast.LENGTH_SHORT).show();
                    }
                    else {
                        Toast.makeText(getApplicationContext(), "Incomplete submission!!", Toast.LENGTH_SHORT).show();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }


        });
    }

    MyLocation locationM = new MyLocation("0","0");

    @SuppressLint("MissingPermission")
    public void fetchLocation() {

//        LocationManager locationManager = (LocationManager) context.getSystemService(LOCATION_SERVICE);


        // Acquire a reference to the system Location Manager
        LocationManager locationManager =
                (LocationManager) Reportact.this.getSystemService(Context.LOCATION_SERVICE);
        // Define a listener that responds to location updates
        LocationListener locationListener = new LocationListener() {
            public void onLocationChanged(Location location) {
                // Called when a new location is found by the network location provider.
                lat = Double.toString(location.getLatitude());
                lon = Double.toString(location.getLongitude());
//                TextView tv = (TextView) findViewById(R.id.txtLoc);
//                tv.setText("Your Location is:" + lat + "--" + lon);
                locationM = new MyLocation(lat_text.getText().toString(),lon_text.getText().toString());
            }

            public void onStatusChanged(String provider, int status, Bundle extras) {
            }

            public void onProviderEnabled(String provider) {
            }

            public void onProviderDisabled(String provider) {
            }
        };
        // Register the listener with the Location Manager to receive location updates
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
    }


    String image = null;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CAMERA_REQUEST_CODE && resultCode == RESULT_OK) {
            Bundle extras = data.getExtras();
            Bitmap imageBitmap = (Bitmap) extras.get("data");

            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            imageBitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
            byte[] byteArray = byteArrayOutputStream.toByteArray();

            image = Base64.encodeToString(byteArray, Base64.DEFAULT);
            // TODO: send to server
        }
    }

    protected void syncServer() throws IOException {
        Thread thread = new Thread(new Runnable() {

            @Override
            public void run() {
                try {

                    OkHttpClient client = new OkHttpClient();

                    Map map = new HashMap();
                    map.put("Latitude",lat_text.getText().toString());
                    map.put("Longitude",lon_text.getText().toString());

                    JSONObject jsonObject = new JSONObject();
                    jsonObject.put("Description", describe.getText().toString());
                    jsonObject.put("Location", new JSONObject(map));
                    jsonObject.put("imageAsBase64", image);
                    String json = jsonObject.toString();
                    Log.d("json",json);


                    PostData("http://"+ip.getText().toString()+":5001/api/report",json);
//                    new SendDeviceDetails().execute("http://10.0.2.2:5000/api/report",json);


//                    RequestBody formBody = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), json);
//
//                    Request request = new Request.Builder()
//                            .url("http://10.0.2.2:5000/api/report")
//                            .post(formBody)
////                            .get()
//                            .build();
//
//
//                    Response response = client.newCall(request).execute();
//                    final String res = response.body().string();
//                    runOnUiThread(new Runnable() {
//                        @Override
//                        public void run() {
//                            describe.setText(res);
//                        }
//                    });
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        thread.start();

    }

    void PostData(String url,String json) throws Exception{
        // 1. create HttpClient
        HttpClient httpclient = new DefaultHttpClient();

        //System.Net.ServicePointManager.SecurityProtocol = SecurityProtocolType.Tls12 | SecurityProtocolType.Tls11 | SecurityProtocolType.Tls;

        // 2. make POST request to the given URL
        HttpPost httpPost = new HttpPost(url);

        // 5. set json to StringEntity
        StringEntity se = new StringEntity(json);

        // 6. set httpPost Entity
        httpPost.setEntity(se);

        // 7. Set some headers to inform server about the type of the content
        httpPost.setHeader("Accept", "application/json");
        httpPost.setHeader("Content-type", "application/json");
        // 8. Execute POST request to the given URL
        HttpResponse httpResponse = httpclient.execute(httpPost);
        // 9. receive response as inputStream
        InputStream inputStream = httpResponse.getEntity().getContent();

        String result;
        // 10. convert inputstream to string
        if(inputStream != null) {
            result = convertStreamToString(inputStream);
            final String response = result;

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (response.contains("yes")) {
                        Toast.makeText(getApplicationContext(), "Successful!!", Toast.LENGTH_LONG).show();

                    } else {
                        Toast.makeText(getApplicationContext(), "Bad Submission!!", Toast.LENGTH_LONG).show();
                    }
                    Intent intent = new Intent(Reportact.this,MainActivity.class);
                    startActivity(intent);
                }
            });

        }
        else
            result = "Did not work!";

    }

    private String convertStreamToString(InputStream is) {
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        StringBuilder sb = new StringBuilder();

        String line = null;
        try {
            while ((line = reader.readLine()) != null) {
                sb.append(line).append('\n');
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                is.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return sb.toString();
    }
}
