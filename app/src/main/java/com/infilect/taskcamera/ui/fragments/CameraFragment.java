package com.infilect.taskcamera.ui.fragments;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.SeekBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import com.infilect.taskcamera.R;
import com.infilect.taskcamera.databinding.FragmentCameraBinding;
import com.infilect.taskcamera.helper.location.CustomLocationManager;
import com.infilect.taskcamera.helper.widgetlisteners.CustomSeekBarChangeListener;
import com.infilect.taskcamera.ui.MainActivity;
import com.infilect.taskcamera.ui.viewmodel.MainViewModel;


public class CameraFragment extends Fragment {
    FragmentCameraBinding binding;
    MainViewModel imageHelpers;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(
                inflater, R.layout.fragment_camera, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        imageHelpers = new ViewModelProvider(getActivity())
                .get(MainViewModel.class);
        imageHelpers.setPreviewLayout(getActivity(), binding.viewFinder);
        imageHelpers.recordLocation(false);
        imageHelpers.getIsLoadingView().observe(getViewLifecycleOwner(), aBoolean -> {
            if (aBoolean) {
                binding.llBuildingPreview.setVisibility(View.VISIBLE);
                binding.ibFlipCamera.setOnClickListener(null);
                binding.ibCaptureImage.setOnClickListener(null);
                binding.sbCameraZoom.setOnSeekBarChangeListener(null);
            } else {
                binding.llBuildingPreview.setVisibility(View.GONE);
                addListners();
            }
        });
        imageHelpers.startCamera();
        imageHelpers.getZoomRatio().observe(getViewLifecycleOwner(), new Observer<Float>() {
            @Override
            public void onChanged(Float aFloat) {
                imageHelpers.setZoomRatio(aFloat);
                binding.sbCameraZoom.setProgress((int) (aFloat * 10));
            }
        });
    }

    private void addListners() {
        binding.ibCaptureImage.setOnClickListener(v -> {
            imageHelpers.captureImage(this::previewImage);
        });
        binding.ibFlipCamera.setOnClickListener(v -> {
            imageHelpers.flipCamera();
        });
        binding.sbCameraZoom.setOnSeekBarChangeListener(new CustomSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    imageHelpers.setZoomRatio((float) (progress / 10));
                }
            }
        });

        binding.ibFlashMode.setOnClickListener(v -> {
            imageHelpers.changeFlashMode();
            updateUi();
        });
        binding.sGps.setOnCheckedChangeListener((buttonView, isChecked) -> {
            LocationManager locationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
            if(isChecked){
                if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                    OnGPS();
                    buttonView.setChecked(false);
                }else {
                    imageHelpers.recordLocation(true);
                }
            }else {
                imageHelpers.recordLocation(false);
            }

        });

    }
    private void OnGPS() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage("Enable GPS").setCancelable(false).setPositiveButton("Yes", new  DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
            }
        }).setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        final AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void updateUi() {
        binding.ibFlashMode.setImageDrawable(getActivity().getDrawable(imageHelpers.getFlashMode().getDrawable()));
    }

    private void previewImage(String uri) {
        CameraFragmentDirections.ActionCameraFragmentToPreviewFragment action
                = CameraFragmentDirections.actionCameraFragmentToPreviewFragment();
        action.setImageUrl(uri);
        Navigation.findNavController(getView()).navigate(action);
    }

}