package com.infilect.taskcamera.ui.adapters;

import android.net.Uri;

import com.google.firebase.storage.StorageReference;

public class StorageItems {
    final String name;
    Uri uri;

    public String getName() {
        return name;
    }

    public StorageItems(StorageReference item, Uri uri) {
        name = item.getName();
        this.uri = uri;
    }
}
