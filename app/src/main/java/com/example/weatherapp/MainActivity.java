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

import org.json.JSONArray;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
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

                // Lấy thời gian mặt trời mọc/lặn (định dạng thành giờ phút)
                long sunriseTimestamp = sys.getLong("sunrise") * 1000L;  // Chuyển sang mili-giây
                long sunsetTimestamp = sys.getLong("sunset") * 1000L;

                SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.getDefault());
                String sunrise = "Mặt trời mọc: " + sdf.format(new Date(sunriseTimestamp));
                String sunset = "Mặt trời lặn: " + sdf.format(new Date(sunsetTimestamp));

                // Hiển thị thông tin lên giao diện
                weatherInfo.setText(temperature + "\n" +
                        feelsLike + "\n" +
                        humidity + "\n" +
                        pressure + "\n" +
                        windSpeed + "\n" +
                        sunrise + "\n" +
                        sunset + "\n"
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

        search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String city = cityName.getText().toString();
                if (city.isEmpty()) {
                    Toast.makeText(MainActivity.this, "Please enter a city name!", Toast.LENGTH_SHORT).show();
                    return;
                }

                String url = "https://api.openweathermap.org/data/2.5/weather?q=" + city + "&appid=928133397391e6af373468b74849e7ab&units=metric&lang=vi";
                GetWeather task = new GetWeather();
                try {
                    task.execute(url).get();
                } catch (ExecutionException | InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
    }
}
