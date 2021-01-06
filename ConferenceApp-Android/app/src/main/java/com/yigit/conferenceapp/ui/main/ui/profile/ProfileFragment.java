package com.yigit.conferenceapp.ui.main.ui.profile;

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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.yigit.conferenceapp.R;
import com.yigit.conferenceapp.data.model.user.FirebaseUserModel;
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
import java.util.Map;

public class ProfileFragment extends Fragment {


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

    //Resim değiştimi diye bakıyoruz.
    private Boolean isImageChanged = false;

    //Uygulamada kullacağımız nesneler
    AppCompatButton btnAddPhoto;
    TextView textEmail;
    EditText editName;
    EditText editPhone;
    AppCompatButton btnSave;
    AppCompatButton btnLogout;
    ImageView imgProfile;

    //Kayıtlı kullanıcı için.
    private FirebaseAuth mAuth;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_profile, container, false);

    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        //Kayıtlı bilgileri alıyoruz.
        mAuth = FirebaseAuth.getInstance();

        //Loading başlatıyoruz, ardından elemanları tanımlıyoruz, tıklanma olaylarını veriyoruz, kullanıcı resmini alıyoruz, ve kullanıcı bilgilerini alıyoruz.
        Utils.initAlertDialogLoading(requireContext());
        initView(view);
        initClick();
        getUserImage();
        getUserInfo();

    }

    private void initView(View view){
        //Nesneler
        btnAddPhoto = view.findViewById(R.id.btnAddPhoto);
        textEmail = view.findViewById(R.id.textEmail);
        editName = view.findViewById(R.id.editName);
        editPhone = view.findViewById(R.id.editPhone);
        btnSave = view.findViewById(R.id.btnSave);
        btnLogout = view.findViewById(R.id.btnLogout);
        imgProfile = view.findViewById(R.id.imgProfile);
    }

    private void initClick() {
        //Çıkış butonuna tıklandığında Mainactivity içerisindeki logout metodunu çağırıyoruz. Bu metod bizi çıkış yaptırarak login sayfasına yönlendirecek.
        btnLogout.setOnClickListener(v -> {
            ((MainActivity)getActivity()).logOut();
        });

        //Resme veya resim ekleye tıklandığında resim seçme veya çekme seçeneği açılacak.
        imgProfile.setOnClickListener(v -> {
            openTakeOrChoosePhoto();
        });

        btnAddPhoto.setOnClickListener(v -> {
            openTakeOrChoosePhoto();
        });

        //Kayıt butonuna basıldığında internet varlığını kontrol edilip daha sonra veriler kontrol edilerek kayıt işlemleri başlayacak.
        btnSave.setOnClickListener(v -> {
            String sonuc = checkAllFieldsAreCorrect();

            if (sonuc.equals("correct")) {
                if (Utils.isNetworkAvailable(requireContext())) {
                    setUserInfos();
                }
                else{
                    Utils.showToast(requireContext(),"İnternet bağlantınızı kontrol ediniz");
                }
            }
            else{
                Utils.showToast(requireContext(),sonuc);
            }

        });
    }

    private void getUserInfo(){
        //Burada önce Users tablomuza erişiyoruz ve onun kendi kullanıcımız için olan elemanını alıyoruz.
        //Daha sonra gelen verileri text alanlarına bastırıyoruz ve loadingi kapatıyoruz.
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("Users")
                .child(mAuth.getCurrentUser().getUid());

        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    FirebaseUserModel user = snapshot.getValue(FirebaseUserModel.class);
                    if (user!=null){
                        editName.setText(user.getName());
                        textEmail.setText(user.getEmail());
                        editPhone.setText(user.getPhone());
                    }
                }

                Utils.dismissAlertDialogLoading();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Utils.dismissAlertDialogLoading();
            }
        });

    }

    private void getUserImage() {
        //Burada depolamaya erişiyoruz ardından images/kullanıcı uid'si.jpg yoluna giderek buradan kullanıcımızın profil fotoğrafını
        //indirip bunu Glide kütüphanesi yardımıyla imageview'e bastırıyoruz.
        StorageReference storageReference = FirebaseStorage.getInstance().getReference();

        storageReference.child("images/"+mAuth.getCurrentUser().getUid()+".jpg").getDownloadUrl().addOnSuccessListener(uri -> {
            GlideUtils.urlFirebaseToImageViewCircle(requireContext(),uri,imgProfile);

        }).addOnFailureListener(exception -> {

        });
    }

    //Fotoğraf yüklemek için fotoğraf çekme veya seçme işlemini ayarlamak için bu metodu kullanıyoruz.
    private void openTakeOrChoosePhoto() {
        //Bottomsheetdialog kurulumu yapıyoruz.
        BottomSheetDialog dialog = new BottomSheetDialog(requireContext(),R.style.BottomSheetDialogTheme);
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

    private void setUserInfos() {
        //Burada değiştirdiğimiz verileri alarak Users tablosuna gidiyoruz ve kendi kullanıcımıza erişiyoruz.
        //Ardından name ve phone alanlarını girdiğimiz verilerle güncelliyoruz.
        //Ardından profil resmi değişmişmi diye bakıyoruz değişmemisse işlemi tamamlıyoruz, değişmişsse fotoğraf yükleme metodunu çağırıyoruz.
        Utils.initAlertDialogLoading(requireContext());
        String name = editName.getText().toString();
        String phone = editPhone.getText().toString();

        FirebaseUser user = mAuth.getCurrentUser();

        if (user != null) {
            //Eğer kayıtlı kullanıcı oluşmuş ise kullanıcı bilgilerini kullanıcının firebase uid'si altında yer alacak şekilde kaydettiriyoruz.
            DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users").child(user.getUid());

            HashMap<String, Object> hashMap = new HashMap<>();
            hashMap.put("name", name.toString());
            hashMap.put("phone", phone.toString());

            reference.updateChildren((Map<String,Object>)hashMap).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    //Kaydetme başarılı olduğunda kullanıcının yüklediği veya hazırda gelen resmi yine depolama kısmına kaydediyoruz.
                    if (isImageChanged)
                        uploadProfilePhoto();
                    else
                        Utils.dismissAlertDialogLoading();
                }
            });


        }
        else{
            Utils.dismissAlertDialogLoading();
        }
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
            //Gönderme başarısız olduğunda burası çalışacak .
            Log.d("fail",exception.getLocalizedMessage());
            Utils.showToast(requireContext(),"Fotoğraf yüklenirken bir sorun oluştu.");
            Utils.dismissAlertDialogLoading();
        }).addOnSuccessListener(taskSnapshot -> {
            //Gönderme başarılı olduğunda burası çalışacak.
            Utils.showToast(requireContext(),"Fotoğraf başarıyla yüklendi.");
            Utils.dismissAlertDialogLoading();
        });
    }

    //Bu metodda alanların boş olup olmadığını kontrol ediyoruz ve ona göre string bir veri döndürüyoruz.
    private String checkAllFieldsAreCorrect() {
        if (editName.getText().toString().isEmpty())
            return "Ad soyad alanı doldurulmalıdır.";
        else if (editPhone.getText().toString().isEmpty())
            return "Telefon numarası alanı doldurulmalıdır.";
        else
            return "correct";
    }

    //Bu metodda Kameradan fotoğraf çekmek için intent kurulumu yapıyoruz.
    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getActivity().getPackageManager()) != null) {
            // File oluşturuyoruz, bu geçici olarak bir resim dosyası oluşturacak.
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                // Hata oluşur ise burası çalışacak
            }
            // Dosya başarıyla oluşturulmuş ise belirlediğimiz yola geçiçi dosya oluşturacak ve kamerayı başlatacak.
            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(requireContext(),
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
        File storageDir = getActivity().getExternalFilesDir(Environment.DIRECTORY_PICTURES);
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
                requireContext(),
                Manifest.permission.READ_EXTERNAL_STORAGE
        ) == PackageManager.PERMISSION_GRANTED;
    }

    //Kamera iznini kontrol ettiğimiz metod
    private Boolean hasCameraPermission() {
        return ContextCompat.checkSelfPermission(
                requireContext(),
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
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        //Kameradan fotoğraf çekildikten sonra burası çalışıyor ve çekilen resmimizi Glide kütüphanesi yardımıyla ImageView'e bastırıyor.
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == AppCompatActivity.RESULT_OK) {
            GlideUtils.localToImageViewCircle(requireContext(), imgUri,imgProfile);
            isImageChanged=true;
        }

        //Galeriden fotoğraf seçildikten sonra burası çalışıyor ve seçilen resmimizi Glide kütüphanesi yardımıyla ImageView'e bastırıyor.
        if (requestCode == REQUEST_IMAGE_PICKER && resultCode == AppCompatActivity.RESULT_OK) {
            List mSelected = Matisse.obtainResult(data);
            if (mSelected != null) {
                imgUri = Matisse.obtainPathResult(data).get(0);
                if (imgUri!=null) {
                    GlideUtils.localToImageViewCircle(requireContext(), imgUri, imgProfile);
                    isImageChanged=true;
                }
            }
        }
    }
}