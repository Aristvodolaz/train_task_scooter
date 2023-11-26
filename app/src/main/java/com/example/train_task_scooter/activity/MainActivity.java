package com.example.train_task_scooter.activity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import com.google.android.gms.location.LocationRequest;
import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import com.yandex.mapkit.geometry.Point;
import android.location.LocationManager;

import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.util.Log;
import android.widget.Toast;

import com.example.train_task_scooter.R;
import com.example.train_task_scooter.dialog.ScooterInfoBottomDialog;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.CommonStatusCodes;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.location.SettingsClient;

import com.google.android.gms.tasks.Task;
import com.yandex.mapkit.Animation;
import com.yandex.mapkit.MapKitFactory;
import com.yandex.mapkit.directions.DirectionsFactory;
import com.yandex.mapkit.directions.driving.DrivingOptions;
import com.yandex.mapkit.directions.driving.DrivingRouter;
import com.yandex.mapkit.directions.driving.DrivingSession;
import com.yandex.mapkit.map.CameraPosition;
import com.yandex.mapkit.map.MapObject;
import com.yandex.mapkit.map.MapObjectCollection;
import com.yandex.mapkit.map.MapObjectTapListener;
import com.yandex.mapkit.map.PlacemarkMapObject;
import com.yandex.mapkit.mapview.MapView;
import com.yandex.runtime.image.ImageProvider;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends BaseActivity {

    private final Point SCREEN_CENTER = new Point((53.249064 + 53.243727) / 2, (34.343156 + 34.365167) / 2);
    private final String MAPKIT_API_KEY = "12f079b1-d006-468f-b2a0-d0ea01443347";
    private Double lat, lon;
    private MapView mapView;
    Handler handler = new Handler();
    Runnable runnable;
    MapObjectCollection mapObject;
    Bitmap bitmap;
    public static final int MY_PERMISSIONS_REQUEST_LOCATION = 287;
    private FusedLocationProviderClient mFusedLocationClient;
    private Task<LocationSettingsResponse> task;
    private LocationCallback mLocationCallback;
    private static final int CODE_DIALOG = 5;
    private static final int CODE_ACTIVITY = 7;
    private static final int MY_PERMISSION_REQUEST_LOCATION = 859;

    private Point startPoint;
    private Point endPoint;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        MapKitFactory.setApiKey(MAPKIT_API_KEY);
        MapKitFactory.initialize(this);

        mapView = findViewById(R.id.mapview);
        getPosition();


        if (lat != null && lon != null) {
            mapView.getMap().move(
                    new CameraPosition(new Point(lat, lon), 14.0f, 0.0f, 0.0f),
                    new Animation(Animation.Type.SMOOTH, 5),
                    null);
        } else {
            mapView.getMap().move(
                    new CameraPosition(new Point(53.243400, 34.363991), 14.0f, 0.0f, 0.0f),
                    new Animation(Animation.Type.SMOOTH, 5),
                    null);
        }
        bitmap = BitmapFactory.decodeResource(this.getResources(), R.drawable.naple);

        // Построение маршрута от корпуса до корпуса
        mapView.getMap().move(new CameraPosition(SCREEN_CENTER, 16, 0, 0));
        drivingRouter = DirectionsFactory.getInstance().createDrivingRouter();
        mapObject = mapView.getMap().getMapObjects().addCollection();

//        createPointEndAndStart();

        addPoint();

    }

    private void addPoint(){
        PlacemarkMapObject startPlacemark = mapObject.addPlacemark(new Point(53.249064,34.343156), ImageProvider.fromBitmap(drawStartBitmap()));
        PlacemarkMapObject endPlacemark = mapObject.addPlacemark(new Point(53.243727, 34.365167), ImageProvider.fromBitmap(drawEndBitmap()));
        startPlacemark.addTapListener(placemarkStartTapListener);


    }

    private MapObjectTapListener placemarkStartTapListener = new MapObjectTapListener() {
        @Override
        public boolean onMapObjectTap(@NonNull MapObject mapObject, @NonNull com.yandex.mapkit.geometry.Point point) {
            ScooterInfoBottomDialog.newInstance(123).show(getSupportFragmentManager(),"hello");
            return true;
        }
    };

    @Override
    protected void initViews(@Nullable Bundle saveInstanceState) {

    }

    @Override
    protected int layoutResId() {
        return R.layout.activity_main;
    }

    @Override
    protected int titleResId() {
        return 0;
    }

//    public void replaceFragment(Fragment fragment, boolean addToBackStack) {
//        FragmentTransaction fragmentTransaction =
//                getSupportFragmentManager()
//                        .beginTransaction()
//                        .replace(R.id.container, fragment, fragment.getClass().getSimpleName());
//        if (addToBackStack) fragmentTransaction.addToBackStack(fragment.getClass().getName());
//        fragmentTransaction.commit();
//    }

    private void getPosition() {
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        boolean isGPS = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        if (!isGPS) {
            startActivityForResult(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS), 7);
        }
        if (ContextCompat.checkSelfPermission(MainActivity.this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                    MY_PERMISSIONS_REQUEST_LOCATION);
        } else {
            LocationRequest mLocationRequest = createLocationRequest();
            LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder().addLocationRequest(mLocationRequest);
            SettingsClient client = LocationServices.getSettingsClient(MainActivity.this);
            task = client.checkLocationSettings(builder.build())
                    .addOnSuccessListener(MainActivity.this, locationSettingsResponse -> {
                        mFusedLocationClient.requestLocationUpdates(mLocationRequest, mLocationCallback, null);
                    });
        }


        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(MainActivity.this);
        mLocationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                lon = locationResult.getLocations().get(0).getLongitude();
                lat = locationResult.getLocations().get(0).getLatitude();
                mapView.getMap().getMapObjects().addPlacemark(new Point(lat,lon),ImageProvider.fromBitmap(drawSimpleBitmap()));
            }
        };
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_LOCATION: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(MainActivity.this, "Разрешение на использование данных GPS получено", Toast.LENGTH_LONG).show();
                    LocationRequest mLocationRequest = createLocationRequest();
                    LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder().addLocationRequest(mLocationRequest);
                    SettingsClient client = LocationServices.getSettingsClient(MainActivity.this);
                    task = client.checkLocationSettings(builder.build())
                            .addOnSuccessListener(MainActivity.this, locationSettingsResponse -> {
                                mFusedLocationClient.requestLocationUpdates(mLocationRequest, mLocationCallback, null);
                            });
                } else {
                    Toast.makeText(this, "Разрешение на получение данных не получено", Toast.LENGTH_SHORT).show();
                }
                return;
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CODE_ACTIVITY && resultCode == 0) {
            makeLocaleSettings(CODE_DIALOG);
        } else if (requestCode == CODE_DIALOG && resultCode == -1) {
            makeLocaleSettings(-1);
        } else {
            Toast.makeText(this, "Разрешение на получение данных не получено", Toast.LENGTH_SHORT).show();
        }
    }
    private Bitmap drawStartBitmap() {
        Bitmap bitmap = Bitmap.createBitmap(64, 64, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        Paint paint = new Paint();
        paint.setColor(Color.BLUE); // Зеленый цвет для начальной точки
        paint.setStyle(Paint.Style.FILL);
        canvas.drawCircle(64 / 2, 64 / 2, 64 / 2, paint);
        return bitmap;
    }

    private Bitmap drawEndBitmap() {
        Bitmap bitmap = Bitmap.createBitmap(64, 64, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        Paint paint = new Paint();
        paint.setColor(Color.CYAN); // Красный цвет для конечной точки
        paint.setStyle(Paint.Style.FILL);
        canvas.drawCircle(64 / 2, 64 / 2, 64 / 2, paint);
        return bitmap;
    }

    public void makeLocaleSettings(int requestCode) {
        LocationRequest mLocationRequest = createLocationRequest();
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(mLocationRequest);
        SettingsClient client = LocationServices.getSettingsClient(this);
        task = client.checkLocationSettings(builder.build())
                .addOnSuccessListener(this, locationSettingsResponse -> {
                    mFusedLocationClient.requestLocationUpdates(mLocationRequest, mLocationCallback, null);
                    Log.e("wwwww", " location setting is success");
                }).addOnFailureListener(this, e -> {
                    int statusCode = ((ApiException) e).getStatusCode();
                    switch (statusCode) {
                        case CommonStatusCodes.RESOLUTION_REQUIRED:
                            if (requestCode != -1) {
                                try {
                                    ResolvableApiException resolvable = (ResolvableApiException) e;
                                    resolvable.startResolutionForResult(MainActivity.this, requestCode);
                                } catch (IntentSender.SendIntentException sendEx) {
                                }
                            }
                            break;
                        case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                            break;
                    }
                });
    }

    protected LocationRequest createLocationRequest() {
        LocationRequest mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(0);
        mLocationRequest.setFastestInterval(0);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        return mLocationRequest;
    }

    public Bitmap drawSimpleBitmap() {
        Bitmap bitmap = Bitmap.createBitmap(36, 36, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        // Отрисовка плейсмарка
        Paint paint = new Paint();
        paint.setColor(Color.parseColor("#E39D32"));
        paint.setStyle(Paint.Style.FILL);
        canvas.drawCircle(24 / 2, 24 / 2, 24 / 2, paint);
        return bitmap;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacks(runnable);
    }

    @Override
    protected void onStop() {
        mapView.onStop();
        MapKitFactory.getInstance().onStop();
        super.onStop();
        handler.removeCallbacks(runnable);
    }

    @Override
    protected void onStart() {
        super.onStart();
        MapKitFactory.getInstance().onStart();
        mapView.onStart();
        handler.postDelayed(runnable = () -> {
            handler.postDelayed(runnable, 5000);
            getPosition();
        }, 5000);
    }


}
