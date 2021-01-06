package com.yigit.conferenceapp.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.widget.Toast;

import com.yigit.conferenceapp.R;

import cn.pedant.SweetAlert.SweetAlertDialog;

public abstract class Utils {

    //Loading dialogumuz.
    private static SweetAlertDialog alertDialog;

    //Kısa süreli mesaj göstermek için kullanıyoruz.
    public static void showToast(Context context, String message)
    {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
    }

    //İnternet var mı diye kontrol etmek için kullanıyoruz.
    public static boolean isNetworkAvailable(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    //Loading dialogumuzu göstermek için kullanıyoruz.
    public static void initAlertDialogLoading(Context context) {
        alertDialog = new SweetAlertDialog(context);
        alertDialog.changeAlertType(SweetAlertDialog.PROGRESS_TYPE);
        alertDialog.setTitleText(context.getString(R.string.alert_loading));
        alertDialog.setCancelable(false);
        alertDialog.show();
    }

    //Loading dialogumuzu kapatmak için kullanıyoruz.
    public static void dismissAlertDialogLoading(){
        if (alertDialog!=null){
            if (alertDialog.isShowing())
                alertDialog.dismissWithAnimation();
        }
    }

    //Uygulamayı kapatma dialogumuzu göstermek için kullanıyoruz.
    public static void initAlertDialogExit(Context context) {
        SweetAlertDialog alertDialogExit = new SweetAlertDialog(context);
        alertDialogExit.changeAlertType(SweetAlertDialog.WARNING_TYPE);
        alertDialogExit.setTitleText(context.getString(R.string.alert_exit_title));
        alertDialogExit.setContentText(context.getString(R.string.alert_exit_subtitle));
        alertDialogExit.showCancelButton(true);
        alertDialogExit.setConfirmText(context.getString(R.string.alert_exit_confirm));
        alertDialogExit.setCancelText(context.getString(R.string.alert_exit_cancel));
        alertDialogExit.setConfirmClickListener(sweetAlertDialog -> {
            alertDialogExit.dismissWithAnimation();
            android.os.Process.killProcess(android.os.Process.myPid());
        });
        alertDialogExit.setCancelClickListener(sweetAlertDialog -> {
            alertDialogExit.dismissWithAnimation();
        });
        alertDialogExit.show();

    }
}
