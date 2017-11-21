package com.example.mlab.androidpresencesystem;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;

import com.google.android.gms.location.LocationListener;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.RelativeLayout;

import com.example.mlab.androidpresencesystem.model.Tracking;
import com.example.mlab.androidpresencesystem.model.User;
import com.firebase.ui.database.FirebaseRecyclerAdapter;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class ListOnline extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks
        , GoogleApiClient.OnConnectionFailedListener, LocationListener {

    private static final int UPDATE_INTERVAL = 5000;
    private static final int FASTEST_INTERVAL = 3000;
    private static final int DISTANCE = 10;
    //Firebase
    DatabaseReference onlineRef, currentUserRef, counterRef, locations;
    private FirebaseRecyclerAdapter<User, ListOnlineViewHolder> mFireAdapter;
    //Location
    private static final int PERMISSION_REQUEST = 111;
    private static final int PLAY_SERVICES_REQ = 222;
    private LocationRequest mLocationRequest;
    private GoogleApiClient mGoogleApiCLient;
    private Location mLastLocation;

    //View
    RecyclerView listOnline;
    RecyclerView.LayoutManager layoutManager;
    RelativeLayout relativeLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_online);

        listOnline = findViewById(R.id.listOnline);
        listOnline.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(this);
        listOnline.setLayoutManager(layoutManager);
        relativeLayout = findViewById(R.id.rel_online);

        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("Presence System");
        setSupportActionBar(toolbar);

        //Instantiate Firebase
        locations = FirebaseDatabase.getInstance().getReference().child("Locations");
        onlineRef = FirebaseDatabase.getInstance().getReference().child(".info/connected");
        counterRef = FirebaseDatabase.getInstance().getReference("lastOnline");//create new child lastOnline
        currentUserRef = counterRef.child(FirebaseAuth.getInstance().getCurrentUser().getUid()); //create new child with key uid

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSION_REQUEST);
        } else {
            if (checkPlayServices()) {
                buildGoogleApi();
                createLocationRequest();
                displayLocation();
            }
        }

        setupSystem();
        //After setup System , we just load  all user from counterRef and display on the recycler
        //This is online lis
        updateList();

    }

    private void displayLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiCLient);
        if (mLastLocation != null) {
            //Update Firebase

            locations.child(FirebaseAuth.getInstance().getCurrentUser().getUid()).
                    setValue(new Tracking(FirebaseAuth.getInstance().getCurrentUser().getEmail(),
                            FirebaseAuth.getInstance().getCurrentUser().getUid(),
                            String.valueOf(mLastLocation.getLatitude()),
                            String.valueOf(mLastLocation.getLongitude()),
                            FirebaseAuth.getInstance().getCurrentUser().getDisplayName()));
        } else {
            Snackbar snackbar = Snackbar.make(relativeLayout, "Location Not Found", Snackbar.LENGTH_LONG);
            snackbar.setActionTextColor(Color.RED);
            snackbar.show();
        }
    }

    private void buildGoogleApi() {
        mGoogleApiCLient = new GoogleApiClient.Builder(this)
                .addOnConnectionFailedListener(this)
                .addConnectionCallbacks(this)
                .addApi(LocationServices.API).build();
        mGoogleApiCLient.connect();


    }

    private boolean checkPlayServices() {

        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS) {

            if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
                GooglePlayServicesUtil.getErrorDialog(resultCode, this, PLAY_SERVICES_REQ);
            } else {
                Snackbar snackbar = Snackbar.make(relativeLayout, "This device is not supported", Snackbar.LENGTH_LONG).setAction("Retry", new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                    }

                });
                snackbar.setActionTextColor(Color.RED);
                snackbar.show();
                finish();
            }

            return false;
        }
        return true;
    }


    private void updateList() {
        mFireAdapter = new FirebaseRecyclerAdapter<User, ListOnlineViewHolder>(User.class,
                R.layout.user_layout, ListOnlineViewHolder.
                class, counterRef) {
            @Override
            protected void populateViewHolder(ListOnlineViewHolder viewHolder, final User model, int position) {


                if (model.getEmail().equalsIgnoreCase(FirebaseAuth.getInstance().getCurrentUser().getEmail())) {
                    System.out.println("Current user");
                    viewHolder.username.setText(String.valueOf(
                            model.getUserName()));
                    viewHolder.username.append("(Me)");
                }else {
                    viewHolder.username.setText(model.getUserName());
                }


                //implement update click listener


                viewHolder.itemClickListenener = new ItemClickListenener() {
                    @Override
                    public void onClick(View view, int position) {
                        //cant click current user logged in
                        //sending information of other users not one logged in
                        if (!model.getEmail().equals(FirebaseAuth.getInstance().getCurrentUser().getEmail())) {
                            Intent in = new Intent(getApplicationContext(), MapTracking.class);

                            in.putExtra("email", model.getEmail());
                            in.putExtra("latitude", mLastLocation.getLatitude());
                            in.putExtra("Longitude", mLastLocation.getLongitude());
                            startActivity(in);
                        }
                    }
                };


            }
        };

        mFireAdapter.notifyDataSetChanged();
        listOnline.setAdapter(mFireAdapter);

    }

    private void setupSystem() {
        onlineRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue(Boolean.class)) {
                    currentUserRef.onDisconnect().removeValue(); //delete old value

                    //Set Online User in list
                    counterRef.child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                            .setValue(new User(FirebaseAuth.getInstance().getCurrentUser()
                                    .getEmail(), "Online", FirebaseAuth.getInstance().getCurrentUser().getDisplayName()));
                    mFireAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        counterRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot postSnapShot : dataSnapshot.getChildren()) {
                    User user = postSnapShot.getValue(User.class);
                    Log.d("LOG", " " + user.getUserName() + " is " + user.getStatus());
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.menu_join:
                counterRef.child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                        .setValue(new User(FirebaseAuth.getInstance().getCurrentUser()
                                .getEmail(), "Online", FirebaseAuth.getInstance().getCurrentUser().getDisplayName()));
                break;
            case R.id.menu_logout:
                currentUserRef.removeValue();
                break;

        }
        return super.onOptionsItemSelected(item);
    }

    private void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(UPDATE_INTERVAL);
        mLocationRequest.setFastestInterval(FASTEST_INTERVAL);
        mLocationRequest.setSmallestDisplacement(DISTANCE);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        displayLocation();
        startLocationUpdates();

    }

    private void startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiCLient, mLocationRequest, this);

    }

    @Override
    public void onConnectionSuspended(int i) {
        mGoogleApiCLient.connect();

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onLocationChanged(Location location) {
        mLastLocation = location;
        displayLocation();

    }

    @Override
    protected void onStart() {
        super.onStart();
        if (mGoogleApiCLient != null)
            mGoogleApiCLient.connect();
    }

    @Override
    protected void onStop() {
        if (mGoogleApiCLient != null) {
            mGoogleApiCLient.disconnect();
        }
        super.onStop();

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_REQUEST: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if (checkPlayServices()) {

                        buildGoogleApi();
                        createLocationRequest();
                        displayLocation();
                    }
                }
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        checkPlayServices();
    }
}
