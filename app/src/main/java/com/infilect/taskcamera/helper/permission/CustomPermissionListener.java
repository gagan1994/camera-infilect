package com.infilect.taskcamera.helper.permission;

import com.karumi.dexter.PermissionToken;

public interface CustomPermissionListener {
     void showPermissionGranted(String permissionName);

    void showPermissionDenied(String permissionName, boolean permanentlyDenied);

    void showPermissionRationale(PermissionToken token);
}
