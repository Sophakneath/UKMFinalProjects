package com.example.phakneath.jobber;

import android.arch.paging.PagedList;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
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
import com.example.phakneath.jobber.Model.ESCCI;
import com.example.phakneath.jobber.Model.saveESCCI;
import com.facebook.shimmer.ShimmerFrameLayout;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
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
import com.shashank.sony.fancydialoglib.Animation;
import com.shashank.sony.fancydialoglib.FancyAlertDialog;
import com.shashank.sony.fancydialoglib.FancyAlertDialogListener;
import com.shashank.sony.fancydialoglib.Icon;
import com.shreyaspatil.firebase.recyclerpagination.DatabasePagingOptions;
import com.shreyaspatil.firebase.recyclerpagination.FirebaseRecyclerPagingAdapter;
import com.shreyaspatil.firebase.recyclerpagination.LoadingState;

public class CategoryActivity extends AppCompatActivity {

    TextView tv_category, noPosts, search;
    ImageView back;
    Intent intent;
    Bundle bundle;

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

    String uid, cat;
    FirebaseRecyclerAdapter<ESCCI, SearchActivity.ViewHolder> firebaseRecyclerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_category);

        mAuth = FirebaseAuth.getInstance();
        uid = mAuth.getUid();
        intent = getIntent();
        bundle = intent.getExtras();
        initView();
        cat = bundle.getString("Category");

        tv_category.setText(cat);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent back = new Intent(CategoryActivity.this, MainActivity.class);
                startActivity(back);
            }
        });

        shimmer.startShimmerAnimation();

        mManager = new LinearLayoutManager(CategoryActivity.this, LinearLayoutManager.VERTICAL, true);
        mManager.setStackFromEnd(true);
        recyclerView = findViewById(R.id.mRecycleView);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(mManager);

        if(TextUtils.isEmpty(search.getText().toString())) getData(cat);

        search.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(s.toString().equals("") || s.toString().equals(null))
                {
                    //Toast.makeText(CategoryActivity.this, "getData", Toast.LENGTH_SHORT).show();
                    recyclerView.setAdapter(null);
                    getData(cat);
                }
                else
                {
                    //Toast.makeText(CategoryActivity.this, "SearchItems", Toast.LENGTH_SHORT).show();
                    recyclerView.setAdapter(null);
                    searchItems(cat, s.toString());
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }
    private void initView()
    {
        tv_category = findViewById(R.id.tv_category);
        back = findViewById(R.id.cat_back_btn);
        recyclerView = findViewById(R.id.mRecycleView);
        shimmer = findViewById(R.id.shimmer);
        noPosts = findViewById(R.id.noPosts);
        search = findViewById(R.id.input_search);
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
                Toast.makeText(CategoryActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
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
                    Toast.makeText(CategoryActivity.this, "Delete Successful", Toast.LENGTH_SHORT).show();
                    mAdapter=null;
                    recyclerView.setAdapter(mAdapter);
                    shimmer.startShimmerAnimation();
                    getData(cat);
                }
            });
        }
    }


    public void editPost(ESCCI escci)
    {
        Intent intent = new Intent(CategoryActivity.this, EditPostsActivity.class);
        Bundle bundle = new Bundle();
        bundle.putSerializable("posts", escci);
        intent.putExtras(bundle);
        startActivity(intent);

    }

    public void saveFavourite(String id, String ownerID)
    {
        saveESCCI s = new saveESCCI(id, ownerID);

        mDatabase = FirebaseDatabase.getInstance().getReference();
        mDatabase.child("Users").child(uid).child("favourite").child(id).setValue(s).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                Toast.makeText(CategoryActivity.this, "Saved", Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void unSaveFavourite(String id)
    {
        mDatabase = FirebaseDatabase.getInstance().getReference();
        mDatabase.child("Users").child(uid).child("favourite").child(id).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                Toast.makeText(CategoryActivity.this, "Unsaved", Toast.LENGTH_SHORT).show();
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
                    fav.setImageDrawable(CategoryActivity.this.getResources().getDrawable(R.drawable.red_fav));
                    fav.setTag("unFav");
                }
                else
                {
                    fav.setImageDrawable(CategoryActivity.this.getResources().getDrawable(R.drawable.favorite));
                    fav.setTag("fav");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void getData(String cat)
    {
        mDatabase = FirebaseDatabase.getInstance().getReference().child("Posting");
        query = mDatabase.child(cat);

        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.hasChildren())
                {
                    noPosts.setVisibility(View.GONE);
                    setPageList();
                    setPagination();

                    mAdapter = new FirebaseRecyclerPagingAdapter<ESCCI, MainActivity.ESCCIViewHolder>(options) {

                        @NonNull
                        @Override
                        public MainActivity.ESCCIViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                            return new MainActivity.ESCCIViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.items_showing_layout, parent,false));
                        }

                        @Override
                        protected void onBindViewHolder(@NonNull MainActivity.ESCCIViewHolder viewHolder, int position, @NonNull ESCCI model) {
                            viewHolder.setItem(model, uid);

                            getImage(viewHolder.image, model.getImage(), CategoryActivity.this, "posting/");

                            viewHolder.fav.setTag("fav");

                            viewHolder.edit.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    PopupMenu menu = new PopupMenu(CategoryActivity.this, v, Gravity.RIGHT);
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

                                                    new FancyAlertDialog.Builder(CategoryActivity.this)
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
                                                                    //swipeRefreshLayout.setRefreshing(true);
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
                                    Intent intent = new Intent(CategoryActivity.this, DetailActivity.class);
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
                                        viewHolder.fav.setImageDrawable(CategoryActivity.this.getResources().getDrawable(R.drawable.red_fav));
                                        v.setTag("unFav");
                                        saveFavourite(model.getId(), model.getOwnerID());
                                    }
                                    else
                                    {
                                        viewHolder.fav.setImageDrawable(CategoryActivity.this.getResources().getDrawable(R.drawable.favorite));
                                        v.setTag("fav");
                                        unSaveFavourite(model.getId());
                                    }
                                }
                            });

                            getSaveFavourite(model.getId(), viewHolder.fav);

                            viewHolder.share.setOnClickListener(new View.OnClickListener() {
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
                    if(mAdapter == null) recyclerView.setAdapter(null);
                    else {
                        recyclerView.setAdapter(mAdapter);
                    }
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

    //search from firebase
    private void searchItems(String key, String newText)
    {
        //Toast.makeText(this, key, Toast.LENGTH_SHORT).show();
        if(!newText.isEmpty()) {
            try{
                //Toast.makeText(this, "done", Toast.LENGTH_SHORT).show();
                mDatabase = FirebaseDatabase.getInstance().getReference().child("Posting").child(key);
                Query query = mDatabase.orderByChild("name").startAt(newText).endAt(newText + "\uf8ff");
                FirebaseRecyclerOptions<ESCCI> options =
                        new FirebaseRecyclerOptions.Builder<ESCCI>()
                                .setQuery(query, ESCCI.class)
                                .setLifecycleOwner(this)
                                .build();

                firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<ESCCI, SearchActivity.ViewHolder>(options) {
                    @NonNull
                    @Override
                    public SearchActivity.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                        View view = LayoutInflater.from(CategoryActivity.this).inflate(R.layout.items_showing_layout, parent, false);
                        return new SearchActivity.ViewHolder(view);
                    }

                    @Override
                    protected void onBindViewHolder(@NonNull SearchActivity.ViewHolder viewHolder, int position, @NonNull ESCCI model) {

                        viewHolder.setItem(model, uid);

                        getImage(viewHolder.image, model.getImage(), CategoryActivity.this, "posting/");

                        viewHolder.fav.setTag("fav");

                        viewHolder.edit.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                PopupMenu menu = new PopupMenu(CategoryActivity.this, v);
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

                                                new FancyAlertDialog.Builder(CategoryActivity.this)
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
                                Intent intent = new Intent(CategoryActivity.this, DetailActivity.class);
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
                                    viewHolder.fav.setImageDrawable(CategoryActivity.this.getResources().getDrawable(R.drawable.red_fav));
                                    v.setTag("unFav");
                                    saveFavourite(model.getId(), model.getOwnerID());
                                }
                                else
                                {
                                    viewHolder.fav.setImageDrawable(CategoryActivity.this.getResources().getDrawable(R.drawable.favorite));
                                    v.setTag("fav");
                                    unSaveFavourite(model.getId());
                                }
                            }
                        });

                        getSaveFavourite(model.getId(), viewHolder.fav);
                    }
                };

                if(firebaseRecyclerAdapter == null) recyclerView.setAdapter(null);
                else recyclerView.setAdapter(firebaseRecyclerAdapter);

            }catch(Exception e) {
                Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
        else {
            recyclerView.setAdapter(null);
        }
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

}
