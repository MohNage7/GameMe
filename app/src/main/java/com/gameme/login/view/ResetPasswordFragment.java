package com.gameme.login.view;


import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.gameme.R;
import com.gameme.base.BaseFragment;
import com.gameme.firebase.FirebaseConstants;
import com.gameme.firebase.FirebaseUtils;
import com.gameme.utils.StringUtils;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;


public class ResetPasswordFragment extends BaseFragment {

    DatabaseReference mUsersReference;
    Query userNameReference;
    @BindView(R.id.email_EditTxt)
    TextView emailTxtView;
    FirebaseAuth mAuth;
    private Unbinder unbinder;

    public ResetPasswordFragment() {
        // Required empty public constructor
    }


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mUsersReference = FirebaseUtils.getReference(FirebaseConstants.Childs.USERS_REF);
        mAuth = FirebaseAuth.getInstance();

    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View resetPasswordView = inflater.inflate(R.layout.fragment_reset_password, container, false);
        unbinder = ButterKnife.bind(this, resetPasswordView);

        return resetPasswordView;
    }

    @OnClick(R.id.resetPassword_Btn)
    public void sendResetEmail() {
        showLoadingDialog(getString(R.string.wait));
        mAuth.signInAnonymously()
                .addOnCompleteListener(getActivity(), new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (!task.isSuccessful()) {
                            Log.w("onComplete", "signInAnonymously", task.getException());
                        } else {
                            checkEmail();
                        }

                    }
                });
    }

    void checkEmail() {
        emailTxtView.setError(null);
        final String email = emailTxtView.getText().toString();
        userNameReference = mUsersReference.orderByChild("email").equalTo(String.valueOf(email));
        if (!TextUtils.isEmpty(email)) {
            if (StringUtils.validateEmail(email)) {
                final ValueEventListener usernameValueEventListener = new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot.getValue() != null) {
                            mAuth.sendPasswordResetEmail(email)
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {
                                                hideLoadingDialog();
                                                Toast.makeText(getActivity(), R.string.email_sent_to_address, Toast.LENGTH_SHORT).show();
                                                onFragmentInteractionListener.setCurrentFragment(new SignInFragment(), null);
                                                mAuth.signOut();
                                            } else {
                                                hideLoadingDialog();
                                                Toast.makeText(getActivity(), R.string.cant_send_email, Toast.LENGTH_SHORT).show();
                                                mAuth.signOut();
                                            }
                                        }
                                    });
                            userNameReference.removeEventListener(this);

                        } else {
                            hideLoadingDialog();
                            emailTxtView.setError(getString(R.string.email_not_exist));
                            mAuth.signOut();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        hideLoadingDialog();
                        Toast.makeText(getActivity(), R.string.cant_send_email, Toast.LENGTH_SHORT).show();
                        emailTxtView.setError(getString(R.string.email_not_exist));
                        mAuth.signOut();
                    }
                };
                userNameReference.addListenerForSingleValueEvent(usernameValueEventListener);

            } else {
                hideLoadingDialog();
                emailTxtView.setError(getString(R.string.error_invalid_email));
                mAuth.signOut();
            }
        } else {
            hideLoadingDialog();
            emailTxtView.setError(getString(R.string.error_field_required));
            mAuth.signOut();
        }
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mAuth.signOut();
    }

    @Override
    public void onStop() {
        super.onStop();
        mAuth.signOut();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        unbinder.unbind();
    }
}
