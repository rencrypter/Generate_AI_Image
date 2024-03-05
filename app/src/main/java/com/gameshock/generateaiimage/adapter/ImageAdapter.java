package com.gameshock.generateaiimage.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Environment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.gameshock.generateaiimage.R;
import com.gameshock.generateaiimage.model.GeneratedImage;
import com.google.android.material.snackbar.Snackbar;

import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ImageAdapter extends RecyclerView.Adapter<ImageAdapter.ImageViewHolder> {

    private List<GeneratedImage> generatedImages;
    private Context context;

    public ImageAdapter(Context context, List<GeneratedImage> generatedImages) {
        this.context = context;
        this.generatedImages = generatedImages;
    }

    @NonNull
    @Override
    public ImageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_image, parent, false);
        return new ImageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ImageViewHolder holder, int position) {
        GeneratedImage generatedImage = generatedImages.get(position);

        Log.d("TAGrrr", "onBindViewHolder: "+generatedImages.get(position));
        // Load image using Glide or your preferred image loading library
        Glide.with(context)
                .load(generatedImage.getImageUrl())
                .into(holder.imageView);

        holder.dBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                holder.imageView.setDrawingCacheEnabled(true);
                holder.imageView.buildDrawingCache();
                holder.imageView.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_HIGH);
                Bitmap bitmap = holder.imageView.getDrawingCache();
                saveImage(bitmap, holder.dBtn);
                holder.imageView.setDrawingCacheEnabled(false);
            }
        });
    }

    private void saveImage(Bitmap bmp, CardView dBtn) {
        if (bmp == null) {

//            Snackbar.make(binding.mainView, "null", Snackbar.LENGTH_SHORT).show();
            Toast.makeText(context, "null", Toast.LENGTH_SHORT).show();
        } else {
            String root = Environment.getExternalStorageDirectory().getAbsolutePath();
            File file = new File(root + "/Download");
            // Get the current date and time
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault());
            String timestamp = dateFormat.format(new Date());

            // Append the timestamp to the filename
            String filename = "ai_image_" + timestamp + ".png";
            File myfile = new File(file, filename);

            if (myfile.exists()) {
                myfile.delete();
            }
            try {
                FileOutputStream fileOutputStream = new FileOutputStream(myfile);
                bmp.compress(Bitmap.CompressFormat.PNG, 100, fileOutputStream);
                fileOutputStream.flush();
                fileOutputStream.close();


                Snackbar.make(dBtn, "Downloaded", 1000).show();
//                Toast.makeText(context, "Downloaded", Toast.LENGTH_SHORT).show();

            } catch (Exception e) {

//                Toast.makeText(context, e.getMessage().toString(), Toast.LENGTH_SHORT).show();
                Snackbar.make(dBtn, e.getMessage().toString(), 1000).show();
            }
        }
    }

    @Override
    public int getItemCount() {
        return generatedImages.size();
    }

    public static class ImageViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;
        CardView dBtn;

        public ImageViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.imageView);
            dBtn = itemView.findViewById(R.id.downloadBtn);
        }
    }
}
