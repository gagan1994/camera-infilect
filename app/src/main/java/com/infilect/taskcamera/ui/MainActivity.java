package com.infilect.taskcamera.ui;

import android.content.DialogInterface;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.infilect.taskcamera.R;

import am.appwise.components.ni.ConnectionCallback;
import am.appwise.components.ni.NoInternetDialog;

public class MainActivity extends AppCompatActivity {

    private NoInternetDialog noInternetDialog;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        noInternetDialog = new NoInternetDialog.Builder(this).build();
        noInternetDialog.setOnDismissListener(dialog ->
                noInternetDialog = new NoInternetDialog.Builder(MainActivity.this).build());
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        noInternetDialog.onDestroy();
    }
}
