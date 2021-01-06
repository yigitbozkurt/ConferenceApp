package com.yigit.conferenceapp.utils;

import android.graphics.drawable.Drawable;

import androidx.annotation.Nullable;

import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.bumptech.glide.request.transition.DrawableCrossFadeTransition;

public  class ImageRequestListener implements RequestListener<Drawable> {

    //Yüklenen resimlerin geçişli bir şekilde animasyonlu yüklenmesi için kullanıyoruz.
    @Override
    public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
        return false;
    }

    @Override
    public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
        if (target!=null){
            target.onResourceReady(resource, new DrawableCrossFadeTransition(500,isFirstResource));
        }
        return true;
    }
}
