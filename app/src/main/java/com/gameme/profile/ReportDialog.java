package com.gameme.profile;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.Window;
import android.widget.Toast;

import com.gameme.R;
import com.gameme.firebase.FirebaseConstants;
import com.gameme.firebase.FirebaseUtils;
import com.gameme.login.model.User;
import com.gameme.utils.SharedPreferencesManager;
import com.google.firebase.database.DatabaseReference;

import butterknife.ButterKnife;
import butterknife.OnClick;

import static com.gameme.firebase.FirebaseConstants.Childs.ANNOYING;
import static com.gameme.firebase.FirebaseConstants.Childs.FAKE;
import static com.gameme.firebase.FirebaseConstants.Childs.INAPPROPRIATE;


public class ReportDialog extends Dialog {
    private Context context;
    private DatabaseReference mDatabase;
    private User currentUser;
    private String username;

    public ReportDialog(Context context, String username) {
        super(context);
        this.context = context;
        mDatabase = FirebaseUtils.getReference(FirebaseConstants.Childs.REPORTS);
        currentUser = SharedPreferencesManager.getLoggedUserObject();
        this.username = username;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_report);
        ButterKnife.bind(this);
    }

    @OnClick(R.id.annoyingBtn)
    public void onAnnyoingClicked() {
        DatabaseReference annoyingRef = mDatabase.child(username).child(ANNOYING);
        sendReportToFirebase(annoyingRef);

    }

    @OnClick(R.id.inappropiateBtn)
    public void onInappropiateBtnClicked() {
        DatabaseReference inappropiateRef = mDatabase.child(username).child(INAPPROPRIATE);
        sendReportToFirebase(inappropiateRef);

    }

    @OnClick(R.id.inappropiateBtn)
    public void onFakeBtnClicked() {
        DatabaseReference fakeRef = mDatabase.child(username).child(FAKE);
        sendReportToFirebase(fakeRef);
    }

    private void sendReportToFirebase(DatabaseReference databaseReference) {
        databaseReference.push();
        databaseReference.child(currentUser.getUsername()).setValue(true);
        ReportDialog.this.dismiss();
        Toast.makeText(context, R.string.thank_you, Toast.LENGTH_SHORT).show();
    }
}
