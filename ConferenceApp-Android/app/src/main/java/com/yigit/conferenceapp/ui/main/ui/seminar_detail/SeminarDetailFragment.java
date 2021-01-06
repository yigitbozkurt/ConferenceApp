package com.yigit.conferenceapp.ui.main.ui.seminar_detail;

import android.app.Dialog;
import android.content.pm.ActivityInfo;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatButton;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.DefaultLoadControl;
import com.google.android.exoplayer2.DefaultRenderersFactory;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory;
import com.google.android.exoplayer2.source.MediaSource;

import com.google.android.exoplayer2.source.ProgressiveMediaSource;
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.ui.PlaybackControlView;
import com.google.android.exoplayer2.ui.SimpleExoPlayerView;
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.upstream.DefaultHttpDataSource;
import com.google.android.exoplayer2.upstream.DefaultHttpDataSourceFactory;
import com.google.android.exoplayer2.util.Util;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.yigit.conferenceapp.R;
import com.yigit.conferenceapp.data.model.seminar.SeminarModel;
import com.yigit.conferenceapp.utils.GlideUtils;
import com.yigit.conferenceapp.utils.Utils;

import java.util.HashMap;
import java.util.Random;

public class SeminarDetailFragment extends Fragment {

    //Sayfa tanımlanmışmı diye ekliyoruz.
    private Boolean isInitted  = false;

    //Ekran döndürme için kullanacağız kullanıcının kaldığı yeri kayıt altına alacağız.
    private final String STATE_RESUME_WINDOW = "resumeWindow";
    private final String STATE_RESUME_POSITION = "resumePosition";
    private final String STATE_PLAYER_FULLSCREEN = "playerFullscreen";

    //Uygulamadaki video elemanları
    private SimpleExoPlayerView mExoPlayerView= null;
    private MediaSource mVideoSource  = null;
    private Boolean mExoPlayerFullscreen = false;
    private ImageView mFullScreenIcon = null;
    private Dialog mFullScreenDialog = null;

    //Kullanıcının videoda kaldığı kısım
    private int mResumeWindow = 0;
    private long mResumePosition = 0;

    //Gelen seminer bilgilerimiz
    SeminarModel seminarModel;

    //Kullanılan elemanlar
    TextView textTitle;
    TextView textDescription;
    TextView textDate;
    TextView textLocation;
    TextView textSpeaker;
    ImageView imgSpeaker;
    AppCompatButton btnRegister;

    //Kayıtlı kullanıcı için
    private FirebaseAuth mAuth;

    //Sayfaya gönderilen seminer modelini bu sayfadakiyle eşliyoruz.
    public SeminarDetailFragment(SeminarModel seminarModel) {
        this.seminarModel = seminarModel;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        //Kullanıcının video kayıt bilgilerini ve pozisyonlarını tutuyoruz.
        if (savedInstanceState != null) {
            mResumeWindow = savedInstanceState.getInt(STATE_RESUME_WINDOW);
            mResumePosition = savedInstanceState.getLong(STATE_RESUME_POSITION);
            mExoPlayerFullscreen = savedInstanceState.getBoolean(STATE_PLAYER_FULLSCREEN);
        }
        return inflater.inflate(R.layout.fragment_seminar_detail, container, false);
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        //Kullanıcının video kayıt bilgilerini ve pozisyonunu güncelliyoruz.
        outState.putInt(STATE_RESUME_WINDOW, mResumeWindow);
        outState.putLong(STATE_RESUME_POSITION, mResumePosition);
        outState.putBoolean(STATE_PLAYER_FULLSCREEN, mExoPlayerFullscreen);
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        //Kullanıcı için
        mAuth = FirebaseAuth.getInstance();

        //Eğer video oynatıcımız tanımlanmamışsa tanımlıyoruz ve videoda gerekli butonları tanımlıyoruz.
        if (mExoPlayerView == null)
        {
            mExoPlayerView = (SimpleExoPlayerView)requireView().findViewById(R.id.video_player);
            initFullscreenDialog();
            initFullscreenButton();

        }

        //Loading başlatıyoruz ve internet varlığını kontrol edip seminer bilgilerini kaydediyoruz ardından video oynatıcımızı kuruyoruz.
        if (Utils.isNetworkAvailable(requireContext())) {
            Utils.initAlertDialogLoading(requireContext());
            initView();
            initRegisterButton();
            initClick();
            initPlayer(seminarModel.getVideo());
        }
        else{
            Utils.showToast(requireContext(),"İnternet bağlantınızı kontrol ediniz");
        }
    }

    private void initView() {
        //Uygulamadaki nesnelerin tanımları
        textTitle=requireView().findViewById(R.id.textTitle);
        textDescription=requireView().findViewById(R.id.textDescription);
        textDate=requireView().findViewById(R.id.textDate);
        textLocation=requireView().findViewById(R.id.textLocation);
        textSpeaker=requireView().findViewById(R.id.textSpeaker);
        imgSpeaker=requireView().findViewById(R.id.imgSpeaker);
        btnRegister=requireView().findViewById(R.id.btnRegister);

        textTitle.setText(seminarModel.getTitle());
        textDescription.setText(seminarModel.getDescription());
        textDate.setText(seminarModel.getDate());
        textLocation.setText(seminarModel.getLocation());
        textSpeaker.setText(seminarModel.getSpeaker_name());

        //Konuşmacı resmini Glide kullanarak yüklüyoruz.
        GlideUtils.urlToImageViewCircle(requireContext(),seminarModel.getSpeaker_photo(),imgSpeaker);
    }

    private void initRegisterButton() {
        //Kullanıcımızı alıyoruz.
        FirebaseUser user = mAuth.getCurrentUser();

        //Burada yapacağımız şey kullanıcımız bu seminere daha önceden kayıt olmuş ise kaydolma butonunu kaldırmak, eğer olmamışsa göstermek.
        //Registered_Seminars tablomuza bağlanıp verileri çekiyoruz, tek tek gezerek seminer içerisinde kullanıcımızın uid'si var mı diye bakıyoruz.
        //Var ise kaydol butonunu kaldırıyoruz. Eğer bulamadıysa aktif ediyoruz.
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Registered_Seminars");
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (snapshot.hasChild(seminarModel.getId())) {
                    DataSnapshot userSnapshot = snapshot.child(seminarModel.getId());
                    if (userSnapshot.hasChild(user.getUid())){
                        btnRegister.setVisibility(View.GONE);
                    }
                    else{
                        btnRegister.setVisibility(View.VISIBLE);
                    }
                }
                else{
                    btnRegister.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void initClick() {
        //Kaydola tıklayınca metodu çağırıyoruz.
        btnRegister.setOnClickListener(v -> {
            registerToSeminar();
        });
    }

    private void registerToSeminar() {
        //Burada Registered_Seminars tablosunun altına seminer modelimizin id'si, onun içerisine kullanıcımızın uid'si olacak şekilde eleman açıyoruz.
        //Ardından içerisine referenceCode adında random bir integer değer atıyoruz.
        //Bunu belirlediğimiz konuma kaydederek kullanıcımızı bu seminere kaydetmiş oluyoruz ve kayıt butonunu gizliyoruz.
        FirebaseUser user = mAuth.getCurrentUser();

        if (user != null) {
            DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Registered_Seminars").child(seminarModel.getId())
                    .child(user.getUid());

            HashMap<String, Integer> hashMap = new HashMap<>();
            hashMap.put("referenceCode", new Random().nextInt());

            reference.setValue(hashMap).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    Utils.showToast(requireContext(),"Kayıt başarıyla tamamlandı.");
                    btnRegister.setVisibility(View.GONE);
                }
            });


        }
    }


    private void initFullscreenDialog() {
        //Tam ekran dialogunu tanımlıyoruz, geri butonuna basılınca tam ekrandan çıkış metodunu ekliyoruz.
        mFullScreenDialog = new Dialog(requireContext(), android.R.style.Theme_Black_NoTitleBar_Fullscreen) {
            @Override
            public void onBackPressed() {
                if (mExoPlayerFullscreen)
                    closeFullscreenDialog();
                super.onBackPressed();
            }
        };
    }

    private void openFullscreenDialog() {
        //Tam ekran butonuna basıldığında ekranı önce yatay pozisyona çeviriyoruz. Ekrandakileri silip tam ekrana çeviriyoruz.
        //Ardından tam ekran ikonunu tam ekrandan çık butonuna çeviriyoruz.
        requireActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        ((ViewGroup)mExoPlayerView.getParent()).removeView(mExoPlayerView);
        mFullScreenDialog.addContentView(mExoPlayerView, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT));
        mFullScreenIcon.setImageDrawable(ContextCompat.getDrawable(requireContext(), R.drawable.ic_fullscreen_exit));
        mExoPlayerFullscreen = true;
        mFullScreenDialog.show();
    }
    private void closeFullscreenDialog() {
        //Tam ekrandan çık butonuna basılınca ekranı dikey pozisyona alıyoruz. Ekrandakileri yerlerine alıyoruz.
        //Ardından tam ekrandan cık butonunu tam ekran butonu yapıyoruz.
        requireActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        ((ViewGroup)mExoPlayerView.getParent()).removeView(mExoPlayerView);
        ((FrameLayout)requireView().findViewById(R.id.main_media_frame)).addView(mExoPlayerView);
        mExoPlayerFullscreen = false;
        mFullScreenDialog.dismiss();
        mFullScreenIcon.setImageDrawable(ContextCompat.getDrawable(requireContext(), R.drawable.ic_fullscreen));
    }

    private void initFullscreenButton() {
        //Tam ekran butonuna basıldığında tam ekrandaysa çık, değilse tam ekran yap metodlarını çağırıyoruz.
        PlaybackControlView controlView = mExoPlayerView.findViewById(R.id.exo_controller);
        mFullScreenIcon = mExoPlayerView.findViewById(R.id.btnFullScreen);
        mFullScreenIcon.setOnClickListener(v -> {
            Log.d("full","tıklandi");
            if (!mExoPlayerFullscreen)
                openFullscreenDialog();
            else
                closeFullscreenDialog();
        });
    }

    @Override
    public void onResume() {
        //Uygulama yeniden çağırıldığında(uygulamayı arka plana atıp geri gelme gibi) video playerimizi eğer tanımlanmamışsa tanımlıyoruz.
        super.onResume();
        if (mExoPlayerView == null)
        {
            mExoPlayerView = (SimpleExoPlayerView)requireView().findViewById(R.id.video_player);
            initFullscreenDialog();
            initFullscreenButton();

        }

        //Ardından eğer video playerimiz tanımlıysa kurma metodlarını çağırıyoruz.
        if(!isInitted && mExoPlayerView!=null && mVideoSource!=null)
            initExoPlayer();

        //Tam ekrandaysak tam ekran moduna geçiyoruz.
        if (mExoPlayerFullscreen)
        {
            ((ViewGroup)mExoPlayerView.getParent()).removeView(mExoPlayerView);
            mFullScreenDialog.addContentView(mExoPlayerView, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
            mFullScreenIcon.setImageDrawable(ContextCompat.getDrawable(requireContext(), R.drawable.ic_fullscreen_exit));
            mFullScreenDialog.show();
        }

    }

    @Override
    public void onPause() {
        //Uygulama durdurulduğunda(örnek olarak alta alma) kullanıcının videoda kaldığı yeri kaydediyoruz ve video playeri temizliyoruz.
        //Tam ekrandan çıkıyoruz.
        //Tanımlanma değişkenini false yapıyoruz(bu geri döndüğümüzde tekrar tanımlanması için)
        super.onPause();
        if (mExoPlayerView != null && mExoPlayerView.getPlayer() != null)
        {
            mResumeWindow = mExoPlayerView.getPlayer().getCurrentWindowIndex();
            mResumePosition = Math.max(0, mExoPlayerView.getPlayer().getContentPosition());
            mExoPlayerView.getPlayer().release();
        }
        if (mFullScreenDialog != null)
            mFullScreenDialog.dismiss();

        isInitted=false;
    }

    private void initPlayer(String url){
        //Burada video player tanımlarken hazır olarak kullanılan kodları yazıyoruz bunlar video playerin çalışacağı sunucu ve altyapı olarak
        //düşünülebilir.
        //Gelen linki kurulum yaparak video kaynağına atıyoruz daha sonra son tanımlama için initExoplayeri çağırıyoruz.
        String userAgent = Util.getUserAgent(requireContext(), requireContext().getApplicationContext().getApplicationInfo().packageName);
        DefaultHttpDataSourceFactory httpDataSourceFactory = new DefaultHttpDataSourceFactory(userAgent, null,
                DefaultHttpDataSource.DEFAULT_CONNECT_TIMEOUT_MILLIS, DefaultHttpDataSource.DEFAULT_READ_TIMEOUT_MILLIS, true);
        DefaultDataSourceFactory dataSourceFactory = new DefaultDataSourceFactory(requireContext(), null, httpDataSourceFactory);
        Uri daUri = Uri.parse(url);
        DefaultExtractorsFactory extractorsFactory = new DefaultExtractorsFactory().setConstantBitrateSeekingEnabled(true);
        ProgressiveMediaSource progressiveMediaSource = new ProgressiveMediaSource.Factory(dataSourceFactory, extractorsFactory)
                .createMediaSource(daUri);
        mVideoSource = progressiveMediaSource;

        initExoPlayer();
    }

    private void initExoPlayer() {
        //Burada yine bant genişliği kontrol gibi mekanizmaları tanımlıyoruz.
        DefaultBandwidthMeter bandwidthMeter = new DefaultBandwidthMeter();
        AdaptiveTrackSelection.Factory videoTrackSelectionFactory = new AdaptiveTrackSelection.Factory(bandwidthMeter);
        DefaultTrackSelector trackSelector = new DefaultTrackSelector(videoTrackSelectionFactory);
        DefaultLoadControl loadControl = new DefaultLoadControl();
        SimpleExoPlayer player = ExoPlayerFactory.newSimpleInstance(requireContext(),
                new DefaultRenderersFactory(requireContext()), trackSelector, loadControl);

        //Ardından bu bilgileri birleştirerek video oynatıcımızın oynatıcısını ayarlıyoruz.
        mExoPlayerView.setPlayer(player);

        //Video oynatıcımızın video durdur ve başlat kısımlarında kontrol ekranı gözükmesini veya gözükmemesini ayarlıyoruz.
        player.addListener(new Player.EventListener() {
            @Override
            public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
                if (playbackState== ExoPlayer.STATE_ENDED)
                    mExoPlayerView.showController();
                else if (!playWhenReady)
                    mExoPlayerView.showController();
                else
                mExoPlayerView.hideController();
            }
        });

        //Eğer video oynatıcımız belli bir pozisonda kalmışsa kaldığı yerden devam etmesini sağlıyoruz.
        Boolean haveResumePosition = mResumeWindow != C.INDEX_UNSET;
        if (haveResumePosition)
        {
            player.seekTo(mResumeWindow, mResumePosition);
        }

        //Video oynatıcımızı kuruyoruz, ardından oynatma özelliğini aktif ediyoruz ve loadingi kaldırıyoruz,böylece videomuz başlıyor.
        player.prepare(mVideoSource, !haveResumePosition, false);
        player.setPlayWhenReady(true);
        isInitted=true;
        Utils.dismissAlertDialogLoading();
    }

}