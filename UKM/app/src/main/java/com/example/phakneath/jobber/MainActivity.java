package com.example.phakneath.jobber;

import android.arch.paging.PagedList;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.phakneath.jobber.Dialog.LoadingDialog;
import com.example.phakneath.jobber.Model.ESCCI;
import com.example.phakneath.jobber.Model.saveESCCI;
import com.example.phakneath.jobber.sharePreferences.UserPreferences;
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

import de.hdodenhof.circleimageview.CircleImageView;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, NavigationView.OnNavigationItemSelectedListener{

    ImageView menu,search;
    DrawerLayout drawerLayout;
    NavigationView navigationView;
    //SwipeRefreshLayout swipeRefreshLayout;
    RecyclerView recyclerView;
    DatabaseReference mDatabase;
    FirebaseRecyclerPagingAdapter mAdapter;
    FirebaseAuth mAuth;
    FirebaseStorage storage;
    StorageReference storageReference,ref;
    ShimmerFrameLayout shimmer;
    LinearLayoutManager mManager;

    PagedList.Config config;
    DatabasePagingOptions<ESCCI> options;
    Query query;

    String uid;
    LoadingDialog dialog;
    CircleImageView profile;
    TextView username;
    TextView viewProfile, noPosts;

    CardView cat_work_card,cat_intern_card,cat_compete_card,cat_event_card, cat_scholar_card;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initView();
        dialog = new LoadingDialog();
        mAuth = FirebaseAuth.getInstance();
        uid = mAuth.getCurrentUser().getUid();

        menu.setOnClickListener(this::onClick);
        search.setOnClickListener(this::onClick);
        navigationView.setNavigationItemSelectedListener(this::onNavigationItemSelected);

        cat_event_card.setOnClickListener(this::onClick);
        cat_intern_card.setOnClickListener(this::onClick);
        cat_scholar_card.setOnClickListener(this::onClick);
        cat_work_card.setOnClickListener(this::onClick);
        cat_compete_card.setOnClickListener(this::onClick);

        mDatabase = FirebaseDatabase.getInstance().getReference().child("Posting");
        query = mDatabase.orderByChild("postingTime");

        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.hasChildren())
                {
                    noPosts.setVisibility(View.GONE);
                    setPageList();
                    setPagination();

                    mAdapter = new FirebaseRecyclerPagingAdapter<ESCCI, ESCCIViewHolder>(options) {

                        @NonNull
                        @Override
                        public ESCCIViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                            return new ESCCIViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.items_showing_layout, parent,false));
                        }

                        @Override
                        protected void onBindViewHolder(@NonNull ESCCIViewHolder viewHolder, int position, @NonNull ESCCI model) {
                            viewHolder.setItem(model, uid);

                            getImage(viewHolder.image, model.getImage(), MainActivity.this, "posting/");

                            viewHolder.fav.setTag("fav");

                            viewHolder.edit.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    PopupMenu menu = new PopupMenu(MainActivity.this, v);
                                    menu.inflate(R.menu.more_menu);
                                    menu.setGravity(Gravity.RIGHT);
                                    menu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                                        @Override
                                        public boolean onMenuItemClick(MenuItem item) {
                                            switch (item.getItemId()) {
                                                case R.id.edit:
                                                    editPost(model);
                                                    return true;

                                                case R.id.delete:

                                                    new FancyAlertDialog.Builder(MainActivity.this)
                                                            .setTitle("Confirmation")
                                                            .setBackgroundColor(Color.parseColor("#292E43"))  //Don't pass R.color.colorvalue
                                                            .setMessage("Are you sure you want to delete this item?")
                                                            .setPositiveBtnBackground(Color.parseColor("#2196F3"))  //Don't pass R.color.colorvalue
                                                            .setPositiveBtnText("Yes")
                                                            .setNegativeBtnText("Cancel")
                                                            .setAnimation(Animation.SIDE)
                                                            .isCancellable(false)
                                                            .setIcon(R.drawable.infos_full, Icon.Visible)
                                                            .OnPositiveClicked(new FancyAlertDialogListener() {
                                                                @Override
                                                                public void OnClick() {
                                                                    //swipeRefreshLayout.setRefreshing(true);
                                                                    shimmer.startShimmerAnimation();
                                                                    shimmer.setVisibility(View.VISIBLE);
                                                                    recyclerView.setVisibility(View.GONE);
                                                                    delete(model);
                                                                    mAdapter.notifyItemRemoved(position);

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
                                    Intent intent = new Intent(MainActivity.this, DetailActivity.class);
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
                                        viewHolder.fav.setImageDrawable(MainActivity.this.getResources().getDrawable(R.drawable.red_fav));
                                        v.setTag("unFav");
                                        saveFavourite(model.getId(),model.getOwnerID(), System.currentTimeMillis());
                                    }
                                    else
                                    {
                                        viewHolder.fav.setImageDrawable(MainActivity.this.getResources().getDrawable(R.drawable.favorite));
                                        v.setTag("fav");
                                        unSaveFavourite(model.getId());
                                    }
                                }
                            });

                            getSaveFavourite(model.getId(), viewHolder.fav);
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
                                    shimmer.startShimmerAnimation();
                                    shimmer.setVisibility(View.GONE);
                                    recyclerView.setVisibility(View.VISIBLE);
                                    break;

                                case FINISHED:
                                    //Reached end of Data set
                                    //swipeRefreshLayout.setRefreshing(false);
                                    shimmer.startShimmerAnimation();
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
                            shimmer.startShimmerAnimation();
                            shimmer.setVisibility(View.GONE);
                            recyclerView.setVisibility(View.VISIBLE);
                            databaseError.toException().printStackTrace();
                            retry();
                        }

                    };
                    mManager = new LinearLayoutManager(MainActivity.this);
                    recyclerView.setHasFixedSize(true);
                    recyclerView.setLayoutManager(mManager);
                    recyclerView.setAdapter(mAdapter);
                }
                else noPosts.setVisibility(View.VISIBLE);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


        /*swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                mAdapter.refresh();
            }
        });*/

        viewProfile.setOnClickListener(this::onClick);
        profile.setOnClickListener(this::onClick);

        getUser();
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

    public void initView()
    {
        menu = findViewById(R.id.menu);
        search = findViewById(R.id.searchView);
        drawerLayout = findViewById(R.id.drawerLayout);
        navigationView = findViewById(R.id.nav);
        //swipeRefreshLayout = findViewById(R.id.swipeRefresh);
        recyclerView = findViewById(R.id.mRecyclerView);

        View headerView = navigationView.getHeaderView(0);
        profile = headerView.findViewById(R.id.users);
        username = headerView.findViewById(R.id.username);
        viewProfile = headerView.findViewById(R.id.views);
        shimmer = findViewById(R.id.shimmer);
        noPosts = findViewById(R.id.noPosts);

        cat_compete_card = findViewById(R.id.main_compete_card);
        cat_event_card = findViewById(R.id.main_event_card);
        cat_intern_card = findViewById(R.id.main_intern_card);
        cat_scholar_card =findViewById(R.id.main_scholar_card);
        cat_work_card = findViewById(R.id.main_career_card);
    }


    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        switch(id)
        {
            case R.id.home: drawerLayout.closeDrawer(Gravity.LEFT);
                break;
            case R.id.fav: startActivity(new Intent(this, FavouriteActivity.class));
                break;
            case R.id.add:
                drawerLayout.closeDrawer(Gravity.LEFT);
                startActivity(new Intent(this, AddPostActivity.class));

                break;
            case R.id.about: //startActivity(new Intent(this, SaveActivity.class));
                break;
            case R.id.privacy: //startActivity(new Intent(this, PrivacyPolicyActivity.class));
                break;
            case R.id.logout: drawerLayout.closeDrawer(Gravity.LEFT); logoutDialog(); signout();
                break;
        }
        return true;
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

    public void delete(ESCCI escci)
    {
        String id = escci.getId();
        mDatabase = FirebaseDatabase.getInstance().getReference();
        mDatabase.child("Posting").child(id).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {

                mDatabase.child("Users").child(uid).child("randomThings").child(id).removeValue();

                deleteImage(escci);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
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
                    Toast.makeText(MainActivity.this, "Delete Successful", Toast.LENGTH_SHORT).show();
                    shimmer.startShimmerAnimation();
                    shimmer.setVisibility(View.GONE);
                    recyclerView.setVisibility(View.VISIBLE);
                    //swipeRefreshLayout.setRefreshing(false);
                    mAdapter.refresh();
                }
            });
        }
    }


    public void editPost(ESCCI escci)
    {
        Intent intent = new Intent(MainActivity.this, EditPostsActivity.class);
        Bundle bundle = new Bundle();
        bundle.putSerializable("posts", escci);
        intent.putExtras(bundle);
        startActivity(intent);

    }

    public void signout()
    {
        mAuth.signOut();
        UserPreferences.remove(this);
        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                startActivity(intent);
                dialog.dismiss();
                finish();
            }
        }, 3000);
    }

    public void logoutDialog()
    {
        dialog.show(getFragmentManager(), "dialogLoading");

    }

    public void saveFavourite(String id, String ownerID, long saveTime)
    {
        saveESCCI s = new saveESCCI(id, ownerID, saveTime);

        mDatabase = FirebaseDatabase.getInstance().getReference();
        mDatabase.child("Users").child(uid).child("favourite").child(id).setValue(s).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                Toast.makeText(MainActivity.this, "Saved", Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void unSaveFavourite(String id)
    {
        mDatabase = FirebaseDatabase.getInstance().getReference();
        mDatabase.child("Users").child(uid).child("favourite").child(id).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                Toast.makeText(MainActivity.this, "Unsaved", Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void getSaveFavourite(String id, ImageView fav)
    {
        mDatabase = FirebaseDatabase.getInstance().getReference();
        mDatabase.child("Users").child(uid).child("favourite").child(id).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()) {
                    fav.setImageDrawable(MainActivity.this.getResources().getDrawable(R.drawable.red_fav));
                    fav.setTag("unFav");
                }
                else
                {
                    fav.setImageDrawable(MainActivity.this.getResources().getDrawable(R.drawable.favorite));
                    fav.setTag("fav");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    public void getUser()
    {
        mDatabase = FirebaseDatabase.getInstance().getReference();
        mDatabase.child("Users").child(uid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                String name = dataSnapshot.child("username").getValue(String.class);
                String image = dataSnapshot.child("image").getValue(String.class);
                username.setText(name);
                if(image != null) getImage(profile, image, MainActivity.this, "profile/");
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    @Override
    public void onClick(View v) {
        if(v == menu)
        {
            drawerLayout.openDrawer(Gravity.LEFT);
        }
        if(v == profile || v == viewProfile)
        {
            drawerLayout.closeDrawer(Gravity.LEFT);
            startActivity(new Intent(this, ProfileActivity.class));
        }
        if(v==search)
        {
            Intent intent = new Intent(MainActivity.this, SearchActivity.class);
            startActivity(intent);
        }

        if(v==cat_compete_card||v==cat_work_card||v==cat_scholar_card||v==cat_intern_card||v==cat_event_card)
        {
            String category="";
            if(v==cat_compete_card) category = "Competitions";
            if(v==cat_event_card) category = "Events";
            if(v==cat_intern_card) category = "Internships";
            if(v==cat_scholar_card) category = "Scholarhips";
            if(v==cat_work_card) category = "Work";
            Bundle catBundle = new Bundle();
            catBundle.putString("Category", category);

            Intent intent = new Intent(MainActivity.this, CategoryActivity.class);
            intent.putExtras(catBundle);
            startActivity(intent);
        }

    }

}
