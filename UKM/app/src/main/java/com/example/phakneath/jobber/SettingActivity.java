package com.example.phakneath.jobber;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.phakneath.jobber.Model.ESCCI;
import com.example.phakneath.jobber.Model.MyESCCI;
import com.example.phakneath.jobber.Model.Users;
import com.example.phakneath.jobber.Model.saveESCCI;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;
import com.theartofdev.edmodo.cropper.CropImage;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class SettingActivity extends AppCompatActivity implements View.OnClickListener{

    CircleImageView profile;
    RelativeLayout editProfile;
    ImageView back, check;
    EditText username, email, nationality, workplace, position;
    ProgressBar progressBar;
    ScrollView scrollView;

    FirebaseAuth mAuth;
    DatabaseReference mDatabase;
    FirebaseStorage storage;
    StorageReference storageReference, ref;
    static StorageTask mUploadTask;

    String uID, name, emails, nation, place, pos;
    String pathImage;
    Uri uri, mCropImageUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);
        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        initView();
        mAuth = FirebaseAuth.getInstance();
        uID = mAuth.getCurrentUser().getUid();

        back.setOnClickListener(this::onClick);
        check.setOnClickListener(this::onClick);
        editProfile.setOnClickListener(this::onClick);
        getUser();
    }

    private void initView() {
        profile = findViewById(R.id.profile);
        editProfile = findViewById(R.id.edit);
        back = findViewById(R.id.back);
        check = findViewById(R.id.check);
        username = findViewById(R.id.users);
        email = findViewById(R.id.email);
        workplace = findViewById(R.id.workplace);
        position = findViewById(R.id.p);
        nationality = findViewById(R.id.nation);
        progressBar = findViewById(R.id.progressBar);
        scrollView = findViewById(R.id.scroll);
    }
    public void getUser()
    {
        scrollView.setVisibility(View.GONE);
        progressBar.setVisibility(View.VISIBLE);
        check.setEnabled(false);
        mDatabase = FirebaseDatabase.getInstance().getReference();
        mDatabase.child("Users").child(uID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String id = dataSnapshot.child("id").getValue(String.class);
                String email = dataSnapshot.child("email").getValue(String.class);
                String username = dataSnapshot.child("username").getValue(String.class);
                String nationality = dataSnapshot.child("nationality").getValue(String.class);
                String workPlace = dataSnapshot.child("workplace").getValue(String.class);
                String position = dataSnapshot.child("position").getValue(String.class);
                String image = dataSnapshot.child("image").getValue(String.class);

                Users users = new Users(id,email,username,nationality,workPlace,position,image);
                updateUI(users);
                if(users.getImage() != null) getImage(profile, users.getImage(), SettingActivity.this, "profile/");
                else
                {
                    scrollView.setVisibility(View.VISIBLE);
                    progressBar.setVisibility(View.GONE);
                    check.setEnabled(true);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }


    public void closeControl(Boolean b)
    {
        username.setEnabled(b); nationality.setEnabled(b); workplace.setEnabled(b);
        position.setEnabled(b); editProfile.setEnabled(b); check.setEnabled(b);

    }

    public void getImage(ImageView img, String getImage, Context context, String path)
    {
        storage = FirebaseStorage.getInstance();
        StorageReference ref = storage.getReference().child(path + getImage);
        try {
            ref.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                @Override
                public void onSuccess(Uri uri) {
                    try {
                        Glide.with(context).load(uri).into(img);
                        scrollView.setVisibility(View.VISIBLE);
                        progressBar.setVisibility(View.GONE);
                        check.setEnabled(true);
                    }catch(Exception e)
                    {
                        e.printStackTrace();
                    }
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void updateUI(Users users)
    {
        email.setEnabled(false);
        username.setText(users.getUsername());
        email.setText(users.getEmail());
        if(users.getNationality() != null) nationality.setText(users.getNationality());
        if(users.getWorkplace() != null) workplace.setText(users.getWorkplace());
        if(users.getPosition() != null) position.setText(users.getPosition());

        username.setSelection(username.getText().length());
    }

    public void editUser()
    {
        nation = nationality.getText().toString().trim();
        place = workplace.getText().toString().trim();
        pos = position.getText().toString().trim();

        mDatabase = FirebaseDatabase.getInstance().getReference();
        mDatabase.child("Users").child(uID).child("username").setValue(name).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                if(pathImage != null) mDatabase.child("Users").child(uID).child("image").setValue(pathImage);
                mDatabase.child("Users").child(uID).child("nationality").setValue(nation);
                mDatabase.child("Users").child(uID).child("workplace").setValue(place);
                mDatabase.child("Users").child(uID).child("position").setValue(pos).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(SettingActivity.this, "Edit Successful", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                });
            }
        });

    }

    public void postExecute()
    {
        if(TextUtils.isEmpty(username.getText().toString()))
        {
            username.setError("Cannot Empty");
            return;
        }
        else
        {
            name = username.getText().toString();
        }

        closeControl(false);
        if(mUploadTask == null || !mUploadTask.isInProgress()) {
            if(pathImage != null) uploadImage(uri , pathImage);
        }
        if(mUploadTask != null && pathImage != null) {
            progressBar.setVisibility(View.VISIBLE);
            mUploadTask.addOnCompleteListener(new OnCompleteListener() {
                @Override
                public void onComplete(@NonNull Task task) {
                    editUser();
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(SettingActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    closeControl(true);
                }
            });
        }
        else
        {
            progressBar.setVisibility(View.VISIBLE);
            editUser();
        }
    }

    /////////////////image ///////////////////////////

    private void startCropImageActivity() {
        CropImage.activity()
                .start(this);
    }
    public void setImage(CropImage.ActivityResult result){
        try {
            File tempFile    = new File(result.getUri().getPath());
            Log.d("test",result.getUri().getPath().toString());
            if(tempFile.exists()){
                profile.setImageURI(result.getUri());
                uri = result.getUri();
                pathImage = uri.getLastPathSegment(); //+ System.currentTimeMillis();
                //extension = getFileExtension(uri);
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public void onSelectImageClick(View view) {
        if (CropImage.isExplicitCameraPermissionRequired(this)) {
            requestPermissions(new String[]{Manifest.permission.CAMERA}, CropImage.CAMERA_CAPTURE_PERMISSIONS_REQUEST_CODE);
        } else {
            //CropImage.startPickImageActivity(this);
            startCropImageActivity();
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CropImage.PICK_IMAGE_PERMISSIONS_REQUEST_CODE) {
            if (mCropImageUri != null && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // required permissions granted, start crop image activity
                startCropImageActivity(mCropImageUri);

            } else {
                Toast.makeText(this, "Cancelling, required permissions are not granted", Toast.LENGTH_LONG).show();
            }
        }
        if (requestCode == CropImage.CAMERA_CAPTURE_PERMISSIONS_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                CropImage.startPickImageActivity(this);
            } else {
                Toast.makeText(this, "Cancelling, required permissions are not granted", Toast.LENGTH_LONG).show();
            }
        }
    }

    private void startCropImageActivity(Uri imageUri) {
        CropImage.activity(imageUri)
                .start(this);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == Activity.RESULT_OK) {
                setImage(result);
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }
        }
    }

    //upload image to Firebase Storage
    public void uploadImage(Uri uri, String imagePath) {

        storageReference = FirebaseStorage.getInstance().getReference();
        if(uri != null)
        {
            ref = storageReference.child("profile/").child(imagePath);
            mUploadTask = ref.putFile(uri)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            //Toast.makeText(MainActivity.this, "Failed "+e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {

                        }
                    });
        }
    }


    @Override
    public void onClick(View v) {
        if(v == back)
        {
            finish();
        }
        else if(v == check)
        {
            scrollView.setScrollY(0);
            postExecute();
        }
        else if(v == editProfile)
        {
            onSelectImageClick(profile);
        }
    }
}
