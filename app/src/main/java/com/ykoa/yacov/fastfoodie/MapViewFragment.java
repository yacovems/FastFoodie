package com.ykoa.yacov.fastfoodie;

        import android.content.Context;
        import android.content.res.Resources;
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

        import com.google.android.gms.maps.CameraUpdateFactory;
        import com.google.android.gms.maps.GoogleMap;
        import com.google.android.gms.maps.OnMapReadyCallback;
        import com.google.android.gms.maps.SupportMapFragment;
        import com.google.android.gms.maps.model.BitmapDescriptorFactory;
        import com.google.android.gms.maps.model.CircleOptions;
        import com.google.android.gms.maps.model.LatLng;
        import com.google.android.gms.maps.model.LatLngBounds;
        import com.google.android.gms.maps.model.MapStyleOptions;
        import com.google.android.gms.maps.model.Marker;
        import com.google.android.gms.maps.model.MarkerOptions;

        import org.json.JSONException;
        import org.json.JSONObject;

        import java.io.IOException;
        import java.io.Serializable;
        import java.util.ArrayList;
        import java.util.HashSet;

/**
 * Created by yacov on 3/7/2018.
 */

public class MapViewFragment extends Fragment implements FragmentInterface,
        GoogleMap.OnMapLoadedCallback, OnMapReadyCallback, Serializable {

    private static final String TAG = "MapViewFragment";
    private boolean onlyFavorites;
    private FragmentCommunication mCallback;
    GoogleMapsAPI mMapAPI;
    GoogleMap mMap;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater,
                             @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.map_fragment,
                container, false);

        // Create a map
         mMapAPI = new GoogleMapsAPI(getActivity(), savedInstanceState);

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

        try {
            // Customise the styling of the base map using a JSON object defined
            // in a raw resource file.
            boolean success = googleMap.setMapStyle(
                    MapStyleOptions.loadRawResourceStyle(
                            getContext(), R.raw.style_json));

            if (!success) {
                Log.e(TAG, "Style parsing failed.");
            }
        } catch (Resources.NotFoundException e) {
            Log.e(TAG, "Can't find style. Error: ", e);
        }

        // Use a custom info window adapter to handle
        // multiple lines of text in the info window contents
        mMap.setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter() {

            @Override
            // Return null here, so that getInfoContents() is called next.
            public View getInfoWindow(Marker arg0) {
                return null;
            }

            @Override
            public View getInfoContents(Marker marker) {
                // Inflate the layouts for the info window, title and snippet.
                View infoWindow = getActivity().getLayoutInflater().inflate(R.layout.custom_info_window,
                        (FrameLayout) getActivity().findViewById(R.id.map), false);

                TextView title = (TextView) infoWindow.findViewById(R.id.title);
                title.setText(marker.getTitle());

                TextView snippet = (TextView) infoWindow.findViewById(R.id.snippet);
                snippet.setText(marker.getSnippet());

                return infoWindow;
            }
        });

        mMapAPI.setMap(mMap);

        // Prompt the user for permission.
        mMapAPI.getLocationPermission(getActivity());

        // Callback for when map is loaded
        mMap.setOnMapLoadedCallback(this);
    }

    @Override
    public void onMapLoaded() {
        Log.d(TAG, "INSIDE ----------> onMApLoaded");

        Location location = mMapAPI.getLocation();
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
        mMap.addCircle(new CircleOptions()
                .center(point)
                .radius(mCallback.getRadius())
                .strokeColor(Color.rgb(102, 40, 0))
                .fillColor(Color.TRANSPARENT));
    }

    public void findNearByRestaurants(LatLng p, boolean onlyFavorites) throws IOException {
        GetNearbyPlacesData getNearbyPlacesData = new GetNearbyPlacesData();
        this.onlyFavorites = onlyFavorites;
        getNearbyPlacesData.execute(p);
    }

    // Async task calls the Yelp API
    public class GetNearbyPlacesData extends AsyncTask<Object, String, ArrayList<RestaurantInfo>> {

        LatLng p;

        @Override
        protected ArrayList<RestaurantInfo> doInBackground(Object... params) {

            p = (LatLng) params[0];
            final YelpService yelpService = new YelpService();


            final int YELP_API_LIMIT = 50;
            final int MAX_OFFSET_NUM = 4;
            ArrayList<String> jsonData = new ArrayList<>();
            ArrayList<RestaurantInfo> placesList = new ArrayList<>();

            try {
                int totalRestaurants = 0;
                for (int i = 0; i < MAX_OFFSET_NUM; i++) {
                    jsonData.add(yelpService.setYelpRequest(getContext(),
                            p.latitude, p.longitude, mCallback.getRadius(), YELP_API_LIMIT,
                            i * YELP_API_LIMIT, "restaurants"));
                    totalRestaurants = new JSONObject(jsonData.get(i)).getInt("total");
                    if (totalRestaurants <= i * YELP_API_LIMIT) {break;}
                }
                System.out.println("-------------------> total of restaurants found in this area: " + totalRestaurants);

                // Parse the Yelp API response
                DataParser dataParser = new DataParser(mCallback.getFavorites(), mCallback.getForbidden());
                for (String json : jsonData) {
                    placesList.addAll(dataParser.parse(json));
                }
                mCallback.setRestaurantList(placesList);

            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return placesList;
        }

        @Override
        protected void onPostExecute(ArrayList<RestaurantInfo> placesList) {
            Log.d("onPostExecute", "parse jsonObject");

            // Show restaurants on the map
            showNearbyPlaces(placesList, onlyFavorites);

            // Update/initialize recyclerView
            mCallback.updateRecyclerView(mCallback.getIsRemovedList());
        }
    }

    // Creates restaurant objects from the Yelp API responses
    // and placing markers on the map
    public void showNearbyPlaces(ArrayList<RestaurantInfo> placesList, boolean onlyFavorites) {

        Log.d("showNearByPlaces", "size of near by places list ------> " + placesList.size());

        final double METERS_TO_MILES = 1609.344;

        // Create arrayList of restaurants
        ArrayList<RestaurantInfo> tempList =  new ArrayList<>();
        ArrayList<RestaurantInfo> tempForbiddenList =  new ArrayList<>();

        for (int i = 0; i < placesList.size(); i++) {

            boolean cuisineExists = false;

            MarkerOptions markerOptions = new MarkerOptions();
            RestaurantInfo restaurant = placesList.get(i);
            System.out.println("------------------>  Res name: " + restaurant.getName() + ", is forbidden: " + restaurant.getIsForbidden());

            // If outside the search radius
            String distance = restaurant.getDistance();
            if (Double.parseDouble(distance) > mCallback.getRadius() / METERS_TO_MILES) {continue;}

            // If over the search cost
            String cost = restaurant.getCost();
            int cInt = 0;
            if (cost.equals("$")) {cInt = 1;}
            else if (cost.equals("$$")) {cInt = 2;}
            else if (cost.equals("$$$")) {cInt = 3;}
            else if (cost.equals("$$$$")) {cInt = 4;}
            if (cInt > mCallback.getCost()) {continue;}

            // If below the search rating
            double rating = Double.parseDouble(restaurant.getRating());
            if (rating < mCallback.getRating()) {continue;}

            // Check if current restaurant is in
            // the user's favorites or forbidden
            if (restaurant.getIsForbidden() == 1) {
                tempForbiddenList.add(restaurant);
                continue;
            }
            if (restaurant.getIsFavorite() == 0 && onlyFavorites) {continue;}

            // Check if the restaurant's cuisine is in the cuisines set.
            // If it is not, don't show it on the map.
            HashSet<String> cuisines = mCallback.getCuisines();
            if (!cuisines.isEmpty()) {

                for (String title : restaurant.getCuisine().toLowerCase().split(" ")) {
                    if (cuisines.contains(title)) {cuisineExists = true;}
                }
                if (!cuisineExists && !cuisines.contains(restaurant.getCuisine().toLowerCase())) {continue;}
            }

            // Create marker on the map
            markerOptions.position(restaurant.getLocation());
            markerOptions.title(restaurant.getName());
            markerOptions.snippet(restaurant.getCuisine() + " - " + restaurant.getRating());

            // Check which pin color should be displayed on the map
            if (restaurant.getIsFavorite() == 1) {
                markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW));
            } else if (rating >= 4) {
                markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
            } else if (rating >= 3) {
                markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE));
            } else {
                markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
            }

            // Add restaurant to temp list
            tempList.add(restaurant);

            // Add marker onto the map view
            mMap.addMarker(markerOptions);
        }

        // Set camera to show all markers
        mMapAPI.setCamera(mCallback.getRadius());

        // Set restaurant list
        mCallback.setTempRestaurantList(tempList);

        // Set temporary forbidden list
        mCallback.setTempForbiddenList(tempForbiddenList);
    }

    @Override
    public void fragmentBecameVisible() {
        // Hide sort button
        mCallback.hideSortButton();
    }
}
