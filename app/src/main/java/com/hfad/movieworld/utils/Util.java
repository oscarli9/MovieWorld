package com.hfad.movieworld.utils;

import android.content.Context;
import android.widget.ImageView;

import androidx.swiperefreshlayout.widget.CircularProgressDrawable;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

import java.text.DateFormat;

public class Util {
    public static void loadUrl(ImageView imageView, String url, int errorDrawble) {
        Context context = imageView.getContext();
        if (context != null) {
            RequestOptions options = new RequestOptions()
                    .placeholder(progressDrawable(context))
                    .error(errorDrawble);
            Glide.with(context.getApplicationContext())
                    .load(url)
                    .apply(options)
                    .into(imageView);
        }
    }

    private static CircularProgressDrawable progressDrawable(Context context) {
        CircularProgressDrawable result = new CircularProgressDrawable(context);
        result.setStrokeWidth(5.0f);
        result.setCenterRadius(30.0f);
        result.start();
        return result;
    }

    public static String getDate(Long time) {
        if (time != null) {
            DateFormat df = DateFormat.getDateInstance();
            return df.format(time);
        }
        return "Unknown";
    }
}


