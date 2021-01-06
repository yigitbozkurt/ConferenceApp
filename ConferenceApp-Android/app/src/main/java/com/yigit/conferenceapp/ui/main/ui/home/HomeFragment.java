package com.yigit.conferenceapp.ui.main.ui.home;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.yigit.conferenceapp.R;
import com.yigit.conferenceapp.data.model.seminar.SeminarModel;
import com.yigit.conferenceapp.ui.main.MainActivity;
import com.yigit.conferenceapp.ui.main.ui.seminar_detail.SeminarDetailFragment;
import com.yigit.conferenceapp.utils.Utils;

import java.util.ArrayList;

public class HomeFragment extends Fragment implements onItemClick {

    //Listeleme yapmak için tanımlamaları ekliyoruz.
    RecyclerView recyclerViewSeminars;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        //Ekrandaki nesneleri çekiyoruz ve internet varlığını kontrol edip seminerleri firebase'den çekiyoruz.
        initView(view);

        if (Utils.isNetworkAvailable(requireContext())) {
            getAllSeminars();
        }
        else{
            Utils.showToast(requireContext(),"İnternet bağlantınızı kontrol ediniz");
        }
    }

    private void initView(View view) {
        recyclerViewSeminars = view.findViewById(R.id.recyclerViewSeminars);
    }

    private void getAllSeminars(){
        //Burada önce recyclerview'i ve adapteri kuruyoruz.
        //Ardından firebasede Seminars kısmına bağlanıyoruz daha sonrasinda seminars içerisindeki verileri alıyoruz.
        //Sonrasında elemanlara tek tek bakıyoruz ve eleman içeriği varmı diye kontrol ediyoruz, var ise listSeminar listemize ekliyoruz.
        //Tüm elemanlar kontrol edildikten sonra toplanan verileri recyclerview'e adapter yardımıyla gönderiyoruz ve yükleniyor dialogunu kapatıyoruz.
        Utils.initAlertDialogLoading(requireContext());
        ArrayList<SeminarModel> listSeminar = new ArrayList<>();

        recyclerViewSeminars.setLayoutManager(new LinearLayoutManager(getContext(),RecyclerView.VERTICAL,false));
        HomeAdapter homeAdapter = new HomeAdapter(getContext(),listSeminar,this);

        DatabaseReference seminarRef = FirebaseDatabase.getInstance().getReference("Seminars");

        seminarRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                for (DataSnapshot seminarSnapshot: snapshot.getChildren()){
                    if (seminarSnapshot.exists()) {
                        SeminarModel seminar = seminarSnapshot.getValue(SeminarModel.class);
                        if (seminar!=null){
                            listSeminar.add(seminar);
                        }
                    }
                }
                homeAdapter.updateAdapter(listSeminar);
                recyclerViewSeminars.setAdapter(homeAdapter);
                Utils.dismissAlertDialogLoading();

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Utils.dismissAlertDialogLoading();
            }
        });
    }


    //Burada bir seminerin üzerine tıklandığında tıklanan seminer bilgisiyle beraber seminer detay sayfasına yönlendirme yapıyoruz.
    @Override
    public void itemClick(SeminarModel seminar) {
        SeminarDetailFragment seminarDetailFragment = new SeminarDetailFragment(seminar);
        ((MainActivity)getActivity()).loadFragment(seminarDetailFragment,"SeminarDetailFragment");
    }
}