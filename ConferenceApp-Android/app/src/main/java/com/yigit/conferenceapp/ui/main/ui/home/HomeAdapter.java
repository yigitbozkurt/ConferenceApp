package com.yigit.conferenceapp.ui.main.ui.home;


import android.content.Context;
import android.media.Image;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.yigit.conferenceapp.R;
import com.yigit.conferenceapp.data.model.seminar.SeminarModel;
import com.yigit.conferenceapp.utils.GlideUtils;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public class HomeAdapter extends RecyclerView.Adapter<HomeAdapter.ViewHolder> {

    //Bu adaptörde içerisine gönderilen verileri tanımlıyoruz.
    private final Context context;
    private List<SeminarModel> seminarList;
    private final onItemClick itemClick;

    public HomeAdapter(Context context, List<SeminarModel> seminarList,onItemClick itemClick) {
        this.context=context;
        this.seminarList = seminarList;
        this.itemClick = itemClick;
    }

    //Bu metodda içerisine gönderilen veriyi listemize eşitliyor ve adaptörü güncelliyoruz.
    public void updateAdapter(List<SeminarModel> seminarList){
        this.seminarList=seminarList;
        notifyDataSetChanged();
    }

    //Burada kartımızda bulunan verileri ekliyor ve tanımlıyoruz.
    public static class ViewHolder extends RecyclerView.ViewHolder {
        final TextView textTitle;
        final TextView textSpeaker;
        final TextView textDate;
        final ImageView imgSeminar;

        public ViewHolder(View view) {
            super(view);
            textTitle = (TextView) view.findViewById(R.id.textTitle);
            textSpeaker = (TextView) view.findViewById(R.id.textSpeaker);
            textDate = (TextView) view.findViewById(R.id.textDate);
            imgSeminar = (ImageView) view.findViewById(R.id.imgSeminar);
        }
    }

    //Burada her item için görünecek kartı belirliyoruz.
    @NotNull
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.cardview_home, viewGroup, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, final int position) {
        //Listemizin gelen pozisyonunda bulunan elemanı alarak gerekli yerlere dolduruyoruz ve resmi Glide yardımıyla imageview'e bastırıyoruz.
        GlideUtils.urlToImageView(context,seminarList.get(position).getThumbnail(),viewHolder.imgSeminar);
        viewHolder.textTitle.setText(seminarList.get(position).getTitle());
        viewHolder.textSpeaker.setText(seminarList.get(position).getSpeaker_name());
        viewHolder.textDate.setText(seminarList.get(position).getDate());

        viewHolder.itemView.setOnClickListener(v -> {
            itemClick.itemClick(seminarList.get(position));
        });
    }

    //toplam item sayısını belirliyoruz.
    @Override
    public int getItemCount() {
        return seminarList.size();
    }
}