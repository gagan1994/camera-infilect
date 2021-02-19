package com.infilect.taskcamera.network;

import android.content.ContentResolver;
import android.content.Context;
import android.net.Uri;
import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;

import okhttp3.MediaType;
import okhttp3.RequestBody;
import okio.BufferedSink;

public class ProgressRequestBody extends RequestBody {
    private static final String LOG_TAG = ProgressRequestBody.class.getSimpleName();

    public interface ProgressCallback {
        public void onProgress(float progress, long total);
    }

    public static class UploadInfo {
        //Content uri for the file
        public Uri contentUri;

        // File size in bytes
        public long contentLength;

        public UploadInfo(File file) {
            this.contentLength = file.length();
            contentUri = Uri.fromFile(file);
        }
    }

    private WeakReference<Context> mContextRef;
    private UploadInfo mUploadInfo;
    private ProgressCallback mListener;

    private static final int UPLOAD_PROGRESS_BUFFER_SIZE = 8192;

    public ProgressRequestBody(Context context, UploadInfo uploadInfo, ProgressCallback listener) {
        mContextRef = new WeakReference<>(context);
        mUploadInfo = uploadInfo;
        mListener = listener;
    }

    @Override
    public MediaType contentType() {
        return MediaType.parse("application/octet-stream");
    }

    @Override
    public void writeTo(BufferedSink sink) throws IOException {
        long fileLength = mUploadInfo.contentLength;
        byte[] buffer = new byte[UPLOAD_PROGRESS_BUFFER_SIZE];
        InputStream in = in();
        long uploaded = 0;

        try {
            int read;
            while ((read = in.read(buffer)) != -1) {
                mListener.onProgress((100*(float)uploaded/(float)fileLength), fileLength);
                uploaded += read;
                sink.write(buffer, 0, read);
            }
        } finally {
            in.close();
        }
    }

    /**
     * WARNING: You must override this function and return the file size or you will get errors
     */
    @Override
    public long contentLength() throws IOException {
        return mUploadInfo.contentLength;
    }

    private InputStream in() throws IOException {
        InputStream stream = null;
        try {
            stream = getContentResolver().openInputStream(mUploadInfo.contentUri);
        } catch (Exception ex) {
            Log.e(LOG_TAG, "Error getting input stream for upload", ex);
        }

        return stream;
    }

    private ContentResolver getContentResolver() {
        if (mContextRef.get() != null) {
            return mContextRef.get().getContentResolver();
        }
        return null;
    }
}
