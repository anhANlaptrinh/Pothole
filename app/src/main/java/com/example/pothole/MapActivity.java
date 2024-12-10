package com.example.pothole;

import static com.mapbox.maps.plugin.animation.CameraAnimationsUtils.getCamera;
import static com.mapbox.maps.plugin.gestures.GesturesUtils.addOnMapClickListener;
import static com.mapbox.maps.plugin.gestures.GesturesUtils.getGestures;
import static com.mapbox.maps.plugin.locationcomponent.LocationComponentUtils.getLocationComponent;
import static com.mapbox.navigation.base.extensions.RouteOptionsExtensions.applyDefaultNavigationOptions;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContract;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.app.ActivityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.example.pothole.model.Pothole;
import com.example.pothole.network.ApiClient;
import com.example.pothole.network.ApiService;
import com.example.pothole.network.PotholeWebSocketClient;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.mapbox.android.core.location.LocationEngine;
import com.mapbox.android.core.location.LocationEngineCallback;
import com.mapbox.android.core.location.LocationEngineProvider;
import com.mapbox.android.core.location.LocationEngineResult;
import com.mapbox.android.gestures.MoveGestureDetector;
import com.mapbox.api.directions.v5.DirectionsCriteria;
import com.mapbox.api.directions.v5.models.Bearing;
import com.mapbox.api.directions.v5.models.RouteOptions;
import com.mapbox.bindgen.Expected;
import com.mapbox.geojson.Point;
import com.mapbox.maps.CameraOptions;
import com.mapbox.maps.EdgeInsets;
import com.mapbox.maps.MapView;
import com.mapbox.maps.Style;
import com.mapbox.maps.extension.style.layers.properties.generated.TextAnchor;
import com.mapbox.maps.plugin.animation.MapAnimationOptions;
import com.mapbox.maps.plugin.annotation.AnnotationPlugin;
import com.mapbox.maps.plugin.annotation.AnnotationPluginImplKt;
import com.mapbox.maps.plugin.annotation.generated.PointAnnotationManager;
import com.mapbox.maps.plugin.annotation.generated.PointAnnotationManagerKt;
import com.mapbox.maps.plugin.annotation.generated.PointAnnotationOptions;
import com.mapbox.maps.plugin.gestures.OnMapClickListener;
import com.mapbox.maps.plugin.gestures.OnMoveListener;
import com.mapbox.maps.plugin.locationcomponent.LocationComponentConstants;
import com.mapbox.maps.plugin.locationcomponent.LocationComponentPlugin;
import com.mapbox.maps.plugin.locationcomponent.generated.LocationComponentSettings;
import com.mapbox.navigation.base.options.NavigationOptions;
import com.mapbox.navigation.base.route.NavigationRoute;
import com.mapbox.navigation.base.route.NavigationRouterCallback;
import com.mapbox.navigation.base.route.RouterFailure;
import com.mapbox.navigation.base.route.RouterOrigin;
import com.mapbox.navigation.core.MapboxNavigation;
import com.mapbox.navigation.core.directions.session.RoutesObserver;
import com.mapbox.navigation.core.directions.session.RoutesUpdatedResult;
import com.mapbox.navigation.core.lifecycle.MapboxNavigationApp;
import com.mapbox.navigation.core.trip.session.LocationMatcherResult;
import com.mapbox.navigation.core.trip.session.LocationObserver;
import com.mapbox.navigation.ui.base.util.MapboxNavigationConsumer;
import com.mapbox.navigation.ui.maps.location.NavigationLocationProvider;
import com.mapbox.navigation.ui.maps.route.line.api.MapboxRouteLineApi;
import com.mapbox.navigation.ui.maps.route.line.api.MapboxRouteLineView;
import com.mapbox.navigation.ui.maps.route.line.model.MapboxRouteLineOptions;
import com.mapbox.navigation.ui.maps.route.line.model.RouteLineError;
import com.mapbox.navigation.ui.maps.route.line.model.RouteLineResources;
import com.mapbox.navigation.ui.maps.route.line.model.RouteSetValue;
import com.mapbox.search.autocomplete.PlaceAutocomplete;
import com.mapbox.search.autocomplete.PlaceAutocompleteSuggestion;
import com.mapbox.search.ui.adapter.autocomplete.PlaceAutocompleteUiAdapter;
import com.mapbox.search.ui.view.CommonSearchViewConfiguration;
import com.mapbox.search.ui.view.SearchResultsView;
import com.mapbox.turf.TurfMeasurement;

import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Queue;
import java.util.Set;

import kotlin.Unit;
import kotlin.coroutines.Continuation;
import kotlin.coroutines.CoroutineContext;
import kotlin.coroutines.EmptyCoroutineContext;
import kotlin.jvm.functions.Function1;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MapActivity extends AppCompatActivity {
    MapView mapView;
    MaterialButton setRoute;
    BottomNavigationView bottomNavigationView;
    FloatingActionButton focusLocationBtn;
    private final NavigationLocationProvider navigationLocationProvider = new NavigationLocationProvider();
    boolean isDetectionEnabled = false;
    private MaterialButton cancelRoute;
    private MapboxRouteLineView routeLineView;
    private MapboxRouteLineApi routeLineApi;
    private PointAnnotationManager selectedPointAnnotationManager;
    private TextView speedText;
    private TextView speedUnit;
    private String permissionGranted;
    private SensorManager sensorManager;
    private Sensor accelerometer;
    private Sensor gyroscope; // Con quay hồi chuyển
    private PotholeWebSocketClient webSocketClient;
    private SensorEventListener sensorEventListener;
    private float lastMagnitude = Float.NaN; // Giá trị độ lớn trước đó
    private float LEVEL_1_THRESHOLD = 1.5f; // Tăng ngưỡng ổ gà nhẹ
    private float LEVEL_2_THRESHOLD = 2.0f; // Ngưỡng ổ gà vừa
    private float LEVEL_3_THRESHOLD = 2.5f; // Ngưỡng ổ gà nặng
    private float GYROSCOPE_THRESHOLD = 0.5f; // Ngưỡng cho tốc độ góc
    private int WINDOW_SIZE = 10; // Kích thước cửa sổ làm mượt
    private long DETECTION_INTERVAL = 1500; // Khoảng thời gian giữa các lần phát hiện (ms)
    private float[] gravity = new float[3]; // Lưu giá trị trọng lực
    private float[] gyroscopeValues = new float[3]; // Lưu giá trị từ con quay hồi chuyển
    private Queue<Float> magnitudeValues = new LinkedList<>(); // Lưu các giá trị độ lớn để làm mượt
    private long lastDetectionTime = 0;
    private long potholeStartTime = 0;
    private long lastAlertTime = 0; // Lưu thời gian cảnh báo gần nhất
    private static final long ALERT_INTERVAL = 5000;// Thời gian bắt đầu phát hiện ổ gà
    ImageView imgAvatar;
    TextView tvname, tvemail;
    boolean focusLocation = true;
    private MapboxNavigation mapboxNavigation;

    private final LocationObserver locationObserver = new LocationObserver() {
        @Override
        public void onNewRawLocation(@NonNull Location location) {
            // Không sử dụng trong phiên bản này
        }

        @Override
        public void onNewLocationMatcherResult(@NonNull LocationMatcherResult locationMatcherResult) {
            Location location = locationMatcherResult.getEnhancedLocation();
            navigationLocationProvider.changePosition(location, locationMatcherResult.getKeyPoints(), null, null);
            if (focusLocation) {
                updateCamera(Point.fromLngLat(location.getLongitude(), location.getLatitude()), (double) location.getBearing());
            }
            checkProximityToPotholes(location);
            checkSpeedAndAdjustThreshold(location);
            updateSpeed(location.getSpeed());
        }
    };

    private void updateSpeed(float speedInMetersPerSecond) {
        int speedInKmh = (int) (speedInMetersPerSecond * 3.6); // Chuyển từ m/s sang km/h

        runOnUiThread(() -> {
            speedText.setText(String.valueOf(speedInKmh)); // Hiển thị số
            speedUnit.setText("km/h"); // Đơn vị
        });
    }

    private void checkSpeedAndAdjustThreshold(Location location) {
        if (location != null) {
            float speedInKmh = location.getSpeed() * 3.6f; // Chuyển đổi m/s sang km/h
            Log.d("SpeedCheck", "Tốc độ hiện tại: " + speedInKmh + " km/h");

            if (speedInKmh > 30) {
                LEVEL_1_THRESHOLD = 2.0f;
                LEVEL_2_THRESHOLD = 2.5f;
                LEVEL_3_THRESHOLD = 3.0f;
                DETECTION_INTERVAL = 2000; // Tăng thời gian giữa các lần phát hiện
                Log.d("ThresholdAdjust", "Tăng ngưỡng phát hiện do tốc độ cao.");
            } else {
                LEVEL_1_THRESHOLD = 1.5f;
                LEVEL_2_THRESHOLD = 2.0f;
                LEVEL_3_THRESHOLD = 2.5f;
                DETECTION_INTERVAL = 1500; // Giá trị mặc định
                Log.d("ThresholdAdjust", "Thiết lập ngưỡng mặc định.");
            }
        }
    }

    private void checkProximityToPotholes(Location userLocation) {
        ApiService apiService = ApiClient.getApiService();
        Call<List<Pothole>> call = apiService.getPotholes();

        call.enqueue(new Callback<List<Pothole>>() {
            @Override
            public void onResponse(Call<List<Pothole>> call, Response<List<Pothole>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    for (Pothole pothole : response.body()) {
                        double distance = calculateDistance(
                                userLocation.getLatitude(),
                                userLocation.getLongitude(),
                                pothole.getLatitude(),
                                pothole.getLongitude());

                        // Kiểm tra nếu khoảng cách dưới 50m, kích hoạt rung và âm thanh
                        if (distance < 50) {
                            triggerPotholeAlert();
                            break; // Chỉ cần cảnh báo một lần cho ổ gà gần nhất
                        }
                    }
                }
            }

            @Override
            public void onFailure(Call<List<Pothole>> call, Throwable t) {
                Log.e("PotholeCheck", "Lỗi tải ổ gà: " + t.getMessage());
            }
        });
    }

    private double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        final int R = 6371; // Bán kính trái đất (km)
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                        Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c * 1000; // Trả về khoảng cách theo mét
    }

    private void triggerPotholeAlert() {
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastAlertTime < ALERT_INTERVAL) {
            return;
        }
        lastAlertTime = currentTime;
        Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        if (vibrator != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createOneShot(1000, VibrationEffect.DEFAULT_AMPLITUDE)); // Rung 1 giây
        }
        MediaPlayer mediaPlayer = MediaPlayer.create(this, R.raw.sound); // Thay `alert_sound` bằng file âm thanh của bạn
        if (mediaPlayer != null) {
            mediaPlayer.setOnCompletionListener(mp -> {
                mediaPlayer.release();
            });
            mediaPlayer.start();
        }
        Toast.makeText(this, "Cảnh báo: Sắp đến ổ gà!", Toast.LENGTH_LONG).show();
    }

    private final RoutesObserver routesObserver = new RoutesObserver() {
        @Override
        public void onRoutesChanged(@NonNull RoutesUpdatedResult routesUpdatedResult) {
            routeLineApi.setNavigationRoutes(routesUpdatedResult.getNavigationRoutes(), new MapboxNavigationConsumer<Expected<RouteLineError, RouteSetValue>>() {
                @Override
                public void accept(Expected<RouteLineError, RouteSetValue> routeLineErrorRouteSetValueExpected) {
                    mapView.getMapboxMap().getStyle(style -> {
                        if (style != null) {
                            routeLineView.renderRouteDrawData(style, routeLineErrorRouteSetValueExpected);
                        }
                    });
                }
            });
        }
    };

    private void updateCamera(Point point, Double bearing) {
        MapAnimationOptions animationOptions = new MapAnimationOptions.Builder().duration(1500L).build();
        CameraOptions cameraOptions = new CameraOptions.Builder().center(point).zoom(16.5)
                .padding(new EdgeInsets(0.0, 0.0, 0.0, 0.0)).bearing(bearing).build();

        getCamera(mapView).easeTo(cameraOptions, animationOptions);
    }

    private final OnMoveListener onMoveListener = new OnMoveListener() {
        @Override
        public void onMoveBegin(@NonNull MoveGestureDetector moveGestureDetector) {
            focusLocation = false;
            getGestures(mapView).removeOnMoveListener(this);
            focusLocationBtn.show();
        }

        @Override
        public boolean onMove(@NonNull MoveGestureDetector moveGestureDetector) {
            return false;
        }

        @Override
        public void onMoveEnd(@NonNull MoveGestureDetector moveGestureDetector) {
            // Không sử dụng trong phiên bản này
        }
    };

    private final ActivityResultLauncher<String> activityResultLauncher = registerForActivityResult(new ActivityResultContracts.RequestPermission(), result -> {
        if (result) {
            if (Manifest.permission.ACCESS_FINE_LOCATION.equals(permissionGranted)) {
                startLocationUpdates();
            } else if (Manifest.permission.POST_NOTIFICATIONS.equals(permissionGranted)) {
                enableNotifications();
            }
            recreate();
        }
    });

    private void startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            mapboxNavigation.startTripSession();
            focusLocationBtn.hide();
            LocationComponentPlugin locationComponentPlugin = getLocationComponent(mapView);
            locationComponentPlugin.setEnabled(true);
            locationComponentPlugin.setLocationProvider(navigationLocationProvider);
        }
    }

    private void enableNotifications() {
        Toast.makeText(this, "Notifications enabled", Toast.LENGTH_SHORT).show();
    }

    private void checkPermissions() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            permissionGranted = Manifest.permission.ACCESS_FINE_LOCATION;
            activityResultLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION);
        } else {
            startLocationUpdates();
        }
    }

    private PlaceAutocomplete placeAutocomplete;
    private SearchResultsView searchResultsView;
    private PlaceAutocompleteUiAdapter placeAutocompleteUiAdapter;
    private TextInputEditText searchET;
    private boolean ignoreNextQueryUpdate = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);
        setupWebSocket();

        bottomNavigationView = findViewById(R.id.bottomNavigationView1);
        mapView = findViewById(R.id.mapView);
        speedText = findViewById(R.id.speedText);
        speedUnit = findViewById(R.id.speedUnit);
        MaterialButton enableDetection = findViewById(R.id.enableDetection);
        focusLocationBtn = findViewById(R.id.focusLocation);
        cancelRoute = findViewById(R.id.cancelRoute);
        selectedPointAnnotationManager = PointAnnotationManagerKt.createPointAnnotationManager(
                AnnotationPluginImplKt.getAnnotations(mapView), mapView);
        cancelRoute.setVisibility(View.GONE);
        setRoute = findViewById(R.id.setRoute);

        // Cấu hình Route Line
        MapboxRouteLineOptions options = new MapboxRouteLineOptions.Builder(this)
                .withRouteLineResources(new RouteLineResources.Builder().build())
                .withRouteLineBelowLayerId(LocationComponentConstants.LOCATION_INDICATOR_LAYER).build();
        routeLineView = new MapboxRouteLineView(options);
        routeLineApi = new MapboxRouteLineApi(options);

        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.test);
        NavigationOptions navigationOptions = new NavigationOptions.Builder(this).accessToken(getString(R.string.mapbox_access_token)).build();
        AnnotationPlugin annotationPlugin = AnnotationPluginImplKt.getAnnotations(mapView);
        PointAnnotationManager pointAnnotationManager = PointAnnotationManagerKt.createPointAnnotationManager(annotationPlugin, mapView);
        MapboxNavigationApp.setup(navigationOptions);
        mapboxNavigation = new MapboxNavigation(navigationOptions);
        checkPermissions();
        bottomNavigationView.setBackground(null);
        bottomNavigationView.setOnItemSelectedListener(item -> {
            if (item.getItemId() == R.id.home) {
                // Xử lý sự kiện
            } else if (item.getItemId() == R.id.about) {
                Intent intent = new Intent(MapActivity.this, MainActivity.class);
                intent.putExtra("openFragment", "about");
                startActivity(intent);
                finish();
            } else if (item.getItemId() == R.id.settings) {
                Intent intent = new Intent(MapActivity.this, MainActivity.class);
                intent.putExtra("openFragment", "settings");
                startActivity(intent);
                finish();
            }
            return true;
        });

        enableDetection.setOnClickListener(view -> {
            isDetectionEnabled = !isDetectionEnabled;

            if (isDetectionEnabled) {
                Toast.makeText(this, "Pothole Detection Enabled", Toast.LENGTH_SHORT).show();
                sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
                accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
                gyroscope = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
                PotholeDetection();
                sensorManager.registerListener(sensorEventListener, accelerometer, SensorManager.SENSOR_DELAY_UI);
                sensorManager.registerListener(sensorEventListener, gyroscope, SensorManager.SENSOR_DELAY_UI);
            } else {
                Toast.makeText(this, "Pothole Detection Disabled", Toast.LENGTH_SHORT).show();
                if (sensorManager != null) {
                    sensorManager.unregisterListener(sensorEventListener, accelerometer);
                    sensorManager.unregisterListener(sensorEventListener, gyroscope);
                }
            }
        });

        mapboxNavigation.registerRoutesObserver(routesObserver);
        mapboxNavigation.registerLocationObserver(locationObserver);

        placeAutocomplete = PlaceAutocomplete.create(getString(R.string.mapbox_access_token));
        searchET = findViewById(R.id.searchET);

        searchResultsView = findViewById(R.id.search_results_view);
        searchResultsView.initialize(new SearchResultsView.Configuration(new CommonSearchViewConfiguration()));

        placeAutocompleteUiAdapter = new PlaceAutocompleteUiAdapter(searchResultsView, placeAutocomplete, LocationEngineProvider.getBestLocationEngine(MapActivity.this));

        searchET.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                // Không sử dụng trong phiên bản này
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (ignoreNextQueryUpdate) {
                    ignoreNextQueryUpdate = false;
                } else {
                    placeAutocompleteUiAdapter.search(charSequence.toString(), new Continuation<Unit>() {
                        @NonNull
                        @Override
                        public CoroutineContext getContext() {
                            return EmptyCoroutineContext.INSTANCE;
                        }

                        @Override
                        public void resumeWith(@NonNull Object o) {
                            runOnUiThread(() -> searchResultsView.setVisibility(View.VISIBLE));
                        }
                    });
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {
                // Không sử dụng trong phiên bản này
            }
        });

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ActivityCompat.checkSelfPermission(MapActivity.this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                permissionGranted = Manifest.permission.POST_NOTIFICATIONS;
                activityResultLauncher.launch(Manifest.permission.POST_NOTIFICATIONS);
            }
        }

        if (ActivityCompat.checkSelfPermission(MapActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(MapActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            permissionGranted = Manifest.permission.ACCESS_FINE_LOCATION;
            activityResultLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION);
            permissionGranted = Manifest.permission.ACCESS_COARSE_LOCATION;
            activityResultLauncher.launch(Manifest.permission.ACCESS_COARSE_LOCATION);
        } else {
            mapboxNavigation.startTripSession();
        }

        focusLocationBtn.hide();
        LocationComponentPlugin locationComponentPlugin = getLocationComponent(mapView);
        getGestures(mapView).addOnMoveListener(onMoveListener);

        setRoute.setOnClickListener(view -> Toast.makeText(MapActivity.this, "Please select a location in map", Toast.LENGTH_SHORT).show());

        mapView.getMapboxMap().loadStyleUri(Style.MAPBOX_STREETS, style -> {
            style.addImage("low-icon", BitmapFactory.decodeResource(getResources(), R.drawable.iconlow));
            style.addImage("medium-icon", BitmapFactory.decodeResource(getResources(), R.drawable.iconmedium));
            style.addImage("high-icon", BitmapFactory.decodeResource(getResources(), R.drawable.iconhigh));
            loadPotholeMarkers();
            mapView.getMapboxMap().setCamera(new CameraOptions.Builder().zoom(16.5).build());
            locationComponentPlugin.setEnabled(true);
            locationComponentPlugin.setLocationProvider(navigationLocationProvider);
            getGestures(mapView).addOnMoveListener(onMoveListener);
            locationComponentPlugin.updateSettings(locationComponentSettings -> {
                locationComponentSettings.setEnabled(true);
                locationComponentSettings.setPulsingEnabled(true);
                return null;
            });

            addOnMapClickListener(mapView.getMapboxMap(), point -> {
                selectedPointAnnotationManager.deleteAll();

                PointAnnotationOptions pointAnnotationOptions = new PointAnnotationOptions()
                        .withTextAnchor(TextAnchor.CENTER)
                        .withIconImage(bitmap)
                        .withPoint(point);

                selectedPointAnnotationManager.create(pointAnnotationOptions);

                setRoute.setOnClickListener(view -> fetchRoute(point));

                return true;
            });

            focusLocationBtn.setOnClickListener(view -> {
                focusLocation = true;
                getGestures(mapView).addOnMoveListener(onMoveListener);
                focusLocationBtn.hide();
            });

            placeAutocompleteUiAdapter.addSearchListener(new PlaceAutocompleteUiAdapter.SearchListener() {
                @Override
                public void onSuggestionsShown(@NonNull List<PlaceAutocompleteSuggestion> list) {
                    // Không sử dụng trong phiên bản này
                }

                @Override
                public void onSuggestionSelected(@NonNull PlaceAutocompleteSuggestion placeAutocompleteSuggestion) {
                    ignoreNextQueryUpdate = true;
                    focusLocation = false;
                    searchET.setText(placeAutocompleteSuggestion.getName());
                    searchResultsView.setVisibility(View.GONE);

                    selectedPointAnnotationManager.deleteAll();
                    PointAnnotationOptions pointAnnotationOptions = new PointAnnotationOptions()
                            .withTextAnchor(TextAnchor.CENTER)
                            .withIconImage(bitmap)
                            .withPoint(placeAutocompleteSuggestion.getCoordinate());
                    selectedPointAnnotationManager.create(pointAnnotationOptions);
                    updateCamera(placeAutocompleteSuggestion.getCoordinate(), 0.0);

                    setRoute.setOnClickListener(view -> fetchRoute(placeAutocompleteSuggestion.getCoordinate()));
                }

                @Override
                public void onPopulateQueryClick(@NonNull PlaceAutocompleteSuggestion placeAutocompleteSuggestion) {
                    // Không sử dụng trong phiên bản này
                }

                @Override
                public void onError(@NonNull Exception e) {
                    Toast.makeText(MapActivity.this, "Search error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        });
    }

    @SuppressLint("MissingPermission")
    private void fetchRoute(Point destination) {
        LocationEngine locationEngine = LocationEngineProvider.getBestLocationEngine(MapActivity.this);
        locationEngine.getLastLocation(new LocationEngineCallback<LocationEngineResult>() {
            @Override
            public void onSuccess(LocationEngineResult result) {
                Location location = result.getLastLocation();

                if (location == null) {
                    setRoute.setEnabled(true);
                    setRoute.setText("Set route");
                    Toast.makeText(MapActivity.this, "Unable to get current location", Toast.LENGTH_SHORT).show();
                    return;
                }

                setRoute.setEnabled(false);
                setRoute.setText("Fetching route...");

                RouteOptions.Builder builder = RouteOptions.builder()
                        .language("vi")
                        .steps(true)
                        .overview(DirectionsCriteria.OVERVIEW_FULL)
                        .profile(DirectionsCriteria.PROFILE_DRIVING);

                Point origin = Point.fromLngLat(location.getLongitude(), location.getLatitude());
                builder.coordinatesList(Arrays.asList(origin, destination));
                builder.alternatives(false);
                builder.profile(DirectionsCriteria.PROFILE_DRIVING);
                builder.bearingsList(Arrays.asList(Bearing.builder().angle(location.getBearing()).degrees(45.0).build(), null));
                applyDefaultNavigationOptions(builder);

                mapboxNavigation.requestRoutes(builder.build(), new NavigationRouterCallback() {
                    @Override
                    public void onRoutesReady(@NonNull List<NavigationRoute> list, @NonNull RouterOrigin routerOrigin) {
                        if (list.isEmpty()) {
                            Toast.makeText(MapActivity.this, "No route found", Toast.LENGTH_SHORT).show();
                            setRoute.setEnabled(true);
                            setRoute.setText("Set route");
                            return;
                        }
                        NavigationRoute route = list.get(0);
                        mapboxNavigation.setNavigationRoutes(list);
                        focusLocationBtn.performClick();
                        setRoute.setEnabled(true);
                        setRoute.setText("Set route");
                        cancelRoute.setVisibility(View.VISIBLE);

                        String geometry = route.getDirectionsRoute().geometry();
                        List<Point> routePoints = decodePolyline(geometry, 6); // Precision mặc định của Mapbox là 6

                        // Log danh sách tọa độ
                        for (int i = 0; i < routePoints.size(); i++) {
                            Point point = routePoints.get(i);
                            Log.d("RoutePoints", "Point " + i + ": " + point.latitude() + ", " + point.longitude());
                        }

                        // Gọi kiểm tra ổ gà
                        checkPotholesOnRoute(routePoints);

                        cancelRoute.setOnClickListener(view -> {
                            mapboxNavigation.setNavigationRoutes(Collections.emptyList());
                            cancelRoute.setVisibility(View.GONE);
                            selectedPointAnnotationManager.deleteAll();
                            Toast.makeText(MapActivity.this, "Route canceled", Toast.LENGTH_SHORT).show();
                        });
                    }

                    @Override
                    public void onFailure(@NonNull List<RouterFailure> list, @NonNull RouteOptions routeOptions) {
                        setRoute.setEnabled(true);
                        setRoute.setText("Set route");
                        cancelRoute.setVisibility(View.GONE);
                        String errorMessage = list.isEmpty() ? "Unknown error" : list.get(0).getMessage();
                        Toast.makeText(MapActivity.this, "Route request failed: " + errorMessage, Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onCanceled(@NonNull RouteOptions routeOptions, @NonNull RouterOrigin routerOrigin) {
                        setRoute.setEnabled(true);
                        setRoute.setText("Set route");
                        cancelRoute.setVisibility(View.GONE);
                    }
                });
            }

            @Override
            public void onFailure(@NonNull Exception exception) {
                setRoute.setEnabled(true);
                setRoute.setText("Set route");
                cancelRoute.setVisibility(View.GONE);
                Toast.makeText(MapActivity.this, "Location request failed: " + exception.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private List<Point> decodePolyline(String encoded, int precision) {
        List<Point> polyline = new ArrayList<>();
        int index = 0, len = encoded.length();
        int lat = 0, lng = 0;

        while (index < len) {
            int b, shift = 0, result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlat = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lat += dlat;

            shift = 0;
            result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlng = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lng += dlng;

            polyline.add(Point.fromLngLat((lng / Math.pow(10, precision)), (lat / Math.pow(10, precision))));
        }
        return polyline;
    }

    private void checkPotholesOnRoute(List<Point> routePoints) {
        ApiService apiService = ApiClient.getApiService();
        Call<List<Pothole>> call = apiService.getPotholes();

        call.enqueue(new Callback<List<Pothole>>() {
            @Override
            public void onResponse(Call<List<Pothole>> call, Response<List<Pothole>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Pothole> potholes = response.body();
                    int potholeCount = 0;

                    // Sử dụng Set để tránh đếm trùng
                    Set<Point> countedPotholes = new HashSet<>();

                    for (Pothole pothole : potholes) {
                        Point potholePoint = Point.fromLngLat(pothole.getLongitude(), pothole.getLatitude());

                        // Kiểm tra ổ gà đã được đếm hay chưa
                        if (!countedPotholes.contains(potholePoint)) {
                            for (int i = 0; i < routePoints.size() - 1; i++) {
                                Point p1 = routePoints.get(i);
                                Point p2 = routePoints.get(i + 1);

                                // Kiểm tra nếu ổ gà gần đoạn đường [p1, p2]
                                if (isPointNearSegment(potholePoint, p1, p2, 5)) { // Ngưỡng 10m
                                    potholeCount++;
                                    countedPotholes.add(potholePoint); // Đánh dấu ổ gà đã được đếm
                                    break;
                                }
                            }
                        }
                    }

                    // Hiển thị kết quả
                    Toast.makeText(MapActivity.this, "Có " + potholeCount + " ổ gà trên tuyến đường này!", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(MapActivity.this, "Không thể tải dữ liệu ổ gà!", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<Pothole>> call, Throwable t) {
                Toast.makeText(MapActivity.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private boolean isPointNearSegment(Point p0, Point p1, Point p2, double threshold) {
        double distToP1 = haversine(p0.latitude(), p0.longitude(), p1.latitude(), p1.longitude());
        double distToP2 = haversine(p0.latitude(), p0.longitude(), p2.latitude(), p2.longitude());

        double dx = p2.longitude() - p1.longitude();
        double dy = p2.latitude() - p1.latitude();

        double t = ((p0.longitude() - p1.longitude()) * dx + (p0.latitude() - p1.latitude()) * dy)
                / (dx * dx + dy * dy);

        double nearestLat, nearestLon;

        if (t < 0) {
            nearestLat = p1.latitude();
            nearestLon = p1.longitude();
        } else if (t > 1) {
            nearestLat = p2.latitude();
            nearestLon = p2.longitude();
        } else {
            nearestLat = p1.latitude() + t * dy;
            nearestLon = p1.longitude() + t * dx;
        }

        double distanceToSegment = haversine(p0.latitude(), p0.longitude(), nearestLat, nearestLon);

        // Log khoảng cách để debug
        Log.d("DistanceCheck", "Khoảng cách ổ gà " + p0 + " tới đoạn [" + p1 + ", " + p2 + "] là: " + distanceToSegment);

        return distanceToSegment <= threshold;
    }

    private double haversine(double lat1, double lon1, double lat2, double lon2) {
        final double R = 6371e3; // Bán kính Trái Đất (m)
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c; // Khoảng cách tính bằng mét
    }

    private void loadPotholeMarkers() {
        ApiService apiService = ApiClient.getApiService();
        Call<List<Pothole>> call = apiService.getPotholes();

        call.enqueue(new Callback<List<Pothole>>() {
            @Override
            public void onResponse(Call<List<Pothole>> call, Response<List<Pothole>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Pothole> potholes = response.body();

                    AnnotationPlugin annotationPlugin = AnnotationPluginImplKt.getAnnotations(mapView);
                    PointAnnotationManager pointAnnotationManager = PointAnnotationManagerKt.createPointAnnotationManager(annotationPlugin, mapView);

                    for (Pothole pothole : potholes) {
                        Point point = Point.fromLngLat(pothole.getLongitude(), pothole.getLatitude());
                        PointAnnotationOptions options = new PointAnnotationOptions()
                                .withPoint(point)
                                .withIconImage(getMarkerIcon(pothole.getSeverity()));
                        pointAnnotationManager.create(options);
                    }
                } else {
                    Toast.makeText(MapActivity.this, "Failed to load potholes", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<Pothole>> call, Throwable t) {
                Toast.makeText(MapActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private String getMarkerIcon(int severity) {
        switch (severity) {
            case 1:
                return "low-icon";
            case 2:
                return "medium-icon";
            case 3:
                return "high-icon";
            default:
                return "default-icon";
        }
    }

    private void setupWebSocket() {
        try {
            URI uri = new URI("wss://potholedetect-backend.onrender.com/pothole-updates");
            webSocketClient = new PotholeWebSocketClient(uri, (latitude, longitude, severity) -> {
                runOnUiThread(() -> addPotholeMarker(latitude, longitude, severity));
            });
            webSocketClient.connect();
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "WebSocket connection failed", Toast.LENGTH_SHORT).show();
        }
    }

    private void addPotholeMarker(double latitude, double longitude, int severity) {
        Point point = Point.fromLngLat(longitude, latitude);
        PointAnnotationOptions options = new PointAnnotationOptions()
                .withPoint(point)
                .withIconImage(getMarkerIcon(severity));
        AnnotationPlugin annotationPlugin = AnnotationPluginImplKt.getAnnotations(mapView);
        PointAnnotationManager pointAnnotationManager = PointAnnotationManagerKt.createPointAnnotationManager(annotationPlugin, mapView);
        pointAnnotationManager.create(options);
    }

    private void PotholeDetection() {
        sensorEventListener = new SensorEventListener() {
            @Override
            public void onSensorChanged(SensorEvent event) {
                switch (event.sensor.getType()) {
                    case Sensor.TYPE_ACCELEROMETER:
                        // Bỏ trọng lực và làm mượt dữ liệu
                        float[] linearAcceleration = removeGravity(event.values);

                        // Tính độ lớn gia tốc
                        float magnitude = calculateMagnitude(linearAcceleration);

                        // Làm mượt độ lớn
                        float smoothedMagnitude = calculateSmoothedMagnitude(magnitude);

                        // Kiểm tra góc quay từ con quay hồi chuyển
                        float gyroscopeMagnitude = calculateMagnitude(gyroscopeValues);

                        if (gyroscopeMagnitude < GYROSCOPE_THRESHOLD) {
                            long currentTime = System.currentTimeMillis();

                            if (!Float.isNaN(lastMagnitude)) {
                                float deltaMagnitude = Math.abs(smoothedMagnitude - lastMagnitude);
                                Log.d("PotholeDetection", "Delta Magnitude: " + deltaMagnitude);

                                if (currentTime - lastDetectionTime > DETECTION_INTERVAL) {
                                    if (deltaMagnitude >= LEVEL_3_THRESHOLD) {
                                        triggerPotholeDetected("Cấp 3", deltaMagnitude);
                                    } else if (deltaMagnitude >= LEVEL_2_THRESHOLD) {
                                        triggerPotholeDetected("Cấp 2", deltaMagnitude);
                                    } else if (deltaMagnitude >= LEVEL_1_THRESHOLD) {
                                        if (currentTime - potholeStartTime > MIN_POTHOLE_DURATION) {
                                            triggerPotholeDetected("Cấp 1", deltaMagnitude);
                                        } else {
                                            potholeStartTime = currentTime;
                                        }
                                    }
                                }
                            }

                            lastMagnitude = smoothedMagnitude;
                        } else {
                            Log.d("PotholeDetection", "Thiết bị đang xoay, bỏ qua sự kiện.");
                        }
                        break;

                    case Sensor.TYPE_GYROSCOPE:
                        gyroscopeValues = Arrays.copyOf(event.values, event.values.length);
                        break;
                }
            }

            @Override
            public void onAccuracyChanged(Sensor sensor, int accuracy) {
                // Không sử dụng trong phiên bản này
            }
        };
    }

    private void triggerPotholeDetected(String level, float deltaMagnitude) {
        int severity = 1; // Mặc định mức nhẹ
        switch (level) {
            case "Cấp 1":
                severity = 1;
                break;
            case "Cấp 2":
                severity = 2;
                break;
            case "Cấp 3":
                severity = 3;
                break;
        }

        // Lấy vị trí hiện tại
        Location lastLocation = navigationLocationProvider.getLastLocation();
        if (lastLocation == null) {
            runOnUiThread(() -> Toast.makeText(MapActivity.this, "Không thể lấy vị trí hiện tại!", Toast.LENGTH_SHORT).show());
            return;
        }

        // Tạo các biến final
        final double latitude = lastLocation.getLatitude();
        final double longitude = lastLocation.getLongitude();
        final int finalSeverity = severity;

        runOnUiThread(() -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(MapActivity.this);
            builder.setTitle("Phát hiện ổ gà!");
            builder.setMessage("Mức độ: " + level + "\nΔ: " + deltaMagnitude +
                    "\nVị trí: (" + latitude + ", " + longitude + ")" +
                    "\nBạn có muốn lưu thông tin này không?");
            builder.setPositiveButton("Có", (dialog, which) -> {
                // Lưu thông tin ổ gà vào database
                savePotholeToDatabase(latitude, longitude, finalSeverity);
            });
            builder.setNegativeButton("Không", (dialog, which) -> dialog.dismiss());

            // Tạo AlertDialog
            AlertDialog dialog = builder.create();
            dialog.show();

            // Tự động đóng hộp thoại sau 2 giây
            new Handler().postDelayed(dialog::dismiss, 2000);
        });

        Log.d("PotholeLevel", level + " | Δ: " + deltaMagnitude);
        lastDetectionTime = System.currentTimeMillis();
        potholeStartTime = 0; // Reset thời gian ổ gà liên tiếp
    }

    private void savePotholeToDatabase(double latitude, double longitude, int severity) {
        ApiService apiService = ApiClient.getApiService();
        Pothole pothole = new Pothole(latitude, longitude, severity);

        apiService.addPothole(pothole).enqueue(new Callback<Pothole>() {
            @Override
            public void onResponse(Call<Pothole> call, Response<Pothole> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(MapActivity.this, "Lưu ổ gà thành công!", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(MapActivity.this, "Lỗi lưu ổ gà: " + response.message(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Pothole> call, Throwable t) {
                Toast.makeText(MapActivity.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private float[] removeGravity(float[] acceleration) {
        final float alpha = 0.8f;
        gravity[0] = alpha * gravity[0] + (1 - alpha) * acceleration[0];
        gravity[1] = alpha * gravity[1] + (1 - alpha) * acceleration[1];
        gravity[2] = alpha * gravity[2] + (1 - alpha) * acceleration[2];

        float[] linearAcceleration = new float[3];
        linearAcceleration[0] = acceleration[0] - gravity[0];
        linearAcceleration[1] = acceleration[1] - gravity[1];
        linearAcceleration[2] = acceleration[2] - gravity[2];
        return linearAcceleration;
    }

    private float calculateMagnitude(float[] values) {
        return (float) Math.sqrt(
                values[0] * values[0] +
                        values[1] * values[1] +
                        values[2] * values[2]
        );
    }


    private float calculateSmoothedMagnitude(float newMagnitude) {
        if (magnitudeValues.size() >= WINDOW_SIZE) {
            magnitudeValues.poll();
        }
        magnitudeValues.add(newMagnitude);
        float sum = 0f;
        for (float mag : magnitudeValues) {
            sum += mag;
        }
        return sum / magnitudeValues.size();
    }

    private static final long MIN_POTHOLE_DURATION = 300; // Khoảng thời gian tối thiểu (ms)

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (webSocketClient != null && webSocketClient.isOpen()) {
            webSocketClient.close();
        }
        if (sensorManager != null) {
            sensorManager.unregisterListener(sensorEventListener, accelerometer);
            sensorManager.unregisterListener(sensorEventListener, gyroscope);
        }
        mapboxNavigation.onDestroy();
        mapboxNavigation.unregisterRoutesObserver(routesObserver);
        mapboxNavigation.unregisterLocationObserver(locationObserver);
    }
}