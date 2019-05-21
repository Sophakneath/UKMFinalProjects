package com.example.phakneath.jobber;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomSheetDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.ShareActionProvider;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.phakneath.jobber.Model.ESCCI;
import com.example.phakneath.jobber.Model.saveESCCI;
import com.facebook.FacebookSdk;
import com.facebook.LoggingBehavior;
import com.facebook.share.model.ShareHashtag;
import com.facebook.share.model.ShareLinkContent;
import com.facebook.share.widget.ShareDialog;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
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
import com.google.firebase.storage.StorageReference;

public class DetailActivity extends AppCompatActivity implements View.OnClickListener, OnMapReadyCallback {

    ImageView picture, profile, icon1, icon2, icon3, icon4, heartFav, imgEdit, imgShare, imgMore, imgDelete;
    RelativeLayout back, share, fav, more, loc;
    LinearLayout eventsOnly, conEdit, conDelete;
    TextView name, mode, by, text1, text2, text3, text4, text5, text6, text7, text8, about, username, position, textEdit, textDelete;
    ESCCI escci;
    String previousImage = null, f;
    LinearLayout detail;
    ProgressBar progressBar;

    DatabaseReference mDatabase;
    FirebaseAuth mAuth;
    FirebaseStorage storage;
    String uid;
    GoogleMap map;
    MarkerOptions marker;
    MapFragment supportMapFragment;
    private ShareActionProvider shareActionProvider;
    View view;
    StorageReference storageReference,ref;
    BottomSheetDialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_detail);

        /*FacebookSdk.setApplicationId(getString(R.string.facebook_app_id));
        //initialize Facebook SDK
        FacebookSdk.sdkInitialize(getApplicationContext());
        if (BuildConfig.DEBUG) {
            FacebookSdk.setIsDebugEnabled(true);
            FacebookSdk.addLoggingBehavior(LoggingBehavior.INCLUDE_ACCESS_TOKENS);
        }*/

        view = LayoutInflater.from(this).inflate(R.layout.more_showing_layout, null,false);
        initView();
        mAuth = FirebaseAuth.getInstance();
        uid = mAuth.getCurrentUser().getUid();
        Intent intent = getIntent();
        escci = (ESCCI) intent.getSerializableExtra("data");
        f = intent.getStringExtra("fav");

        updateUI();
        getOwnerInfo();

        back.setOnClickListener(this::onClick);
        more.setOnClickListener(this::onClick);
        fav.setOnClickListener(this::onClick);
        profile.setOnClickListener(this::onClick);
        share.setOnClickListener(this::onClick);
        imgMore.setOnClickListener(this::onClick);
        imgShare.setOnClickListener(this::onClick);
        imgEdit.setOnClickListener(this::onClick);
        imgDelete.setOnClickListener(this::onClick);
        textEdit.setOnClickListener(this::onClick);
        textDelete.setOnClickListener(this::onClick);
        conEdit.setOnClickListener(this::onClick);
        conDelete.setOnClickListener(this::onClick);

        supportMapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.googlemap);
        supportMapFragment.getMapAsync(this);
        if (f.equals("unFav")) {
            heartFav.setImageDrawable(this.getResources().getDrawable(R.drawable.red_fav));
            heartFav.setTag("unFav");
        }
        else
        {
            heartFav.setImageDrawable(this.getResources().getDrawable(R.drawable.favorite));
            heartFav.setTag("fav");
        }
    }

    public void initView() {

        picture = findViewById(R.id.picture);
        profile = findViewById(R.id.profile);
        icon1 = findViewById(R.id.icon1);
        icon2 = findViewById(R.id.icon2);
        icon3 = findViewById(R.id.icon3);
        icon4 = findViewById(R.id.icon4);
        back = findViewById(R.id.label);
        share = findViewById(R.id.share);
        fav = findViewById(R.id.fav);
        more = findViewById(R.id.more);
        name = findViewById(R.id.name);
        mode = findViewById(R.id.modes);
        by = findViewById(R.id.by);
        text1 = findViewById(R.id.text1);
        text2 = findViewById(R.id.text2);
        text3 = findViewById(R.id.text3);
        text4 = findViewById(R.id.text4);
        text5 = findViewById(R.id.text5);
        text6 = findViewById(R.id.text6);
        text7 = findViewById(R.id.text7);
        text8 = findViewById(R.id.text8);
        about = findViewById(R.id.about);
        username = findViewById(R.id.username);
        position = findViewById(R.id.position);
        loc = findViewById(R.id.loc);
        heartFav = findViewById(R.id.heart);
        progressBar = findViewById(R.id.progress);
        detail = findViewById(R.id.detail);
        eventsOnly = findViewById(R.id.eventsOnly);
        imgMore = findViewById(R.id.imgMore);
        imgShare = findViewById(R.id.imgShare);
        imgEdit = view.findViewById(R.id.imgEdit);
        imgDelete = view.findViewById(R.id.imgDelete);
        textEdit = view.findViewById(R.id.textEdit);
        textDelete = view.findViewById(R.id.textDelete);
        conEdit = view.findViewById(R.id.conEdit);
        conDelete = view.findViewById(R.id.conDelete);
    }

    public void updateUI() {
        progressBar.setVisibility(View.VISIBLE);
        detail.setVisibility(View.GONE);

        name.setText(escci.getName());
        by.setText("By " + escci.getOrganizer());
        mode.setText(escci.getMode());

        about.setText(escci.getAbout());
        //Toast.makeText(this, ""+about.getLineCount(), Toast.LENGTH_SHORT).show();

        if (!escci.getImage().equals(previousImage))
            getImagePosting(picture, escci.getImage(), this);
        else
        {
            progressBar.setVisibility(View.GONE);
            detail.setVisibility(View.VISIBLE);
        }

        loc.setVisibility(View.GONE);

        if (escci.getOwnerID().equals(uid) == true)
            more.setVisibility(View.VISIBLE);
        else
            more.setVisibility(View.GONE);

        switch (escci.getMode()) {
            case "Career":
                icon1.setImageDrawable(this.getResources().getDrawable(R.drawable.position_30dp));
                text1.setText("Position Required");
                text2.setText(escci.getRandom());
                updateOtherUI();
                break;

            case "Competition":
                icon1.setImageDrawable(this.getResources().getDrawable(R.drawable.age));
                text1.setText("Age Eligibility");
                text2.setText(escci.getRandom());
                updateOtherUI();
                break;

            case "Events":
                updateEventsUI();
                break;

            case "Internship":
                String[] b = escci.getStartDate().split(" ");
                String[] c = escci.getEndDate().split(" ");

                icon1.setImageDrawable(this.getResources().getDrawable(R.drawable.period));
                text1.setText("Internship Period");
                text2.setText("Period: " + b[2] + " " + b[1] + " - " + c[2] + " " + c[1]);
                updateOtherUI();
                break;

            case "Scholarship":
                String[] f = escci.getRandom().split(" ");
                String degree = null;
                if (f.length == 2) degree = f[0] + " & " + f[1];
                else if (f.length == 3) degree = f[0] + ", " + f[1] + " & " + f[2];
                else degree = f[0];

                icon1.setImageDrawable(this.getResources().getDrawable(R.drawable.scholarship));
                text1.setText("Scholarship Degree");
                text2.setText(degree);
                updateOtherUI();
                break;
        }
    }

    public void updateEventsUI() {
        icon1.setImageDrawable(this.getResources().getDrawable(R.drawable.date));
        icon2.setImageDrawable(this.getResources().getDrawable(R.drawable.location));
        icon3.setImageDrawable(this.getResources().getDrawable(R.drawable.admission));
        icon4.setImageDrawable(this.getResources().getDrawable(R.drawable.links));

        String[] h = escci.getDate().split(" ");
        text1.setText(escci.getDate());
        text2.setText(escci.geteTimeStart() + " " + escci.geteTimeEnd() + " GMT +8");

        text3.setText(escci.geteLocation());
        text4.setText(escci.geteAddress());
        text5.setText("Admission");
        if (escci.iseAdmission()) text6.setText(escci.geteFee() + "");
        else text6.setText("Free of Charge");

        text7.setText("Link for detail");
        if (escci.getLink() != null) {
            eventsOnly.setVisibility(View.VISIBLE);
            text8.setText(escci.getLink());
        }
        else {
            eventsOnly.setVisibility(View.GONE);
        }

        loc.setVisibility(View.VISIBLE);
    }

    public void updateOtherUI() {
        icon2.setImageDrawable(this.getResources().getDrawable(R.drawable.date));
        icon3.setImageDrawable(this.getResources().getDrawable(R.drawable.links));

        text3.setText("Deadline of Apllication");
        text4.setText(escci.getDate());
        text5.setText("Link for detail");
        if (escci.getLink() != null) text6.setText(escci.getLink());
        else text6.setText("None");
    }

    public void getOwnerInfo() {
        mDatabase = FirebaseDatabase.getInstance().getReference();
        mDatabase.child("Users").child(escci.getOwnerID()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String name = dataSnapshot.child("username").getValue(String.class);
                String pos = dataSnapshot.child("position_30dp").getValue(String.class);
                String img = dataSnapshot.child("image").getValue(String.class);
                String id = dataSnapshot.child("id").getValue(String.class);

                username.setText(name);
                username.setTag(id);
                if (pos != null) position.setText(pos);
                else position.setText("None");

                if (img != null) getImageProfile(profile, img, DetailActivity.this);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    public void getImageProfile(ImageView img, String getImage, Context context) {
        storage = FirebaseStorage.getInstance();
        StorageReference ref = storage.getReference().child("profile/" + getImage);
        try {
            ref.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                @Override
                public void onSuccess(Uri uri) {
                    try {
                        Glide.with(context).load(uri).into(img);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void getImagePosting(ImageView img, String getImage, Context context) {
        storage = FirebaseStorage.getInstance();
        StorageReference ref = storage.getReference().child("posting/" + getImage);
        try {
            ref.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                @Override
                public void onSuccess(Uri uri) {
                    try {
                        Glide.with(context).load(uri).into(img);
                        progressBar.setVisibility(View.GONE);
                        detail.setVisibility(View.VISIBLE);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == 1) {
            if (resultCode == Activity.RESULT_OK) {
                escci = (ESCCI) data.getSerializableExtra("result");
                LatLng latLng = new LatLng(escci.getLatitute(), escci.getLongitute());
                moveCamera(latLng);
                updateUI();
            }
            if (resultCode == Activity.RESULT_CANCELED) {
                //Write your code if there's no result
            }
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        map = googleMap;
        LatLng latLng = new LatLng(escci.getLatitute(), escci.getLongitute());
        moveCamera(latLng);
    }

    private void moveCamera(LatLng latLng)
    {
        latLng = new LatLng(escci.getLatitute(), escci.getLongitute());
        marker = new MarkerOptions().position(latLng).title(escci.geteLocation()).draggable(false);
        map.addMarker(marker);
        CameraUpdate camera = CameraUpdateFactory.newLatLngZoom(latLng, 10f);
        map.moveCamera(camera);
    }

    public void saveFavourite(String id, String ownerID)
    {
        saveESCCI s = new saveESCCI(id, ownerID);

        mDatabase = FirebaseDatabase.getInstance().getReference();
        mDatabase.child("Users").child(uid).child("favourite").child(id).setValue(s).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                Toast.makeText(DetailActivity.this, "Saved", Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void unSaveFavourite(String id) {
        mDatabase = FirebaseDatabase.getInstance().getReference();
        mDatabase.child("Users").child(uid).child("favourite").child(id).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                Toast.makeText(DetailActivity.this, "Unsaved", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onClick(View v) {

        if (v == back) {
            finish();
        }
        else if (v == more || v ==  imgMore) {
            dialog = new BottomSheetDialog(DetailActivity.this);
            if(view.getParent() != null) {
                ((ViewGroup)view.getParent()).removeView(view);
            }

            dialog.setContentView(view);
            dialog.show();
        }
        else if (v == fav) {
            if (heartFav.getTag() == "fav") {
                heartFav.setImageDrawable(this.getResources().getDrawable(R.drawable.red_fav));
                heartFav.setTag("unFav");
                saveFavourite(escci.getId(), escci.getOwnerID());
            } else {
                heartFav.setImageDrawable(this.getResources().getDrawable(R.drawable.favorite));
                heartFav.setTag("fav");
                unSaveFavourite(escci.getId());
            }
        }
        else if(v == profile)
        {
            Intent intent;
            if(escci.getOwnerID().equals(uid))
            {
                intent = new Intent(this, ProfileActivity.class);
            }
            else
            {
                intent = new Intent(this, OtherProfileActivity.class);
                intent.putExtra("id", escci.getOwnerID());
            }
            startActivity(intent);
        }
        else if(v == share || v == imgShare)
        {
            shareContent();
            //shareToFacebook(escci);
            //Toast.makeText(this, escci.getImage(), Toast.LENGTH_SHORT).show();
        }
        else if(v == imgEdit || v == textEdit || v == conEdit)
        {
            previousImage = escci.getImage();
            Intent intent = new Intent(DetailActivity.this, EditPostsActivity.class);
            Bundle bundle = new Bundle();
            bundle.putSerializable("posts", escci);
            intent.putExtras(bundle);
            startActivityForResult(intent, 1);
            dialog.dismiss();
        }
        else if(v == imgDelete || v == textDelete || v == conDelete)
        {
            dialog.setContentView(LayoutInflater.from(this).inflate(R.layout.loading_delete_dialog_, null,false));
            delete(escci);
        }
    }

    public void delete(ESCCI escci)
    {
        String id = escci.getId();
        mDatabase = FirebaseDatabase.getInstance().getReference();
        mDatabase.child("Posting").child(escci.getMode()).child(id).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {

                mDatabase.child("Users").child(uid).child("randomThings").child(id).removeValue();
                mDatabase.child("Posting").child("AllPosts").child(id).removeValue();

                deleteImage(escci);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(DetailActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void deleteImage(ESCCI escci) {

        storageReference = FirebaseStorage.getInstance().getReference();
        String p = escci.getImage();
        if(p != null) {
            ref = storageReference.child("posting/").child(p);
            ref.delete().addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    Toast.makeText(DetailActivity.this, "Delete Successful", Toast.LENGTH_SHORT).show();
                    dialog.dismiss();
                    finish();
                }
            });
        }
    }

    /*public static class MyReceiver extends BroadcastReceiver {

        DetailActivity detailActivity = new DetailActivity();

        public MyReceiver() {
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            // do something here
            String selectedAppPackage = String.valueOf(intent.getExtras().get(Intent.EXTRA_CHOSEN_COMPONENT));
            Log.d("facebook", "onReceive: " + selectedAppPackage);
            if(selectedAppPackage.contains("com.facebook.composer"))
            {
                detailActivity.shareToFacebook(detailActivity.escci);
                Toast.makeText(context, "f", Toast.LENGTH_SHORT).show();
            }
        }
    }*/

    public void shareContent()
    {
        //Intent receiver = new Intent(DetailActivity.this, MyReceiver.class);
        //PendingIntent pendingIntent = PendingIntent.getBroadcast(DetailActivity.this, 0, receiver, PendingIntent.FLAG_UPDATE_CURRENT);

        Intent intent = new Intent(Intent.ACTION_SEND);
        //intent.setType("image/*");
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_SUBJECT, "  ***Resource from Kairos mobile application\n Download our app to explore the best opportunities");
        intent.putExtra(Intent.EXTRA_TEXT, "https://" + escci.getLink());
        startActivity(Intent.createChooser(intent, "Share using"));//, pendingIntent.getIntentSender()));
    }

    /*public void shareToFacebook(ESCCI escci)
    {
            String caption = "#lostfreee\n" +
                    "\n\n#Description: " +
                    "\n#Reward: ";

            ShareLinkContent content = new ShareLinkContent.Builder()
                    .setContentUrl(Uri.parse("https://developers.facebook.com"))
                    .setShareHashtag(new ShareHashtag.Builder().setHashtag(caption).build())
                    .build();

            ShareDialog shareDialog = new ShareDialog(DetailActivity.this);
            shareDialog.show(content, ShareDialog.Mode.AUTOMATIC);
    }*/
}
