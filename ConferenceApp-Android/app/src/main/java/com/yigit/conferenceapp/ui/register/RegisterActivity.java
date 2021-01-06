package com.yigit.conferenceapp.ui.register;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import android.Manifest;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.yigit.conferenceapp.R;
import com.yigit.conferenceapp.ui.custom.ImageSizeFilter;
import com.yigit.conferenceapp.ui.main.MainActivity;
import com.yigit.conferenceapp.utils.GlideUtils;
import com.yigit.conferenceapp.utils.Utils;
import com.zhihu.matisse.Matisse;
import com.zhihu.matisse.MimeType;
import com.zhihu.matisse.engine.impl.GlideEngine;
import com.zhihu.matisse.filter.Filter;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

public class RegisterActivity extends AppCompatActivity {

    //Kamera ve Okuma yazma izinleri için istek kodu.
    private final int PERMISSIONS_CAMERA = 122;
    private final int PERMISSIONS_READ_WRITE = 123;

    //Fotoğraf çekme ve seçme istek kodu.
    private final int REQUEST_IMAGE_CAPTURE = 124;
    private final int REQUEST_IMAGE_PICKER = 125;

    //Fotoğraf çekme veya seçme işlemini tutacak int değişkeni
    private int selectedType = -1;

    //Seçilen veya çekilen fotoğrafın dosya yolunu tutması için kullacağız.
    private String imgUri = null;
    private String currentPhotoPath = null;

    //Buton tıklandığında aktif etmek için kullanacağız.
    private Boolean buttonClicked = false;

    //Ekrandaki nesnelerin değişkenleri
    ImageView imgProfile;
    TextView labelAddPhoto;
    AppCompatButton btnRegister;
    EditText editName;
    EditText editEmail;
    EditText editPhone;
    EditText editPassword;
    EditText editPasswordAgain;
    LinearLayout labelLogin;

    //Resim değiştimi diye bakıyoruz.
    private Boolean isImageChanged = false;

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mAuth = FirebaseAuth.getInstance();

        //Uygulamada kullanılacak metodları çağırıyoruz.
        initView();
        initClick();
    }


    //Uygulamada kullanacağımız nesneleri ekliyoruz.
    private void initView() {
        imgProfile = findViewById(R.id.imgProfile);
        labelAddPhoto = findViewById(R.id.labelAddPhoto);
        btnRegister = findViewById(R.id.btnRegister);
        editName = findViewById(R.id.editName);
        editEmail = findViewById(R.id.editEmail);
        editPhone = findViewById(R.id.editPhone);
        editPassword = findViewById(R.id.editPassword);
        editPasswordAgain = findViewById(R.id.editPasswordAgain);
        labelLogin = findViewById(R.id.labelLogin);
    }

    //Uygulamadaki nesnelerin tıklanma olaylarını burada ayarlıyoruz.
    private void initClick() {
        imgProfile.setOnClickListener(v -> {
            openTakeOrChoosePhoto();
        });

        labelAddPhoto.setOnClickListener(v -> {
            openTakeOrChoosePhoto();
        });

        //Kayıt butonuna tıklandığında önce gerekli kontrolleri yapıyoruz.
        btnRegister.setOnClickListener(v -> {
            if (!buttonClicked) {
                buttonClicked = true;
                //Burada alanların boş olup olmadığına bakıyoruz boş ise uyarı metni gösteriyoruz.
                String sonuc = checkAllFieldsAreCorrect();

                if (sonuc.equals("correct")) {
                    String check = checkAllFieldsInputsAreCorrect();

                    //Burada alanlarda yanlışlık var mı diye bakıyoruz var ise yine uyarı metni gösteriyoruz.
                    if (check.equals("correct")) {
                        //Eğer her şey uygunsa internet varlığını kontrol edip kayıt metodumuzu çağırıyoruz.
                        if (Utils.isNetworkAvailable(this)) {
                            registerWithFirebase(editEmail.getText().toString(),editPassword.getText().toString());
                        }
                        else{
                            Utils.showToast(this,"İnternet bağlantınızı kontrol ediniz");
                        }


                    } else {
                        buttonClicked=false;
                        Utils.showToast(this, check);
                    }
                } else {
                    buttonClicked=false;
                    Utils.showToast(this, sonuc);
                }
            }

        });

        labelLogin.setOnClickListener(v -> {
            finish();
        });


    }

    private void registerWithFirebase(String email, String password){
        Utils.initAlertDialogLoading(this);
        //Burada email ve şifre ile firebase'ye kaydoluyoruz ve gelen sonucu kontrol ediyoruz.
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        // Eğer sonuç olumlu ise kullanıcı bilgilerini alıyoruz ve devam ediyoruz.
                        Log.d("registerSuccessful", "createUserWithEmail:success");
                        FirebaseUser user = mAuth.getCurrentUser();

                        //Başarı durumunda bu metodu çağırıyoruz, bu metod veritabanına kullanıcının adını, mailini ve telefon numarasını kaydedecek.
                        addUserInfos();
                    } else {
                        buttonClicked=false;
                        // Kayıt başarısız olduğunda burası çalışacak ve hata olduğunu gösterecek.
                        Log.w("registerFail", "createUserWithEmail:failure", task.getException());
                        Toast.makeText(RegisterActivity.this, "Kayıt oluşturulurken bir hata oluştu. Lütfen bilgileri kontrol ediniz.",
                                Toast.LENGTH_SHORT).show();
                        Utils.dismissAlertDialogLoading();
                    }

                });
    }

    private void addUserInfos() {
        String name = editName.getText().toString();
        String email = editEmail.getText().toString();
        String phone = editPhone.getText().toString();

        FirebaseUser user = mAuth.getCurrentUser();

        if (user != null) {
            //Eğer kayıtlı kullanıcı oluşmuş ise kullanıcı bilgilerini kullanıcının firebase uid'si altında yer alacak şekilde kaydettiriyoruz.
            DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users").child(user.getUid());

            HashMap<String, String> hashMap = new HashMap<>();
            hashMap.put("name", name);
            hashMap.put("email", email);
            hashMap.put("phone", phone);

            reference.setValue(hashMap).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    //Kaydetme başarılı olduğunda kullanıcının yüklediği veya hazırda gelen resmi yine depolama kısmına kaydediyoruz.
                    if (isImageChanged){
                        uploadProfilePhoto();
                    }
                    else{
                        Toast.makeText(RegisterActivity.this, "Kayıt başarıyla oluşturuldu.", Toast.LENGTH_SHORT).show();
                        Utils.dismissAlertDialogLoading();
                        startActivity(new Intent(this, MainActivity.class));
                        finish();
                    }

                }
                else{
                    Utils.dismissAlertDialogLoading();
                }
            });

        }
        buttonClicked=false;
    }

    private void uploadProfilePhoto(){
        // Depolama alanını çağırıyoruz.
        StorageReference storageRef = FirebaseStorage.getInstance().getReference();

        FirebaseUser user = mAuth.getCurrentUser();

        //Depolama alanı içerisinde images/ kullanıcının uid.jpg olacak şekilde bir alan oluşturuyoruz.
        StorageReference mountainImagesRef = storageRef.child("images/"+user.getUid()+".jpg");

        // ImageView içerisindeki resmi byte dosyasına çeviriyoruz.
        imgProfile.setDrawingCacheEnabled(true);
        imgProfile.buildDrawingCache();
        Bitmap bitmap = ((BitmapDrawable) imgProfile.getDrawable()).getBitmap();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] data = baos.toByteArray();

        //Çevirdiğimiz dosyayı firebase depolamaya gönderiyoruz.
        UploadTask uploadTask = mountainImagesRef.putBytes(data);

        uploadTask.addOnFailureListener(exception -> {
            //Gönderme başarısız olduğunda burası çalışacak ancak yinede kullanıcı başarıyla kayıt olduğu için ilerleyebileceğiz ve anasayfaya yönlenecek.
            Toast.makeText(RegisterActivity.this, "Kayıt başarıyla oluşturuldu.", Toast.LENGTH_SHORT).show();
            Utils.dismissAlertDialogLoading();
            startActivity(new Intent(this, MainActivity.class));
            finish();

        }).addOnSuccessListener(taskSnapshot -> {
            //Gönderme başarılı olduğunda burası çalışacak ve kullanıcı anasayfaya yönlendirilecek.
            Toast.makeText(RegisterActivity.this, "Kayıt başarıyla oluşturuldu.", Toast.LENGTH_SHORT).show();
            Utils.dismissAlertDialogLoading();
            startActivity(new Intent(this, MainActivity.class));
            finish();
        });
    }

    //Bu metodda alanların boş olup olmadığını kontrol ediyoruz ve ona göre string bir veri döndürüyoruz.
    private String checkAllFieldsAreCorrect() {
        if (editName.getText().toString().isEmpty())
            return "Ad soyad alanı doldurulmalıdır.";
        else if (editEmail.getText().toString().isEmpty())
            return "E Posta alanı doldurulmalıdır.";
        else if (editPhone.getText().toString().isEmpty())
            return "Telefon numarası alanı doldurulmalıdır.";
        else if (editPassword.getText().toString().isEmpty())
            return "Şifre alanı doldurulmalıdır.";
        else if (editPasswordAgain.getText().toString().isEmpty())
            return "Şifre tekrar alanı doldurulmalıdır.";
        else
            return "correct";
    }

    //Bu metodda ad soyad kısmının başında ve sonunda boşluk varsa kaldırıyoruz ardından şifreler uyuşuyor mu diye kontrol edip yine string bir veri döndürüyoruz.
    private String checkAllFieldsInputsAreCorrect() {
        if(editName.getText().charAt(editName.getText().length()-1)==' ')
        {
            String newText = editName.getText().toString().substring(0,editName.getText().length());
            editName.setText(newText);
        }

        if(editName.getText().charAt(0)==' ')
        {
            String newText = editName.getText().toString().substring(1,editName.getText().length());
            editName.setText(newText);
        }

        if (editPassword.getText().length()<6)
            return "Şifre minimum 6 karakterden oluşmalıdır.";
        else if (!editPassword.getText().toString().equals(editPasswordAgain.getText().toString()))
            return "Şifreler eşleşmiyor. Lütfen doğru giriniz.";
        else
            return "correct";

    }

    //Fotoğraf yüklemek için fotoğraf çekme veya seçme işlemini ayarlamak için bu metodu kullanıyoruz.
    private void openTakeOrChoosePhoto() {
        //Bottomsheetdialog kurulumu yapıyoruz.
        BottomSheetDialog dialog = new BottomSheetDialog(this,R.style.BottomSheetDialogTheme);
        View view = getLayoutInflater().inflate(R.layout.bottom_sheet_image_from,null);

        dialog.setContentView(view);
        dialog.show();

        //Fotoğraf çek ve Galeriden seç butonlarını ayarlıyoruz ve tıklanma olaylarını veriyoruz.
        CardView btnTakePhoto = view.findViewById(R.id.btnTakePhoto);
        CardView btnPickPhoto = view.findViewById(R.id.btnPickPhoto);
        if (view!=null) {
            btnTakePhoto.setOnClickListener(v -> {
                //Eğer fotoğraf çek seçilmişse kamera ve okuma yazma izinlerini kontrol ediyoruz. Eğer verilmiş ise diğer aşamaya; verilmemiş ise
                //Kamera ve okuma yazma iznini kullanıcıya soruyoruz.
                selectedType=0;
                if (hasCameraPermission() && hasReadStoragePermission()){
                    dispatchTakePictureIntent();
                }
                else{
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        requestPermissions(
                                new String[]{Manifest.permission.CAMERA},
                                PERMISSIONS_CAMERA
                        );
                    }
                }
                dialog.dismiss();
            });

            btnPickPhoto.setOnClickListener(v -> {
                //Eğer galeriden seç seçilmişse  okuma yazma iznini kontrol ediyoruz. Eğer verilmiş ise diğer aşamaya; verilmemiş ise
                //Okuma yazma iznini kullanıcıya soruyoruz.
                selectedType=1;
                if(hasReadStoragePermission()) {
                    getImageFromMatisse();
                }
                else{
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                                PERMISSIONS_READ_WRITE);
                    }

                }
                dialog.dismiss();
            });

        }
    }

    //Bu metodda Kameradan fotoğraf çekmek için intent kurulumu yapıyoruz.
    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            // File oluşturuyoruz, bu geçici olarak bir resim dosyası oluşturacak.
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                // Hata oluşur ise burası çalışacak
            }
            // Dosya başarıyla oluşturulmuş ise belirlediğimiz yola geçiçi dosya oluşturacak ve kamerayı başlatacak.
            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(this,
                        "com.yigit.conferenceapp.fileprovider",
                        photoFile);
                imgUri=currentPhotoPath;
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
            }
        }
    }

    //Resim dosyası oluşturmak için kullandığımız metod.
    private File createImageFile() throws IOException {
        // Resim geçiçi dosyası oluşturuyoruz ve ismini tarih saat ile karışık şekilde random isimlendiriyoruz.
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Dosyanın yolunu alıyoruz.
        currentPhotoPath = image.getAbsolutePath();
        return image;
    }

    //Galeriden seçmek için Matisse kütüphanesini burada kullanıyoruz.
    private void getImageFromMatisse()
    {
        //Ayarları yapıyoruz burdaki ayarlarda tip=Resim, adet=1, max_boyut=3mb gibi ayarları veriyoruz.
        Matisse.from(this)
                .choose(MimeType.ofImage())
                .countable(false)
                .maxSelectable(1)
                .gridExpectedSize(getResources().getDimensionPixelSize(R.dimen.grid_expected_size))
                .restrictOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED)
                .addFilter(new ImageSizeFilter(3 * Filter.K * Filter.K))
                .thumbnailScale(0.85f)
                .imageEngine(new GlideEngine())
                .showSingleMediaType(true)
                .showPreview(false) // Default is `true`
                .forResult(REQUEST_IMAGE_PICKER);
    }


    //Okuma yazma iznini kontrol ettiğimiz metod
    private Boolean hasReadStoragePermission() {
        return ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_EXTERNAL_STORAGE
        ) == PackageManager.PERMISSION_GRANTED;
    }

    //Kamera iznini kontrol ettiğimiz metod
    private Boolean hasCameraPermission() {
        return ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED;
    }

    //İzinlerden aldğımız sonuçları burada görüyoruz ve işlemlerimizi ona göre ayarlıyoruz.
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        //Eğer kamera izni verilmiş ise diğer aşamaya geçiyoruz.
        if(requestCode == PERMISSIONS_CAMERA)
        {
            if(!hasReadStoragePermission()) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    requestPermissions(
                            new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                            PERMISSIONS_READ_WRITE
                    );
                }
            }
            else{
                dispatchTakePictureIntent();
            }
        }

        //Eğer okuma yazma izni verilmiş ise diğer aşamaya geçiyoruz.
        if(requestCode == PERMISSIONS_READ_WRITE)
        {
            if (selectedType==0)
                dispatchTakePictureIntent();
            else if (selectedType==1)
                getImageFromMatisse();
        }
    }

    //Çekme veya seçme işlemi tamamlandığında burası çalışıyor ve resmi yakalıyor.
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        //Kameradan fotoğraf çekildikten sonra burası çalışıyor ve çekilen resmimizi Glide kütüphanesi yardımıyla ImageView'e bastırıyor.
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == AppCompatActivity.RESULT_OK) {
            GlideUtils.localToImageViewCircle(this, imgUri,imgProfile);
            isImageChanged=true;
        }

        //Galeriden fotoğraf seçildikten sonra burası çalışıyor ve seçilen resmimizi Glide kütüphanesi yardımıyla ImageView'e bastırıyor.
        if (requestCode == REQUEST_IMAGE_PICKER && resultCode == AppCompatActivity.RESULT_OK) {
            List mSelected = Matisse.obtainResult(data);
            if (mSelected != null) {
                imgUri = Matisse.obtainPathResult(data).get(0);
                if (imgUri!=null)
                    GlideUtils.localToImageViewCircle(this, imgUri,imgProfile);
                    isImageChanged=true;
            }
        }
    }

}