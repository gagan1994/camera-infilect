package com.infilect.taskcamera.services;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.app.TaskInfo;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.firebase.storage.UploadTask;
import com.infilect.taskcamera.R;
import com.infilect.taskcamera.helper.firebasestorage.FirebaseStorageWrapper;
import com.infilect.taskcamera.network.FileUploader;
import com.infilect.taskcamera.network.ProgressRequestBody;

import java.io.File;
import java.util.ArrayList;
import java.util.UUID;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static androidx.core.app.NotificationCompat.PRIORITY_MIN;

public class FileUploadService extends Service {

    public static final String START_UPLOAD = "START_UPLOAD";
    public static final String CANCEL_UPLOAD = "CANCEL_UPLOAD";
    private static final String UPLOAD_TYPE = "UPLOAD_TYPE";

    public static final Integer COMPLETED_PROGRESS = -1;
    private static final String FILE_LIST = "FILE_LIST";
    private static final String TAG = FileUploadService.class.getSimpleName();
    private static final String CHANNEL_ID = "FILE_UPLOAD";
    private static final int NOTIFICATION_ID = 111;

    private BlockingQueue<String> fileQueue = new ArrayBlockingQueue<>(100);
    private Handler mFileUploadHandler = new Handler();
    private FileUploadTask mFileUploadTask;
    private boolean isFileUploadRunning;

    private static MutableLiveData<Integer> uploadingProgress = new MutableLiveData<>();

    public static LiveData<Integer> getUploadingProgress() {
        return uploadingProgress;
    }

    public static void startUpload(Context context, ArrayList<String> fileList, UploadType uploadType) {
        Intent intent = new Intent(context, FileUploadService.class);
        intent.setAction(START_UPLOAD);
        intent.putStringArrayListExtra(FILE_LIST, fileList);
        intent.putExtra(UPLOAD_TYPE, uploadType);
        context.startService(intent);
    }

    public static void startUpload(Context context, String imageFilePath, UploadType uploadType) {
        ArrayList<String> fileToUpload = new ArrayList<>();
        fileToUpload.add(imageFilePath);
        startUpload(context, fileToUpload, uploadType);
    }

    public static void cancelUpload(Context context) {
        Intent intent = new Intent(context, FileUploadService.class);
        intent.setAction(CANCEL_UPLOAD);
        context.startService(intent);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String action = intent.getAction();
        if (!TextUtils.isEmpty(action)) {
            switch (action) {
                case START_UPLOAD:
                    ArrayList<String> fileList = intent.getStringArrayListExtra(FILE_LIST);
                    UploadType uploadType = (UploadType) intent.getSerializableExtra(UPLOAD_TYPE);
                    if (fileList != null) {
                        startFileUpload(fileList, uploadType);
                    }
                    break;
                case CANCEL_UPLOAD:
                    cancelFileUpload();
                    break;
            }
        }
        return super.onStartCommand(intent, flags, startId);
    }

    private void cancelFileUpload() {
        fileQueue.clear();
        if (mFileUploadTask != null) {
            mFileUploadHandler.removeCallbacks(mFileUploadTask);
            isFileUploadRunning = false;
            mFileUploadTask = null;
            hideNotification();
        }
    }

    private void startFileUpload(ArrayList<String> fileList, UploadType uploadType) {
        for (String filePath : fileList) {
            if (!fileQueue.contains(filePath)) {
                fileQueue.add(filePath);
            }
        }
        startUploadingFile(uploadType);
    }

    private void startUploadingFile(UploadType uploadType) {
        if (mFileUploadTask == null) {
            mFileUploadTask = new FileUploadTask(uploadType);
        }
        if (!isFileUploadRunning) {
            isFileUploadRunning = true;
            mFileUploadHandler.post(mFileUploadTask);
        }
    }

    class FileUploadTask implements Runnable {

        private final UploadType uploadType;

        public FileUploadTask(UploadType uploadType) {
            this.uploadType = uploadType;
        }

        @Override
        public void run() {
            if (fileQueue.isEmpty()) {
                return;
            }
            try {
                String fileName = UUID.randomUUID().toString() + ".jpg";
                String filePath = fileQueue.take();
                Log.i(TAG, "Upload File: " + filePath);
                switch (uploadType) {
                    case FIREBASE:
                        callUploadFileFirebase(fileName, filePath);
                        break;
                    case RETROFIT:
                        callUploadFileRetrofit(fileName, filePath);
                        break;
                }
                showUploadNotification(filePath);
                mFileUploadHandler.post(this);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private void callUploadFileFirebase(String fileName, String filePath) {
        UploadTask task = FirebaseStorageWrapper.uploadFile(fileName, filePath);
        task.addOnProgressListener(snapshot -> {
            float totalByteTransferred = (float) snapshot.getBytesTransferred();
            float total = (float) snapshot.getBytesTransferred();
            float percentage = (totalByteTransferred / total) * 100;
            uploadingProgress.postValue((int) percentage);
        });
        task.addOnCompleteListener(task1 -> uploadCompleted());
    }

    private void callUploadFileRetrofit(String fileName, String filePath) {
        File file = new File(filePath);
        Log.i(TAG, "Uploading file " + filePath);
        Call<ResponseBody> call = FileUploader.uploadFile(fileName, new ProgressRequestBody(this,
                new ProgressRequestBody.UploadInfo(file),
                (progress, total) -> {
                    uploadingProgress.postValue((int) progress);
                }));
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                uploadCompleted();
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Log.e(TAG, "onFailure: " + t.getLocalizedMessage());
                t.printStackTrace();
                uploadCompleted();
            }
        });
    }

    private void uploadCompleted() {
        uploadingProgress.postValue(COMPLETED_PROGRESS);
        isFileUploadRunning = false;
        hideNotification();
    }

    private void hideNotification() {
        NotificationManagerCompat.from(this).cancel(NOTIFICATION_ID);
        stopForeground(true);
    }


    private void showUploadNotification(String fileName) {
        String channelId = "";
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            channelId = createNotificationChannel("my_service", "Uploading file");
        }
        String messageText = "";
        if (fileQueue.size() > 0) {
            messageText = messageText + "\n" + fileQueue.size() + " is remaining.";
        }
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, channelId);
        Notification notification = notificationBuilder.setOngoing(true)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle("File Upload")
                .setContentText(messageText)
                .setPriority(PRIORITY_MIN)
                .setCategory(Notification.CATEGORY_SERVICE)
                .build();
        startForeground(NOTIFICATION_ID, notification);
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private String createNotificationChannel(String channelId, String channelName) {
        NotificationChannel chan = new NotificationChannel(channelId,
                channelName, NotificationManager.IMPORTANCE_NONE);
        chan.setLightColor(Color.BLUE);
        chan.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);
        NotificationManager service = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        ;
        service.createNotificationChannel(chan);
        return channelId;
    }
}