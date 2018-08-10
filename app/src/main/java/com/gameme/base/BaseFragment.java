package com.gameme.base;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ProgressBar;

import com.gameme.HomeActivity;
import com.gameme.R;
import com.gameme.login.model.User;
import com.gameme.utils.SharedPreferencesManager;
import com.ontbee.legacyforks.cn.pedant.SweetAlert.SweetAlertDialog;

public class BaseFragment extends Fragment {
    public Context mContext;
    public OnFragmentInteractionListener onFragmentInteractionListener;
    public User mCurrentUser;
    private SweetAlertDialog pDialog;

    public static void SetProgressColor(ProgressBar progressBar, Context mContext) {
        progressBar.
                getIndeterminateDrawable().setColorFilter
                (ContextCompat.getColor(mContext, R.color.colorPrimary), PorterDuff.Mode.MULTIPLY);

    }

    public static void hideSoftKeyBoard(Context context, View view) {
        view.clearFocus();
        InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    public static void showSoftKeyBoard(Context context, View view) {
        InputMethodManager inputMethodManager = (InputMethodManager) context.getSystemService(Activity.INPUT_METHOD_SERVICE);
        view.requestFocus();
        inputMethodManager.showSoftInput(view, 0);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = getActivity();
        mCurrentUser = SharedPreferencesManager.getLoggedUserObject();
    }

    public void showLoadingDialog(String loadingText) {
        pDialog = new SweetAlertDialog(mContext, SweetAlertDialog.PROGRESS_TYPE);
        pDialog.setCancelable(true);
        pDialog.setTitleText(loadingText);
        pDialog.setCancelable(false);
        pDialog.show();
    }

    public void showNoInternetConnection() {
        pDialog = new SweetAlertDialog(mContext, SweetAlertDialog.ERROR_TYPE);
        pDialog.setTitleText("Connection Failed");
        pDialog.setContentText(mContext.getString(R.string.no_internet))
                .setConfirmText(mContext.getString(R.string.exit_app))
                .showCancelButton(false)
                .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                    @Override
                    public void onClick(SweetAlertDialog sweetAlertDialog) {
                        sweetAlertDialog.dismissWithAnimation();
                        ((Activity) mContext).finish();
                    }
                });
        pDialog.setCancelable(false);
        try {
            pDialog.show();

        } catch (Exception e) {
            Log.e("Exception", e.getMessage());
        }
    }

    public void hideLoadingDialog() {
        if (pDialog != null)
            if (pDialog.isShowing()) {
                pDialog.dismiss();
            }
    }

    public void dismissDialogWithSuccess(String text) {
        if (pDialog.isShowing())
            pDialog.setTitleText("")
                    .setContentText(text)
                    .setConfirmText("Ok")
                    .showCancelButton(false)
                    .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                        @Override
                        public void onClick(SweetAlertDialog sweetAlertDialog) {
                            sweetAlertDialog.dismissWithAnimation();
                            ((Activity) mContext).finish();
                        }
                    })
                    .changeAlertType(SweetAlertDialog.SUCCESS_TYPE);


    }

    public void dismissDialogWithSuccess() {
        if (pDialog.isShowing())
            pDialog.setTitleText("")
                    .setContentText(mContext.getString(R.string.completed))
                    .setConfirmText("Ok")
                    .showCancelButton(false)
                    .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                        @Override
                        public void onClick(SweetAlertDialog sweetAlertDialog) {
                            sweetAlertDialog.dismissWithAnimation();
                            Intent homeIntent = new Intent(mContext, HomeActivity.class);
                            homeIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                            mContext.startActivity(homeIntent);
                            ((Activity) mContext).finish();
                        }
                    })
                    .changeAlertType(SweetAlertDialog.SUCCESS_TYPE);


    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            onFragmentInteractionListener = (OnFragmentInteractionListener) context;
        }
    }
}
