package com.hackomaniacs.ingenius;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class MapActivity extends AppCompatActivity implements LocationListener,GoogleApiClient.ConnectionCallbacks,GoogleApiClient.OnConnectionFailedListener,
        GoogleMap.OnInfoWindowClickListener,GoogleMap.OnMapLongClickListener,GoogleMap.OnMapClickListener{

    private GoogleMap googleMap;
    String text;
    LocationManager locationManager;
    double[] lat =new double[100];
    double[] lon =new double[100];
    int c=0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        try{
            defineMap();
        }catch (Exception e){
            e.printStackTrace();
        }
        locationManager=(LocationManager)getSystemService(LOCATION_SERVICE);
        Criteria criteria =new Criteria();
        String provider= locationManager.getBestProvider(criteria,true);
        Location location=locationManager.getLastKnownLocation(provider);
        locationManager.requestLocationUpdates(provider,2000,0,this);

        googleMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                marker.showInfoWindow();
                Toast.makeText(getApplicationContext(),text,Toast.LENGTH_SHORT).show();
                return true;
            }
        });
    }

    private void defineMap() {
        if (googleMap == null) {
            googleMap = ((MapFragment) getFragmentManager().findFragmentById(
                    R.id.map)).getMap();

            // check if map is created successfully or not
            if (googleMap == null) {
                Toast.makeText(getApplicationContext(),
                        "Sorry! unable to create maps", Toast.LENGTH_SHORT)
                        .show();
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        defineMap();
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onLocationChanged(Location location) {

        if(googleMap != null){
            createMarker(location);
            drawLines();
        }

    }

    private void createMarker(Location location) {
        googleMap.clear();
        LatLng currentPosition = new LatLng(location.getLatitude(), location.getLongitude());
        googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentPosition, 18));

        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        List<Address> list = null;
        try {
            list = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
        } catch (IOException e) {
        }

        Address address = list.get(0);
        text = String.format("%s,%s,%s", address.getMaxAddressLineIndex() > 0 ? address.getAddressLine(0) : "",
                address.getLocality(),
                address.getCountryName());
        googleMap.addMarker(new MarkerOptions().position(currentPosition).title(text).
                snippet("Lat:" + Double.toString(location.getLatitude()) + "Lng" + Double.toString(location.getLongitude())));
        lat[c] = location.getLatitude();
        lon[c] = location.getLongitude();
        c++;
    }

    private void drawLines() {
        for(int i=1; i<=c;i++)
        {
            if(i==c)
            {
                break;
            }

            googleMap.addPolyline((new PolylineOptions())
                    .add(new LatLng(lat[i-1],lon[i-1]),new LatLng(lat[i],lon[i])).width(5).color(Color.BLUE)
                    .geodesic(true));
        }

    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        //not needed
    }

    @Override
    public void onProviderEnabled(String provider) {
        //not needed

    }

    @Override
    public void onProviderDisabled(String provider) {
        //not needed

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {


    }

    @Override
    public void onInfoWindowClick(Marker marker) {

    }

    @Override
    public void onMapClick(LatLng latLng) {

    }

    @Override
    public void onMapLongClick(LatLng latLng) {

    }
}
