package com.infilect.taskcamera.ui.fragments;

import android.Manifest;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.GridLayoutManager;

import com.google.android.material.snackbar.Snackbar;
import com.infilect.taskcamera.R;
import com.infilect.taskcamera.Utils;
import com.infilect.taskcamera.helper.permission.CustomMultiplePermissionListener;
import com.infilect.taskcamera.helper.permission.CustomPermissionListener;
import com.infilect.taskcamera.services.FileUploadService;
import com.infilect.taskcamera.ui.adapters.RvImagesAdapter;
import com.infilect.taskcamera.ui.viewmodel.MainViewModel;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.multi.CompositeMultiplePermissionsListener;
import com.karumi.dexter.listener.multi.SnackbarOnAnyDeniedMultiplePermissionsListener;

import java.util.ArrayList;
import java.util.List;

public class MainFragment extends Fragment implements CustomPermissionListener {

    com.infilect.taskcamera.databinding.FragmentMainBinding binding;
    private CompositeMultiplePermissionsListener allPermissionsListener;
    private List<String> permissions = new ArrayList<>();
    private RvImagesAdapter rvImagesAdapter;
    private MainViewModel imageHelpers;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(
                inflater, R.layout.fragment_main, container, false);
        permissions.add(Manifest.permission.CAMERA);
        permissions.add(Manifest.permission.ACCESS_FINE_LOCATION);
        permissions.add(Manifest.permission.ACCESS_COARSE_LOCATION);
        permissions.add(Manifest.permission.READ_EXTERNAL_STORAGE);
        permissions.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        allPermissionsListener =
                new CompositeMultiplePermissionsListener(new CustomMultiplePermissionListener(this),
                        SnackbarOnAnyDeniedMultiplePermissionsListener.Builder.with(binding.getRoot(),
                                R.string.permission_need)
                                .withOpenSettingsButton(R.string.settings_btn_text)
                                .build());
        Utils.showOnboardingScreen(getActivity(), binding.fabCamera);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        binding.pbLoadingLayout.setVisibility(View.VISIBLE);
        binding.fabCamera.setOnClickListener(v -> requestRuntimePermission());
        imageHelpers = new ViewModelProvider(getActivity()).get(MainViewModel.class);
        rvImagesAdapter = new RvImagesAdapter();
        binding.rvImageItems.setLayoutManager(new GridLayoutManager(getActivity(), 2));
        binding.rvImageItems.setAdapter(rvImagesAdapter);
        FileUploadService.getUploadingProgress().observe(getViewLifecycleOwner(),
                progress -> {
                    if (progress == FileUploadService.COMPLETED_PROGRESS) {
                        binding.uploadingLayout.setVisibility(View.GONE);
                        binding.pbUpload.setProgress(0);
                        updateList();
                    } else {
                        binding.uploadingLayout.setVisibility(View.VISIBLE);
                        binding.pbUpload.setProgress(progress);
                    }
                });
        imageHelpers.getImageList().observe(getViewLifecycleOwner(), storageItems -> {
            rvImagesAdapter.setItems(storageItems);
            if (rvImagesAdapter.getItemCount() == 0) {
                showEmptySnackbar();
            }
            binding.pbLoadingLayout.setVisibility(View.GONE);
        });
    }

    private void showEmptySnackbar() {
        Snackbar snackbar = Snackbar
                .make(getView(),
                        "List is empty please add items by opening camera and uploading them",
                        Snackbar.LENGTH_LONG);
        snackbar.show();
    }

    private void updateList() {
        binding.pbLoadingLayout.setVisibility(View.VISIBLE);
        imageHelpers.loadDatas();
    }

    private void requestRuntimePermission() {
        Dexter.withContext(getActivity())
                .withPermissions(permissions)
                .withListener(allPermissionsListener)
                .onSameThread()
                .check();
    }

    @Override
    public void showPermissionGranted(String permissionName) {
        permissions.remove(permissionName);
        if (permissions.size() == 0) {
            openCamera();
        }
        binding.tvPermission.setVisibility(View.GONE);
    }

    private void openCamera() {
        Navigation.findNavController(getView()).navigate(R.id.action_mainFragment_to_cameraFragment);
    }

    @Override
    public void showPermissionDenied(String permissionName, boolean permanentlyDenied) {
        binding.tvPermission.setVisibility(View.VISIBLE);
    }

    @Override
    public void showPermissionRationale(PermissionToken token) {
    }
}