package com.gameme.base;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;

import com.gameme.R;
import com.gameme.login.model.User;
import com.gameme.utils.SharedPreferencesManager;
import com.ontbee.legacyforks.cn.pedant.SweetAlert.SweetAlertDialog;

public class BaseFragment extends Fragment {
    public Context mContext;
    public OnFragmentInteractionListener onFragmentInteractionListener;
    public User mCurrentUser;
    private SweetAlertDialog pDialog;


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
        pDialog.setTitleText(getString(R.string.connection_failed));
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


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            onFragmentInteractionListener = (OnFragmentInteractionListener) context;
        }
    }
}
