package com.example.pothole;

import static android.location.Location.distanceBetween;
import static android.webkit.URLUtil.isValidUrl;

import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.TextView;

import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.formatter.ValueFormatter;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.utils.ColorTemplate;

import java.util.ArrayList;
import java.util.List;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.pothole.model.Pothole;
import com.example.pothole.network.ApiClient;
import com.example.pothole.network.ApiService;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import com.mapbox.android.core.location.LocationEngine;
import com.mapbox.android.core.location.LocationEngineCallback;
import com.mapbox.android.core.location.LocationEngineProvider;
import com.mapbox.android.core.location.LocationEngineRequest;
import com.mapbox.android.core.location.LocationEngineResult;
import com.mapbox.geojson.Point;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class AboutFragment extends Fragment {
    private TextView userGreeting, weatherCondition, nearbyPotholesInfo, selectedRadius, weatherTemperature, dateDay, dateMonth, locationText, level1, level2, level3, PotholeCount;
    private ImageView weatherIcon, dateIcon, locationIcon, UserAvatar;
    private LocationEngine locationEngine;
    private LocationCallback locationEngineCallback;
    private SeekBar radiusSeekBar;
    private final String WEATHER_API_KEY = "cdd4bd831d2d6fbf10fe92e701d0dbdf";
    private double cachedLatitude = 0.0;
    private double cachedLongitude = 0.0;
    private String cachedWeather = null;
    private PieChart pieChart;
    private final int DEFAULT_RADIUS = 500;
    private int currentRadius = DEFAULT_RADIUS;
    private LinearLayout numberLayout;
    private RadioGroup radioGroup;
    private static final String API_URL = "http://BackendPothole-env.eba-eggp9dp7.ap-southeast-1.elasticbeanstalk.com/api/pothole/list";
    private RadioButton radioNumbers, radioChart;
    private OkHttpClient getOkHttpClient() {
        return new OkHttpClient.Builder()
                .connectTimeout(60, TimeUnit.SECONDS) // Thời gian chờ kết nối
                .readTimeout(60, TimeUnit.SECONDS)    // Thời gian chờ đọc dữ liệu
                .writeTimeout(60, TimeUnit.SECONDS)   // Thời gian chờ ghi dữ liệu
                .retryOnConnectionFailure(true)      // Tự động thử lại khi kết nối thất bại
                .build();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_about, container, false);

        userGreeting = view.findViewById(R.id.userGreeting);
        weatherCondition = view.findViewById(R.id.weatherCondition);
        weatherTemperature = view.findViewById(R.id.weatherTemperature);
        UserAvatar = view.findViewById(R.id.userAvatar);
        dateDay = view.findViewById(R.id.dateDay);
        dateMonth = view.findViewById(R.id.dateMonth);
        locationText = view.findViewById(R.id.locationText);
        weatherIcon = view.findViewById(R.id.weatherIcon);
        dateIcon = view.findViewById(R.id.dateIcon);
        locationIcon = view.findViewById(R.id.locationIcon);
        level1 = view.findViewById(R.id.level1);
        level2 = view.findViewById(R.id.level2);
        level3 = view.findViewById(R.id.level3);
        numberLayout = view.findViewById(R.id.numbersLayout);
        PotholeCount = view.findViewById(R.id.potholeCount);
        radioGroup = view.findViewById(R.id.radioGroup);
        numberLayout = view.findViewById(R.id.numbersLayout);
        pieChart = view.findViewById(R.id.pieChart);
        radiusSeekBar = view.findViewById(R.id.radiusSeekBar);
        selectedRadius = view.findViewById(R.id.selectedRadius);
        nearbyPotholesInfo = view.findViewById(R.id.nearbyPotholesInfo);
        selectedRadius.setText(getString(R.string.distance_label, currentRadius));
        radiusSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                currentRadius = progress * 10; // Cập nhật bán kính hiện tại
                selectedRadius.setText(getString(R.string.distance_label, currentRadius));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                // Không làm gì
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                fetchPotholes(currentRadius); // Gọi fetchPotholes với bán kính mới
            }
        });

        radioGroup.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.radioNumbers) {
                numberLayout.setVisibility(View.VISIBLE);
                pieChart.setVisibility(View.GONE);
            } else if (checkedId == R.id.radioChart) {
                numberLayout.setVisibility(View.GONE);
                pieChart.setVisibility(View.VISIBLE);
                setupPieChart(); // Cập nhật biểu đồ
            }
        });
        updateDate();
        loadUserInfo();
        initializeLocationEngine();
        PotholeCount();
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
        locationEngine.getLastLocation(locationEngineCallback);
    }

    private void updateWeather(double latitude, double longitude) {
        if (Math.abs(cachedLatitude - latitude) < 0.01 && Math.abs(cachedLongitude - longitude) < 0.01 && cachedWeather != null) {
            updateUI(() -> {
                weatherCondition.setText(cachedWeather.split(", ")[0]); // Mô tả thời tiết
                weatherTemperature.setText(cachedWeather.split(", ")[1]); // Nhiệt độ
            });
            return;
        }

        SharedPreferences sharedPreferences = requireContext().getSharedPreferences("AppSettings", Context.MODE_PRIVATE);
        String language = sharedPreferences.getString("language", "en"); // Mặc định là tiếng Anh

        String url = "https://api.openweathermap.org/data/2.5/weather?lat=" + latitude +
                "&lon=" + longitude + "&appid=" + WEATHER_API_KEY + "&units=metric&lang=" + language;

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
                        updateUI(() -> weatherCondition.setText(getString(R.string.weather_unavailable)));
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
                updateUI(() -> locationText.setText(getString(R.string.location_unavailable)));
            }
        } catch (IOException e) {
            updateUI(() -> locationText.setText(getString(R.string.location_unavailable)));
            Log.e("Geocoder", "Error fetching location", e);
        }
    }

    private void updateDate() {
        SharedPreferences sharedPreferences = requireContext().getSharedPreferences("AppSettings", Context.MODE_PRIVATE);
        String language = sharedPreferences.getString("language", "en");
        Locale locale = new Locale(language);
        Locale.setDefault(locale);
        SimpleDateFormat dayFormat = new SimpleDateFormat("EEEE", locale);
        SimpleDateFormat monthFormat = new SimpleDateFormat("MMM yyyy", locale);
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

                    fragment.cachedLatitude = latitude;
                    fragment.cachedLongitude = longitude;

                    fragment.updateWeather(latitude, longitude);
                    fragment.updateLocation(latitude, longitude);

                    // Gọi fetchPotholes với bán kính hiện tại
                    fragment.fetchPotholes(fragment.currentRadius);
                }
            }
        }

        @Override
        public void onFailure(@NonNull Exception exception) {
            AboutFragment fragment = fragmentRef.get();
            if (fragment != null && fragment.isAdded()) {
                fragment.updateUI(() -> fragment.locationText.setText("Unable to fetch location"));
                Log.e("LocationEngine", "Error fetching location", exception);
            }
        }
    }

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

    private void loadUserInfo() {
        SharedPreferences sharedPreferences = requireActivity().getSharedPreferences("LoginPrefs", requireActivity().MODE_PRIVATE);

        String name = sharedPreferences.getString("username", "Guest");
        String avatarString = sharedPreferences.getString("avatar", "");

        userGreeting.setText(getString(R.string.hello_user, name));

        if (!avatarString.isEmpty()) {
            if (isValidUrl(avatarString)) {
                Glide.with(this).load(avatarString).into(UserAvatar);
            } else {
                try {
                    if (avatarString.startsWith("data:image")) {
                        avatarString = avatarString.substring(avatarString.indexOf(",") + 1);
                    }
                    byte[] decodedString = android.util.Base64.decode(avatarString, android.util.Base64.DEFAULT);
                    Bitmap decodedBitmap = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                    UserAvatar.setImageBitmap(decodedBitmap);
                } catch (Exception e) {
                    e.printStackTrace();
                    UserAvatar.setImageResource(R.drawable.ic_avatar_default);
                }
            }
        } else {
            UserAvatar.setImageResource(R.drawable.ic_avatar_default);
        }
    }

    private void PotholeCount() {
        OkHttpClient client = getOkHttpClient();

        // Tạo yêu cầu HTTP
        Request request = new Request.Builder()
                .url("http://BackendPothole-env.eba-eggp9dp7.ap-southeast-1.elasticbeanstalk.com/api/pothole/list") // URL API
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                // Xử lý lỗi kết nối
                Log.e("PotholeAPI", "Lỗi kết nối: " + e.getMessage());
                requireActivity().runOnUiThread(() ->
                        Toast.makeText(requireContext(), "Lỗi kết nối: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                );
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful() && response.body() != null) {
                    String responseBody = response.body().string();
                    List<Pothole> potholes = parsePotholeList(responseBody);

                    if (potholes == null) {
                        requireActivity().runOnUiThread(() ->
                                Toast.makeText(requireContext(), "Lỗi xử lý dữ liệu từ API!", Toast.LENGTH_SHORT).show()
                        );
                        return;
                    }

                    int[] potholeCounts = new int[3];

                    // Tính toán số lượng ổ gà theo mức độ
                    for (Pothole pothole : potholes) {
                        switch (pothole.getSeverity()) {
                            case 1:
                                potholeCounts[0]++;
                                break;
                            case 2:
                                potholeCounts[1]++;
                                break;
                            case 3:
                                potholeCounts[2]++;
                                break;
                        }
                    }

                    updateUI(() -> {
                        level1.setText("" + potholeCounts[0]);
                        level2.setText("" + potholeCounts[1]);
                        level3.setText("" + potholeCounts[2]);
                        PotholeCount.setText("" + (potholeCounts[0] + potholeCounts[1] + potholeCounts[2]));
                    });

                } else {
                    // Xử lý khi API trả về lỗi
                    Log.e("PotholeAPI", "API trả về lỗi: " + response.code());
                    requireActivity().runOnUiThread(() ->
                            Toast.makeText(requireContext(), "Không thể tải dữ liệu ổ gà!", Toast.LENGTH_SHORT).show()
                    );
                }
            }
        });
    }

    private void setupPieChart() {
        int countLevel1 = parseSafeInt(level1.getText().toString());
        int countLevel2 = parseSafeInt(level2.getText().toString());
        int countLevel3 = parseSafeInt(level3.getText().toString());

        // Chuẩn bị dữ liệu biểu đồ
        List<PieEntry> entries = new ArrayList<>();
        if (countLevel1 > 0) entries.add(new PieEntry(countLevel1, getString(R.string.level1)));
        if (countLevel2 > 0) entries.add(new PieEntry(countLevel2, getString(R.string.level2)));
        if (countLevel3 > 0) entries.add(new PieEntry(countLevel3, getString(R.string.level3)));

        PieDataSet dataSet = new PieDataSet(entries, "");
        dataSet.setColors(ColorTemplate.MATERIAL_COLORS);
        dataSet.setValueTextSize(16f);
        dataSet.setValueTypeface(Typeface.DEFAULT_BOLD);

        PieData data = new PieData(dataSet);
        data.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                return String.valueOf((int) value); // Hiển thị số nguyên
            }
        });

        pieChart.setUsePercentValues(false);
        pieChart.setData(data);
        pieChart.setCenterText(getString(R.string.pothole_levels) + "\n" +
                getString(R.string.total) + ": " + (countLevel1 + countLevel2 + countLevel3));
        pieChart.setCenterTextSize(20f);
        pieChart.setHoleRadius(50f);

        pieChart.getDescription().setEnabled(false);

        // Cấu hình chú thích
        Legend legend = pieChart.getLegend();
        legend.setVerticalAlignment(Legend.LegendVerticalAlignment.BOTTOM);
        legend.setHorizontalAlignment(Legend.LegendHorizontalAlignment.CENTER);
        legend.setOrientation(Legend.LegendOrientation.HORIZONTAL);
        legend.setTextSize(14f);
        legend.setTypeface(Typeface.DEFAULT_BOLD);

        pieChart.invalidate();
    }

    private int parseSafeInt(String value) {
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            Log.e("ParseError", "Invalid number format: " + value);
            return 0; // Giá trị mặc định nếu parse thất bại
        }
    }

    private void fetchPotholes(int radius) {
        Log.d("RadiusDebug", "Fetching potholes with radius: " + radius); // Log bán kính
        OkHttpClient client = new OkHttpClient();

        Request request = new Request.Builder()
                .url(API_URL)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                Log.e("PotholeAPI", "Lỗi kết nối: " + e.getMessage());
                requireActivity().runOnUiThread(() ->
                        nearbyPotholesInfo.setText(getString(R.string.connection_error1)));
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful() && response.body() != null) {
                    String responseBody = response.body().string();
                    List<Pothole> potholes = parsePotholeList(responseBody);

                    if (potholes == null || potholes.isEmpty()) {
                        requireActivity().runOnUiThread(() ->
                                nearbyPotholesInfo.setText(getString(R.string.no_pothole_data)));
                        return;
                    }

                    List<Pothole> filteredPotholes = filterPotholes(potholes, radius);

                    requireActivity().runOnUiThread(() ->
                            nearbyPotholesInfo.setText(getString(R.string.pothole_in_radius, filteredPotholes.size())));
                } else {
                    Log.e("PotholeAPI", "API trả về lỗi: " + response.code());
                    requireActivity().runOnUiThread(() ->
                            nearbyPotholesInfo.setText(getString(R.string.api_error)));
                }
            }
        });
    }

    private List<Pothole> parsePotholeList(String json) {
        try {
            Gson gson = new Gson();
            Type listType = new TypeToken<List<Pothole>>() {}.getType();
            return gson.fromJson(json, listType);
        } catch (Exception e) {
            Log.e("ParseError", "Lỗi phân tích JSON: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    private List<Pothole> filterExactLocation(List<Pothole> potholes) {
        List<Pothole> filteredPotholes = new ArrayList<>();
        for (Pothole pothole : potholes) {
            if (Math.abs(cachedLatitude - pothole.getLatitude()) < 0.0001 &&
                    Math.abs(cachedLongitude - pothole.getLongitude()) < 0.0001) {
                filteredPotholes.add(pothole);
            }
        }
        return filteredPotholes;
    }

    private List<Pothole> filterPotholes(List<Pothole> potholes, int radius) {
        List<Pothole> filteredPotholes = new ArrayList<>();
        for (Pothole pothole : potholes) {
            if (isWithinRadius(cachedLatitude, cachedLongitude, pothole.getLatitude(), pothole.getLongitude(), radius)) {
                filteredPotholes.add(pothole);
            }
        }
        return filteredPotholes;
    }

    private boolean isWithinRadius(double lat1, double lon1, double lat2, double lon2, int radius) {
        double earthRadius = 6371000; // Bán kính Trái Đất tính bằng mét
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);

        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                        Math.sin(dLon / 2) * Math.sin(dLon / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        double distance = earthRadius * c;

        return distance <= radius;
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (isVisibleToUser && isResumed()) {
            // Chỉ thực hiện khi Fragment được hiển thị
            loadUserInfo();
            initializeLocationEngine();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (locationEngine != null) {
            locationEngine.removeLocationUpdates(locationEngineCallback);
        }
    }

}
