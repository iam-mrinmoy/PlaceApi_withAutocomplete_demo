package com.onqanetapp.mp.placeapi_withautocomplete_demo;

import android.Manifest;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.location.Address;
import android.location.Geocoder;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Html;
import android.text.Spanned;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.location.places.AutocompletePrediction;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.PlaceBuffer;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener,OnMapReadyCallback {

    protected GoogleApiClient mGoogleApiClient;
    private PlaceAutocompleteAdapter mAdapter;
    private AutoCompleteTextView mAutocompleteView;
    private TextView mPlaceDetailsText,tvCity,tvState,tvZip,tvCountry;
    private TextView mPlaceDetailsAttribution;
    private static final int MY_PERMISSIONS_REQUEST_READ_LOCATION = 1;
    static String TAG ="place";
    private Boolean mapGranted = false;
    private double lat=-34.041458;
    private double lon=150.790100;
    SupportMapFragment mapFragment;
    private static final LatLngBounds BOUNDS_GREATER_SYDNEY = new LatLngBounds(
            new LatLng(-34.041458, 150.790100), new LatLng(-33.682247, 151.383362));
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this, 0 /* clientId */, this)
                .addApi(Places.GEO_DATA_API)
                .build();

        setContentView(R.layout.activity_main);

        mAutocompleteView = (AutoCompleteTextView)
                findViewById(R.id.autocomplete_places);
        // Register a listener that receives callbacks when a suggestion has been selected
        mAutocompleteView.setOnItemClickListener(mAutocompleteClickListener);
        mPlaceDetailsText = (TextView) findViewById(R.id.all);
        tvCity = (TextView) findViewById(R.id.tvCity);
        tvState = (TextView) findViewById(R.id.tvState);
        tvZip = (TextView) findViewById(R.id.tvZip);
        tvCountry = (TextView) findViewById(R.id.tvCountry);

        // Set up the adapter that will retrieve suggestions from the Places Geo Data API that cover
        // the entire world.
        mAdapter = new PlaceAutocompleteAdapter(this, mGoogleApiClient, BOUNDS_GREATER_SYDNEY,null);
        mAutocompleteView.setAdapter(mAdapter);
        
            ////// Map   //////
        mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        if (mapGranted != true) {
            checkRuntimePermission();
            mapGranted=true;
        }
    }
    @Override
    public void onMapReady(GoogleMap googleMap) {

        LatLng onqanet = new LatLng(lat, lon);
        //map.setMyLocationEnabled(true);
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(onqanet, 13));

        googleMap.addMarker(new MarkerOptions()
                .title("Resturant")
                .snippet("Room No 603, J-1/16, EP & GP Block,Sector- V, \n" +
                        "Electronics Complex,Salt Lake, \n" +
                        "Shaila Towers,Kolkata, \n" +
                        "West Bengal 700091")
                .icon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_launcher))
                .position(onqanet));

    }
    public void checkRuntimePermission() {

        // checking for runtime permission
        if (ContextCompat.checkSelfPermission(MainActivity.this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(MainActivity.this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Log.d("yoyo", "inside permission check");
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, MY_PERMISSIONS_REQUEST_READ_LOCATION);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        //Log.d("yoyo","from request");
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_READ_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission was granted, yay! Do the

                } else {
                    // permission denied, boo! Disable the
                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request
        }
    }

    private AdapterView.OnItemClickListener mAutocompleteClickListener
            = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            /*
             Retrieve the place ID of the selected item from the Adapter.
             The adapter stores each Place suggestion in a AutocompletePrediction from which we
             read the place ID and title.
              */
            final AutocompletePrediction item = mAdapter.getItem(position);
            final String placeId = item.getPlaceId();
            final CharSequence primaryText = item.getPrimaryText(null);

            Log.i(TAG, "Autocomplete item selected: " + primaryText);

            /*
             Issue a request to the Places Geo Data API to retrieve a Place object with additional
             details about the place.
              */
            PendingResult<PlaceBuffer> placeResult = Places.GeoDataApi
                    .getPlaceById(mGoogleApiClient, placeId);
            placeResult.setResultCallback(mUpdatePlaceDetailsCallback);

            Toast.makeText(getApplicationContext(), "Clicked: " + primaryText,
                    Toast.LENGTH_SHORT).show();
            Log.i(TAG, "Called getPlaceById to get Place details for " + placeId);
        }
    };

    private ResultCallback<PlaceBuffer> mUpdatePlaceDetailsCallback
            = new ResultCallback<PlaceBuffer>() {
        @Override
        public void onResult(PlaceBuffer places) {
            if (!places.getStatus().isSuccess()) {
                // Request did not complete successfully
                Log.e(TAG, "Place query did not complete. Error: " + places.getStatus().toString());
                places.release();
                return;
            }
            // Get the Place object from the buffer.
            final Place place = places.get(0);

            // Format details of the place for display and show it in a TextView.
            mPlaceDetailsText.setText( place.getName()+"\n"+
                    place.getId()+"\n"+
                    place.getAddress()+"\n"+
                    place.getPhoneNumber()+"\n"+
                    place.getWebsiteUri());

            lat=place.getLatLng().latitude;
            lon=place.getLatLng().longitude;
            initializeAddress(lat,lon);
            ////// Map   //////
            mapFragment = (SupportMapFragment) getSupportFragmentManager()
                    .findFragmentById(R.id.map);
            mapFragment.getMapAsync(MainActivity.this);
            if (mapGranted != true) {
                checkRuntimePermission();
                mapGranted=true;
            }
            Log.i(TAG, "Place details received: " + place.getName());

            places.release();
        }
    };
public void initializeAddress(Double la,Double lo){
    Geocoder geocoder;
    List<Address> addresses;
    geocoder = new Geocoder(this, Locale.getDefault());
try {
    addresses = geocoder.getFromLocation(la, lo, 1); // Here 1 represent max location result to returned, by documents it recommended 1 to 5

    /*String address = addresses.get(0).getAddressLine(0); // If any additional address line present than only, check with max available address lines by getMaxAddressLineIndex()
    String city = addresses.get(0).getLocality();
    String state = addresses.get(0).getAdminArea();
    String country = addresses.get(0).getCountryName();
    String postalCode = addresses.get(0).getPostalCode();
    String knownName = addresses.get(0).getFeatureName();*/

    tvCity.setText(addresses.get(0).getLocality());
    tvState.setText(addresses.get(0).getAdminArea());
    tvZip.setText(addresses.get(0).getPostalCode());
    tvCountry.setText(addresses.get(0).getCountryName());
}catch (Exception e){}



}
    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

        Log.e(TAG, "onConnectionFailed: ConnectionResult.getErrorCode() = "
                + connectionResult.getErrorCode());

        // TODO(Developer): Check error code and notify the user of error state and resolution.
        Toast.makeText(this,
                "Could not connect to Google API Client: Error " + connectionResult.getErrorCode(),
                Toast.LENGTH_SHORT).show();
    }
}
