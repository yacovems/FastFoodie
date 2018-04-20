package com.ykoa.yacov.fastfoodie;

import android.util.Log;

import com.google.android.gms.maps.model.LatLng;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by Yacov on 3/11/2018.
 */

public class DataParser {

    private HashMap<String, String> favorites;
    private HashMap<String, String> forbidden;

    public DataParser(HashMap<String, String> favorites,
                      HashMap<String, String> forbidden) {
        this.favorites = favorites;
        this.forbidden = forbidden;
    }

    public ArrayList<RestaurantInfo> parse(String jsonData) {
        JSONArray jsonArray = null;
        JSONObject jsonObject;

        try {
            jsonObject = new JSONObject(jsonData);
            jsonArray = jsonObject.getJSONArray("businesses");
        } catch (JSONException e) {
            Log.d("Places", "parse error");
            e.printStackTrace();
        }
        return getPlaces(jsonArray);
    }

    private ArrayList<RestaurantInfo> getPlaces(JSONArray jsonArray) {

        int placesCount = jsonArray.length();
        ArrayList<RestaurantInfo> placesList = new ArrayList<>();
        RestaurantInfo placeMap = null;

        for (int i = 0; i < placesCount; i++) {
            try {
                placeMap = getPlace((JSONObject) jsonArray.get(i));
                if (placeMap.getName().equals("no name") ||
                        placeMap.getRating() == -1) {
                    continue;
                }

                placesList.add(placeMap);

            } catch (JSONException e) {
                Log.d("Places", "Error in Adding places");
                e.printStackTrace();
            }
        }
        return placesList;
    }

    private RestaurantInfo getPlace(JSONObject restaurantJSON) {

        String id = "";
        String name = "";
        String phoneNum = "";
        String website = "";
        String cost = "";
        String reviewCount = "";
        String address = "";
        String cuisine = "";
        String imgURL = "";
        String distance = "";

        double rating = 0;
        LatLng latLng = null;

        try {
            // Get id
            id = restaurantJSON.getString("id");

            // Get name
            name = restaurantJSON.optString("name", "no name");

            // Get phone number
            phoneNum = restaurantJSON.optString("phone", "phone not available");

            // Get yelp website
            website = restaurantJSON.optString("url", "https://www.yelp.com/");

            // Get rating
            rating = restaurantJSON.optDouble("rating", -1);

            // Get coordinates
            double latitude = 0;
            double longitude = 0;
            if (restaurantJSON.getJSONObject("coordinates") != null) {
                JSONObject coordinates = restaurantJSON.getJSONObject("coordinates");
                latitude = coordinates.getDouble("latitude");
                longitude = coordinates.getDouble("longitude");
                latLng = new LatLng(latitude, longitude);
            }

            // Get cuisine
            JSONArray categoriesObject = restaurantJSON.getJSONArray("categories");
            cuisine = categoriesObject.getJSONObject(0).getString("title");

            // Get Address
            JSONObject location = restaurantJSON.getJSONObject("location");
            address = location.getString("address1");
            address += ", " + location.getString("city");

            // Get price range
            cost = restaurantJSON.optString("price", "$$");

            // Get distance
            DecimalFormat value = new DecimalFormat("#.#");
            double d = restaurantJSON.getDouble("distance");
            distance = value.format(d / 1609.344);

            // Get image
            imgURL = restaurantJSON.getString("image_url");

            // Get review count
            int rCount = restaurantJSON.optInt("review_count", 0);
            reviewCount = "" + rCount;

        } catch (JSONException e) {
            e.printStackTrace();
        }

        boolean isFavorite = false;
        if (favorites.containsKey(id)) {isFavorite = true;}

        boolean isForbidden = false;
        if (forbidden.containsKey(id)) {isForbidden = true;}

        // Create a restaurant object
        return new RestaurantInfo(name, address, phoneNum, cuisine,
                rating, cost, distance, imgURL, reviewCount, isFavorite, isForbidden, id, latLng, website);
    }
}