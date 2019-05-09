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
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.phakneath.jobber.Model.ESCCI;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.makeramen.roundedimageview.RoundedImageView;
import com.shreyaspatil.firebase.recyclerpagination.DatabasePagingOptions;
import com.shreyaspatil.firebase.recyclerpagination.FirebaseRecyclerPagingAdapter;
import com.shreyaspatil.firebase.recyclerpagination.LoadingState;

public class SearchActivity extends AppCompatActivity implements View.OnClickListener{

    ImageView iv_filter_filter_btn, iv_filter_back, btn_filter_confirm, btn_filter_cancel;
    Dialog dialog;
    View mFilterDialog;

    boolean work, event,scholar,intern,all, compete;
    CardView card_all, card_event, card_scholar, card_work, card_intern, card_compete;
    TextView tv_work,tv_intern,tv_scholar,tv_compete,tv_event,tv_all;
    ImageView iv_filter_work, iv_filter_scholar, iv_filter_events, iv_filter_compete, iv_filter_intern, iv_filter_any;
    SearchView search_field;

    FirebaseStorage storage;
    FirebaseRecyclerPagingAdapter mAdapter;
    DatabaseReference Database;
    //FirebaseAuth mAuth;
    StorageReference storageReference;
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

        work = false;
        event=false;
        scholar=false;
        compete =false;
        intern = false;
        all=true;

        mFilterDialog = getLayoutInflater().inflate(R.layout.filter_dialog,null);
        iv_filter_back = findViewById(R.id.label);
        iv_filter_filter_btn = findViewById(R.id.filter);
        btn_filter_cancel = mFilterDialog.findViewById(R.id.btn_filter_cancel);
        btn_filter_confirm = mFilterDialog.findViewById(R.id.btn_filter_confirm);

        iv_filter_any = mFilterDialog.findViewById(R.id.iv_filter_all);
        iv_filter_compete = mFilterDialog.findViewById(R.id.iv_filter_competition);
        iv_filter_events = mFilterDialog.findViewById(R.id.iv_filter_events);
        iv_filter_intern = mFilterDialog.findViewById(R.id.iv_filter_internship);
        iv_filter_scholar = mFilterDialog.findViewById(R.id.iv_filter_scholarship);
        iv_filter_work = mFilterDialog.findViewById(R.id.iv_filter_work);

        card_all = mFilterDialog.findViewById(R.id.card_all);
        card_compete = mFilterDialog.findViewById(R.id.card_compete);
        card_work = mFilterDialog.findViewById(R.id.card_work);
        card_event = mFilterDialog.findViewById(R.id.card_event);
        card_intern = mFilterDialog.findViewById(R.id.card_intern);
        card_scholar = mFilterDialog.findViewById(R.id.card_scholar);

        tv_work = mFilterDialog.findViewById(R.id.tv_filter_work);
        tv_compete = mFilterDialog.findViewById(R.id.tv_filter_competition);
        tv_event = mFilterDialog.findViewById(R.id.tv_filters_event);
        tv_intern = mFilterDialog.findViewById(R.id.tv_filter_internship);
        tv_scholar = mFilterDialog.findViewById(R.id.tv_filter_scholarship);
        tv_all = mFilterDialog.findViewById(R.id.tv_filter_all);

        search_field = mFilterDialog.findViewById(R.id.searchView);

        dialog = new Dialog(this);

        iv_filter_back.setOnClickListener(this::onClick);
        iv_filter_filter_btn.setOnClickListener(this::onClick);
        btn_filter_confirm.setOnClickListener(this::onClick);
        btn_filter_cancel.setOnClickListener(this::onClick);
        Database = FirebaseDatabase.getInstance().getReference().child("Posting");


    }

    public void chooseFilter(){

        iv_filter_work.setOnClickListener(this::onClick);
        card_work.setOnClickListener(this::onClick);
        tv_work.setOnClickListener(this::onClick);

        iv_filter_events.setOnClickListener(this::onClick);
        card_event.setOnClickListener(this::onClick);
        tv_event.setOnClickListener(this::onClick);

        iv_filter_compete.setOnClickListener(this::onClick);
        card_compete.setOnClickListener(this::onClick);
        tv_compete.setOnClickListener(this::onClick);

        iv_filter_intern.setOnClickListener(this::onClick);
        card_intern.setOnClickListener(this::onClick);
        tv_intern.setOnClickListener(this::onClick);

        iv_filter_scholar.setOnClickListener(this::onClick);
        card_scholar.setOnClickListener(this::onClick);
        tv_scholar.setOnClickListener(this::onClick);

        iv_filter_any.setOnClickListener(this::onClick);
        card_all.setOnClickListener(this::onClick);
        tv_all.setOnClickListener(this::onClick);
        checkFilter();

    }

    void checkFilter()
    {
        if(card_work.getCardBackgroundColor().getDefaultColor()==untick
                &&card_event.getCardBackgroundColor().getDefaultColor()==untick
                &&card_scholar.getCardBackgroundColor().getDefaultColor()==untick
                &&card_compete.getCardBackgroundColor().getDefaultColor()==untick
                && card_intern.getCardBackgroundColor().getDefaultColor()==untick)
        {
            card_all.setCardBackgroundColor(tick);
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

        if (card_compete.getCardBackgroundColor().getDefaultColor()==untick)
        {
            compete = false;
        }
        else {
            compete = true;
        }

        if (card_scholar.getCardBackgroundColor().getDefaultColor()==untick)
        {
            scholar = false;
        }
        else
        {
            scholar = true;
        }

        if (card_event.getCardBackgroundColor().getDefaultColor()==untick)
        {
            event = false;
        }
        else{
            event = true;
        }

        if (card_work.getCardBackgroundColor().getDefaultColor()==untick)
        {
            work = false;
        }
        else {
            work = true;
        }

        if(card_all.getCardBackgroundColor().getDefaultColor()==untick)
        {
            all = false;
        }
        else{
            all = true;
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
            setBooleanValue();
            dialog.dismiss();
            String value = "work: "+ String.valueOf(work)
                    +"intern: "+ String.valueOf(intern)
                    +"Compete: " + String.valueOf(compete)
                    +"scholar: "+ String.valueOf(scholar)
                    +"events: "+ String.valueOf(event)
                    +"all: " + String.valueOf(all);
            Toast.makeText(SearchActivity.this,value,Toast.LENGTH_LONG).show();
        }

        if(v==iv_filter_work||v==card_work||v==tv_work)
        {
            int backgroundColor = card_work.getCardBackgroundColor().getDefaultColor();
            if(backgroundColor==untick)
            {
                card_work.setCardBackgroundColor(tick);
                card_all.setCardBackgroundColor(untick);
            }
            else {
                card_work.setCardBackgroundColor(untick);
                checkFilter();
            }
        }

        if(v==iv_filter_compete||v==card_compete||v==tv_compete)
        {
            int backgroundColor = card_compete.getCardBackgroundColor().getDefaultColor();
            if(backgroundColor==untick)
            {
                card_compete.setCardBackgroundColor(tick);
                card_all.setCardBackgroundColor(untick);
            }
            else
            {
                card_compete.setCardBackgroundColor(untick);
                checkFilter();
            }
        }

        if(v==iv_filter_events||v==card_event||v==tv_event)
        {
            int backgroundColor = card_event.getCardBackgroundColor().getDefaultColor();
            if(backgroundColor==untick)
            {
                card_event.setCardBackgroundColor(tick);
                card_all.setCardBackgroundColor(untick);
            }
            else
            {
                card_event.setCardBackgroundColor(untick);
                checkFilter();
            }

        }

        if(v==iv_filter_intern||v==card_intern||v==tv_intern)
        {
            int backgroundColor = card_intern.getCardBackgroundColor().getDefaultColor();
            if(backgroundColor==untick)
            {
                card_intern.setCardBackgroundColor(tick);
                card_all.setCardBackgroundColor(untick);
            }
            else
            {
                card_intern.setCardBackgroundColor(untick);
                checkFilter();
            }
        }

        if (v==iv_filter_scholar||v==card_scholar||v==tv_scholar)
        {
            int  backgroundColor = card_scholar.getCardBackgroundColor().getDefaultColor();
            if(backgroundColor==untick)
            {
                card_scholar.setCardBackgroundColor(tick);
                card_all.setCardBackgroundColor(untick);
            }
            else
            {
                card_scholar.setCardBackgroundColor(untick);
                checkFilter();
            }
        }


        if(v==iv_filter_any||v==card_all||v==tv_all)
        {
            int backgroundColor = card_all.getCardBackgroundColor().getDefaultColor();
            if(backgroundColor==untick){
                card_all.setCardBackgroundColor(tick);
                card_scholar.setCardBackgroundColor(untick);
                card_intern.setCardBackgroundColor(untick);
                card_compete.setCardBackgroundColor(untick);
                card_event.setCardBackgroundColor(untick);
                card_work.setCardBackgroundColor(untick);
            }
            else
            {
                card_all.setCardBackgroundColor(untick);
            }
        }

    }

}


