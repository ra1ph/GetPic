package com.ra1ph.getpic.utils;

import android.app.AlertDialog;
import android.content.Context;
import com.ra1ph.getpic.R;

/**
 * Created with IntelliJ IDEA.
 * User: ra1ph
 * Date: 26.06.13
 * Time: 12:12
 * To change this template use File | Settings | File Templates.
 */
public class DialogManager {
    public static void errorDialog(Context context, String error){
        AlertDialog dialog2 = new AlertDialog.Builder(context).create();
        dialog2.setTitle(context.getResources().getString(R.string.error));
        dialog2.setMessage(error);
        dialog2.show();
    }

    public static void wrongAuthDialog(Context context){
        AlertDialog dialog2 = new AlertDialog.Builder(context).create();
        dialog2.setTitle(context.getResources().getString(R.string.wrong_auth));
        dialog2.setMessage(context.getResources().getString(R.string.wrong_login_password));
        dialog2.show();
    }
}
