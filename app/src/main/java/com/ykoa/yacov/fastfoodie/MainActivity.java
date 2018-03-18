package com.ykoa.yacov.fastfoodie;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
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

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener,
        FragmentCommunication {

    private static final String TAG = "MainActivity";
    private ViewPager mViewPager;
    private ArrayList<RestaurantInfo> mRestaurantList;
    private ImageButton[] filterButtons;
    private int buttonsID;
    private int searchRadius = 500;
    private Spinner cuisineSpinner;
    private ArrayAdapter<CharSequence> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

        // Initialize filter buttons
        buttons();

        // Set navigation drawer
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

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

        // When a button is clicked, open the popup window
        filterButtons[0] = (ImageButton) findViewById(R.id.distance);
        filterButtons[0].setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {


                buttonsID = 0;
                int[] location = new int[2];
                filterButtons[buttonsID].getLocationOnScreen(location);
                showPopup(MainActivity.this, location[0], location[1],
                        filterButtons[0].getWidth(), filterButtons[0].getHeight(), 2);
            }
        });

        filterButtons[1] = (ImageButton) findViewById(R.id.cost);
        filterButtons[1].setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                buttonsID = 1;
                int[] location = new int[2];
                filterButtons[buttonsID].getLocationOnScreen(location);
                showPopup(MainActivity.this, location[0], location[1],
                        filterButtons[1].getWidth(), filterButtons[1].getHeight(), 4);
            }
        });

        filterButtons[2] = (ImageButton) findViewById(R.id.rating);
        filterButtons[2].setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                buttonsID = 2;
                int[] location = new int[2];
                filterButtons[buttonsID].getLocationOnScreen(location);
                showPopup(MainActivity.this, location[0], location[1],
                        filterButtons[2].getWidth(), filterButtons[2].getHeight(), 5);
            }
        });

        filterButtons[3] = (ImageButton) findViewById(R.id.cuisine);
        filterButtons[3].setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                buttonsID = 3;
                int[] location = new int[2];
                filterButtons[buttonsID].getLocationOnScreen(location);
                showPopup(MainActivity.this, location[0], location[1],
                        filterButtons[3].getWidth(), filterButtons[3].getHeight(), 10);
            }
        });

        filterButtons[4] = (ImageButton) findViewById(R.id.favorite);
        filterButtons[4].setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Update map and list to show only favorites
                // regardless of the current radius

            }
        });
    }

    // The method that displays the popup.
    private void showPopup(final Activity context, int x, int y, int width, int height, int btnNumber) {
        int popupWidth = width;
        int popupHeight = height * btnNumber;

        // Inflate the popup_layout.xml
        LinearLayout viewGroup = (LinearLayout) context.findViewById(R.id.popup0);
        LayoutInflater layoutInflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View layout = layoutInflater.inflate(R.layout.distance_popup_layout, viewGroup);

        if (buttonsID == 0) {
            viewGroup = (LinearLayout) context.findViewById(R.id.popup0);
            layoutInflater = (LayoutInflater) context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            layout = layoutInflater.inflate(R.layout.distance_popup_layout, viewGroup);
        } else if (buttonsID == 1) {
            viewGroup = (LinearLayout) context.findViewById(R.id.popup1);
            layoutInflater = (LayoutInflater) context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            layout = layoutInflater.inflate(R.layout.cost_popup_layout, viewGroup);

        } else if (buttonsID == 2) {
            viewGroup = (LinearLayout) context.findViewById(R.id.popup2);
            layoutInflater = (LayoutInflater) context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            layout = layoutInflater.inflate(R.layout.rating_popup_layout, viewGroup);
        } else if (buttonsID == 3) {
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
        final PopupWindow popup = new PopupWindow(context);
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

        ImageButton walkBtn = (ImageButton) layout.findViewById(R.id.walkD);
        ImageButton driveBtn = (ImageButton) layout.findViewById(R.id.driveD);
//        walkBtn.setOnClickListener(new View.OnClickListener() {
//
//            @Override
//            public void onClick(View v) {
//                popup.dismiss();
//                setRadius(500);
//                Toast.makeText(context, "walking distance", Toast.LENGTH_SHORT).show();
//            }
//        });
//
//        driveBtn.setOnClickListener(new View.OnClickListener() {
//
//            @Override
//            public void onClick(View v) {
//                popup.dismiss();
//                setRadius(1000);
//                Toast.makeText(context, "driving distance", Toast.LENGTH_SHORT).show();
//            }
//        });


    }

    private void setupViewPager(ViewPager viewPager) {
        final SectionsPageAdapter adapter = new SectionsPageAdapter(getSupportFragmentManager());

        // Send parcel with task list data to both
        // map and restaurant list fragments

//        PendingTasksFragment PTF = new PendingTasksFragment();
//        Bundle b = new Bundle();
//        b.putParcelableArrayList("pending list", mTaskList);
//        b.putParcelableArrayList("completed list", mCompletedTaskList);
//        PTF.setArguments(b);

        RestaurantListFragment RLF = new RestaurantListFragment();
        Bundle b2 = new Bundle();
        b2.putParcelableArrayList("restaurant list", mRestaurantList);
        RLF.setArguments(b2);

        adapter.addFragment(new MapViewFragment(), "Map");
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
            // Handle the camera action
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
}
