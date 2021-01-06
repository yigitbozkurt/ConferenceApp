package com.yigit.conferenceapp.ui.splash;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.yigit.conferenceapp.R;
import com.yigit.conferenceapp.ui.login.LoginActivity;
import com.yigit.conferenceapp.ui.main.MainActivity;
import com.yigit.conferenceapp.ui.register.RegisterActivity;

public class SplashActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        //Firebase giriş bilgisi için değişkeni çağırıyoruz.
        mAuth = FirebaseAuth.getInstance();

        //Splash metodumuzu çağırıyoruz.
        initSplash();
    }

    private void initSplash() {
        //2000 ms yani 2 saniye bekledikten sonra loginToApp metodunu çağırıyoruz.
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                //Süre tamamlandığında metodu çağırıyoruz.
                loginToApp();
            }
        },2000);

    }

    private void loginToApp(){
        //Aktif kullanıcıyı alıyoruz, boş olabilir.
        FirebaseUser currentUser = mAuth.getCurrentUser();

        //Eğer aktif kullanıcı yok ise login sayfasına, var ise giriş sayfasına yönlendiriyoruz.
        if (currentUser==null){
            startActivity(new Intent(this, LoginActivity.class));
        }
        else{
            startActivity(new Intent(this, MainActivity.class));
        }
        finish();
    }

}