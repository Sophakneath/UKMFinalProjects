package com.example.phakneath.jobber;

import android.arch.paging.PagedList;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.phakneath.jobber.Model.ESCCI;
import com.example.phakneath.jobber.Model.MyESCCI;
import com.example.phakneath.jobber.Model.Users;
import com.example.phakneath.jobber.Model.saveESCCI;
import com.facebook.Profile;
import com.facebook.shimmer.ShimmerFrameLayout;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.makeramen.roundedimageview.RoundedImageView;
import com.shashank.sony.fancydialoglib.Animation;
import com.shashank.sony.fancydialoglib.FancyAlertDialog;
import com.shashank.sony.fancydialoglib.FancyAlertDialogListener;
import com.shashank.sony.fancydialoglib.Icon;
import com.shreyaspatil.firebase.recyclerpagination.DatabasePagingOptions;
import com.shreyaspatil.firebase.recyclerpagination.FirebaseRecyclerPagingAdapter;
import com.shreyaspatil.firebase.recyclerpagination.LoadingState;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileActivity extends AppCompatActivity implements OnClickListener {

    FirebaseAuth mAuth;
    DatabaseReference mDatabase;
    FirebaseStorage storage;
    StorageReference storageReference, ref;

    String uID;
    ShimmerFrameLayout shimmer;
    CircleImageView profile;
    TextView position, username, nationAndWork, countPosts, countFav, add, countCareer, countSch, countEvents, countCom, countInt, noPosts, fav;
    ImageView back, setting, addPosts;
    CardView career, scholarship, events, competition, internship;
    LinearLayoutManager mManager;
    //SwipeRefreshLayout swipeRefreshLayout;
    RecyclerView recyclerView;
    FirebaseRecyclerPagingAdapter mAdapter;
    int count1=0, count2=0, count3=0, count4=0, count5=0;

    Query query;
    PagedList.Config config;
    DatabasePagingOptions<ESCCI> options;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_profile);

        initView();
        mAuth = FirebaseAuth.getInstance();
        uID = mAuth.getCurrentUser().getUid();

        getUser();
        back.setOnClickListener(this::onClick);
        setting.setOnClickListener(this::onClick);
        add.setOnClickListener(this::onClick);
        addPosts.setOnClickListener(this::onClick);
        career.setOnClickListener(this::onClick);
        scholarship.setOnClickListener(this::onClick);
        events.setOnClickListener(this::onClick);
        competition.setOnClickListener(this::onClick);
        internship.setOnClickListener(this::onClick);
        countFav.setOnClickListener(this::onClick);
        fav.setOnClickListener(this::onClick);

        shimmer.startShimmerAnimation();

        getData();
    }

    private void getData()
    {
        mDatabase = FirebaseDatabase.getInstance().getReference().child("Posting").child("AllPosts");
        query = mDatabase.orderByChild("ownerID").equalTo(uID);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.getValue() != null)
                {
                    noPosts.setVisibility(View.GONE);
                    setPageList();
                    setPagination();

                    mAdapter = new FirebaseRecyclerPagingAdapter<ESCCI, ProfileActivity.ESCCIViewHolder>(options) {

                        @NonNull
                        @Override
                        public ProfileActivity.ESCCIViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                            return new ProfileActivity.ESCCIViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.items_showing_layout, parent,false));
                        }

                        @Override
                        protected void onBindViewHolder(@NonNull ProfileActivity.ESCCIViewHolder viewHolder, int position, @NonNull ESCCI model) {
                            viewHolder.setItem(model, uID);

                            getImage(viewHolder.image, model.getImage(), ProfileActivity.this, "posting/");

                            viewHolder.fav.setTag("fav");

                            viewHolder.edit.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    PopupMenu menu = new PopupMenu(ProfileActivity.this, v, Gravity.RIGHT);
                                    menu.inflate(R.menu.more_menu);
                                    //menu.setGravity(Gravity.RIGHT);
                                    menu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                                        @Override
                                        public boolean onMenuItemClick(MenuItem item) {
                                            switch (item.getItemId()) {
                                                case R.id.edit:
                                                    editPost(model);
                                                    return true;

                                                case R.id.delete:

                                                    new FancyAlertDialog.Builder(ProfileActivity.this)
                                                            .setTitle("Confirmation")
                                                            .setBackgroundColor(Color.parseColor("#292E43"))  //Don't pass R.color.colorvalue
                                                            .setMessage("Are you sure you want to delete this item?")
                                                            .setPositiveBtnBackground(Color.parseColor("#2196F3"))  //Don't pass R.color.colorvalue
                                                            .setPositiveBtnText("Yes")
                                                            .setNegativeBtnText("Cancel")
                                                            .setAnimation(Animation.SIDE)
                                                            .isCancellable(false)
                                                            .setIcon(R.drawable.infos_50dp, Icon.Visible)
                                                            .OnPositiveClicked(new FancyAlertDialogListener() {
                                                                @Override
                                                                public void OnClick() {
                                                                    shimmer.startShimmerAnimation();
                                                                    shimmer.setVisibility(View.VISIBLE);
                                                                    recyclerView.setVisibility(View.GONE);
                                                                    delete(model);
                                                                }
                                                            })
                                                            .OnNegativeClicked(new FancyAlertDialogListener() {
                                                                @Override
                                                                public void OnClick() {
                                                                }
                                                            }).build();

                                                    return true;

                                                default:
                                                    return false;
                                            }

                                        }
                                    });

                                    menu.show();
                                }
                            });

                            viewHolder.container.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    String f = (String) viewHolder.fav.getTag();
                                    Intent intent = new Intent(ProfileActivity.this, DetailActivity.class);
                                    Bundle bundle = new Bundle();
                                    bundle.putSerializable("data", model);
                                    bundle.putString("fav", f);
                                    intent.putExtras(bundle);
                                    startActivity(intent);
                                }
                            });

                            viewHolder.fav.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    if(v.getTag() == "fav")
                                    {
                                        viewHolder.fav.setImageDrawable(ProfileActivity.this.getResources().getDrawable(R.drawable.red_fav));
                                        v.setTag("unFav");
                                        saveFavourite(model.getId(), model.getOwnerID());
                                    }
                                    else
                                    {
                                        viewHolder.fav.setImageDrawable(ProfileActivity.this.getResources().getDrawable(R.drawable.favorite));
                                        v.setTag("fav");
                                        unSaveFavourite(model.getId());
                                    }
                                }
                            });

                            getSaveFavourite(model.getId(), viewHolder.fav);

                            viewHolder.share.setOnClickListener(new OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    shareContent(model);
                                }
                            });
                        }

                        @Override
                        protected void onLoadingStateChanged(@NonNull LoadingState state) {
                            switch (state) {
                                case LOADING_INITIAL:
                                case LOADING_MORE:
                                    // Do your loading animation
                                    //swipeRefreshLayout.setRefreshing(true);
                                    shimmer.startShimmerAnimation();
                                    shimmer.setVisibility(View.VISIBLE);
                                    recyclerView.setVisibility(View.GONE);
                                    break;

                                case LOADED:
                                    // Stop Animation
                                    //swipeRefreshLayout.setRefreshing(false);
                                    shimmer.stopShimmerAnimation();
                                    shimmer.setVisibility(View.GONE);
                                    recyclerView.setVisibility(View.VISIBLE);
                                    break;

                                case FINISHED:
                                    //Reached end of Data set
                                    //swipeRefreshLayout.setRefreshing(false);
                                    shimmer.stopShimmerAnimation();
                                    shimmer.setVisibility(View.GONE);
                                    recyclerView.setVisibility(View.VISIBLE);
                                    break;

                                case ERROR:
                                    retry();
                                    break;
                            }
                        }

                        @Override
                        protected void onError(@NonNull DatabaseError databaseError) {
                            //swipeRefreshLayout.setRefreshing(false);
                            shimmer.stopShimmerAnimation();
                            shimmer.setVisibility(View.GONE);
                            recyclerView.setVisibility(View.VISIBLE);
                            databaseError.toException().printStackTrace();
                            retry();
                        }

                    };

                    mManager = new LinearLayoutManager(ProfileActivity.this, LinearLayoutManager.VERTICAL, true);
                    mManager.setStackFromEnd(true);
                    recyclerView.setHasFixedSize(true);
                    recyclerView.setLayoutManager(mManager);
                    recyclerView.setAdapter(mAdapter);
                    recyclerView.setNestedScrollingEnabled(false);
                    mManager.setAutoMeasureEnabled(true);
                }
                else
                {
                    noPosts.setVisibility(View.VISIBLE);
                    shimmer.stopShimmerAnimation();
                    shimmer.setVisibility(View.GONE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    public void initView() {
        profile = findViewById(R.id.profile);
        username = findViewById(R.id.username);
        nationAndWork = findViewById(R.id.workplace);
        countPosts = findViewById(R.id.countPosts);
        countFav = findViewById(R.id.countFav);
        addPosts = findViewById(R.id.addPosts);
        add = findViewById(R.id.posting);
        back = findViewById(R.id.label);
        setting = findViewById(R.id.setting);
        career = findViewById(R.id.career);
        scholarship = findViewById(R.id.scholarship);
        events = findViewById(R.id.events);
        competition = findViewById(R.id.competition);
        internship = findViewById(R.id.internship);
        //swipeRefreshLayout = findViewById(R.id.swipeRefresh);
        recyclerView = findViewById(R.id.mRecyclerView);
        countCareer = findViewById(R.id.countCareer);
        countSch = findViewById(R.id.countSch);
        countEvents = findViewById(R.id.countEvents);
        countCom = findViewById(R.id.countCom);
        countInt = findViewById(R.id.countInt);
        position = findViewById(R.id.position);
        shimmer = findViewById(R.id.shimmer);
        //mainScreen = findViewById(R.id.mainScreen);
        noPosts = findViewById(R.id.noPosts);
        fav = findViewById(R.id.myFav);
        }

    @Override
    protected void onStart() {
        super.onStart();
        if(mAdapter != null) {
            mAdapter.refresh();
            mAdapter.startListening();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if(mAdapter != null) mAdapter.stopListening();
    }

    public void getUser()
    {
        mDatabase = FirebaseDatabase.getInstance().getReference();
        mDatabase.child("Users").child(uID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                List<MyESCCI> myESCCIS = new ArrayList<>();
                List<ESCCI> saveESCCIS = new ArrayList<>();

                String id = dataSnapshot.child("id").getValue(String.class);
                String email = dataSnapshot.child("email").getValue(String.class);
                String username = dataSnapshot.child("username").getValue(String.class);
                String nationality = dataSnapshot.child("nationality").getValue(String.class);
                String workPlace = dataSnapshot.child("workplace").getValue(String.class);
                String position = dataSnapshot.child("position_30dp").getValue(String.class);
                String image = dataSnapshot.child("image").getValue(String.class);
                for(DataSnapshot d: dataSnapshot.child("randomThings").getChildren())
                {
                    myESCCIS.add(d.getValue(MyESCCI.class));
                }

                for(DataSnapshot d: dataSnapshot.child("favourite").getChildren())
                {
                    saveESCCIS.add(d.getValue(ESCCI.class));
                }

                Users users = new Users(id,email,username,nationality,workPlace,position,image);
                updateUI(users, saveESCCIS, myESCCIS);
                if(users.getImage() != null) getImage(profile, users.getImage(), ProfileActivity.this, "profile/");
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
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

    private void updateUI(Users users, List<ESCCI> saves, List<MyESCCI> myESCCIS ) {
        count1 =0;
        count2 = 0;
        count3 = 0;
        count4 = 0;
        count5 = 0;
        username.setText(users.getUsername());
        if(users.getPosition() != null || users.getPosition() != "") position.setText(users.getPosition());
        if(users.getNationality() != null && users.getWorkplace() != null || users.getNationality() != "" && users.getWorkplace() != "") nationAndWork.setText(users.getNationality() + ", " + users.getWorkplace());
        else if(users.getNationality() != null && users.getWorkplace() == null || users.getNationality() != "" && users.getWorkplace() == "") nationAndWork.setText(users.getNationality());
        else if(users.getNationality() == null && users.getWorkplace() != null || users.getNationality() == "" && users.getWorkplace() != "") nationAndWork.setText(users.getWorkplace());
        else nationAndWork.setText("No data provided");

        if(myESCCIS != null)
        {
            countPosts.setText(myESCCIS.size() + "");
            for(MyESCCI myESCCI : myESCCIS)
            {
                switch(myESCCI.getMode())
                {
                    case "Events": count1++; break;
                    case "Career": count2++; break;
                    case "Competition": count3++; break;
                    case "Internship": count4++; break;
                    case "Scholarship": count5++; break;
                }
            }
            countEvents.setText(count1+ " Posts");
            countCareer.setText(count2+ " Posts");
            countCom.setText(count3+ " Posts");
            countInt.setText(count4+" Posts");
            countSch.setText(count5+ " Posts");
        }
        if(saves != null)
        {
            countFav.setText(saves.size()+"");
        }
    }

    public void setPageList()
    {
        config = new PagedList.Config.Builder()
                .setEnablePlaceholders(false)
                .setPrefetchDistance(1)
                .setPageSize(5)
                .build();
    }

    public void setPagination()
    {
        options = new DatabasePagingOptions.Builder<ESCCI>()
                .setLifecycleOwner(this)
                .setQuery(query, config, ESCCI.class)
                .build();
    }

    public void delete(ESCCI escci)
    {
        String id = escci.getId();
        mDatabase = FirebaseDatabase.getInstance().getReference();
        mDatabase.child("Posting").child(escci.getMode()).child(id).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {

                mDatabase.child("Users").child(uID).child("randomThings").child(id).removeValue();
                mDatabase.child("Posting").child("AllPosts").child(id).removeValue();

                deleteImage(escci);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(ProfileActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
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
                    Toast.makeText(ProfileActivity.this, "Delete Successful", Toast.LENGTH_SHORT).show();
                    mAdapter=null;
                    recyclerView.setAdapter(mAdapter);
                    shimmer.startShimmerAnimation();
                    getData();
                }
            });
        }
    }


    public void editPost(ESCCI escci)
    {
        Intent intent = new Intent(ProfileActivity.this, EditPostsActivity.class);
        Bundle bundle = new Bundle();
        bundle.putSerializable("posts", escci);
        intent.putExtras(bundle);
        startActivity(intent);

    }

    public void saveFavourite(String id, String ownerID)
    {
        saveESCCI s = new saveESCCI(id, ownerID);

        mDatabase = FirebaseDatabase.getInstance().getReference();
        mDatabase.child("Users").child(uID).child("favourite").child(id).setValue(s).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                Toast.makeText(ProfileActivity.this, "Saved", Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void unSaveFavourite(String id)
    {
        mDatabase = FirebaseDatabase.getInstance().getReference();
        mDatabase.child("Users").child(uID).child("favourite").child(id).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                Toast.makeText(ProfileActivity.this, "Unsaved", Toast.LENGTH_SHORT).show();
            }
        });
    }


    public void getSaveFavourite(String id, ImageView fav)
    {
        mDatabase = FirebaseDatabase.getInstance().getReference();
        mDatabase.child("Users").child(uID).child("favourite").child(id).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()) {
                    fav.setImageDrawable(ProfileActivity.this.getResources().getDrawable(R.drawable.red_fav));
                    fav.setTag("unFav");
                }
                else
                {
                    fav.setImageDrawable(ProfileActivity.this.getResources().getDrawable(R.drawable.favorite));
                    fav.setTag("fav");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    public static class ESCCIViewHolder extends RecyclerView.ViewHolder
    {
        RoundedImageView image;
        TextView name, time, location, modes;
        ImageView share, fav, edit;
        CardView container;

        public ESCCIViewHolder(View itemView) {
            super(itemView);

            image = itemView.findViewById(R.id.imageView);
            name = itemView.findViewById(R.id.title);
            time = itemView.findViewById(R.id.dateTime);
            location = itemView.findViewById(R.id.location);
            share = itemView.findViewById(R.id.share);
            fav = itemView.findViewById(R.id.fav);
            modes = itemView.findViewById(R.id.modes);
            edit = itemView.findViewById(R.id.edit);
            container = itemView.findViewById(R.id.container);
        }

        public void setItem(ESCCI obj, String uid)
        {
            name.setText(obj.getName());
            modes.setText(obj.getMode());
            if(obj.getOwnerID().equals(uid) == true)
                edit.setVisibility(View.VISIBLE);
            else
                edit.setVisibility(View.GONE);
            switch(obj.getMode())
            {
                case "Career":
                    String [] h = obj.getDate().split(" ");
                    time.setText(obj.getRandom());
                    location.setText(h[2] + " " + h[1] + " " + h[3]);
                    break;
                case "Competition":
                    String [] g = obj.getDate().split(" ");
                    time.setText(obj.getRandom());
                    location.setText(g[2] + " " + g[1] + " " + g[3]);
                    break;
                case "Events":
                    String[] a = obj.getDate().split(" ");
                    time.setText(a[2] + " " + a[1] + " - " + obj.geteTimeStart() + " GMT +8");
                    location.setText(obj.geteLocation());
                    break;
                case "Internship":
                    String[] b = obj.getStartDate().split(" ");
                    String[] c = obj.getEndDate().split(" ");
                    String[] d = obj.getDate().split(" ");
                    time.setText("Period: " + b[2] + " " + b[1] + " - " + c[2] + " " + c[1]);
                    location.setText(d[2] + " " + d[1] + " " + d[3]);
                    break;
                case "Scholarship":
                    String[] e = obj.getDate().split(" ");
                    String[] f = obj.getRandom().split(" ");
                    String degree=null;
                    if(f.length == 2) degree = f[0] + " & " +f[1];
                    else if(f.length == 3) degree = f[0] + ", " + f[1] + " & " + f[2];
                    else degree = f[0];

                    time.setText(degree);
                    location.setText(e[2] + " " + e[1] + " " + e[3]);
                    break;
            }
        }
    }

    public void openMyCategories(String mode, int count)
    {
        Intent intent = new Intent(this, MyCategoriesActivity.class);
        intent.putExtra("mode", mode);
        intent.putExtra("count", count);
        intent.putExtra("id", uID);
        startActivity(intent);
    }

    public void shareContent(ESCCI escci)
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


    @Override
    public void onClick(View v) {

        if(v == back)
        {
            finish();
        }
        else if(v == add || v== addPosts)
        {
            startActivity(new Intent(this, AddPostActivity.class));
        }
        else if(v == career)
        {
            openMyCategories("Career", count2);
        }
        else if(v == scholarship)
        {
            openMyCategories("Scholarship", count5);
        }
        else if(v == events)
        {
            openMyCategories("Events", count1);
        }
        else if(v == competition)
        {
            openMyCategories("Competition", count3);
        }
        else if(v == internship)
        {
            openMyCategories("Internship", count4);
        }
        else if(v == setting)
        {
            startActivity(new Intent(this, SettingActivity.class));
        }
        else if(v == countFav || v == fav)
        {
            startActivity(new Intent(this, FavouriteActivity.class));
        }
    }
}
