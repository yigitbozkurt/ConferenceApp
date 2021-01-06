package com.yigit.conferenceapp.ui.main;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.MenuItem;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.yigit.conferenceapp.R;
import com.yigit.conferenceapp.ui.login.LoginActivity;
import com.yigit.conferenceapp.ui.main.ui.home.HomeFragment;
import com.yigit.conferenceapp.ui.main.ui.planner.PlannerFragment;
import com.yigit.conferenceapp.ui.main.ui.profile.ProfileFragment;
import com.yigit.conferenceapp.utils.Utils;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

public class MainActivity extends AppCompatActivity {

    //Ana üç sayfanın tanımlanma değişkenleri
    Boolean isInitHome = false;
    Boolean isInitPlanner = false;
    Boolean isInitProfile = false;

    //Hali hazırda görünen ekranın tagi
    String currentFragmentTag = "HomeFragment";

    //Uygulama tanımlanmış mı diye kontrol etmek için
    Boolean initted = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //Sayfayı dikey yapmak için
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setContentView(R.layout.activity_main);

        //Burada eğer uygulama tanımlanmamış ise altbarı çağırıyor ve anasayfayı yüklüyoruz.
        if (!initted){
            initView();
            openMainPage();
            initted=true;
        }

    }

    //Bu metodda alt barımızı ve bar itemlerinin tıklanma olaylarını tanımlıyoruz.
    private void initView() {
        BottomNavigationView navView = findViewById(R.id.nav_view);

        navView.setOnNavigationItemSelectedListener(item -> {
            if (item.getItemId()==R.id.navigation_home){
                openMainPage();
            }
            else if (item.getItemId()==R.id.navigation_planner){
                openPlannerPage();
            }
            else if (item.getItemId()==R.id.navigation_profile){
                openProfilePage();
            }
            return true;
        });
    }

    //Ana sayfamızı açmak için önce önde açık olan sayfa varsa kapatıyoruz daha sonra Ana sayfamız daha önceden bir kere açılmışsa yani
    //tanımlanmışsa tanımlananı getiriyoruz, eğer tanımlanmamışsa baştan oluşturuyor ve ekranda gösteriyoruz.
    private void openMainPage() {
        int fragmentCount = getSupportFragmentManager().getBackStackEntryCount();
        for (int i=0;i<fragmentCount;i++) {
            getSupportFragmentManager().popBackStackImmediate();
        }

        if (isInitHome) {
            if (!currentFragmentTag.equals("HomeFragment")) {
                Fragment homeFragment = getSupportFragmentManager().findFragmentByTag("HomeFragment");
                replaceFragment(homeFragment, "HomeFragment");
                currentFragmentTag = "HomeFragment";
            }
        } else {
            HomeFragment homeFragment = new HomeFragment();
            replaceFragment(homeFragment, "HomeFragment");
            currentFragmentTag = "HomeFragment";
            isInitHome = true;
        }
    }

    //Planner sayfamızı açmak için önce önde açık olan sayfa varsa kapatıyoruz daha sonra planner sayfamız daha önceden bir kere açılmışsa yani
    //tanımlanmışsa tanımlananı getiriyoruz, eğer tanımlanmamışsa baştan oluşturuyor ve ekranda gösteriyoruz.
    private void openPlannerPage() {
        int fragmentCount = getSupportFragmentManager().getBackStackEntryCount();
        for (int i=0;i<fragmentCount;i++) {
            getSupportFragmentManager().popBackStackImmediate();
        }

        if (isInitPlanner) {
            if (!currentFragmentTag.equals("PlannerFragment")) {
                Fragment plannerFragment = getSupportFragmentManager().findFragmentByTag("PlannerFragment");
                replaceFragment(plannerFragment, "PlannerFragment");
                currentFragmentTag = "PlannerFragment";
            }
        } else {
            PlannerFragment plannerFragment = new PlannerFragment();
            replaceFragment(plannerFragment, "PlannerFragment");
            currentFragmentTag = "PlannerFragment";
            isInitPlanner = true;
        }
    }

    //Profil sayfamızı açmak için önce önde açık olan sayfa varsa kapatıyoruz daha sonra profil sayfamız daha önceden bir kere açılmışsa yani
    //tanımlanmışsa tanımlananı getiriyoruz, eğer tanımlanmamışsa baştan oluşturuyor ve ekranda gösteriyoruz.
    private void openProfilePage() {
        int fragmentCount = getSupportFragmentManager().getBackStackEntryCount();
        for (int i=0;i<fragmentCount;i++) {
            getSupportFragmentManager().popBackStackImmediate();
        }

        if (isInitProfile) {
            if (!currentFragmentTag.equals("ProfileFragment")) {
                Fragment profileFragment = getSupportFragmentManager().findFragmentByTag("ProfileFragment");
                replaceFragment(profileFragment, "ProfileFragment");
                currentFragmentTag = "ProfileFragment";
            }
        } else {
            ProfileFragment profileFragment = new ProfileFragment();
            replaceFragment(profileFragment, "ProfileFragment");
            currentFragmentTag = "ProfileFragment";
            isInitProfile = true;
        }
    }


    //Burada ana üç sayfamızı yönetmek için kullandığımız metodumuz var.
    //Bu metod içerisine gönderdiğimiz sayfayı yoksa ekliyor, varsa gösteriyor ve diğer sayfaları gizliyor.
    public void replaceFragment(Fragment fragment, String tag) {

        if (fragment.isAdded()){
            getSupportFragmentManager().beginTransaction().show(fragment).commit();
        }
        else{
            getSupportFragmentManager().beginTransaction().add(R.id.frameLayout,fragment,tag).commit();
        }

        for (int i=0;i < getSupportFragmentManager().getFragments().size();i++){
            if (getSupportFragmentManager().getFragments().get(i)!=fragment && getSupportFragmentManager().getFragments().get(i).isAdded()){
                getSupportFragmentManager().beginTransaction().hide(getSupportFragmentManager().getFragments().get(i)).commit();
            }
        }


    }

    //Burada mantık içerisine yolladığımız ekranı ve ekranın ismini tanımlayıp ekranda gösteriyoruz örneğin detay sayfasına gitmek için bu metoda
    //loadFragment(detayFragment,"DetayFragment") yolluyoruz ve o ekleme işlemlerini yaparak o sayfayı açıyor.
    public Boolean loadFragment(Fragment fragment ,String tag) {
        if (fragment != null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .add(R.id.frameLayout, fragment, tag)
                    .addToBackStack(tag)
                    .commit();
            return true;
        }
        return false;
    }

    //Uygulamadan çıkış yapmak için kullanıyoruz, burada bizi firebaseden cıkararak login sayfasına yönlendiriyor.
    public void logOut(){
        FirebaseAuth.getInstance().signOut();
        startActivity(new Intent(MainActivity.this, LoginActivity.class));
        finish();
    }

    //Uygulamada herhangi bir yerde geri tuşuna basılınca eğer önde bir sayfa varsa onu kapatıyor, yoksa uygulamadan çıkma dialogunu gösteriyoruz.
    @Override
    public void onBackPressed() {
        if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
            getSupportFragmentManager().popBackStackImmediate();

        }
        else{
            Utils.initAlertDialogExit(this);
        }

    }
}