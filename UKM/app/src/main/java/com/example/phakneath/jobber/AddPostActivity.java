package com.example.phakneath.jobber;

import android.Manifest;
import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.example.phakneath.jobber.Model.ESCCI;
import com.example.phakneath.jobber.Model.MyESCCI;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;
import com.theartofdev.edmodo.cropper.CropImage;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class AddPostActivity extends AppCompatActivity implements View.OnClickListener{

    Spinner categories;
    LinearLayout eventContainer, scholarshipContainer, ccContainer, internshipContainer, actionBar;
    TextView random;
    EditText etName, etOrganizer, etEWhen, etEFrom, etEUntil, etEWhere, etEAddress, etEFee, etEAbout, etELink;
    EditText etSDate, etSLink, etSAbout;
    EditText etCRandom, etCDate, etCLink, etCAbout;
    EditText etIFrom, etITo, etIDate, etILink, etIAbout;
    CheckBox chFree, chFee, chBachelor, chMaster, chPHD;
    Button btnPost;
    ImageView picture, plus, back;
    ProgressBar progressBar;
    ScrollView scroll;

    String fee;
    boolean admission;

    FirebaseAuth mAuth;
    DatabaseReference mDatabase;
    static StorageTask mUploadTask;
    StorageReference ref;
    StorageReference storageReference;

    ArrayAdapter<String> dataAdapter;
    List<String> list;
    ESCCI myPost;
    String id, uID, mode, name, organizer, from, until, where, address, about, date, link,randomText, degree, image;
    long postingTime;
    String pathImage;
    Uri uri, mCropImageUri;
    double longit, lat;
    String add;

    final Calendar myCalendar = Calendar.getInstance();
    DatePickerDialog.OnDateSetListener dates;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //getWindow().setStatusBarColor(ContextCompat.getColor(this,R.color.whitey));
        setContentView(R.layout.activity_add_post);
        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        initView();
        mAuth = FirebaseAuth.getInstance();
        uID = mAuth.getCurrentUser().getUid();

        list = new ArrayList<>();
        list.add("Career");
        list.add("Competition");
        list.add("Events");
        list.add("Internship");
        list.add("Scholarship");

        btnPost.setOnClickListener(this::onClick);

        dataAdapter = new ArrayAdapter<String>(this, R.layout.spinner_item, list);
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        categories.setAdapter(dataAdapter);

        categories.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if(parent.getSelectedItemPosition() == 0)
                {
                    onOffContainer(ccContainer, scholarshipContainer,eventContainer,internshipContainer);
                    random.setText("Offered Position");
                }
                else if(parent.getSelectedItemPosition() == 1)
                {
                    onOffContainer(ccContainer, scholarshipContainer,eventContainer,internshipContainer);
                    random.setText("Age Eligibility");
                }
                else if(parent.getSelectedItemPosition() == 2)
                {
                    onOffContainer(eventContainer, scholarshipContainer,ccContainer,internshipContainer);
                }
                else if(parent.getSelectedItemPosition() == 3)
                {
                    onOffContainer(internshipContainer, ccContainer,eventContainer,scholarshipContainer);
                }
                else if(parent.getSelectedItemPosition() == 4)
                {
                    onOffContainer(scholarshipContainer, ccContainer,eventContainer,internshipContainer);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        chFree.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked) chFee.setChecked(false);
                else chFee.setChecked(true);
            }
        });

        chFee.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked) chFree.setChecked(false);
                else chFree.setChecked(true);
            }
        });

        etEWhen.setOnClickListener(this::onClick);
        etEFrom.setOnClickListener(this::onClick);
        etEUntil.setOnClickListener(this::onClick);

        etCDate.setOnClickListener(this::onClick);
        etIFrom.setOnClickListener(this::onClick);
        etITo.setOnClickListener(this::onClick);
        etIDate.setOnClickListener(this::onClick);
        etSDate.setOnClickListener(this::onClick);
        picture.setOnClickListener(this::onClick);
        plus.setOnClickListener(this::onClick);
        back.setOnClickListener(this::onClick);
        etEAddress.setOnClickListener(this::onClick);
    }

    private void initView() {
        categories = findViewById(R.id.categories);
        eventContainer = findViewById(R.id.eventsContainer);
        scholarshipContainer = findViewById(R.id.scholarshipContainer);
        ccContainer = findViewById(R.id.ccContainer);
        internshipContainer = findViewById(R.id.internshipContainer);
        random = findViewById(R.id.random);
        actionBar = (LinearLayout) findViewById(R.id.top);
        etName = findViewById(R.id.name);
        etOrganizer = findViewById(R.id.organzier);
        etEWhen = findViewById(R.id.date);
        etEFrom = findViewById(R.id.from);
        etEUntil = findViewById(R.id.until);
        etEWhere = findViewById(R.id.where);
        etEAddress = findViewById(R.id.address);
        etEFee = findViewById(R.id.amountFee);
        etEAbout = findViewById(R.id.about);
        etELink = findViewById(R.id.eLink);
        etSDate = findViewById(R.id.sDate);
        etSLink = findViewById(R.id.sLink);
        etSAbout = findViewById(R.id.sAbout);
        etCRandom = findViewById(R.id.cposition);
        etCDate = findViewById(R.id.cDate);
        etCLink = findViewById(R.id.cLink);
        etCAbout = findViewById(R.id.cAbout);
        etIFrom = findViewById(R.id.iFrom);
        etITo = findViewById(R.id.iTo);
        etIDate = findViewById(R.id.iDate);
        etILink = findViewById(R.id.iLink);
        etIAbout = findViewById(R.id.iAbout);
        chFree = findViewById(R.id.free);
        chFee = findViewById(R.id.fee);
        chBachelor = findViewById(R.id.bachelor);
        chMaster = findViewById(R.id.master);
        chPHD = findViewById(R.id.phd);
        btnPost = findViewById(R.id.posts);
        picture = findViewById(R.id.picture);
        plus = findViewById(R.id.plus);
        back = findViewById(R.id.label);
        progressBar = findViewById(R.id.progressBar);
        scroll = findViewById(R.id.scroll);
    }

    public void onOffContainer(LinearLayout a, LinearLayout b, LinearLayout c, LinearLayout d)
    {
        a.setVisibility(View.VISIBLE);
        b.setVisibility(View.GONE);
        c.setVisibility(View.GONE);
        d.setVisibility(View.GONE);
    }

    public void postExecute()
    {
        ESCCI myPost=null;
        if(TextUtils.isEmpty(etName.getText().toString()) || TextUtils.isEmpty(etOrganizer.getText().toString()))
        {
            etName.setError("Cannot Empty");
            etOrganizer.setError("Cannot Empty");
            return;
        }
        else
        {
            name = etName.getText().toString();
            organizer = etOrganizer.getText().toString();
        }

        if(pathImage == null) {
            Toast.makeText(this, "Please choose an image for your post", Toast.LENGTH_SHORT).show();
            return;
        }

        mode = categories.getSelectedItem().toString();
        image = pathImage;

        switch(mode)
        {
            case "Career": myPost = checkEmptyCareerAndCompetition(); break;
            case "Competition": myPost = checkEmptyCareerAndCompetition(); break;
            case "Events": myPost = checkEmptyEvents(); break;
            case "Internship": myPost = checkEmptyInternship(); break;
            case "Scholarship": myPost = checkEmptySchlarship(); break;
        }

        if(myPost != null)
        {
            closeControl(false);
            if(mUploadTask == null || !mUploadTask.isInProgress()) {
                uploadImage(uri , pathImage);
            }
            if(mUploadTask != null) {
                progressBar.setVisibility(View.VISIBLE);
                ESCCI finalMyPost = myPost;
                mUploadTask.addOnCompleteListener(new OnCompleteListener() {
                    @Override
                    public void onComplete(@NonNull Task task) {
                        saveToServer(finalMyPost);
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(AddPostActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                        closeControl(true);
                    }
                });
            }
        }
    }

    public void closeControl(Boolean b)
    {
        etName.setEnabled(b); etOrganizer.setEnabled(b); etEWhen.setEnabled(b);
        etEFrom.setEnabled(b); etEUntil.setEnabled(b); etEWhere.setEnabled(b); etEAddress.setEnabled(b);
        etEFee.setEnabled(b); etEAbout.setEnabled(b); etELink.setEnabled(b);
        etSDate.setEnabled(b); etSLink.setEnabled(b); etSAbout.setEnabled(b);
        etCRandom.setEnabled(b); etCDate.setEnabled(b); etCLink.setEnabled(b); etCAbout.setEnabled(b);
        etIFrom.setEnabled(b); etITo.setEnabled(b); etIDate.setEnabled(b); etILink.setEnabled(b); etIAbout.setEnabled(b);
        categories.setEnabled(b);
        chFree.setEnabled(b); chFee.setEnabled(b); chBachelor.setEnabled(b); chMaster.setEnabled(b); chPHD.setEnabled(b);
        picture.setEnabled(b);
        btnPost.setEnabled(b);
    }

    public ESCCI checkEmptyCareerAndCompetition()
    {
        ESCCI myPost=null;
        if(TextUtils.isEmpty(etCRandom.getText().toString()) || TextUtils.isEmpty(etCDate.getText().toString()) || TextUtils.isEmpty(etCAbout.getText().toString()) || TextUtils.isEmpty(etCLink.getText().toString()))
        {
            etCRandom.setError("Cannot Empty");
            etCDate.setError("Cannot Empty");
            etCAbout.setError("Cannot Empty");
            etCLink.setError("Cannot Empty");
        }
        else
        {
            postingTime = System.currentTimeMillis();
            id = uID + postingTime;
            randomText = etCRandom.getText().toString();
            date = etCDate.getText().toString();
            about = etCAbout.getText().toString();
            link = etCLink.getText().toString();

            myPost = new ESCCI(id, uID, name,mode,organizer,randomText,date,link,about,postingTime,image);
        }
        return myPost;
    }

    public ESCCI checkEmptyEvents()
    {
        ESCCI myPost=null;

        if(chFee.isChecked() && TextUtils.isEmpty(etEFee.getText().toString()))
        {
            etEFee.setError("Cannot Empty");
        }
        else if(chFee.isChecked())
        {
            admission = true;
            fee = etEFee.getText().toString();
        }
        else if(chFree.isChecked())
        {
            admission = false;
        }
        else if(chFee.isChecked() == false && chFree.isChecked() == false)
        {
            chFee.setError("Please choose");
            chFree.setError("Please choose");
        }

        if(TextUtils.isEmpty(etEWhen.getText().toString()) || TextUtils.isEmpty(etEFrom.getText().toString()) || TextUtils.isEmpty(etEUntil.getText().toString()) || TextUtils.isEmpty(etEWhere.getText().toString()) || TextUtils.isEmpty(etEAddress.getText().toString()) || TextUtils.isEmpty(etEAbout.getText().toString()) || TextUtils.isEmpty(etELink.getText().toString()))
        {
            etEWhen.setError("Cannot Empty");
            etEFrom.setError("Cannot Empty");
            etEUntil.setError("Cannot Empty");
            etEWhere.setError("Cannot Empty");
            etEAddress.setError("Cannot Empty");
            etEAbout.setError("Cannot Empty");
            etELink.setError("Canoot Empty");
        }
        else
        {
            postingTime = System.currentTimeMillis();
            id = uID + postingTime;
            date = etEWhen.getText().toString();
            from = etEFrom.getText().toString();
            until = etEUntil.getText().toString();
            where = etEWhere.getText().toString();
            address = etEAddress.getText().toString();
            about = etEAbout.getText().toString();
            link = etELink.getText().toString();

            myPost = new ESCCI(id,uID,name,mode,organizer,date,from,until,where,address,link,admission,fee,about,postingTime, image, longit, lat);
        }
        return myPost;
    }

    public ESCCI checkEmptyInternship()
    {
        ESCCI myPost=null;

        if(TextUtils.isEmpty(etIFrom.getText().toString()) || TextUtils.isEmpty(etITo.getText().toString()) || TextUtils.isEmpty(etIDate.getText().toString()) || TextUtils.isEmpty(etIAbout.getText().toString()) || TextUtils.isEmpty(etILink.getText().toString()))
        {
            etIFrom.setError("Cannot Empty");
            etITo.setError("Cannot Empty");
            etIDate.setError("Cannot Empty");
            etIAbout.setError("Cannot Empty");
            etILink.setError("Cannot Empty");
        }
        else
        {
            postingTime = System.currentTimeMillis();
            id = uID + postingTime;
            from = etIFrom.getText().toString();
            until = etITo.getText().toString();
            date = etIDate.getText().toString();
            about = etIAbout.getText().toString();
            link = etILink.getText().toString();

            myPost = new ESCCI(id,uID,name,mode,organizer,from,until,date,link,about,postingTime, image);
        }

        return myPost;
    }

    public ESCCI checkEmptySchlarship()
    {
        ESCCI myPost=null;
        degree = "";

        if(chBachelor.isChecked())
        {
            degree = "Bachelor ";
        }
        if(chMaster.isChecked())
        {
            degree += "Master ";
        }
        if(chPHD.isChecked())
        {
            degree += "PHD";
        }
        else if(chBachelor.isChecked() == false && chMaster.isChecked() == false && chPHD.isChecked() == false)
        {
            chBachelor.setError("Please choose");
            chMaster.setError("Please choose");
            chPHD.setError("Please choose");
        }

        if(TextUtils.isEmpty(etSDate.getText().toString()) || TextUtils.isEmpty(etSAbout.getText().toString()) || TextUtils.isEmpty(etSLink.getText().toString()))
        {
            etSDate.setError("Cannot Empty");
            etSAbout.setError("Cannot Empty");
            etSLink.setError("Cannot Empty");
        }
        else
        {
            postingTime = System.currentTimeMillis();
            id = uID + postingTime;
            date = etSDate.getText().toString();
            about = etSAbout.getText().toString();
            link = etSLink.getText().toString();

            myPost = new ESCCI(id, uID, name,mode,organizer,degree,date,link,about,postingTime, image);
        }

        return myPost;
    }

    public void saveToServer(ESCCI myPost)
    {
        mDatabase = FirebaseDatabase.getInstance().getReference();
        mDatabase.child("Posting").child(myPost.getMode()).child(id).setValue(myPost).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                MyESCCI myESCCI = new MyESCCI(myPost.getId(), myPost.getMode(), myPost.getOwnerID());
                mDatabase.child("Users").child(uID).child("randomThings").child(id).setValue(myESCCI);
                mDatabase.child("Posting").child("AllPosts").child(id).setValue(myPost);
                progressBar.setVisibility(View.GONE);
                Toast.makeText(AddPostActivity.this, "Add Successfully", Toast.LENGTH_SHORT).show();
                finish();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(AddPostActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                closeControl(true);
            }
        });
    }

    public void timePicker(EditText a)
    {
        Calendar mcurrentTime = Calendar.getInstance();
        int hour = mcurrentTime.get(Calendar.HOUR_OF_DAY);
        int minute = mcurrentTime.get(Calendar.MINUTE);
        TimePickerDialog mTimePicker;
        mTimePicker = new TimePickerDialog(AddPostActivity.this, new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker timePicker, int selectedHour, int selectedMinute) {
                String timeSet = "";
                if (selectedHour > 12) {
                    selectedHour -= 12;
                    timeSet = "pm";
                } else if (selectedHour == 0) {
                    selectedHour += 12;
                    timeSet = "am";
                } else if (selectedHour == 12)
                    timeSet = "pm";
                else
                    timeSet = "am";

                a.setText(String.format("%02d:%02d %s", selectedHour, selectedMinute, timeSet));
            }
        }, hour, minute, true);//Yes 24 hour time
        mTimePicker.setTitle("Select Time");
        mTimePicker.show();
    }

    public void datePicker(EditText a)
    {
        dates = new DatePickerDialog.OnDateSetListener() {

            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear,
                                  int dayOfMonth) {
                // TODO Auto-generated method stub
                myCalendar.set(Calendar.YEAR, year);
                myCalendar.set(Calendar.MONTH, monthOfYear);
                myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);

                String myFormat = "EE, dd MMMM, yyyy"; //In which you need put here
                SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.US);

                a.setText(sdf.format(myCalendar.getTime()));
            }

        };
    }

    public void openDatePicker()
    {
        new DatePickerDialog(AddPostActivity.this, dates, myCalendar
                .get(Calendar.YEAR), myCalendar.get(Calendar.MONTH),
                myCalendar.get(Calendar.DAY_OF_MONTH)).show();
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
                picture.setImageURI(result.getUri());
                uri = result.getUri();
                pathImage = uri.getLastPathSegment(); //+ System.currentTimeMillis();
                //extension = getFileExtension(uri);
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public void onSelectImageClick(View view) {
        if (CropImage.isExplicitCameraPermissionRequired(this) || CropImage.isReadExternalStoragePermissionsRequired(this, null)) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(new String[]{Manifest.permission.CAMERA, Manifest.permission.READ_EXTERNAL_STORAGE}, CropImage.CAMERA_CAPTURE_PERMISSIONS_REQUEST_CODE);
            }
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
                plus.setVisibility(View.GONE);
                setImage(result);
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }
        }

        if (requestCode == 1) {
            if(resultCode == Activity.RESULT_OK){
                longit = data.getDoubleExtra("longtitute",0);
                lat = data.getDoubleExtra("latitute",0);
                add = data.getStringExtra("address");
                etEAddress.setText(add);
            }
        }
    }

    //upload image to Firebase Storage
    public void uploadImage(Uri uri, String imagePath) {

        storageReference = FirebaseStorage.getInstance().getReference();
        if(uri != null)
        {
            ref = storageReference.child("posting/").child(imagePath);
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
        if(v == btnPost)
        {
            InputMethodManager imm = (InputMethodManager) getSystemService(this.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(btnPost.getWindowToken(), 0);
            scroll.setScrollY(0);
            scroll.fullScroll(ScrollView.FOCUS_UP);
            postExecute();
        }
        else if(v == etEWhen)
        {
            datePicker(etEWhen);
            Toast.makeText(this, "opens", Toast.LENGTH_SHORT).show();
            openDatePicker();
        }
        else if(v == etEFrom)
        {
            datePicker(etEFrom);
            timePicker(etEFrom);
        }
        else if(v == etEUntil)
        {
            datePicker(etEUntil);
            timePicker(etEUntil);
        }
        else if(v == etCDate)
        {
            datePicker(etCDate);
            openDatePicker();
        }
        else if(v == etIFrom)
        {
            datePicker(etIFrom);
            openDatePicker();
        }
        else if(v == etITo)
        {
            datePicker(etITo);
            openDatePicker();
        }
        else if(v == etIDate)
        {
            datePicker(etIDate);
            openDatePicker();
        }
        else if(v == etSDate)
        {
            datePicker(etSDate);
            openDatePicker();
        }
        else if(v == picture)
        {
            onSelectImageClick(picture);
        }
        else if(v == plus)
        {
            onSelectImageClick(picture);
        }
        else if(v == back)
        {
            finish();
        }
        else if(v == etEAddress)
        {
            Intent intent = new Intent(this, GoogleMapActivity.class);
            startActivityForResult(intent,1);
        }
    }

}
