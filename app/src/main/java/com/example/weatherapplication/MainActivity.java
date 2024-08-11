package com.example.weatherapplication;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.airbnb.lottie.LottieAnimationView;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    private FusedLocationProviderClient fusedLocationClient;
    private LocationManager locationManager;
    private boolean permissionDeniedOnce = false;
    private LottieAnimationView lodar;
    private TextView temp;
    private TextView minTemp;
    private TextView maxText;
    private TextView speed;
    private TextInputEditText city;
    private TextView humidity;

    double latitude = -1.00;
    double longitude = -1.00;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        minTemp = findViewById(R.id.minTemp);
        temp = findViewById(R.id.currTemp);
        maxText = findViewById(R.id.maxTemp);
        humidity = findViewById(R.id.edHumidity);
        speed = findViewById(R.id.edSpeed);
        lodar = findViewById(R.id.lodar);
        city = findViewById(R.id.etCity);

        lodar.setVisibility(View.VISIBLE);
        lodar.playAnimation();
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");
        String currentDateAndTime = sdf.format(new Date());
        TextView currDate = findViewById(R.id.date);
        currDate.setText(currentDateAndTime);

        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            promptEnableLocation();
            checkLocationPermission();
        } else {
            checkLocationPermission();
        }

    }

    private void fetch(){
        if(longitude == -1.00 && latitude == -1.00){
            getLastLocation();
        }else{
            String url = "https://api.api-ninjas.com/v1/weather?lat="+latitude+"&lon="+longitude;

            CustomJsonObjectRequest jsonObjectRequest = new CustomJsonObjectRequest(
                    Request.Method.GET,
                    url,
                    null,
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            try {
                                String currTemp = response.getString("temp");
                                String getMin = response.getString("min_temp");
                                String getMax = response.getString("max_temp");
                                String humidity1 = response.getString("humidity");
                                String windSpeed1 = response.getString("wind_speed");

                                humidity.setText(humidity1);
                                temp.setText(currTemp+"°");
                                minTemp.setText(getMin);
                                speed.setText(windSpeed1);
                                maxText.setText(getMax);
                                lodar.pauseAnimation();
                                lodar.setVisibility(View.INVISIBLE);
                            } catch (JSONException e) {
                                throw new RuntimeException(e);
                            }
                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            Log.e("Error", error.toString());
                        }
                    }
            );

            Map<String, String> headers = new HashMap<>();
            headers.put("X-Api-Key", "b0aXc26qSica3VXSO2re4g==js3OqO5reZjDLcFj");
            jsonObjectRequest.setCustomHeaders(headers);

            Singleton.getInstance(this).addToRequestQueue(jsonObjectRequest);
        }
    }

    private void promptEnableLocation() {
        Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
        startActivity(intent);
    }

    private void checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
        } else {
            getLastLocation();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    getLastLocation();
                }
            } else {
                if (!permissionDeniedOnce) {
                    permissionDeniedOnce = true;
                    AlertDialog.Builder alertBuilder = new AlertDialog.Builder(MainActivity.this);
                    alertBuilder.setMessage("You need to give Location Permissions to continue.");
                    alertBuilder.setPositiveButton("Grant Permissions", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            permissionDeniedOnce = false;
                            checkLocationPermission();
                        }
                    });
                    AlertDialog alert = alertBuilder.create();
                    alert.show();
                }
            }
        }
    }

    private void getLastLocation() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.getLastLocation()
                    .addOnCompleteListener(this, new OnCompleteListener<Location>() {
                        @Override
                        public void onComplete(@NonNull Task<Location> task) {
                            if (task.isSuccessful() && task.getResult() != null) {
                                Location location = task.getResult();
                                latitude = location.getLatitude();
                                longitude = location.getLongitude();
                                Log.d("Location",latitude+" "+longitude);
                                fetch();
                            } else {
                                Log.w("location", "Failed to get location.");
                            }
                        }
                    });
        } else {
            checkLocationPermission();
        }
    }

    private void getLatLongFromCityName(String cityName) {
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocationName(cityName, 1);
            if (addresses.size() > 0) {
                Address address = addresses.get(0);
                latitude = address.getLatitude();
                longitude = address.getLongitude();
                System.out.println("Latitude: " + latitude + ", Longitude: " + longitude);
                fetch();
            } else {
                // snackbar
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void searchCity(View view) {
        if(city.getText().toString().equals("")){
            //snackbar
        }else{
            getLatLongFromCityName(city.getText().toString());
            lodar.setVisibility(View.VISIBLE);
            lodar.playAnimation();
            getLastLocation();
        }
    }
}
