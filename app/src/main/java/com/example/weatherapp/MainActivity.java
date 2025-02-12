package com.example.weatherapp;

import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Looper;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;

import org.json.JSONArray;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.ExecutionException;

public class MainActivity extends AppCompatActivity {
    EditText cityName;
    Button search;
    TextView weatherInfo;
    ImageView weatherIcon;

    class GetWeather extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... urls) {
            StringBuilder result = new StringBuilder();
            try {
                URL url = new URL(urls[0]);
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.connect();

                InputStream inputStream = urlConnection.getInputStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));

                String line;
                while ((line = reader.readLine()) != null) {
                    result.append(line).append("\n");
                }
                return result.toString();
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            try {
                JSONObject jsonObject = new JSONObject(result);
                JSONObject main = jsonObject.getJSONObject("main");
                JSONArray weatherArray = jsonObject.getJSONArray("weather");
                JSONObject weather = weatherArray.getJSONObject(0);
                JSONObject wind = jsonObject.getJSONObject("wind");
                JSONObject sys = jsonObject.getJSONObject("sys");

                // Lấy thông tin
                String temperature = "Nhiệt độ: " + main.getString("temp") + "°C";
                String feelsLike = "Cảm giác như: " + main.getString("feels_like") + "°C";
                String humidity = "Độ ẩm: " + main.getString("humidity") + "%";
                String pressure = "Áp suất: " + main.getString("pressure") + " hPa";
                String windSpeed = "Gió: " + wind.getString("speed") + " m/s";


                // Hiển thị thông tin lên giao diện
                weatherInfo.setText(temperature + "\n" +
                        feelsLike + "\n" +
                        humidity + "\n" +
                        pressure + "\n" +
                        windSpeed + "\n"
                        );

                // Cập nhật biểu tượng thời tiết
                setWeatherIcon(weather.getString("main"));

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void setWeatherIcon(String condition) {
        switch (condition.toLowerCase()) {
            case "clear":
                weatherIcon.setImageResource(R.drawable.sun);
                break;
            case "clouds":
                weatherIcon.setImageResource(R.drawable.cloudy);
                break;
            case "rain":
                weatherIcon.setImageResource(R.drawable.rainy);
                break;
            case "storm":
                weatherIcon.setImageResource(R.drawable.storm);
                break;
            case "snow":
                weatherIcon.setImageResource(R.drawable.snowy);
                break;
            case "wind":
                weatherIcon.setImageResource(R.drawable.windy);
                break;
            default:
                weatherIcon.setImageResource(R.drawable.cloudy_sunny);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        cityName = findViewById(R.id.cityName);
        search = findViewById(R.id.search);
        weatherInfo = findViewById(R.id.weatherInfo);
        weatherIcon = findViewById(R.id.weatherIcon);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        // Kiểm tra và lấy vị trí
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
        } else {
            getCurrentLocation();
        }

        search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String city = cityName.getText().toString();
                if (city.isEmpty()) {
                    Toast.makeText(MainActivity.this, "Please enter a city name!", Toast.LENGTH_SHORT).show();
                    return;
                }
                String url = "https://api.openweathermap.org/data/2.5/weather?q=" + city + "&appid=928133397391e6af373468b74849e7ab&units=metric&lang=vi";
                new GetWeather().execute(url);
            }
        });
    }

    private FusedLocationProviderClient fusedLocationClient;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1001;
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getCurrentLocation();
            } else {
                Toast.makeText(this, "Quyền truy cập vị trí bị từ chối", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void getCurrentLocation() {
        LocationRequest locationRequest = new LocationRequest.Builder(
                Priority.PRIORITY_HIGH_ACCURACY, 5000
        ).build();

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        fusedLocationClient.requestLocationUpdates(locationRequest, new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                super.onLocationResult(locationResult);
                fusedLocationClient.removeLocationUpdates(this);

                if (locationResult != null && locationResult.getLastLocation() != null) {
                    Location location = locationResult.getLastLocation();
                    double latitude = location.getLatitude();
                    double longitude = location.getLongitude();

                    // Gọi API OpenWeatherMap với tọa độ GPS
                    String url = "https://api.openweathermap.org/data/2.5/weather?lat=" + latitude + "&lon=" + longitude + "&appid=928133397391e6af373468b74849e7ab&units=metric&lang=vi";
                    new GetWeather().execute(url);
                }
            }
        }, Looper.getMainLooper());
    }


}
