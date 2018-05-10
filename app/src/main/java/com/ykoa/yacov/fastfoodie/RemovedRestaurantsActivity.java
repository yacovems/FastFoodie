package com.ykoa.yacov.fastfoodie;

import android.content.Intent;
import android.support.design.widget.NavigationView;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

public class RemovedRestaurantsActivity
        extends AppCompatActivity
        implements FragmentCommunication,
        NavigationView.OnNavigationItemSelectedListener{

    private static final String TAG = "RemovedResListFragment";
    ArrayList<RestaurantInfo> restaurantsList = new ArrayList<>();
    ArrayList<RestaurantInfo> tempForbiddenList = new ArrayList<>();

    HashMap<String, String> favorites = new HashMap<>();
    HashMap<String, String> forbidden = new HashMap<>();

    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    String userId = "";
    private Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = getIntent();
        restaurantsList = intent.getParcelableArrayListExtra("restaurants_list");
        tempForbiddenList = intent.getParcelableArrayListExtra("forbidden_list");
        favorites = (HashMap) intent.getSerializableExtra("favorites");
        forbidden = (HashMap) intent.getSerializableExtra("forbidden");
        userId = intent.getStringExtra("user_id");
        setContentView(R.layout.activity_removed_restaurants);

        // Set toolbar
        toolbar = (Toolbar) findViewById(R.id.toolbar2);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        // Set navigation drawer
        setUpNavDrawer(toolbar);
    }

    private void setUpNavDrawer(Toolbar toolbar) {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout2);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar,
                R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view2);
        navigationView.setItemIconTintList(null);
        navigationView.setNavigationItemSelectedListener(this);

        // Set nav drawer user info
        Menu navMenu = navigationView.getMenu();
//        navMenu.getItem(0).setTitle(userName);
//        new DownloadImageTask(navigationView.getMenu(),
//                getResources()).execute(userImage);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {

        // Get width and height of the device's screen
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int width = displayMetrics.widthPixels;
        int height = displayMetrics.heightPixels;

        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_profile) {

        } else if (id == R.id.nav_removed_restaurants) {

        } else if (id == R.id.nav_share) {

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout2);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();


//        finish();
    }

    @Override
    public void setRadius(int radius) {

    }

    @Override
    public int getRadius() {
        return 0;
    }

    @Override
    public void setRestaurantList(ArrayList<RestaurantInfo> list) {

    }

    @Override
    public void setTempRestaurantList(ArrayList<RestaurantInfo> list) {

    }

    @Override
    public void setTempForbiddenList(ArrayList<RestaurantInfo> list) {

    }

    @Override
    public void setCuisines(HashSet<String> cuisines) {

    }

    @Override
    public HashSet<String> getCuisines() {
        return null;
    }

    @Override
    public ArrayList<RestaurantInfo> getRestaurantList() {
        return restaurantsList;
    }

    @Override
    public ArrayList<RestaurantInfo> getTempRestaurantList() {
        return null;
    }

    @Override
    public ArrayList<RestaurantInfo> getTempForbiddenList() {
        return tempForbiddenList;
    }

    @Override
    public void setIsInitialized(boolean changed) {

    }

    @Override
    public boolean getIsInitialized() {
        return false;
    }

    @Override
    public GoogleMap getMap() {
        return null;
    }

    @Override
    public void setMap(GoogleMap map) {

    }

    @Override
    public LatLng getLatLng() {
        return null;
    }

    @Override
    public void setLatLng(LatLng position) {

    }

    @Override
    public void setCost(int cost) {

    }

    @Override
    public int getCost() {
        return 0;
    }

    @Override
    public void setRating(int rating) {

    }

    @Override
    public int getRating() {
        return 0;
    }

    @Override
    public HashMap<String, String> getFavorites() {
        return favorites;
    }

    @Override
    public void setFavorites(HashMap<String, String> favorites) {

    }

    @Override
    public HashMap<String, String> getForbidden() {
        return forbidden;
    }

    @Override
    public void setForbidden(HashMap<String, String> forbidden) {

    }

    @Override
    public String getUserId() {
        return null;
    }

    @Override
    public void updateDB(HashMap<String, Object> user) {
        db.collection("users").document(userId)
                .update(user)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "Update Cloud Firestore successfully");
                    }
                });
    }

    @Override
    public void updateRecyclerView(boolean isrRemoved) {

    }

    @Override
    public void updateMapView() {
        Intent main = new Intent();
        main.putParcelableArrayListExtra("restaurants_list", restaurantsList);
        main.putParcelableArrayListExtra("forbidden_list", tempForbiddenList);
        main.putExtra("favorites", favorites);
        main.putExtra("forbidden", forbidden);
        setResult(RESULT_OK, main);
    }

    @Override
    public void showSortButton() {

    }

    @Override
    public void hideSortButton() {

    }

    @Override
    public boolean getIsRemovedList() {
        return false;
    }

    @Override
    public void setIsRemovedList(boolean removedList) {

    }
}
