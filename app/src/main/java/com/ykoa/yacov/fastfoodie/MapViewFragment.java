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
        import android.widget.ImageView;
        import android.widget.TextView;

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
        import java.util.HashSet;
        import java.util.List;

/**
 * Created by yacov on 3/7/2018.
 */

public class MapViewFragment extends Fragment implements FragmentInterface,
        GoogleMap.OnMapLoadedCallback, OnMapReadyCallback {

    private static final String TAG = "MapViewFragment";
    private FragmentCommunication mCallback;
    private int searchRadius;
    private int searchCost;
    private int searchRating;
    private HashMap<String, String> favorites = null;
    private HashMap<String, String> forbidden = null;
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

        // Retrieves the search radius value
        // (default or set by the user)
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

    // When map is ready to be used
    @Override
    public void onMapReady(GoogleMap googleMap) {
        Log.d(TAG, "INSIDE -----------> onMApReady");
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

                TextView title = ((TextView) infoWindow.findViewById(R.id.title));
                title.setText(marker.getTitle());

                TextView snippet = ((TextView) infoWindow.findViewById(R.id.snippet));
                snippet.setText(marker.getSnippet());

                return infoWindow;
            }
        });

        mMapAPI.setMap(mMap);

        // Prompt the user for permission.
        mMapAPI.getLocationPermission();

        // Callback for when map is loaded
        mMap.setOnMapLoadedCallback(this);
    }

    @Override
    public void onMapLoaded() {
        Log.d(TAG, "INSIDE ----------> onMApLoaded");

        location = mMapAPI.getLocation();
        LatLng point = new LatLng(location.getLatitude(), location.getLongitude());
        mCallback.setLatLng(point);
        mCallback.setMap(mMap);

        // Set search radius around user location
        drawCircle(point);
        try {
            // Look for near by restaurants
            findNearByRestaurants(point, false);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Draws a circle on the map, depending
    // on the search radius size.
    public void drawCircle(LatLng point) {
        mMap.clear();
        searchRadius = mCallback.getRadius();
        mMap.addCircle(new CircleOptions()
                .center(point)
                .radius(searchRadius)
                .strokeColor(Color.rgb(102, 40, 0))
                .fillColor(Color.TRANSPARENT));
    }

    public void findNearByRestaurants(LatLng p, boolean onlyFavorites) throws IOException {
        GetNearbyPlacesData getNearbyPlacesData = new GetNearbyPlacesData();
        getNearbyPlacesData.execute(p,onlyFavorites);
    }

    // Async task calls the Yelp API
    public class GetNearbyPlacesData extends AsyncTask<Object, String, String> {

        LatLng p;
        boolean onlyFavorites;

        @Override
        protected String doInBackground(Object... params) {
            p = (LatLng) params[0];
            onlyFavorites = (Boolean) params[1];
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

        // Creates restaurant objects from the Yelp API responses
        // and placing markers on the map
        private void showNearbyPlaces(List<HashMap<String, String>> hm) {

            Log.d("showNearByPlaces", "size of near by places list ------> " + hm.size());
            searchCost = mCallback.getCost();
            searchRating = mCallback.getRating();
            favorites = mCallback.getFavorites();
            forbidden = mCallback.getForbidden();

            // Create arrayList of restaurants
            ArrayList<RestaurantInfo> list =  new ArrayList<>();

            for (int i = 0; i < hm.size(); i++) {
                MarkerOptions markerOptions = new MarkerOptions();
                HashMap<String, String> place = hm.get(i);
                if (place.get("lat") == null || place.get("lng") == null || place.get("rating") == null) {continue;}

                // If outside the search radius
                String distance = place.get("distance");
                if (Double.parseDouble(distance) > searchRadius) {continue;}

                // If not open at the moment
                String status = place.get("is_closed");
                if (status.equals("closed")) {continue;}

                // If over the search cost
                String cost = place.get("cost");
                int cInt = 0;
                if (cost.equals("$")) {cInt = 1;}
                else if (cost.equals("$$")) {cInt = 2;}
                else if (cost.equals("$$$")) {cInt = 3;}
                else if (cost.equals("$$$$")) {cInt = 4;}
                if (cInt > searchCost) {continue;}

                // If below the search rating
                double rating = Double.parseDouble(place.get("rating"));
                if (rating < searchRating) {continue;}

                // Rest of restaurant info
                DecimalFormat value = new DecimalFormat("#.#");
                String d = value.format(Double.parseDouble(distance) / 1609.344);
                double lat = Double.parseDouble(place.get("lat"));
                double lng = Double.parseDouble(place.get("lng"));
                String placeName = place.get("name");
                String address = place.get("address");
                String cuisine = place.get("cuisine");
                String imgURL = place.get("image");
                String reviewCount = place.get("review_count");
                String phoneNum = place.get("phone");
                String id = place.get("id");

                // Check if current restaurant is in
                // the user's favorites or forbidden
                if (forbidden.containsKey(id)) {continue;}
                boolean isFavorite = false;
                if (favorites.containsKey(id)) {isFavorite = true;}
                else {if (onlyFavorites) {continue;}}

                // Create a restaurant object
                list.add(new RestaurantInfo(placeName, address, phoneNum, cuisine, rating, cost, d, imgURL, reviewCount, isFavorite, id));

                // Create marker on the map
                LatLng latLng = new LatLng(lat, lng);
                markerOptions.position(latLng);
                markerOptions.title(placeName);
                markerOptions.snippet(cuisine + " - " + rating);
                if (isFavorite) {
                    markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW));
                } else if (rating >= 4) {
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

            // Update/initialize recyclerView
            mCallback.updateRecyclerView();
        }
    }

    @Override
    public void fragmentBecameVisible() {
        favorites = mCallback.getFavorites();
        forbidden = mCallback.getForbidden();
        searchRadius = mCallback.getRadius();
        searchCost = mCallback.getCost();
        searchRating = mCallback.getRating();
    }
}
