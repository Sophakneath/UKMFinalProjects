package com.example.phakneath.jobber;

import android.arch.paging.PagedList;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
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

public class FavouriteActivity extends AppCompatActivity implements View.OnClickListener{

    ImageView back;
    //SwipeRefreshLayout swipeRefreshLayout;
    RecyclerView recyclerView;
    DatabaseReference mDatabase;
    FirebaseRecyclerPagingAdapter mAdapter;
    FirebaseAuth mAuth;
    FirebaseStorage storage;
    StorageReference storageReference,ref;
    ShimmerFrameLayout shimmer;
    TextView noPost, countPosts;

    PagedList.Config config;
    DatabasePagingOptions<ESCCI> options;
    Query query;

    List<saveESCCI> a;

    String uid;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favourite);

        initView();
        mAuth = FirebaseAuth.getInstance();
        uid = mAuth.getCurrentUser().getUid();

        back.setOnClickListener(this::onClick);

        shimmer.startShimmerAnimation();
        getDataFavouriteFromServer(uid);
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
        if(mAdapter != null)
            mAdapter.stopListening();
    }
    public void initView()
    {
        back = findViewById(R.id.back);
        //swipeRefreshLayout = findViewById(R.id.swipeRefresh);
        recyclerView = findViewById(R.id.mRecyclerView);
        shimmer = findViewById(R.id.shimmer);
        noPost = findViewById(R.id.noPosts);
        countPosts = findViewById(R.id.countPosts);
    }

    @Override
    public void onClick(View v) {
        if(v == back)
        {
            finish();
        }
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

    public void setPagination(Query query)
    {
        options = new DatabasePagingOptions.Builder<ESCCI>()
                .setLifecycleOwner(this)
                .setQuery(query, config, ESCCI.class)
                .build();
    }

    public void getImage(ImageView img, String getImage, Context context)
    {
        storage = FirebaseStorage.getInstance();
        StorageReference ref = storage.getReference().child("posting/" + getImage);
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
                Toast.makeText(FavouriteActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
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
                    Toast.makeText(FavouriteActivity.this, "Delete Successful", Toast.LENGTH_SHORT).show();
                    shimmer.stopShimmerAnimation();
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
        Intent intent = new Intent(FavouriteActivity.this, EditPostsActivity.class);
        Bundle bundle = new Bundle();
        bundle.putSerializable("posts", escci);
        intent.putExtras(bundle);
        startActivity(intent);

    }

    public void saveFavourite(String id, String ownerID, long saveTime)
    {
        saveESCCI s = new saveESCCI(id, ownerID, saveTime);

        mDatabase = FirebaseDatabase.getInstance().getReference();
        mDatabase.child("Users").child(uid).child("favourite").child(id).setValue(s).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                Toast.makeText(FavouriteActivity.this, "Saved", Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void unSaveFavourite(String id)
    {
        mDatabase = FirebaseDatabase.getInstance().getReference();
        mDatabase.child("Users").child(uid).child("favourite").child(id).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                Toast.makeText(FavouriteActivity.this, "Unsaved", Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void getDataFavouriteFromServer(String id)
    {
        mDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child(id).child("favourite");
        query = mDatabase.orderByChild("postingTime");
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.hasChildren())
                {
                    countPosts.setText(dataSnapshot.getChildrenCount()+ " Posts");
                    noPost.setVisibility(View.GONE);
                    setPageList();
                    setPagination(query);
                    mAdapter = new FirebaseRecyclerPagingAdapter<ESCCI, FavouriteActivity.ESCCIViewHolder>(options) {

                        @NonNull
                        @Override
                        public FavouriteActivity.ESCCIViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                            return new FavouriteActivity.ESCCIViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.items_showing_layout, parent,false));
                        }

                        @Override
                        protected void onBindViewHolder(@NonNull FavouriteActivity.ESCCIViewHolder viewHolder, int position, @NonNull ESCCI model) {
                            viewHolder.setItem(model, uid);

                            getImage(viewHolder.image, model.getImage(), FavouriteActivity.this);

                            viewHolder.fav.setImageDrawable(FavouriteActivity.this.getResources().getDrawable(R.drawable.red_fav));
                            viewHolder.fav.setTag("unFav");

                            viewHolder.edit.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    PopupMenu menu = new PopupMenu(FavouriteActivity.this, v);
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

                                                    new FancyAlertDialog.Builder(FavouriteActivity.this)
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
                                                                    shimmer.startShimmerAnimation();
                                                                    shimmer.setVisibility(View.VISIBLE);
                                                                    recyclerView.setVisibility(View.GONE);
                                                                    //swipeRefreshLayout.setRefreshing(true);
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
                                    Intent intent = new Intent(FavouriteActivity.this, DetailActivity.class);
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
                                        viewHolder.fav.setImageDrawable(FavouriteActivity.this.getResources().getDrawable(R.drawable.red_fav));
                                        v.setTag("unFav");
                                        saveFavourite(model.getId(),model.getOwnerID(), System.currentTimeMillis());
                                    }
                                    else
                                    {
                                        viewHolder.fav.setImageDrawable(FavouriteActivity.this.getResources().getDrawable(R.drawable.favorite));
                                        v.setTag("fav");
                                        unSaveFavourite(model.getId());
                                    }
                                }
                            });
                        }

                        @Override
                        protected void onLoadingStateChanged(@NonNull LoadingState state) {
                            switch (state) {
                                case LOADING_INITIAL:
                                case LOADING_MORE:
                                    // Do your loading animation
                                    shimmer.startShimmerAnimation();
                                    shimmer.setVisibility(View.VISIBLE);
                                    recyclerView.setVisibility(View.GONE);
                                    //swipeRefreshLayout.setRefreshing(true);
                                    break;

                                case LOADED:
                                    // Stop Animation
                                    shimmer.stopShimmerAnimation();
                                    shimmer.setVisibility(View.GONE);
                                    recyclerView.setVisibility(View.VISIBLE);
                                    //swipeRefreshLayout.setRefreshing(false);
                                    break;

                                case FINISHED:
                                    //Reached end of Data set
                                    shimmer.stopShimmerAnimation();
                                    shimmer.setVisibility(View.GONE);
                                    recyclerView.setVisibility(View.VISIBLE);
                                    //swipeRefreshLayout.setRefreshing(false);
                                    break;

                                case ERROR:
                                    retry();
                                    break;
                            }
                        }

                        @Override
                        protected void onError(@NonNull DatabaseError databaseError) {
                            shimmer.stopShimmerAnimation();
                            shimmer.setVisibility(View.GONE);
                            recyclerView.setVisibility(View.VISIBLE);
                            //swipeRefreshLayout.setRefreshing(false);
                            databaseError.toException().printStackTrace();
                            retry();
                        }

                    };

                    LinearLayoutManager mManager = new LinearLayoutManager(FavouriteActivity.this);
                    recyclerView.setHasFixedSize(true);
                    recyclerView.setLayoutManager(mManager);
                    recyclerView.setAdapter(mAdapter);
                }
                else
                {
                    noPost.setVisibility(View.VISIBLE);
                    shimmer.stopShimmerAnimation();
                    shimmer.setVisibility(View.GONE);
                }
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
    }
}
