package com.ykoa.yacov.fastfoodie;

import android.app.Activity;
import android.app.LauncherActivity;
import android.content.ClipData;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.ScaleDrawable;
import android.os.Bundle;
import android.text.Layout;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.Spinner;
import android.widget.Toast;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener, FragmentCommunication {

    private static final String TAG = "MainActivity";

    // Default search parameters
    private final int DEFAULT_SEARCH_RADIUS = 500;
    private final int DEFAULT_SEARCH_COST = 4;
    private final int DEFAULT_SEARCH_RATING = 1;

    private int searchRadius = DEFAULT_SEARCH_RADIUS;
    private int searchCost = DEFAULT_SEARCH_COST;
    private int searchRating = DEFAULT_SEARCH_RATING;

    private PopupWindow popup;
    private ViewPager mViewPager;
    private ArrayList<RestaurantInfo> mRestaurantList;
    private ImageButton[] filterButtons;
    private ImageButton[] popupButtons;
    private int filterBtnID;
    private Spinner cuisineSpinner;
    private ArrayAdapter<CharSequence> adapter;
    private boolean hasChanged;
    private GoogleMap mMap = null;
    private RestaurantListFragment RLF = null;
    private MapViewFragment MVF = null;
    private LatLng deviceLocation = null;
    private HashMap<String, RestaurantInfo> favorites = null;
    private HashMap<String, RestaurantInfo> forbidden = null;
    private String userName;
    private String userImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        // Get user info from Facebook initial login
        Intent previousIntent = getIntent();
        Log.d(TAG, "------- Entered MainActivity from LoginActivity");
        userName = previousIntent.getStringExtra("user_name");
        userImage = previousIntent.getStringExtra("user_image");

        // Initialize filter buttons
        buttons();

        // Set navigation drawer
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setItemIconTintList(null);
        navigationView.setNavigationItemSelectedListener(this);

        // Set nav drawer user info
        Menu navMenu = navigationView.getMenu();
        navMenu.getItem(0).setTitle(userName);
        new DownloadImageTask(navigationView.getMenu(), getResources()).execute(userImage);

        // Initialize array list of restaurants.
        mRestaurantList = new ArrayList<>();

        // Set up the ViewPager with the sections adapter.
        mViewPager = findViewById(R.id.container);
        setupViewPager(mViewPager);

        TabLayout tabLayout = findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(mViewPager);
    }

    public void buttons() {
        // 5 filter buttons
        filterButtons = new ImageButton[5];

        ImageButton b = (ImageButton) findViewById(R.id.distance);
        Drawable d = getResources().getDrawable(R.drawable.walk);
        makeFilterButton(d, b,0, 2);

        b = (ImageButton) findViewById(R.id.cost);
        d = getResources().getDrawable(R.drawable.cost);
        makeFilterButton(d, b,1, 5);

        b = (ImageButton) findViewById(R.id.rating);
        d = getResources().getDrawable(R.drawable.star);
        makeFilterButton(d, b, 2, 5);

        b = (ImageButton) findViewById(R.id.cuisine);
        d = getResources().getDrawable(R.drawable.cuisine);
        makeFilterButton(d, b, 3, 10);

        b = (ImageButton) findViewById(R.id.favorite);
        d = getResources().getDrawable(R.drawable.favorite_border);
        makeFilterButton(d, b, 4, 5);
    }

    private void makeFilterButton(Drawable drawable, ImageButton button,
                                  final int buttonNum, final int size) {
        filterButtons[buttonNum] = button;
        filterButtons[buttonNum].setImageDrawable(drawable);
        filterButtons[buttonNum].setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                filterBtnID = buttonNum;
                int[] location = new int[2];
                filterButtons[filterBtnID].getLocationOnScreen(location);
                showPopup(MainActivity.this, location[0], location[1],
                        filterButtons[buttonNum].getWidth(), filterButtons[buttonNum].getHeight(), size);
            }
        });
    }

    private void makePopupButton(final int radius,
                                 final int cost, final int rating, int buttonNum,
                                 ImageButton button, final Drawable drawable) {

        if (drawable == null) {
            Log.d(TAG, "--------------------->>>>>> noooooooooooo " + buttonNum);
        } else if (button == null) {
            Log.d(TAG, "--------------------->>>>>> noooooooooooo2 " + buttonNum);
        }

        popupButtons[buttonNum] = button;
        popupButtons[buttonNum].setImageDrawable(drawable);
        popupButtons[buttonNum].setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popup.dismiss();
                if (radius != 0) {setRadius(radius);}
                if (cost != 0) {setCost(cost);}
                if (rating != 0) {setRating(rating);}

                MVF.drawCircle(deviceLocation);
                try {
                    MVF.findNearByRestaurants(deviceLocation);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                filterButtons[filterBtnID].setImageDrawable(drawable);
            }
        });

    }

    // The method that displays the popup.
    private void showPopup(final Activity context, int x, int y, int width, int height, int btnNumber) {
        int popupWidth = width;
        int popupHeight = height * btnNumber;

        // Inflate the popup_layout.xml
        LinearLayout viewGroup = null;
        LayoutInflater layoutInflater = null;
        View layout = null;
        ImageButton b = null;
        Drawable d = null;

        // 11 popup buttons
        popupButtons = new ImageButton[11];

        if (filterBtnID == 0) {
            viewGroup = (LinearLayout) context.findViewById(R.id.popup0);
            layoutInflater = (LayoutInflater) context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            layout = layoutInflater.inflate(R.layout.distance_popup_layout, viewGroup);

            b = (ImageButton) layout.findViewById(R.id.driveD);
            d = getResources().getDrawable(R.drawable.drive);
            makePopupButton(500, 0, 0, 0, b, d);

            b = (ImageButton) layout.findViewById(R.id.walkD);
            d = getResources().getDrawable(R.drawable.walk);
            makePopupButton(1000, 0, 0, 1, b, d);

        } else if (filterBtnID == 1) {
            viewGroup = (LinearLayout) context.findViewById(R.id.popup1);
            layoutInflater = (LayoutInflater) context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            layout = layoutInflater.inflate(R.layout.cost_popup_layout, viewGroup);

            b = (ImageButton) layout.findViewById(R.id.dollarSign4);
            d = getResources().getDrawable(R.drawable.cost);
            makePopupButton(0, 4, 0, 2, b, d);

            b = (ImageButton) layout.findViewById(R.id.dollarSign3);
            d = getResources().getDrawable(R.drawable.cost);
            makePopupButton(0, 3, 0, 3, b, d);

            b = (ImageButton) layout.findViewById(R.id.dollarSign2);
            d = getResources().getDrawable(R.drawable.cost);
            makePopupButton(0, 2, 0, 4, b, d);

            b = (ImageButton) layout.findViewById(R.id.dollarSign);
            d = getResources().getDrawable(R.drawable.cost);
            makePopupButton(0, 1, 0, 5, b, d);

        } else if (filterBtnID == 2) {
            viewGroup = (LinearLayout) context.findViewById(R.id.popup2);
            layoutInflater = (LayoutInflater) context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            layout = layoutInflater.inflate(R.layout.rating_popup_layout, viewGroup);

            b = (ImageButton) layout.findViewById(R.id.star5);
            d = getResources().getDrawable(R.drawable.star);
            makePopupButton(0, 0, 5, 6, b, d);

            b = (ImageButton) layout.findViewById(R.id.star4);
            d = getResources().getDrawable(R.drawable.star);
            makePopupButton(0, 0, 4, 7, b, d);

            b = (ImageButton) layout.findViewById(R.id.star3);
            d = getResources().getDrawable(R.drawable.star);
            makePopupButton(0, 0, 3, 8, b, d);

            b = (ImageButton) layout.findViewById(R.id.star2);
            d = getResources().getDrawable(R.drawable.star);
            makePopupButton(0, 0, 2, 9, b, d);

            b = (ImageButton) layout.findViewById(R.id.star);
            d = getResources().getDrawable(R.drawable.star);
            makePopupButton(0, 0, 1, 10, b, d);

        } else if (filterBtnID == 3) {
            viewGroup = (LinearLayout) context.findViewById(R.id.popup3);
            layoutInflater = (LayoutInflater) context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            layout = layoutInflater.inflate(R.layout.cuisine_popup_layout, viewGroup);

            cuisineSpinner = (Spinner) findViewById(R.id.spinner);
            // Create an ArrayAdapter using the string array and a default spinner layout
            adapter = ArrayAdapter.createFromResource(this,
                    R.array.cuisine_array, android.R.layout.simple_spinner_item);
            // Specify the layout to use when the list of choices appears
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        }

        // Creating the PopupWindow
        popup = new PopupWindow(context);
        popup.setAnimationStyle(R.style.Animation);
        popup.setContentView(layout);
        popup.setWidth(popupWidth);
        popup.setHeight(popupHeight);
        popup.setFocusable(true);

        // Apply the adapter to the spinner
        if (adapter != null) {
            cuisineSpinner.setAdapter(adapter);
        }
        // Displaying the popup at the specified location, + offsets.
        popup.showAtLocation(layout, Gravity.NO_GRAVITY, x, y - popupHeight + 10);
    }
    
    private void makeLayout() {
        
    }

    private void setupViewPager(ViewPager viewPager) {
        final SectionsPageAdapter adapter = new SectionsPageAdapter(getSupportFragmentManager());

        // Send parcel with task list data to both
        // map and restaurant list fragments

        MVF = new MapViewFragment();
        Bundle b = new Bundle();
        MVF.setArguments(b);

        RLF = new RestaurantListFragment();
        Bundle b2 = new Bundle();
        b2.putParcelableArrayList("restaurant list", mRestaurantList);
        RLF.setArguments(b2);

        adapter.addFragment(MVF, "Map");
        adapter.addFragment(RLF, "List");
        viewPager.setAdapter(adapter);

        // Instead of recreating the activity, notify the adapter when
        // a new fragment was picked and update the data set.
        mViewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(final int i, final float v, final int i2) {
            }
            @Override
            public void onPageSelected(final int i) {
                FragmentInterface fragment = (FragmentInterface)
                        adapter.instantiateItem(mViewPager, i);
                if (fragment != null) {
                    fragment.fragmentBecameVisible();
                }
            }
            @Override
            public void onPageScrollStateChanged(final int i) {
            }
        });
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_profile) {
            
        } else if (id == R.id.nav_review) {

        } else if (id == R.id.nav_favorits) {

        } else if (id == R.id.nav_settings) {
            Intent i = new Intent(MainActivity.this, SettingsActivity.class);
            startActivity(i);
        } else if (id == R.id.nav_share) {

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
    
    // Fragment communication
    @Override
    public void setRadius(int radius) {
        searchRadius = radius;
    }

    @Override
    public int getRadius() {return searchRadius;}

    @Override
    public void setRestaurantList(ArrayList<RestaurantInfo> list) {
        mRestaurantList = list;
    }

    @Override
    public ArrayList<RestaurantInfo> getRestaurantList() {
        return mRestaurantList;
    }

    @Override
    public void setHasChanged(boolean changed) {this.hasChanged = changed;}

    @Override
    public boolean getHasChanged() {return hasChanged;}

    @Override
    public GoogleMap getMap() {
        return mMap;
    }

    @Override
    public void setMap(GoogleMap map) {
        mMap = map;
    }

    @Override
    public LatLng getLatLng() {return deviceLocation;}

    @Override
    public void setLatLng(LatLng position) {
        deviceLocation = position;
    }

    @Override
    public void setCost(int cost) {
        searchCost = cost;
    }

    @Override
    public int getCost() {
        return searchCost;
    }

    @Override
    public void setRating(int rating) {
        searchRating = rating;
    }

    @Override
    public int getRating() {
        return searchRating;
    }
}
