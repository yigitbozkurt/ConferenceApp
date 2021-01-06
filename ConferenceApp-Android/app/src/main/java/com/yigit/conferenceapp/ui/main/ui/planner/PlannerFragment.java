package com.yigit.conferenceapp.ui.main.ui.planner;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.yigit.conferenceapp.R;
import com.yigit.conferenceapp.data.model.seminar.SeminarModel;
import com.yigit.conferenceapp.ui.main.MainActivity;
import com.yigit.conferenceapp.ui.main.ui.home.HomeAdapter;
import com.yigit.conferenceapp.ui.main.ui.home.onItemClick;
import com.yigit.conferenceapp.ui.main.ui.seminar_detail.SeminarDetailFragment;
import com.yigit.conferenceapp.utils.Utils;

import java.util.ArrayList;

public class PlannerFragment extends Fragment implements onItemClick {

    //Giriş yapan kullanıcıyı almak için kullanacağız.
    private FirebaseAuth mAuth;

    //Listeleme için kullanacağız.
    RecyclerView recyclerViewRegisteredSeminars;


    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_planner, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mAuth = FirebaseAuth.getInstance();

        //Recyclerview'i tanımlayıp ardından internet varlığını kontrol edip kayıt olduğumuz seminerleri çekiyoruz.
        initView(view);

        if (Utils.isNetworkAvailable(requireContext())) {
            getRegisteredSeminarsId();
        }
        else{
            Utils.showToast(requireContext(),"İnternet bağlantınızı kontrol ediniz");
        }
    }

    private void initView(View view) {
        recyclerViewRegisteredSeminars = view.findViewById(R.id.recyclerViewRegisteredSeminars);
    }

    private void getRegisteredSeminarsId(){
        //Burada önce loading başlatıyoruz.
        //Daha sonra Registered_Seminars veritabanından verileri alıyoruz, gelen verilerdeki elemanları alıyoruz, daha sonra iç elemanda gelen user_uid
        //yani kullanıcımızın firebase id'si mevcut ise o seminerin id numarasını listSeminarIds'e eklıyoruz.
        //Böylelikle katıldığımız seminerlerin idlerini bir liste içerisinde kullanabileceğiz.
        //Ardından gelen seminer id sayımız 0dan büyükmü diye kontrol ediyoruz büyük ise bu seminar id'leri getSeminarList metoduna gönderiyoruz.
        Utils.initAlertDialogLoading(requireContext());
        FirebaseUser user = mAuth.getCurrentUser();
        ArrayList<String> listSeminarIds = new ArrayList<>();
        
        DatabaseReference seminarRef = FirebaseDatabase.getInstance().getReference("Registered_Seminars");

        seminarRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                for (DataSnapshot seminarSnapshot: snapshot.getChildren()){
                    if (seminarSnapshot.exists()) {
                        for (DataSnapshot userSnapshot: seminarSnapshot.getChildren()){
                            if (userSnapshot.exists()) {
                                if (userSnapshot.getKey().equals(user.getUid())){
                                    listSeminarIds.add(seminarSnapshot.getKey());
                                }
                            }
                        }
                    }
                }
                
                if (listSeminarIds.size()>0){
                    getSeminarList(listSeminarIds);
                }
                else{
                    Utils.dismissAlertDialogLoading();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Utils.dismissAlertDialogLoading();
            }
        });
    }

    private void getSeminarList(ArrayList<String> listSeminarIds) {
        //Burada Seminars veritabanına bağlanıyoruz, alt elemanı alıp varlığını kontrol ediyoruz.
        //Ardından gelen elemanları tek tek gezerek seminer id'lerini listemizdeki seminer id'lerle uyuşup uyuşmadığına bakıyoruz.
        //Uyuşan olduğunda listSeminar listemize ekliyoruz. İşlem tamamlandığında toplanan verileri recyclerViewRegisteredSeminars içerisine bastırıyoruz.
        ArrayList<SeminarModel> listSeminar = new ArrayList<>();

        recyclerViewRegisteredSeminars.setLayoutManager(new LinearLayoutManager(getContext(),RecyclerView.VERTICAL,false));
        PlannerAdapter plannerAdapter = new PlannerAdapter(getContext(),listSeminar,this);
            DatabaseReference seminarRef = FirebaseDatabase.getInstance().getReference("Seminars");

            seminarRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {

                    for (DataSnapshot seminarSnapshot: snapshot.getChildren()){
                        if (seminarSnapshot.exists()) {
                            SeminarModel seminar = seminarSnapshot.getValue(SeminarModel.class);
                            if (seminar!=null){
                                for (int i=0;i<listSeminarIds.size();i++){
                                    if (seminar.getId().equals(listSeminarIds.get(i))){
                                        listSeminar.add(seminar);
                                    }
                                }
                            }
                        }
                    }
                    plannerAdapter.updateAdapter(listSeminar);
                    recyclerViewRegisteredSeminars.setAdapter(plannerAdapter);
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