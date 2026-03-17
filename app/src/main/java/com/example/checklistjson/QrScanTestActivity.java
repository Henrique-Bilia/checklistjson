package com.example.checklistjson;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.content.ContextCompat;

import com.google.common.util.concurrent.ListenableFuture;
import com.google.mlkit.vision.barcode.BarcodeScanner;
import com.google.mlkit.vision.barcode.BarcodeScannerOptions;
import com.google.mlkit.vision.barcode.BarcodeScanning;
import com.google.mlkit.vision.barcode.common.Barcode;
import com.google.mlkit.vision.common.InputImage;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;

public class QrScanTestActivity extends AppCompatActivity {

    public static final String EXTRA_RETURN_RESULT = "return_result";
    public static final String EXTRA_RESULT_RAW = "qr_raw_result";

    private PreviewView previewView;
    private TextView tvResultado;

    private BarcodeScanner scanner;
    private volatile boolean processing = false;
    private volatile boolean alreadyRead = false;

    private final ActivityResultLauncher<String> requestCameraPermission =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    startCamera();
                } else {
                    tvResultado.setText("Permissão de câmera negada. Ative para testar o QR.");
                }
            });

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qr_scan_test);

        previewView = findViewById(R.id.previewView);
        tvResultado = findViewById(R.id.tvResultado);

        BarcodeScannerOptions options = new BarcodeScannerOptions.Builder()
                .setBarcodeFormats(Barcode.FORMAT_QR_CODE)
                .build();
        scanner = BarcodeScanning.getClient(options);

        ensureCameraPermissionAndStart();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (scanner != null) {
            scanner.close();
        }
    }

    private void ensureCameraPermissionAndStart() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            startCamera();
        } else {
            requestCameraPermission.launch(Manifest.permission.CAMERA);
        }
    }

    private void startCamera() {
        ListenableFuture<ProcessCameraProvider> cameraProviderFuture = ProcessCameraProvider.getInstance(this);
        Executor mainExecutor = ContextCompat.getMainExecutor(this);

        cameraProviderFuture.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();
                bindUseCases(cameraProvider);
            } catch (Exception e) {
                tvResultado.setText("Falha ao iniciar câmera: " + e.getMessage());
            }
        }, mainExecutor);
    }

    private void bindUseCases(@NonNull ProcessCameraProvider cameraProvider) {
        cameraProvider.unbindAll();

        Preview preview = new Preview.Builder().build();
        preview.setSurfaceProvider(previewView.getSurfaceProvider());

        ImageAnalysis analysis = new ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build();

        analysis.setAnalyzer(ContextCompat.getMainExecutor(this), this::analyze);

        CameraSelector cameraSelector = new CameraSelector.Builder()
                .requireLensFacing(CameraSelector.LENS_FACING_BACK)
                .build();

        cameraProvider.bindToLifecycle(this, cameraSelector, preview, analysis);
    }

    @SuppressLint("UnsafeOptInUsageError")
    private void analyze(@NonNull ImageProxy imageProxy) {
        if (alreadyRead || processing) {
            imageProxy.close();
            return;
        }

        android.media.Image mediaImage = imageProxy.getImage();
        if (mediaImage == null) {
            imageProxy.close();
            return;
        }

        processing = true;
        InputImage image = InputImage.fromMediaImage(mediaImage, imageProxy.getImageInfo().getRotationDegrees());

        scanner.process(image)
                .addOnSuccessListener(barcodes -> {
                    if (alreadyRead) return;
                    String raw = firstRawValue(barcodes);
                    if (!TextUtils.isEmpty(raw)) {
                        alreadyRead = true;
                        boolean returnResult = getIntent() != null && getIntent().getBooleanExtra(EXTRA_RETURN_RESULT, false);
                        if (returnResult) {
                            Intent data = new Intent();
                            data.putExtra(EXTRA_RESULT_RAW, raw);
                            setResult(RESULT_OK, data);
                            finish();
                        } else {
                            Intent intent = new Intent(QrScanTestActivity.this, QrParseTestActivity.class);
                            intent.putExtra(QrParseTestActivity.EXTRA_QR_RAW, raw);
                            startActivity(intent);
                            finish();
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    // Mantém silencioso para não “piscar” erros no preview.
                })
                .addOnCompleteListener(t -> {
                    processing = false;
                    imageProxy.close();
                });
    }

    private static String firstRawValue(@NonNull List<Barcode> barcodes) {
        for (Barcode b : barcodes) {
            String raw = b.getRawValue();
            if (raw != null && !raw.trim().isEmpty()) {
                return raw.trim();
            }
        }
        return "";
    }

    private static Map<String, String> parseQr(String qrText) {
        Map<String, String> map = new HashMap<>();
        if (qrText == null) return map;

        String[] partes = qrText.split(";");
        for (String p : partes) {
            String[] kv = p.split("=", 2);
            if (kv.length == 2) {
                String chave = kv[0].trim().toUpperCase();
                String valor = kv[1].trim();
                if (!chave.isEmpty()) {
                    map.put(chave, valor);
                }
            }
        }
        return map;
    }
}

