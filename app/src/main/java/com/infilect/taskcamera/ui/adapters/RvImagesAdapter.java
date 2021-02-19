package com.infilect.taskcamera.ui.adapters;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.infilect.taskcamera.R;
import com.infilect.taskcamera.Utils;
import com.infilect.taskcamera.databinding.ImageItemLayoutBinding;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

public class RvImagesAdapter extends RecyclerView.Adapter<RvImagesAdapter.VH> {
    List<StorageItems> mDatas = new ArrayList<>();

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ImageItemLayoutBinding binding = DataBindingUtil.inflate(
                LayoutInflater.from(parent.getContext()), R.layout.image_item_layout, null, false);

        return new VH(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        holder.setItem(mDatas.get(position));
    }

    @Override
    public int getItemCount() {
        return mDatas.size();
    }

    public void setItems(List<StorageItems> storageItems) {
        this.mDatas = storageItems;
        notifyDataSetChanged();
    }

    public class VH extends RecyclerView.ViewHolder {
        private final ImageItemLayoutBinding binding;

        public VH(@NonNull ImageItemLayoutBinding binding) {
            super(binding.getRoot());
            this.binding = binding;

        }

        public void setItem(StorageItems storageReference) {
            Picasso.get().load(storageReference.uri)
                    .error(R.drawable.ic_warning)
                    .into(binding.ivImages, new Callback() {
                        @Override
                        public void onSuccess() {
                            binding.imageLoading.setVisibility(View.GONE);
                        }

                        @Override
                        public void onError(Exception e) {
                            e.printStackTrace();
                            binding.imageLoading.setVisibility(View.GONE);
                        }
                    });

        }
    }
}
