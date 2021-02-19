package com.infilect.taskcamera.ui.viewmodel;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;

import androidx.annotation.NonNull;
import androidx.camera.core.Camera;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.FocusMeteringAction;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.MeteringPoint;
import androidx.camera.core.MeteringPointFactory;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.common.util.concurrent.ListenableFuture;
import com.infilect.taskcamera.Utils;
import com.infilect.taskcamera.helper.camera.FlashModes;
import com.infilect.taskcamera.helper.firebasestorage.FirebaseStorageWrapper;
import com.infilect.taskcamera.helper.location.CustomLocationManager;
import com.infilect.taskcamera.ui.adapters.StorageItems;

import java.io.File;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class MainViewModel extends ViewModel {


    CustomLocationManager locationManager;
    FlashModes flashMode = FlashModes.getHead();
    MutableLiveData<List<StorageItems>> listItems;
    private final MutableLiveData<Boolean> isLoadingView = new MutableLiveData<>();
    private final MutableLiveData<Float> zoomRatio = new MutableLiveData<>();
    private PreviewView previewLayout;
    private Context context;
    private final Executor executor = Executors.newSingleThreadExecutor();
    private int CAMERA_FACING = CameraSelector.LENS_FACING_FRONT;
    private ListenableFuture<ProcessCameraProvider> cameraProviderFuture;
    private ImageCapture imageCapture;
    private Camera camera;
    private CameraSelector cameraSelector;
    private ScaleGestureDetector scaleGestureDetector;
    private final ScaleGestureDetector.SimpleOnScaleGestureListener listener =
            new ScaleGestureDetector.SimpleOnScaleGestureListener() {
                @Override
                public boolean onScale(ScaleGestureDetector detector) {
                    float currentZoomRatio =
                            camera.getCameraInfo().getZoomState().getValue().getZoomRatio();
                    float delta = detector.getScaleFactor();
                    float zoom = currentZoomRatio * delta;
                    zoomRatio.setValue(zoom);
                    return true;
                }
            };
    private boolean isRecord;

    public LiveData<Boolean> getIsLoadingView() {
        return isLoadingView;
    }

    public FlashModes getFlashMode() {
        return flashMode;
    }

    public MutableLiveData<Float> getZoomRatio() {
        return zoomRatio;
    }

    public void setZoomRatio(Float zoom) {
        camera.getCameraControl().setZoomRatio(zoom);
    }

    public void setPreviewLayout(Context context, PreviewView previewLayout) {
        this.context = context;
        this.previewLayout = previewLayout;
        scaleGestureDetector = new ScaleGestureDetector(context, listener);
        this.previewLayout.setOnTouchListener((v, event) -> {
            scaleGestureDetector.onTouchEvent(event);
            onFocus(event);
            return true;
        });
        locationManager = new CustomLocationManager(context, new CustomLocationManager.Listener() {
            @Override
            public void showGpsOnScreenIndicator(boolean hasSignal) {
                Log.i("CustomLocationManager", "hasSignal " + hasSignal);
            }

            @Override
            public void hideGpsOnScreenIndicator() {
                Log.i("CustomLocationManager", "hideGpsOnScreenIndicator");
            }
        });
    }

    public void startCamera() {
        isLoadingView.setValue(true);
        try {
            cameraProviderFuture.get().unbindAll();
        } catch (Exception e) {
            e.printStackTrace();
        }
        cameraProviderFuture = ProcessCameraProvider.getInstance(context);
        cameraProviderFuture.addListener(() -> {
            try {
                bindPreview(cameraProviderFuture.get());
                isLoadingView.setValue(false);
            } catch (ExecutionException | InterruptedException e) {
                // No errors need to be handled for this Future.
                // This should never be reached.
            }
        }, ContextCompat.getMainExecutor(context));
    }

    void bindPreview(ProcessCameraProvider cameraProvider) {
        Preview preview = new Preview
                .Builder()
                .build();
        cameraSelector = new CameraSelector.Builder()
                .requireLensFacing(CAMERA_FACING)
                .build();
        ImageAnalysis imageAnalysis = new ImageAnalysis.Builder()
                .setTargetRotation(previewLayout.getDisplay().getRotation())
                .build();
        ImageCapture.Builder builder = new ImageCapture.Builder();

        imageCapture = builder
                .setFlashMode(flashMode.getValue())
                .build();
        preview.setSurfaceProvider(previewLayout.createSurfaceProvider());
        camera = cameraProvider.bindToLifecycle((LifecycleOwner) context,
                cameraSelector, preview, imageAnalysis, imageCapture);
    }

    private boolean isFront() {
        return CAMERA_FACING == CameraSelector.LENS_FACING_FRONT;
    }

    public void captureImage(ImageListener listener) {
        File file = Utils.getFilePath(context);
        ImageCapture.OutputFileOptions outputFileOptions = new ImageCapture
                .OutputFileOptions.Builder(file)
                .build();
        int cameraFacing = CAMERA_FACING;
        imageCapture.takePicture(outputFileOptions, executor, new ImageCapture.OnImageSavedCallback() {
            @Override
            public void onImageSaved(@NonNull ImageCapture.OutputFileResults outputFileResults) {
                File savedFile=Utils.processCapturedImage(context,file,isRecord,
                        locationManager,cameraFacing);

                new Handler(Looper.getMainLooper()).post(() -> {
                    listener.capturedImage(savedFile.toString());
                });
            }

            @Override
            public void onError(@NonNull ImageCaptureException error) {
                error.printStackTrace();
            }
        });
    }

    public void flipCamera() {
        CAMERA_FACING = isFront() ?
                CameraSelector.LENS_FACING_BACK : CameraSelector.LENS_FACING_FRONT;
        startCamera();
    }

    public void onFocus(MotionEvent event) {
        final MeteringPointFactory factory = previewLayout.createMeteringPointFactory(cameraSelector);
        final MeteringPoint point = factory.createPoint(event.getX(), event.getY());
        final FocusMeteringAction action = new FocusMeteringAction.Builder(point).build();
        camera.getCameraControl().startFocusAndMetering(action);
    }

    public void changeFlashMode() {
        flashMode = flashMode.getNext();
        startCamera();
    }

    public MutableLiveData<List<StorageItems>> getImageList() {
        if (listItems == null) {
            listItems = new MutableLiveData<>();
            loadDatas();
        }
        return listItems;
    }

    public void loadDatas() {
        FirebaseStorageWrapper.getInstance()
                .getItemsList(listItems);
    }


    public void recordLocation(boolean isRecord) {
        this.isRecord = isRecord;
        locationManager.recordLocation(isRecord);
    }
}
