package com.infilect.taskcamera.helper.permission;

import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;

import java.util.List;

public class CustomMultiplePermissionListener implements MultiplePermissionsListener {
    private final CustomPermissionListener listener;
    public CustomMultiplePermissionListener(CustomPermissionListener listener) {
        this.listener = listener;
    }

    @Override public void onPermissionsChecked(MultiplePermissionsReport report) {
        for (PermissionGrantedResponse response : report.getGrantedPermissionResponses()) {
            listener.showPermissionGranted(response.getPermissionName());
        }

        for (PermissionDeniedResponse response : report.getDeniedPermissionResponses()) {
            listener.showPermissionDenied(response.getPermissionName(), response.isPermanentlyDenied());
        }
    }

    @Override public void onPermissionRationaleShouldBeShown(List<PermissionRequest> permissions,
                                                             PermissionToken token) {
        listener.showPermissionRationale(token);
    }
}
