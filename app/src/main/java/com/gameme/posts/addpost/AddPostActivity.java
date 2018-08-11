package com.gameme.posts.addpost;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.gameme.R;
import com.gameme.database.AppDatabase;
import com.gameme.database.AppExecutor;
import com.gameme.firebase.FirebaseConstants;
import com.gameme.firebase.FirebaseUtils;
import com.gameme.firebase.controllers.UserController;
import com.gameme.login.model.User;
import com.gameme.posts.model.Post;
import com.gameme.utils.SharedPreferencesManager;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.lyft.android.scissors.CropView;
import com.ontbee.legacyforks.cn.pedant.SweetAlert.SweetAlertDialog;

import net.yslibrary.android.keyboardvisibilityevent.KeyboardVisibilityEvent;
import net.yslibrary.android.keyboardvisibilityevent.KeyboardVisibilityEventListener;

import java.io.ByteArrayOutputStream;
import java.util.Map;
import java.util.UUID;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static com.gameme.widget.PostsService.startActionPosts;

public class AddPostActivity extends AppCompatActivity {

    public static final String POST_TYPE = "post_type";
    private static final int GALLERY_INTENT = 2;
    @BindView(R.id.post_image_view)
    CropView postImageView;
    @BindView(R.id.add_post_button)
    Button addPostButton;
    @BindView(R.id.post_edit_text)
    EditText postContentEditText;
    @BindView(R.id.post_input_layout)
    TextInputLayout textInputLayout;
    @BindView(R.id.add_question_appbar)
    Toolbar toolbar;
    Uri imageURI;
    User currentUser;
    private DatabaseReference mDatabaseReference;
    private DatabaseReference mPostReference;
    private SweetAlertDialog pDialog;
    private ChildEventListener mPostListener;
    private int postType;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_question_layout);

        ButterKnife.bind(this);

        currentUser = SharedPreferencesManager.getLoggedUserObject();

        postType = getIntent().getIntExtra(POST_TYPE, 0);

        setupFirebase();

        initViews();

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


    }

    private void setupFirebase() {
        mDatabaseReference = FirebaseUtils.getReference(FirebaseConstants.Childs.POSTS);
    }

    void initViews() {
        // set our custom ToolBar " action bar "
        if (postType == Post.QUESTION) {
            toolbar.setTitle(R.string.new_question);
            textInputLayout.setHint(getString(R.string.write_question));
        } else {
            toolbar.setTitle(R.string.new_post);
            textInputLayout.setHint(getString(R.string.write_something));

        }
        setSupportActionBar(toolbar);


        postContentEditText.setError(null);

        KeyboardVisibilityEvent.setEventListener(
                this,
                new KeyboardVisibilityEventListener() {
                    @Override
                    public void onVisibilityChanged(boolean isOpen) {
                        if (isOpen)
                            addPostButton.setVisibility(View.GONE);
                        else
                            addPostButton.setVisibility(View.VISIBLE);
                    }
                });

    }

    @OnClick(R.id.add_photo_ImgView)
    public void addImage() {
        // choose image from studio and display it
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        startActivityForResult(intent, GALLERY_INTENT);

    }

    @OnClick(R.id.add_post_button)
    public void addQuestion() {
        if (postType == Post.QUESTION) {
            if (TextUtils.isEmpty(postContentEditText.getText()))
                postContentEditText.setError(getString(R.string.error_field_required));
            else
                uploadPostToServer();
        } else {
            if (TextUtils.isEmpty(postContentEditText.getText()) && imageURI == null)
                Toast.makeText(this, R.string.must_upload_or_write, Toast.LENGTH_SHORT).show();
            else
                uploadPostToServer();
        }

    }

    private void uploadPostToServer() {

        mPostReference = mDatabaseReference.push();

        final ChildEventListener postListeners = new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, String s) {
                if (pDialog.isShowing())
                    dismissDialog();
                UserController.setPostsCount(currentUser.getUserId(), 1);
                mPostReference.removeEventListener(mPostListener);
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        };

        mPostReference.addChildEventListener(postListeners);
        mPostListener = postListeners;


        if (imageURI == null) {
            pDialog = new SweetAlertDialog(this, SweetAlertDialog.PROGRESS_TYPE);
            pDialog.setCancelable(false);
            pDialog.setTitleText(getString(R.string.loading));
            pDialog.show();
            final Post post = new Post();
            post.setContent(postContentEditText.getText().toString());
            post.setUsername(currentUser.getUsername());
            post.setFullName(currentUser.getFullName());
            post.setUserPicture(currentUser.getProfilePicture());
            post.setUserId(currentUser.getUserId());
            post.setPostId(mPostReference.getKey());
            post.setPostType(postType);
            post.setLikesCount(0);
            post.setImage("");
            //post.setTime(ServerValue.TIMESTAMP);
            post.setAnswersCount(0);
            post.setVotesCount(0);
            Map<String, Object> forumValues = post.toMap();
            // send to server
            mPostReference.updateChildren(forumValues).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()) {
                        insertPostIntoDb(post);
                    }
                }
            });

        } else {
            uploadDataWithImage();
        }
    }

    void uploadDataWithImage() {

        pDialog = new SweetAlertDialog(this, SweetAlertDialog.PROGRESS_TYPE);
        pDialog.setCancelable(false);
        pDialog.setTitleText(getString(R.string.loading));
        pDialog.show();

        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference mStorageReference = storage.getReferenceFromUrl(FirebaseConstants.References.FIREBASE_STORAGE);

        final String pictureName = currentUser.getUsername() + "_post_" + UUID.randomUUID().toString();
        // create path for photos
        final StorageReference picturePath = mStorageReference.child("posts_images").child(pictureName);

        // upload photo
        picturePath.putBytes(getImageAsBytes()).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {


            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.e("Failure", e.getMessage());
            }
        });

        picturePath.putBytes(getImageAsBytes()).continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
            @Override
            public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) {
                if (!task.isSuccessful()) {
                    Toast.makeText(AddPostActivity.this, R.string.can_not_upload, Toast.LENGTH_SHORT).show();
                }
                return picturePath.getDownloadUrl();
            }
        }).addOnCompleteListener(new OnCompleteListener<Uri>() {
            @Override
            public void onComplete(@NonNull Task<Uri> task) {
                if (task.isSuccessful()) {
                    Uri downloadUri = task.getResult();
                    final Post post = new Post();
                    post.setContent(postContentEditText.getText().toString());
                    post.setUsername(currentUser.getUsername());
                    post.setFullName(currentUser.getFullName());
                    post.setUserPicture(currentUser.getProfilePicture());
                    post.setUserId(currentUser.getUserId());
                    post.setPostId(mPostReference.getKey());
                    post.setImage(downloadUri.toString());
                    post.setImageName(pictureName);
                    post.setPostType(postType);
                    //post.setTime(ServerValue.TIMESTAMP);
                    post.setAnswersCount(0);
                    post.setVotesCount(0);
                    Map<String, Object> forumValues = post.toMap();
                    // send to server
                    mPostReference.updateChildren(forumValues).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                insertPostIntoDb(post);
                            }
                        }
                    });
                }
            }
        });

    }

    private void insertPostIntoDb(final Post post) {
        final AppDatabase appDatabase = AppDatabase.getsInstance();
        AppExecutor.getsInstance().getDiskIO().execute(new Runnable() {
            @Override
            public void run() {
                appDatabase.getPostsDao().insertPost(post);
            }
        });

        startActionPosts(this);

    }

    byte[] getImageAsBytes() {
        Bitmap bitmap = postImageView.crop();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 50, baos);
        return baos.toByteArray();
    }

    void dismissDialog() {
        if (pDialog.isShowing())
            pDialog.setTitleText(getString(R.string.question_uploaded))
                    .setContentText("")
                    .setConfirmText("Ok")
                    .showCancelButton(false)
                    .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                        @Override
                        public void onClick(SweetAlertDialog sweetAlertDialog) {
                            sweetAlertDialog.dismissWithAnimation();
                            AddPostActivity.this.finish();
                        }
                    })
                    .changeAlertType(SweetAlertDialog.SUCCESS_TYPE);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
        }
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == GALLERY_INTENT && resultCode == RESULT_OK) {
            imageURI = data.getData();
            // display selected image
            Glide.with(this).load(imageURI).into(postImageView);
        }
    }


    @Override
    public void onStop() {
        super.onStop();
        // Remove post value event listener
        if (mPostListener != null) {
            mPostReference.removeEventListener(mPostListener);
        }
    }

    @Override
    public void finish() {
        super.finish();
        setResult(RESULT_OK, getIntent());
    }
}
