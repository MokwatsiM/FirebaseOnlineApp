package com.example.mlab.androidpresencesystem;

import android.graphics.drawable.BitmapDrawable;
import android.location.Location;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.text.TextUtils;

import com.example.mlab.androidpresencesystem.model.Tracking;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.text.DecimalFormat;

public class MapTracking extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private String email, username;
    DatabaseReference locations;
    Double lat, lng;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map_tracking);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        locations = FirebaseDatabase.getInstance().getReference("Locations");
        if (getIntent() != null) {

            email = getIntent().getStringExtra("email");
            lat = getIntent().getDoubleExtra("latitude", 0);
            lng = getIntent().getDoubleExtra("Longitude", 0);

        }

        if (!TextUtils.isEmpty(email))
            loadLocationForThisUser(email);
    }

    private void loadLocationForThisUser(String email) {
        Query user_location = locations.orderByChild("email").equalTo(email);

        user_location.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot postSnapShot : dataSnapshot.getChildren()) {
                    Tracking tracking = postSnapShot.getValue(Tracking.class);

                    LatLng friendsLocation = new LatLng(Double.parseDouble(tracking.getLat())
                            , Double.parseDouble(tracking.getLng()));

                    //creating location for user logged in
                    Location locationFrom = new Location("");
                    locationFrom.setLatitude(lat);
                    locationFrom.setLongitude(lng);

                    //Creating location for friends
                    Location locationTo = new Location("");
                    locationTo.setLatitude(friendsLocation.latitude);
                    locationTo.setLongitude(friendsLocation.longitude);

                    //Function to calculate distance between two points
                    // calcDistance(locationFrom,locationTo)
                    double distanceFromTo = calcDistance(locationFrom, locationTo);
                    mMap.clear();
                    mMap.addMarker(new MarkerOptions().position(friendsLocation)
                            .title(tracking.getUserName() + " " + tracking.getEmail())
                            .snippet("Distance " + new DecimalFormat("#.#")
                                    .format((locationFrom.distanceTo(locationTo)) / 1000) + " Km")
                            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)));

                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(lat, lng), 12.0f));
                }

                //create marker for current user logged in
                LatLng currentUser = new LatLng(lat, lng);
                mMap.addMarker(new MarkerOptions().position(currentUser).
                        title(FirebaseAuth.getInstance().getCurrentUser().getDisplayName() +
                                " " + FirebaseAuth.getInstance().getCurrentUser().getEmail()));


            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private double calcDistance(Location locationFrom, Location locationTo) {
        double latitudeToSendToDegFrom = locationFrom.getLatitude();
        double latitudeToSendToDegTo = locationFrom.getLatitude();
        double theta = locationFrom.getLongitude() - locationTo.getLongitude();
        double distance = Math.sin(deg2Rad(latitudeToSendToDegFrom))
                * Math.sin(deg2Rad(latitudeToSendToDegTo))
                * Math.cos(deg2Rad(latitudeToSendToDegFrom))
                * Math.cos(deg2Rad(latitudeToSendToDegTo))
                * Math.cos(deg2Rad(theta));
        distance = Math.acos(distance);
        distance = rad2Deg(distance);
        distance = distance * 60 * 1.1515;
        return distance;
    }

    private double rad2Deg(double rad) {
        return (rad * 180 / Math.PI);
    }

    private double deg2Rad(double deg) {
        return (deg * Math.PI / 180.0);
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

    }
}
