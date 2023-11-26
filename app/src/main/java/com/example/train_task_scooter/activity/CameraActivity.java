package com.example.train_task_scooter.activity;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.SparseArray;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.example.train_task_scooter.R;
import com.google.android.gms.vision.CameraSource;
import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.android.gms.vision.barcode.BarcodeDetector;
import java.text.SimpleDateFormat;
import java.util.Date;
import butterknife.BindView;
import io.reactivex.rxjava3.annotations.NonNull;

public class CameraActivity extends BaseActivity {
    private static final int CAMERA_PERMISSION_REQUEST = 100;
    @BindView(R.id.camera_preview)
    SurfaceView cameraView;
    @BindView(R.id.empty_view)
    ImageView emptyView;
    private boolean cameraStarted = false;
    private CameraSource cameraSource;
    private BarcodeDetector barcodeDetector;
    private ProgressDialog dialog;
    private boolean recordingEnabled = true;

    private void visableWaitDialog(){
        dialog = new ProgressDialog(this);
        dialog.setIndeterminate(false);
        dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        dialog.setMessage("Пожалуйста подождите...");
        dialog.setCancelable(false);
        dialog.show();
    }
    private void initializeCamera() {
        barcodeDetector = new BarcodeDetector.Builder(this)
                .setBarcodeFormats(Barcode.QR_CODE)
                .build();

        cameraSource = new CameraSource.Builder(this, barcodeDetector)
                .setAutoFocusEnabled(true)
                .build();

        cameraView.getHolder().addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
            }

            @Override
            public void surfaceCreated(SurfaceHolder holder) {
                try {
                    if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                        requestCameraPermission();
                    } else {
                        cameraSource.start(cameraView.getHolder());
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {
                cameraSource.stop();
            }
        });

        barcodeDetector.setProcessor(new Detector.Processor<Barcode>() {
            @Override
            public void release() {}

            @Override
            public void receiveDetections(Detector.Detections<Barcode> detections) {
                final SparseArray<Barcode> barcodes = detections.getDetectedItems();
                if (barcodes.size() != 0 && !cameraStarted && recordingEnabled) {
                    String qrCodeData = barcodes.valueAt(0).displayValue;

                    //todo тут будет обработка и отправка данных на новый фрагмент
//                    replaceFragment(PayFragment.newInstance(qrCodeData), true);
                }
            }
        });
    }

//    private String getDate() {
//        Date currentDate = new Date();
//        SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy");
//        return dateFormat.format(currentDate);
//    }
//
//    private String getTime(){
//        Date currentDate = new Date();
//        SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
//        return dateFormat.format(currentDate);
//    }

    private void requestCameraPermission() {
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, CAMERA_PERMISSION_REQUEST);
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CAMERA_PERMISSION_REQUEST) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                initializeCamera();
            } else {
                Toast.makeText(this, "Предоставьте разрешение для камеры", Toast.LENGTH_SHORT).show();
                requestCameraPermission();
            }
        }
    }

//    @Override
//    public void errorMessage(String msg) {
//        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
//        recordingEnabled = false;
//    }
//
//    @Override
//    public void successMessage(String msg) {
//        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
//        finish();
//    }


    @Override
    protected void initViews(@Nullable Bundle saveInstanceState) {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            requestCameraPermission();
        } else {
            if (!cameraStarted)
                initializeCamera();
        }
    }

    @Override
    protected int layoutResId() {
        return R.layout.camera_activity;
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
}
