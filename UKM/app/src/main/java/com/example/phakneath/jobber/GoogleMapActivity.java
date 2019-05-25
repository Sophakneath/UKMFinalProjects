package com.example.phakneath.jobber;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.phakneath.jobber.Adapter.PlaceAutoCompleteAdapter;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class GoogleMapActivity extends AppCompatActivity implements OnMapReadyCallback, GoogleApiClient.OnConnectionFailedListener, View.OnClickListener{

    private static final String TAG = "MapActivity";
    double longit, lat;
    String add;

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1234;
    private static final float DEFAULT_ZOOM = 15f;

    //widgets
    private AutoCompleteTextView mSearchText;
    private ImageView mGps;
    private Button choose;
    private GoogleMap mMap;
    private GoogleApiClient mGoogleApiClient;

    private FusedLocationProviderClient mFusedLocationProviderClient;
    private PlaceAutoCompleteAdapter mPlaceAutocompleteAdapter;

    Geocoder geocoder;
    List<Address> addresses=null;

    Task<LocationSettingsResponse> task;

    private static final LatLngBounds LAT_LNG_BOUNDS = new LatLngBounds(new LatLng(-40, -168), new LatLng(71, 136));
    Location currentLocation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_google_map);

        initView();
        choose.setOnClickListener(this::onClick);

        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        fetchLastLocation();
        mGoogleApiClient = new GoogleApiClient
                .Builder(this)
                .addApi(Places.GEO_DATA_API)
                .enableAutoManage(this, 0,this)
                .build();

        init();

        mGps.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "onClick: clicked gps icon");
                longit = currentLocation.getLongitude();
                lat = currentLocation.getLatitude();
                fetchLastLocation();
            }
        });
    }

    public void fetchLastLocation() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)) {

                showExplaination();
            } else {

                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        LOCATION_PERMISSION_REQUEST_CODE);
                return;
            }
        }

        Task<Location> task = mFusedLocationProviderClient.getLastLocation();
        task.addOnSuccessListener(new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                if(location != null)
                {
                    currentLocation = location;
                    //Toast.makeText(GoogleMapActivity.this, currentLocation.getLatitude() +" " + currentLocation.getLongitude(), Toast.LENGTH_SHORT).show();
                    SupportMapFragment supportMapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
                    supportMapFragment.getMapAsync(GoogleMapActivity.this);
                }
            }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    public void initView()
    {
        mSearchText = findViewById(R.id.input_search);
        mGps = (ImageView) findViewById(R.id.ic_gps);
        choose = findViewById(R.id.choose);
    }

    private void showExplaination() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Require location permission");
        builder.setMessage("This app need location permisssion to get the location information");
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                ActivityCompat.requestPermissions(GoogleMapActivity.this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Toast.makeText(GoogleMapActivity.this, "Sorry, this fuction cannot be worked until the permission is granted", Toast.LENGTH_SHORT).show();
            }
        }).create();

        builder.show();
    }

    public void checkLocationSetting()
    {
        LocationManager lm = (LocationManager) getApplicationContext().getSystemService(Context.LOCATION_SERVICE);
        boolean gps_enabled = false;
        boolean network_enabled = false;

        try{
            gps_enabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER);

        }catch(Exception e)
        {
        }

        try{
            network_enabled = lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        }catch(Exception e)
        {}

        if(!gps_enabled || !network_enabled)
        {
            new AlertDialog.Builder(this).setMessage("Please turn on your Location Setting")
                    .setPositiveButton("Open Location Setting", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            startActivityForResult(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS), LOCATION_PERMISSION_REQUEST_CODE);
                        }
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        Log.d(TAG, "onMapReady: onMap");
        LatLng latLng = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());
        moveCamera(latLng, googleMap);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        Log.d(TAG, "onRequestPermissionsResult: called.");

        switch(requestCode){
            case LOCATION_PERMISSION_REQUEST_CODE:{
                if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    fetchLastLocation();
                }
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        switch(requestCode)
        {
            case LOCATION_PERMISSION_REQUEST_CODE:
                switch(resultCode)
                {
                    case Activity.RESULT_OK:
                        Toast.makeText(this, "Location setting has turned on", Toast.LENGTH_SHORT).show();
                        fetchLastLocation();
                        break;
                    case Activity.RESULT_CANCELED:
                        Toast.makeText(this, "Location setting has not turned on", Toast.LENGTH_SHORT).show();
                        fetchLastLocation();
                        break;
                }
                break;
        }
    }

    private void init(){
        Log.d(TAG, "init: initializing");

        mPlaceAutocompleteAdapter = new PlaceAutoCompleteAdapter(this, mGoogleApiClient,
                LAT_LNG_BOUNDS, null);

        mSearchText.setAdapter(mPlaceAutocompleteAdapter);

        mSearchText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int actionId, KeyEvent keyEvent) {
                if(actionId == EditorInfo.IME_ACTION_SEARCH
                        || actionId == EditorInfo.IME_ACTION_DONE
                        || keyEvent.getAction() == KeyEvent.ACTION_DOWN
                        || keyEvent.getAction() == KeyEvent.KEYCODE_ENTER){

                    //execute our method for searching
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            geoLocate();
                        }
                    }).start();
                }

                return false;
            }
        });

        hideSoftKeyboard(mSearchText);
    }


    private void geoLocate() {
        Log.d(TAG, "geoLocate: geolocating");

        String searchString = mSearchText.getText().toString();

        geocoder = new Geocoder(GoogleMapActivity.this);
        List<Address> list = new ArrayList<>();
        try {
            list = geocoder.getFromLocationName(searchString, 1);
        } catch (IOException e) {
            Log.e(TAG, "geoLocate: IOException: " + e.getMessage());
        }

        if (list.size() > 0) {
            Address address = list.get(0);

            Log.d(TAG, "geoLocate: found a location: " + address.toString());
            //Toast.makeText(this, address.toString(), Toast.LENGTH_SHORT).show();

            lat = address.getLatitude();
            longit = address.getLongitude();
            add = address.getAddressLine(0);

            //Toast.makeText(this, lat + " , " + longit, Toast.LENGTH_SHORT).show();

            GoogleMapActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    moveCamera(new LatLng(address.getLatitude(), address.getLongitude()),
                            mMap);
                }
            });
        }
    }

    public void moveCamera(LatLng latLng, GoogleMap googleMap)
    {
        googleMap.clear();
        hideSoftKeyboard(mSearchText);
        Log.d(TAG, "onMapReady: " + currentLocation.getLongitude() + currentLocation.getLatitude());
        MarkerOptions markerOptions = new MarkerOptions().position(latLng).title("You are here");
        googleMap.animateCamera(CameraUpdateFactory.newLatLng(latLng));
        googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng,DEFAULT_ZOOM));
        googleMap.addMarker(markerOptions);
    }

    private void hideSoftKeyboard(View view){
        InputMethodManager imm = (InputMethodManager) this.getSystemService(Activity.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }


    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onClick(View v) {
        if(v == choose)
        {
            if(TextUtils.isEmpty(mSearchText.getText().toString().trim()) && currentLocation != null)
            {
                longit = currentLocation.getLongitude();
                lat = currentLocation.getLatitude();
                /*geocoder = new Geocoder(this, Locale.getDefault());

                try {
                    addresses = geocoder.getFromLocation(lat, longit, 1);
                } catch (IOException e) {
                    e.printStackTrace();
                }

                if(addresses != null) add = addresses.get(0).getAddressLine(0);*/
            }

            geocoder = new Geocoder(this, Locale.getDefault());
            try {
                addresses = geocoder.getFromLocation(lat, longit, 1);
            } catch (IOException e) {
                e.printStackTrace();
            }
            if(addresses != null) add = addresses.get(0).getAddressLine(0);

            Intent returnIntent = new Intent();
            returnIntent.putExtra("longtitute", longit);
            returnIntent.putExtra("latitute", lat);
            returnIntent.putExtra("address", add);
            setResult(Activity.RESULT_OK,returnIntent);
            finish();
        }
    }
}
