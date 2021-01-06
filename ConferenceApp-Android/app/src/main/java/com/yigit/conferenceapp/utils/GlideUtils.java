package com.yigit.conferenceapp.utils;

import android.content.Context;
import android.net.Uri;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.bumptech.glide.request.RequestOptions;
import com.google.firebase.storage.StorageReference;

public abstract class GlideUtils {

    //Bu classta resimlerimizi düz yuvarlak gibi biçimlerde kullanabilmek için glide kütüphanemizi kullandığımız yer
    //Örneğin profil resmini çekerken urlToImageViewCircle yi vererek gerekli alana bastırmasını sağlıyoruz.
    public static void urlToImageView(Context context , String url , ImageView img ) {
        Glide.with(context)
                .load(Uri.parse(url))
                .transition(DrawableTransitionOptions.withCrossFade())
                .transform(new CenterCrop())
                .listener(new ImageRequestListener())
                .into(img);
    }

    public static void urlToImageViewCircle(Context context , String url , ImageView img ) {
        Glide.with(context)
                .load(url)
                .transition(DrawableTransitionOptions.withCrossFade())
                .transform(new CenterCrop())
                .apply(RequestOptions.circleCropTransform())
                .into(img);
    }

    public static void urlFirebaseToImageViewCircle(Context context , Uri url , ImageView img ) {
        Glide.with(context)
                .load(url)
                .transition(DrawableTransitionOptions.withCrossFade())
                .transform(new CenterCrop())
                .apply(RequestOptions.circleCropTransform())
                .into(img);
    }


    public static void localToImageViewCircle(Context context, String url, ImageView img) {
        Glide.with(context)
                .asBitmap()
                .load(url)
                .transform(new CenterCrop())
                .apply(RequestOptions.circleCropTransform())
                .into(img);
    }
}
