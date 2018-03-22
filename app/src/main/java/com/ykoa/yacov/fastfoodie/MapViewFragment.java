package com.ykoa.yacov.fastfoodie;

        import android.content.Context;
        import android.graphics.Color;
        import android.location.Location;
        import android.os.AsyncTask;
        import android.os.Bundle;
        import android.support.annotation.Nullable;
        import android.support.v4.app.Fragment;
        import android.util.Log;
        import android.view.LayoutInflater;
        import android.view.View;
        import android.view.ViewGroup;
        import android.widget.FrameLayout;
        import com.google.android.gms.maps.GoogleMap;
        import com.google.android.gms.maps.OnMapReadyCallback;
        import com.google.android.gms.maps.SupportMapFragment;
        import com.google.android.gms.maps.model.BitmapDescriptorFactory;
        import com.google.android.gms.maps.model.Circle;
        import com.google.android.gms.maps.model.CircleOptions;
        import com.google.android.gms.maps.model.LatLng;
        import com.google.android.gms.maps.model.Marker;
        import com.google.android.gms.maps.model.MarkerOptions;
        import java.io.IOException;
        import java.text.DecimalFormat;
        import java.util.ArrayList;
        import java.util.HashMap;
        import java.util.List;

/**
 * Created by yacov on 3/7/2018.
 */

public class MapViewFragment extends Fragment implements FragmentInterface,
        GoogleMap.OnMapLoadedCallback, OnMapReadyCallback {

    private static final String TAG = "MapViewFragment";
    private FragmentCommunication mCallback;
    private int searchRadius;
    GoogleMapsAPI mMapAPI;
    GoogleMap mMap;
    Location location = null;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater,
                             @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.map_fragment,
                container, false);

        // Retrieve tasks lists from bundle
//        Bundle data = getArguments();
//        if (data != null) {getBundleArgs(data);}

        searchRadius = mCallback.getRadius();

        // Create a map
         mMapAPI = new GoogleMapsAPI(getActivity(), savedInstanceState, searchRadius);

        // Build the map
        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        // This makes sure that the container activity has implemented
        // the callback interface. If not, it throws an exception
        try {
            mCallback = (FragmentCommunication) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString()
                    + " must implement DataCommunication");
        }
    }

    /**
     * Manipulates the map when it's available.
     * This callback is triggered when the map is ready to be used.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        Log.d(TAG, "-------------------------> onMApReady");
        mMap = googleMap;

        // Use a custom info window adapter to handle multiple lines of text in the
        // info window contents.
        mMap.setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter() {

            @Override
            // Return null here, so that getInfoContents() is called next.
            public View getInfoWindow(Marker arg0) {
                return null;
            }

            @Override
            public View getInfoContents(Marker marker) {
                // Inflate the layouts for the info window, title and snippet.
                View infoWindow = getActivity().getLayoutInflater().inflate(R.layout.custom_info_contents,
                        (FrameLayout) getActivity().findViewById(R.id.map), false);



                return infoWindow;
            }
        });

        mMapAPI.setMap(mMap);

        // Prompt the user for permission.
        mMapAPI.getLocationPermission();

        // Turn on the My Location layer and the related control on the map.
        mMapAPI.updateLocationUI();

        // Get the current location of the device and set the position of the map.
        mMapAPI.getDeviceLocation();

        mMap.setOnMapLoadedCallback(this);
    }

    @Override
    public void onMapLoaded() {
        Log.d(TAG, "-------------------------> onMApLoaded");
        location = mMapAPI.getLocation();
        LatLng point = new LatLng(location.getLatitude(), location.getLongitude());
        mCallback.setLatLng(point);
        mCallback.setMap(mMap);
        // Set search radius around user location.
        drawCircle(point);
        try {
            findNearByRestaurants(point);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Draws a circle on the map, depending
     * on the search radius size.
     */
    public void drawCircle(LatLng point) {
        mMap.clear();
        searchRadius = mCallback.getRadius();
        Circle circle = mMap.addCircle(new CircleOptions()
                .center(point)
                .radius(searchRadius)
                .strokeColor(Color.rgb(102,40,0))
                .fillColor(Color.TRANSPARENT));
    }

    public void findNearByRestaurants(LatLng p) throws IOException {
        GetNearbyPlacesData getNearbyPlacesData = new GetNearbyPlacesData();
        getNearbyPlacesData.execute(p);
    }

    public class GetNearbyPlacesData extends AsyncTask<Object, String, String> {

        LatLng p;

        @Override
        protected String doInBackground(Object... params) {
            p = (LatLng) params[0];
            final YelpService yelpService = new YelpService();
            String jsonData = null;
            try {
                jsonData = yelpService.setYelpRequest(getContext(),
                        p.latitude, p.longitude, searchRadius,  "restaurants");
            } catch (IOException e) {
                e.printStackTrace();
            }
            return jsonData;
        }

        @Override
        protected void onPostExecute(String result) {
            Log.d("onPostExecute", "parse jsonObject");

            DataParser dataParser = new DataParser();
            List<HashMap<String, String>> hm = dataParser.parse(result);
            showNearbyPlaces(hm);
        }

        private void showNearbyPlaces(List<HashMap<String, String>> hm) {

            Log.d("showNearByPlaces", "size of near by places list ------> " + hm.size());

            ArrayList<RestaurantInfo> list =  new ArrayList<>();

            for (int i = 0; i < hm.size(); i++) {
                MarkerOptions markerOptions = new MarkerOptions();
                HashMap<String, String> place = hm.get(i);
                if (place.get("lat") == null || place.get("lng") == null || place.get("rating") == null) {continue;}
                String status = place.get("is_closed");
                if (status.equals("closed")) {continue;}
                String distance = place.get("distance");
                if (Double.parseDouble(distance) > searchRadius) {continue;}
                DecimalFormat value = new DecimalFormat("#.#");
                String d = value.format(Double.parseDouble(distance) / 1609.344);
                double lat = Double.parseDouble(place.get("lat"));
                double lng = Double.parseDouble(place.get("lng"));
                String placeName = place.get("name");
                String address = place.get("address");
                double rating = Double.parseDouble(place.get("rating"));
                String cost = place.get("cost");
                String cuisine = place.get("cuisine");
                String imgURL = place.get("image");
                String reviewCount = place.get("review_count");
                String phoneNum = place.get("phone");

                // Create a restaurant object
                list.add(new RestaurantInfo(placeName, address, phoneNum, cuisine, rating, cost, d, imgURL, reviewCount));

                // Create marker for the map
                LatLng latLng = new LatLng(lat, lng);
                markerOptions.position(latLng);
                markerOptions.title(placeName + " - " + cuisine + " - " + rating);
                if (rating >= 4) {
                    markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
                } else if (rating >= 3) {
                    markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE));
                } else {
                    markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
                }
                // Add marker onto the map view
                mMap.addMarker(markerOptions);

            }
            // Set restaurant list
            mCallback.setRestaurantList(list);
            mCallback.setHasChanged(true);
        }
    }

//    @Override
//    public void onLocationChanged(Location location) {
//        Log.d("onLocationChanged", "entered");
//
//        mLastLocation = location;
//        if (mCurrLocationMarker != null) {
//            mCurrLocationMarker.remove();
//        }
//
//        //Place current location marker
//        latitude = location.getLatitude();
//        longitude = location.getLongitude();
//        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
//        MarkerOptions markerOptions = new MarkerOptions();
//        markerOptions.position(latLng);
//        markerOptions.title("Current Position");
//        markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_MAGENTA));
//        mCurrLocationMarker = mMap.addMarker(markerOptions);
//
//        //move map camera
//        mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
//        mMap.animateCamera(CameraUpdateFactory.zoomTo(11));
//        Toast.makeText(MapsActivity.this,"Your Current Location", Toast.LENGTH_LONG).show();
//
//        Log.d("onLocationChanged", String.format("latitude:%.3f longitude:%.3f",latitude,longitude));
//
//        //stop location updates
//        if (mGoogleApiClient != null) {
//            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
//            Log.d("onLocationChanged", "Removing Location Updates");
//        }
//        Log.d("onLocationChanged", "Exit");
//
//    }

//    /**
//     * Gets the current location of the device, and positions the map's camera.
//     */
//    private void getDeviceLocation(final GoogleMap map) {
//        /*
//         * Get the best and most recent location of the device, which may be null in rare
//         * cases when a location is not available.
//         */
//        Log.d(TAG, "INSIDE ---> getDeviceLocation!");
//        try {
//            if (mLocationPermissionGranted) {
//                Task<Location> locationResult = mFusedLocationProviderClient.getLastLocation();
//                locationResult.addOnCompleteListener(getActivity(), new OnCompleteListener<Location>() {
//                    @Override
//                    public void onComplete(@NonNull Task<Location> task) {
//                        if (task.isSuccessful()) {
//                            // Set the map's camera position to the current location of the device.
//                            mLastKnownLocation = task.getResult();
//                            LatLng position = new LatLng(mLastKnownLocation.getLatitude(), mLastKnownLocation.getLongitude());
//                            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(position, DEFAULT_ZOOM));
//
//                            // Set search radius around user location.
//                            drawCircle(map, position);
//                            try {
//                                findNearByRestaurants(position);
//                            } catch (IOException e) {
//                                e.printStackTrace();
//                            }
//
//                        } else {
//                            Log.d(TAG, "Current location is null. Using defaults.");
//                            Log.e(TAG, "Exception: %s", task.getException());
//                            mMap.moveCamera(CameraUpdateFactory
//                                    .newLatLngZoom(mDefaultLocation, DEFAULT_ZOOM));
//                            mMap.getUiSettings().setMyLocationButtonEnabled(false);
//                        }
//                    }
//                });
//            }
//        } catch (SecurityException e)  {
//            Log.e("Exception: %s", e.getMessage());
//        }
//    }
//
//    /**
//     * Prompts the user for permission to use the device location.
//     */
//    private void getLocationPermission() {
//        Log.d(TAG, "INSIDE ---> getLocationPermission!");
//        /*
//         * Request location permission, so that we can get the location of the
//         * device. The result of the permission request is handled by a callback,
//         * onRequestPermissionsResult.
//         */
//        if (ContextCompat.checkSelfPermission(getContext().getApplicationContext(),
//                android.Manifest.permission.ACCESS_FINE_LOCATION)
//                == PackageManager.PERMISSION_GRANTED) {
//            mLocationPermissionGranted = true;
//        } else {
//            ActivityCompat.requestPermissions(getActivity(),
//                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
//                    PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
//        }
//    }
//
//    /**
//     * Handles the result of the request for location permissions.
//     */
//    @Override
//    public void onRequestPermissionsResult(int requestCode,
//                                           @NonNull String permissions[],
//                                           @NonNull int[] grantResults) {
//        mLocationPermissionGranted = false;
//        switch (requestCode) {
//            case PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION: {
//                // If request is cancelled, the result arrays are empty.
//                if (grantResults.length > 0
//                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//                    mLocationPermissionGranted = true;
//                }
//            }
//        }
//        updateLocationUI();
//    }
//
//    /**
//     * Updates the map's UI settings based on whether the user has granted location permission.
//     */
//    private void updateLocationUI() {
//        Log.d(TAG, "INSIDE ---> updateLocationUI!");
//        if (mMap == null) {
//            return;
//        }
//        try {
//            if (mLocationPermissionGranted) {
//                mMap.setMyLocationEnabled(true);
//                mMap.getUiSettings().setMyLocationButtonEnabled(true);
//            } else {
//                mMap.setMyLocationEnabled(false);
//                mMap.getUiSettings().setMyLocationButtonEnabled(false);
//                mLastKnownLocation = null;
//                getLocationPermission();
//            }
//        } catch (SecurityException e) {
//            Log.e("Exception: %s", e.getMessage());
//        }
//    }


    @Override
    public void fragmentBecameVisible() {
        searchRadius = mCallback.getRadius();
    }
}
