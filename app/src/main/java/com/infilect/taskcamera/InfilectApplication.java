package com.infilect.taskcamera;

import android.app.Application;

import com.infilect.taskcamera.helper.camera.FlashModes;
import com.infilect.taskcamera.helper.firebasestorage.FirebaseStorageWrapper;
import com.squareup.picasso.OkHttp3Downloader;
import com.squareup.picasso.Picasso;

public class InfilectApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        FlashModes.init();
        FirebaseStorageWrapper.init(this);
        Picasso.Builder builder = new Picasso.Builder(this);
        builder.downloader(new OkHttp3Downloader(this,Integer.MAX_VALUE));
        Picasso built = builder.build();
        Picasso.setSingletonInstance(built);
    }
}
