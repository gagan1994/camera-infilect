package com.infilect.taskcamera.ui.fragments;

import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.infilect.taskcamera.R;
import com.infilect.taskcamera.Utils;
import com.infilect.taskcamera.databinding.FragmentPreviewBinding;
import com.infilect.taskcamera.services.FileUploadService;
import com.infilect.taskcamera.services.UploadType;

import java.io.File;

public class PreviewFragment extends Fragment {


    private FragmentPreviewBinding binding;
    private String imageFilePath;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        binding = DataBindingUtil.inflate(
                inflater, R.layout.fragment_preview, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        imageFilePath = PreviewFragmentArgs.fromBundle(getArguments()).getImageUrl();
        File imgFile = new File(imageFilePath);
        if (imgFile.exists()) {
            binding.ivPic.setImageURI(Uri.fromFile(imgFile));
            binding.fabUplaodRetrofit.setOnClickListener(this::uploadImageRetrofit);
            binding.fabUplaodFirebase.setOnClickListener(this::uploadImageFirebase);
        }
        checkAndAddLocation(imageFilePath);
    }

    private void checkAndAddLocation(String imageFilePath) {
        double[] latLong = Utils.getLatLongFromFile(imageFilePath);
        if(latLong==null||latLong.length==0){
            return;
        }
        binding.tvLocationDetails.setText("Latitude: " + latLong[0] + " Longitude: " + latLong[1]);

    }

    private void uploadImageRetrofit(View view) {
        FileUploadService.startUpload(getActivity(), imageFilePath, UploadType.RETROFIT);
        popBack();
    }

    private void uploadImageFirebase(View view) {
        FileUploadService.startUpload(getActivity(), imageFilePath, UploadType.FIREBASE);
        popBack();
    }

    private void popBack() {
        Navigation.findNavController(getView()).popBackStack();
        Toast.makeText(getActivity(), "Uploading file", Toast.LENGTH_SHORT).show();
    }
}