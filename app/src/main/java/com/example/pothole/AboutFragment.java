package com.example.pothole;

import android.location.Geocoder;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.gson.Gson;
import com.mapbox.android.core.location.LocationEngine;
import com.mapbox.android.core.location.LocationEngineCallback;
import com.mapbox.android.core.location.LocationEngineProvider;
import com.mapbox.android.core.location.LocationEngineRequest;
import com.mapbox.android.core.location.LocationEngineResult;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class AboutFragment extends Fragment {
    private TextView userGreeting, weatherCondition, weatherTemperature, dateDay, dateMonth, locationText;
    private ImageView weatherIcon, dateIcon, locationIcon;
    private LocationEngine locationEngine;
    private LocationCallback locationEngineCallback;
    private final String WEATHER_API_KEY = "cdd4bd831d2d6fbf10fe92e701d0dbdf";

    private double cachedLatitude = 0.0;
    private double cachedLongitude = 0.0;
    private String cachedWeather = null;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_about, container, false);

        // Ánh xạ view
        userGreeting = view.findViewById(R.id.userGreeting);
        weatherCondition = view.findViewById(R.id.weatherCondition);
        weatherTemperature = view.findViewById(R.id.weatherTemperature);
        dateDay = view.findViewById(R.id.dateDay);
        dateMonth = view.findViewById(R.id.dateMonth);
        locationText = view.findViewById(R.id.locationText);
        weatherIcon = view.findViewById(R.id.weatherIcon);
        dateIcon = view.findViewById(R.id.dateIcon);
        locationIcon = view.findViewById(R.id.locationIcon);

        // Cập nhật giao diện
        userGreeting.setText("Hello, Quoc Bao");
        updateDate();

        initializeLocationEngine();

        return view;
    }

    private void initializeLocationEngine() {
        locationEngine = LocationEngineProvider.getBestLocationEngine(requireContext());
        locationEngineCallback = new LocationCallback(this);

        LocationEngineRequest request = new LocationEngineRequest.Builder(5000L)
                .setPriority(LocationEngineRequest.PRIORITY_HIGH_ACCURACY)
                .setMaxWaitTime(10000L)
                .build();

        locationEngine.requestLocationUpdates(request, locationEngineCallback, requireActivity().getMainLooper());
    }

    private void updateWeather(double latitude, double longitude) {
        if (Math.abs(cachedLatitude - latitude) < 0.01 && Math.abs(cachedLongitude - longitude) < 0.01 && cachedWeather != null) {
            updateUI(() -> {
                weatherCondition.setText(cachedWeather.split(", ")[0]); // Mô tả thời tiết
                weatherTemperature.setText(cachedWeather.split(", ")[1]); // Nhiệt độ
            });
            return;
        }

        String url = "https://api.openweathermap.org/data/2.5/weather?lat=" + latitude +
                "&lon=" + longitude + "&appid=" + WEATHER_API_KEY + "&units=metric";

        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(5, TimeUnit.SECONDS)
                .readTimeout(5, TimeUnit.SECONDS)
                .retryOnConnectionFailure(true)
                .build();

        Request request = new Request.Builder().url(url).build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                updateUI(() -> weatherCondition.setText("Không thể lấy dữ liệu thời tiết"));
                Log.e("WeatherAPI", "Request failed", e);
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful() && response.body() != null) {
                    try {
                        String responseBody = response.body().string();
                        Gson gson = new Gson();
                        WeatherResponse weatherResponse = gson.fromJson(responseBody, WeatherResponse.class);

                        double temp = weatherResponse.main.temp;
                        String condition = weatherResponse.weather[0].description;

                        cachedLatitude = latitude;
                        cachedLongitude = longitude;
                        cachedWeather = capitalizeFirstLetter(condition) + ", " + temp + "°C";

                        updateUI(() -> {
                            weatherCondition.setText(capitalizeFirstLetter(condition));
                            weatherTemperature.setText(temp + "°C");
                        });
                    } catch (Exception e) {
                        updateUI(() -> weatherCondition.setText("Lỗi khi xử lý dữ liệu thời tiết"));
                        Log.e("WeatherAPI", "Error parsing response", e);
                    }
                } else {
                    updateUI(() -> weatherCondition.setText("Lỗi API"));
                    Log.e("WeatherAPI", "API response error");
                }
            }
        });
    }

    private void updateLocation(double latitude, double longitude) {
        Geocoder geocoder = new Geocoder(requireContext(), Locale.forLanguageTag("vi"));
        try {
            List<android.location.Address> addresses = geocoder.getFromLocation(latitude, longitude, 1);
            if (addresses != null && !addresses.isEmpty()) {
                String city = addresses.get(0).getSubAdminArea();
                String adminArea = addresses.get(0).getAdminArea();
                String location = city != null ? city + ", " + adminArea : adminArea;

                updateUI(() -> locationText.setText(location));
            } else {
                updateUI(() -> locationText.setText("Không xác định vị trí"));
            }
        } catch (IOException e) {
            updateUI(() -> locationText.setText("Lỗi khi lấy vị trí"));
            Log.e("Geocoder", "Error fetching location", e);
        }
    }

    private void updateDate() {
        SimpleDateFormat dayFormat = new SimpleDateFormat("EEEE", Locale.ENGLISH);
        SimpleDateFormat monthFormat = new SimpleDateFormat("MMM yyyy", Locale.ENGLISH);

        dateDay.setText(dayFormat.format(new Date()));
        dateMonth.setText(monthFormat.format(new Date()));
    }

    private String capitalizeFirstLetter(String input) {
        return input.substring(0, 1).toUpperCase() + input.substring(1);
    }

    private void updateUI(Runnable task) {
        if (isAdded() && getView() != null) {
            requireActivity().runOnUiThread(task);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (locationEngine != null) {
            locationEngine.removeLocationUpdates(locationEngineCallback);
        }
    }

    private static class LocationCallback implements LocationEngineCallback<LocationEngineResult> {
        private final WeakReference<AboutFragment> fragmentRef;

        LocationCallback(AboutFragment fragment) {
            this.fragmentRef = new WeakReference<>(fragment);
        }

        @Override
        public void onSuccess(LocationEngineResult result) {
            AboutFragment fragment = fragmentRef.get();
            if (fragment != null && fragment.isAdded()) {
                if (result.getLastLocation() != null) {
                    double latitude = result.getLastLocation().getLatitude();
                    double longitude = result.getLastLocation().getLongitude();

                    fragment.updateWeather(latitude, longitude);
                    fragment.updateLocation(latitude, longitude);
                }
            }
        }

        @Override
        public void onFailure(@NonNull Exception exception) {
            AboutFragment fragment = fragmentRef.get();
            if (fragment != null && fragment.isAdded()) {
                fragment.updateUI(() -> fragment.locationText.setText("Không thể lấy vị trí"));
                Log.e("LocationEngine", "Error fetching location", exception);
            }
        }
    }

    // Lớp phản hồi thời tiết cho Gson
    static class WeatherResponse {
        Main main;
        Weather[] weather;

        static class Main {
            double temp;
        }

        static class Weather {
            String description;
        }
    }
}
