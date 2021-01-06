package com.yigit.conferenceapp.ui.login;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.yigit.conferenceapp.R;
import com.yigit.conferenceapp.ui.main.MainActivity;
import com.yigit.conferenceapp.ui.register.RegisterActivity;
import com.yigit.conferenceapp.utils.Utils;

public class LoginActivity extends AppCompatActivity {

    EditText editEmail;
    EditText editPassword;
    AppCompatButton btnLogin;
    LinearLayout labelRegister;

    private Boolean buttonClicked = false;

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mAuth = FirebaseAuth.getInstance();

        initView();
        initClick();
    }

    //Ekranda kullanılacak nesneleri tanımlıyoruz.
    private void initView() {
        editEmail = findViewById(R.id.editEmail);
        editPassword = findViewById(R.id.editPassword);
        btnLogin = findViewById(R.id.btnLogin);
        labelRegister = findViewById(R.id.labelRegister);
    }

    //Ekrandaki nesneleri tıklanma olaylarını veriyoruz.
    private void initClick() {
        //Kayıt ol kısmına basında kayıt sayfasına yönlendiriyoruz.
        labelRegister.setOnClickListener(v -> {
            startActivity(new Intent(this, RegisterActivity.class));
        });

        //Giriş yap butonuna tıklanınca alanları kontrol ediyoruz.
        btnLogin.setOnClickListener(v -> {
            if (!buttonClicked) {
                buttonClicked = true;
                String sonuc = checkAllFieldsAreCorrect();

                if (sonuc.equals("correct")) {
                    //Eğer alanlar doğru girilmiş ise internet varlığını kontrol edip metodumuzu çağırıyoruz.
                    if (Utils.isNetworkAvailable(this)) {
                        loginWithFirebase(editEmail.getText().toString(), editPassword.getText().toString());
                    }
                    else{
                        Utils.showToast(this,"İnternet bağlantınızı kontrol ediniz");
                    }
                } else {
                    //Eğer hatalı girilmiş ise mesaj gösteriyoruz.
                    buttonClicked = false;
                    Utils.showToast(this, sonuc);
                }
            }
        });
    }

    //Email ve şifre alanları ile firebase'ye giriş yapıyoruz.
    private void loginWithFirebase(String email, String password) {
        Utils.initAlertDialogLoading(this);
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        // Giriş başarılı ise loginToApp metodumuzu çağırıyoruz ve mesaj gösteriyoruz.
                        Log.d("loginSuccess", "signInWithEmail:success");
                        Toast.makeText(LoginActivity.this, "Başarıyla giriş yapıldı.",
                                Toast.LENGTH_SHORT).show();
                        FirebaseUser user = mAuth.getCurrentUser();
                        loginToApp(user);
                        Utils.dismissAlertDialogLoading();
                    } else {
                        buttonClicked=false;
                        // Giriş başarısız ise başarısız mesajı döndürüyoruz.
                        Log.w("loginFail", "signInWithEmail:failure", task.getException());
                        Toast.makeText(LoginActivity.this, "Giriş başarısız. Lütfen alanları kontrol ediniz.",
                                Toast.LENGTH_SHORT).show();
                        Utils.dismissAlertDialogLoading();
                    }
                });
    }


    //Başarılı giriş yapılmış ise anasayfaya yönlendiriyoruz.
    private void loginToApp(FirebaseUser user) {
        if (user != null) {
            startActivity(new Intent(LoginActivity.this, MainActivity.class));
            finish();
        }
    }

    //Bu metodda alanların boş olup olmadığını kontrol ediyoruz ve ona göre string bir veri döndürüyoruz.
    private String checkAllFieldsAreCorrect() {
        if (editEmail.getText().toString().isEmpty())
            return "E posta alanı doldurulmalıdır.";
        else if (editPassword.getText().toString().isEmpty())
            return "Şifre alanı doldurulmalıdır.";
        else
            return "correct";
    }
}