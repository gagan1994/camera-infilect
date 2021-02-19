package com.infilect.taskcamera.helper.firebasestorage;

import android.content.Context;
import android.net.Uri;

import androidx.lifecycle.MutableLiveData;

import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.infilect.taskcamera.R;
import com.infilect.taskcamera.ui.adapters.StorageItems;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class FirebaseStorageWrapper {
    private static FirebaseStorageWrapper instance;

    public static FirebaseStorageWrapper getInstance() {
        return instance;
    }

    public static void init(Context context) {
        if (instance == null) {
            instance = new FirebaseStorageWrapper(context);
        }
    }

    private final FirebaseStorage storage;

    private FirebaseStorageWrapper(Context context) {
        storage = FirebaseStorage.getInstance(context.getString(R.string.firebase_folder_path));
    }

    public FirebaseStorage getStorage() {
        return storage;
    }

    public void getItemsList(MutableLiveData<List<StorageItems>> listItems) {
        getInstance().getStorage().getReference().listAll()
                .addOnCompleteListener(task -> {
                    List<StorageReference> val = task.getResult().getItems();
                    List<StorageItems> storageItems = new ArrayList<>();
                    if (val.size() == 0) {
                        listItems.postValue(storageItems);
                    }
                    for (StorageReference item : val) {
                        item.getDownloadUrl().addOnSuccessListener(uri -> {
                            storageItems.add(new StorageItems(item, uri));
                            if (storageItems.size() == val.size()) {
                                storageItems.sort((o1, o2) -> o1.getName().compareTo(o2.getName()));
                                listItems.postValue(storageItems);
                            }
                        });
                    }
                });
    }

    public static UploadTask uploadFile(String fileName, String filePath) {
        return getInstance().getStorage().getReference(fileName)
                .putFile(Uri.fromFile(new File(filePath)));
    }
}
