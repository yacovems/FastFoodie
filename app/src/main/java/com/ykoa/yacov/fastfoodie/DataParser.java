package com.ykoa.yacov.fastfoodie;

import android.util.Log;
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

    public List<HashMap<String, String>> parse(String jsonData) {
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

    private List<HashMap<String, String>> getPlaces(JSONArray jsonArray) {

        int placesCount = jsonArray.length();
        List<HashMap<String, String>> placesList = new ArrayList<>();

        HashMap<String, String> placeMap = null;

        for (int i = 0; i < placesCount; i++) {
            try {
                placeMap = getPlace((JSONObject) jsonArray.get(i));
                placesList.add(placeMap);

            } catch (JSONException e) {
                Log.d("Places", "Error in Adding places");
                e.printStackTrace();
            }
        }
        return placesList;
    }

    private HashMap<String, String> getPlace(JSONObject restaurantJSON) {
        HashMap<String, String> place = new HashMap<String, String>();
        try {
            // Get name
            String name = restaurantJSON.getString("name");

            // Get phone number
            String phone = restaurantJSON.optString("display_phone", "Phone not available");

            // Get yelp website
            String website = restaurantJSON.getString("url");

            // Get rating
            double rating = restaurantJSON.getDouble("rating");

            // Get coordinates
            double latitude = 0;
            double longitude = 0;
            if (restaurantJSON.getJSONObject("coordinates") != null) {
                JSONObject coordinates = restaurantJSON.getJSONObject("coordinates");
                latitude = coordinates.getDouble("latitude");
                longitude = coordinates.getDouble("longitude");
            }

            // Get cuisine
            JSONArray categoriesObject = restaurantJSON.getJSONArray("categories");
            String cuisine = categoriesObject.getJSONObject(0).getString("alias");

            // Get Address
            JSONObject location = restaurantJSON.getJSONObject("location");
            String address = location.getString("address1");
            address += ", " + location.getString("city");

            // Get price range
            String cost = restaurantJSON.getString("price");

            // Get distance
            double distance = restaurantJSON.getDouble("distance");

            // Get image
            String img = restaurantJSON.getString("image_url");

            // Get review count
            int reviewCount = restaurantJSON.getInt("review_count");

            // Get is_closed
            boolean isClosed = restaurantJSON.getBoolean("is_closed");
            String status = "open";
            if (isClosed) {status = "closed";}

            // Fill the place hash map.
            place.put("name", name);
            place.put("address", address);
            place.put("rating", "" + rating);
            place.put("cost", cost);
            place.put("lat", "" + latitude);
            place.put("lng", "" + longitude);
            place.put("cuisine", cuisine);
            place.put("phone", phone);
            place.put("website", website);
            place.put("distance", "" + distance);
            place.put("image", img);
            place.put("review_count", "" + reviewCount);
            place.put("is_closed", status);

        } catch (JSONException e) {
            e.printStackTrace();
        }

        return place;
    }
}