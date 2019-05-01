package com.example.phakneath.jobber;

import android.content.Intent;
import android.graphics.Color;
import android.os.UserHandle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.phakneath.jobber.Model.Users;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.shashank.sony.fancydialoglib.Animation;
import com.shashank.sony.fancydialoglib.FancyAlertDialog;
import com.shashank.sony.fancydialoglib.FancyAlertDialogListener;
import com.shashank.sony.fancydialoglib.Icon;

import java.util.ArrayList;
import java.util.List;

public class RegisterActivity extends AppCompatActivity implements View.OnClickListener{

    FirebaseAuth mAuth;
    EditText username, email, password;
    Button signup;
    TextView gotoLogin;
    private String id, name, myEmail;
    //private String receiveToken, receiveEmail;
    DatabaseReference mDatabase;
    RelativeLayout progress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        mAuth = FirebaseAuth.getInstance();
        initView();
        signup.setOnClickListener(this::onClick);
        gotoLogin.setOnClickListener(this::onClick);
        //phoneNum.requestFocusFromTouch();
        //playerId = OneSignal.getPermissionSubscriptionState().getSubscriptionStatus().getUserId();
        //AppSingleton.getInstance().setPlayerId(playerId);
    }

    public void initView()
    {
        username = findViewById(R.id.users);
        email = findViewById(R.id.email);
        password = findViewById(R.id.password);
        signup = findViewById(R.id.signup);
        gotoLogin = findViewById(R.id.gotologin);
        progress = findViewById(R.id.blurr);
    }

    public void createNewUser(String email, String password)
    {
        mAuth.createUserWithEmailAndPassword(email,password).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()){
                    progress.setVisibility(View.GONE);
                    //receiveToken = mAuth.getCurrentUser().getIdToken(true).toString();
                    //receiveEmail = mAuth.getCurrentUser().getEmail().toString();
                    Toast.makeText(RegisterActivity.this,"Register successfull", Toast.LENGTH_SHORT).show();
                    Log.d("Register", mAuth.getCurrentUser().getIdToken(true).toString());
                    Log.d("Register", mAuth.getCurrentUser().getUid());

                    id =  mAuth.getCurrentUser().getUid();
                    writeUser(String.valueOf(id));
                    new FancyAlertDialog.Builder(RegisterActivity.this)
                            .setTitle("Verification")
                            .setBackgroundColor(Color.parseColor("#292E43"))  //Don't pass R.color.colorvalue
                            .setMessage("Thank you for signing in, Please verify your email before login !")
                            .setPositiveBtnBackground(Color.parseColor("#2196F3"))  //Don't pass R.color.colorvalue
                            .setPositiveBtnText("VERIFY")
                            .setNegativeBtnText("Cancel")
                            .setAnimation(Animation.SIDE)
                            .isCancellable(false)
                            .setIcon(R.drawable.infos_full, Icon.Visible)
                            .OnPositiveClicked(new FancyAlertDialogListener() {
                                @Override
                                public void OnClick() {
                                    progress.setVisibility(View.VISIBLE);
                                    mAuth.getCurrentUser().sendEmailVerification().addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if(task.isSuccessful())
                                            {
                                                Toast.makeText(RegisterActivity.this, "Please Check Your Email to Verify", Toast.LENGTH_LONG).show();
                                                mAuth.signOut();
                                                startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
                                                progress.setVisibility(View.GONE);
                                                finish();
                                            }
                                        }
                                    });
                                }
                            })
                            .OnNegativeClicked(new FancyAlertDialogListener() {
                                @Override
                                public void OnClick() {
                                }
                            }).build();
                }else{
                    //progressBar.setVisibility(View.GONE);
                    Log.d("error",task.getException().toString());
                    Toast.makeText(RegisterActivity.this,task.getException().toString(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    public void writeUser(String id)
    {
        /*if(mUploadTask == null || !mUploadTask.isInProgress())
            uploadImage(uri);*/
        name = username.getText().toString().trim();
        myEmail = email.getText().toString().trim();

        mDatabase = FirebaseDatabase.getInstance().getReference();
        Users user = new Users(id,myEmail,name);
        mDatabase.child("Users").child(id).setValue(user);
    }

    @Override
    public void onClick(View v) {
        if(v == signup)
        {
            InputMethodManager imm = (InputMethodManager) getSystemService(this.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(signup.getWindowToken(), 0);
            if (TextUtils.isEmpty(email.getText()) || TextUtils.isEmpty(username.getText()) || TextUtils.isEmpty(password.getText())) {
                email.setError("Cannot empty");
                username.setError("Cannot empty");
                password.setError("Cannot empty");
            } else {
                progress.setVisibility(View.VISIBLE);
                createNewUser(email.getText().toString().trim(), password.getText().toString().trim());
            }

        }
        else if(v == gotoLogin)
        {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        }
    }
}

