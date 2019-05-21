package com.example.phakneath.jobber;

import android.app.Dialog;
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
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.phakneath.jobber.Model.ESCCI;
import com.example.phakneath.jobber.Model.Users;
import com.example.phakneath.jobber.Model.saveESCCI;
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
import com.makeramen.roundedimageview.RoundedImageView;
import com.shashank.sony.fancydialoglib.Animation;
import com.shashank.sony.fancydialoglib.FancyAlertDialog;
import com.shashank.sony.fancydialoglib.FancyAlertDialogListener;
import com.shashank.sony.fancydialoglib.Icon;
import com.shreyaspatil.firebase.recyclerpagination.DatabasePagingOptions;
import com.shreyaspatil.firebase.recyclerpagination.FirebaseRecyclerPagingAdapter;

import de.hdodenhof.circleimageview.CircleImageView;

public class SearchActivity extends AppCompatActivity implements View.OnClickListener{

    ImageView iv_filter_filter_btn, iv_filter_back, btn_filter_confirm, btn_filter_cancel;
    Dialog dialog;
    View mFilterDialog;
    TextView noPosts;
    EditText search;

    boolean work,intern;
    String uID;
    CardView card_work, card_intern;
    TextView tv_work,tv_intern;
    ImageView iv_filter_work,iv_filter_intern;
    SearchView search_field;
    String controlSearch;

    RecyclerView recyclerView;
    DatabaseReference mDatabase;
    FirebaseRecyclerPagingAdapter mAdapter;
    FirebaseAuth mAuth;
    FirebaseStorage storage;
    StorageReference storageReference,ref;
    FirebaseRecyclerAdapter<ESCCI, ViewHolder> firebaseRecyclerAdapter;
    FirebaseRecyclerAdapter<Users, viewHolderOrg> firebaseRecyclerAdapterOrg;

    Query query;
    PagedList.Config config;
    DatabasePagingOptions<ESCCI> options;
    //String uid;

    int tick = Color.rgb(211,235,255);
    int untick = Color.WHITE;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        work = true;
        intern = false;
        controlSearch = "opp";

        mFilterDialog = getLayoutInflater().inflate(R.layout.filter_dialog,null);
        dialog = new Dialog(this);
        initView();
        mAuth = FirebaseAuth.getInstance();
        uID = mAuth.getUid();

        iv_filter_back.setOnClickListener(this::onClick);
        iv_filter_filter_btn.setOnClickListener(this::onClick);
        btn_filter_confirm.setOnClickListener(this::onClick);
        btn_filter_cancel.setOnClickListener(this::onClick);

        search.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                //Toast.makeText(SearchActivity.this, "yes " + s.toString(), Toast.LENGTH_SHORT).show();
                LinearLayoutManager layoutManager = new LinearLayoutManager(SearchActivity.this, LinearLayoutManager.VERTICAL, false);
                recyclerView.setLayoutManager(layoutManager);
                if(controlSearch.equals("opp")) searchItems("name", "", s.toString());
                else{ searchOrg("username", s.toString()); }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

    }

    //search from firebase
    private void searchItems(String orderBy, String key, String newText)
    {
        if(!newText.isEmpty()) {
            try{
                //Toast.makeText(this, "done", Toast.LENGTH_SHORT).show();
                mDatabase = FirebaseDatabase.getInstance().getReference().child("Posting").child("AllPosts");
                Query query = mDatabase.orderByChild("name").startAt(newText).endAt(newText + "\uf8ff");
                FirebaseRecyclerOptions<ESCCI> options =
                        new FirebaseRecyclerOptions.Builder<ESCCI>()
                                .setQuery(query, ESCCI.class)
                                .setLifecycleOwner(this)
                                .build();

                firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<ESCCI, ViewHolder>(options) {
                    @NonNull
                    @Override
                    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                        View view = LayoutInflater.from(SearchActivity.this).inflate(R.layout.items_showing_layout, parent, false);
                        return new ViewHolder(view);
                    }

                    @Override
                    protected void onBindViewHolder(@NonNull ViewHolder viewHolder, int position, @NonNull ESCCI model) {

                        viewHolder.setItem(model, uID);

                        getImage(viewHolder.image, model.getImage(), SearchActivity.this, "posting/");

                        viewHolder.fav.setTag("fav");

                        viewHolder.edit.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                PopupMenu menu = new PopupMenu(SearchActivity.this, v);
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

                                                new FancyAlertDialog.Builder(SearchActivity.this)
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
                                Intent intent = new Intent(SearchActivity.this, DetailActivity.class);
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
                                    viewHolder.fav.setImageDrawable(SearchActivity.this.getResources().getDrawable(R.drawable.red_fav));
                                    v.setTag("unFav");
                                    saveFavourite(model.getId(), model.getOwnerID());
                                }
                                else
                                {
                                    viewHolder.fav.setImageDrawable(SearchActivity.this.getResources().getDrawable(R.drawable.favorite));
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
                };

                recyclerView.setAdapter(firebaseRecyclerAdapter);
                /*Toast.makeText(this, ""+firebaseRecyclerAdapter.getItem(0).getId(), Toast.LENGTH_SHORT).show();
                if(firebaseRecyclerAdapter.getItem(0).getId().equals(null))
                {
                    noPosts.setVisibility(View.VISIBLE);
                    recyclerView.setVisibility(View.GONE);
                }
                else {
                    noPosts.setVisibility(View.GONE);
                    recyclerView.setVisibility(View.VISIBLE);
                }*/

            }catch(Exception e) {
                Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
        else {
            recyclerView.setAdapter(null);
        }
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        RoundedImageView image;
        TextView name, time, location, modes;
        ImageView share, fav, edit;
        CardView container;

        public ViewHolder(View itemView) {
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

        public void setItem(ESCCI obj, String uid) {
            name.setText(obj.getName());
            modes.setText(obj.getMode());
            if (obj.getOwnerID().equals(uid) == true)
                edit.setVisibility(View.VISIBLE);
            else
                edit.setVisibility(View.GONE);
            switch (obj.getMode()) {
                case "Career":
                    String[] h = obj.getDate().split(" ");
                    time.setText(obj.getRandom());
                    location.setText(h[2] + " " + h[1] + " " + h[3]);
                    break;
                case "Competition":
                    String[] g = obj.getDate().split(" ");
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
                    String degree = null;
                    if (f.length == 2) degree = f[0] + " & " + f[1];
                    else if (f.length == 3) degree = f[0] + ", " + f[1] + " & " + f[2];
                    else degree = f[0];

                    time.setText(degree);
                    location.setText(e[2] + " " + e[1] + " " + e[3]);
                    break;
            }
        }

    }

    private void searchOrg(String orderBy, String newText)
    {
        if(!newText.isEmpty()) {
            try{
                //Toast.makeText(this, "done", Toast.LENGTH_SHORT).show();
                mDatabase = FirebaseDatabase.getInstance().getReference().child("Users");
                Query query = mDatabase.orderByChild("username").startAt(newText).endAt(newText + "\uf8ff");
                FirebaseRecyclerOptions<Users> options =
                        new FirebaseRecyclerOptions.Builder<Users>()
                                .setQuery(query, Users.class)
                                .setLifecycleOwner(this)
                                .build();

                firebaseRecyclerAdapterOrg = new FirebaseRecyclerAdapter<Users, viewHolderOrg>(options) {
                    @NonNull
                    @Override
                    public viewHolderOrg onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                        View view = LayoutInflater.from(SearchActivity.this).inflate(R.layout.org_showing_layout, parent, false);
                        return new viewHolderOrg(view);
                    }

                    @Override
                    protected void onBindViewHolder(@NonNull viewHolderOrg holder, int position, @NonNull Users model) {
                        holder.setItem(model);
                        if(model.getImage() != null) getImage(holder.image, model.getImage(), SearchActivity.this, "profile/");
                        holder.container.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                if(uID.equals(model.getId())){
                                    Intent intent = new Intent(SearchActivity.this, ProfileActivity.class);
                                    startActivity(intent);
                                }
                                else {
                                    Intent intent = new Intent(SearchActivity.this, OtherProfileActivity.class);
                                    Bundle bundle = new Bundle();
                                    bundle.putSerializable("id", model.getId());
                                    intent.putExtras(bundle);
                                    startActivity(intent);
                                }
                            }
                        });
                    }
                };

                recyclerView.setAdapter(firebaseRecyclerAdapterOrg);
                /*Toast.makeText(this, ""+firebaseRecyclerAdapter.getItem(0).getId(), Toast.LENGTH_SHORT).show();
                if(firebaseRecyclerAdapter.getItem(0).getId().equals(null))
                {
                    noPosts.setVisibility(View.VISIBLE);
                    recyclerView.setVisibility(View.GONE);
                }
                else {
                    noPosts.setVisibility(View.GONE);
                    recyclerView.setVisibility(View.VISIBLE);
                }*/

            }catch(Exception e) {
                Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
        else {
            recyclerView.setAdapter(null);
        }
    }

    public static class viewHolderOrg extends RecyclerView.ViewHolder {
        CircleImageView image;
        TextView name, position, nation;
        CardView container;

        public viewHolderOrg(View itemView) {
            super(itemView);

            image = itemView.findViewById(R.id.image);
            name = itemView.findViewById(R.id.username);
            position = itemView.findViewById(R.id.position);
            container = itemView.findViewById(R.id.container);
            nation = itemView.findViewById(R.id.nation);
        }

        public void setItem(Users obj) {
            name.setText(obj.getUsername());
            if(obj.getPosition() != null) position.setText(obj.getPosition());
            if(obj.getNationality() != null) nation.setText(obj.getNationality());
        }

    }

    @Override
    protected void onStart() {
        super.onStart();
        if(firebaseRecyclerAdapter != null) {
            firebaseRecyclerAdapter.startListening();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if(firebaseRecyclerAdapter != null) firebaseRecyclerAdapter.stopListening();
    }

    private void initView()
    {
        iv_filter_back = findViewById(R.id.label);
        iv_filter_filter_btn = findViewById(R.id.filter);
        btn_filter_cancel = mFilterDialog.findViewById(R.id.btn_filter_cancel);
        btn_filter_confirm = mFilterDialog.findViewById(R.id.btn_filter_confirm);

        card_work = mFilterDialog.findViewById(R.id.card_work);
        card_intern = mFilterDialog.findViewById(R.id.card_intern);

        iv_filter_intern = mFilterDialog.findViewById(R.id.iv_filter_internship);
        iv_filter_work = mFilterDialog.findViewById(R.id.iv_filter_work);

        tv_work = mFilterDialog.findViewById(R.id.tv_filter_work);
        tv_intern = mFilterDialog.findViewById(R.id.tv_filter_internship);

        search_field = mFilterDialog.findViewById(R.id.searchView);
        noPosts = findViewById(R.id.noPosts);
        recyclerView = findViewById(R.id.recyclerView);
        search = findViewById(R.id.input_search);
    }

    public void chooseFilter(){

        iv_filter_work.setOnClickListener(this::onClick);
        card_work.setOnClickListener(this::onClick);
        tv_work.setOnClickListener(this::onClick);

        iv_filter_intern.setOnClickListener(this::onClick);
        card_intern.setOnClickListener(this::onClick);
        tv_intern.setOnClickListener(this::onClick);
        checkFilter();
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

                mDatabase.child("Users").child(uID).child("randomThings").child(id).removeValue();
                mDatabase.child("Posting").child("AllPosts").child(id).removeValue();

                deleteImage(escci);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(SearchActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
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
                    Toast.makeText(SearchActivity.this, "Delete Successful", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }


    public void editPost(ESCCI escci)
    {
        Intent intent = new Intent(SearchActivity.this, EditPostsActivity.class);
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
                Toast.makeText(SearchActivity.this, "Saved", Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void unSaveFavourite(String id)
    {
        mDatabase = FirebaseDatabase.getInstance().getReference();
        mDatabase.child("Users").child(uID).child("favourite").child(id).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                Toast.makeText(SearchActivity.this, "Unsaved", Toast.LENGTH_SHORT).show();
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
                    fav.setImageDrawable(SearchActivity.this.getResources().getDrawable(R.drawable.red_fav));
                    fav.setTag("unFav");
                }
                else
                {
                    fav.setImageDrawable(SearchActivity.this.getResources().getDrawable(R.drawable.favorite));
                    fav.setTag("fav");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    void checkFilter()
    {
        if(card_work.getCardBackgroundColor().getDefaultColor()==untick)
        {
            card_intern.setCardBackgroundColor(tick);
        }
        else
        {
            card_work.setCardBackgroundColor(tick);
        }

    }

    void setBooleanValue()
    {
        if(card_intern.getCardBackgroundColor().getDefaultColor()==untick)
        {
            intern = false;
        }
        else
        {
            intern = true;
        }
        if (card_work.getCardBackgroundColor().getDefaultColor()==untick)
        {
            work = false;
        }
        else {
            work = true;
        }
    }

    @Override
    public void onClick(View v) {
        if(v == iv_filter_filter_btn){
            dialog.setContentView(mFilterDialog);
            dialog.show();
            chooseFilter();
        }

        if(v== iv_filter_back){
            Intent intent = new Intent(SearchActivity.this, MainActivity.class);
            startActivity(intent);
        }

        if(v==btn_filter_cancel){
            dialog.dismiss();
        }

        if (v==btn_filter_confirm){
            //set boolean value according to cardbackgroundcolor
            recyclerView.setAdapter(null);
            search.setText("");
            setBooleanValue();
            if(card_work.getCardBackgroundColor().getDefaultColor() == tick)
            {
                controlSearch = "opp";
            }
            else
            {
                controlSearch = "org";
            }
            dialog.dismiss();
        }

        if(v==iv_filter_work||v==card_work||v==tv_work)
        {
            int backgroundColor = card_work.getCardBackgroundColor().getDefaultColor();
            if(backgroundColor==untick)
            {
                card_work.setCardBackgroundColor(tick);
                card_intern.setCardBackgroundColor(untick);
            }
            else {
                card_work.setCardBackgroundColor(untick);
                checkFilter();
            }
        }
        if(v==iv_filter_intern||v==card_intern||v==tv_intern)
        {
            int backgroundColor = card_intern.getCardBackgroundColor().getDefaultColor();
            if(backgroundColor==untick)
            {
                card_intern.setCardBackgroundColor(tick);
                card_work.setCardBackgroundColor(untick);
            }
            else
            {
                card_intern.setCardBackgroundColor(untick);
                checkFilter();
            }
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


