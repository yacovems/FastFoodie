package com.ykoa.yacov.fastfoodie;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.Icon;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;

public class MainActivity extends AppCompatActivity implements
        NavigationView.OnNavigationItemSelectedListener,
        FragmentCommunication {

    private static final String TAG = "MainActivity";

    // Default search parameters
    private final int DEFAULT_SEARCH_RADIUS = 300;
    private final int DEFAULT_SEARCH_COST = 4;
    private final int DEFAULT_SEARCH_RATING = 1;
    private final int NUM_FILTER_BUTTONS = 4;
    private final int NUM_POPUP_BUTTONS = 11;
    private final int NUM_CUISINE_BUTTONS = 18;

    private int searchRadius = DEFAULT_SEARCH_RADIUS;
    private int searchCost = DEFAULT_SEARCH_COST;
    private int searchRating = DEFAULT_SEARCH_RATING;

    private ViewPager mViewPager;
    private ArrayList<RestaurantInfo> mRestaurantList;
    private ArrayList<RestaurantInfo> mTempRestaurantList;
    private ArrayList<RestaurantInfo> mTempForbiddenList;
    private ImageButton[] filterButtons;
    private ImageButton[] popupButtons;
    private FloatingActionButton[] sortButtons;
    private Button[] cuisineButtons;
    private boolean[] cuisineButtonState;
    private int filterBtnID;
    private boolean hasChanged;
    private boolean sortBtnClicked;
    private GoogleMap mMap = null;
    private RestaurantListFragment removedRLF = null;
    private RestaurantListFragment RLF = null;
    private MapViewFragment MVF = null;
    private LatLng deviceLocation = null;
    private HashMap<String, String> favorites = null;
    private HashMap<String, String> forbidden = null;
    private String userName;
    private String userImage;
    private String userEmail;
    private String userId;
    private PopupWindow popup;
    private InternalStorageOps iso;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private Toolbar toolbar;
    private boolean onlyFavorites;
    private HashSet<String> cuisines;
    private int[] tabIcons = {
            R.drawable.map,
            R.drawable.restaurants
    };
    private String[] tabLabels = {
            "Map",
            "Restaurants"
    };

    private boolean removedList = false;
    FragmentManager fragmentManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        iso = new InternalStorageOps();

        // Get user info
        getUserInfo();

        // Initialize array list of restaurants.
        mRestaurantList = new ArrayList<>();
        mTempRestaurantList = new ArrayList<>();
        mTempForbiddenList = new ArrayList<>();

        removedRLF = new RestaurantListFragment();
        fragmentManager = getSupportFragmentManager();

        // Initialize cuisine HashSet
        cuisines = new HashSet<>();

        // Setup the ViewPager with the sections adapter
        mViewPager = findViewById(R.id.container);
        setupViewPager(mViewPager);

        // Setup the tab layout
        TabLayout tabLayout = findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(mViewPager);
        setupTabIcons(tabLayout);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Check which request we're responding to
        if (requestCode == 1) {
            // Make sure the request was successful
            if (resultCode == RESULT_OK) {
                mRestaurantList = data.getParcelableArrayListExtra("restaurants_list");
                mTempForbiddenList = data.getParcelableArrayListExtra("forbidden_list");
                favorites = (HashMap) data.getSerializableExtra("favorites");
                forbidden = (HashMap) data.getSerializableExtra("forbidden");

                // After adding back restaurants to future searches,
                // update the map and restaurants list
                updateMapView();
                updateRecyclerView(removedList);
            }
        }
    }

    private void setupViewPager(ViewPager viewPager) {
        final SectionsPageAdapter adapter = new SectionsPageAdapter(getSupportFragmentManager());

        MVF = new MapViewFragment();
        RLF = new RestaurantListFragment();

        adapter.addFragment(MVF, "Map");
        adapter.addFragment(RLF, "Restaurants");
        viewPager.setAdapter(adapter);

        // Instead of recreating the activity, notify the adapter when
        // a new fragment was picked and update the data set.
        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(final int i, final float v, final int i2) {
            }

            @Override
            public void onPageScrollStateChanged(final int i) {
            }

            @Override
            public void onPageSelected(final int i) {
                FragmentInterface fragment = (FragmentInterface)
                        adapter.instantiateItem(mViewPager, i);
                if (fragment != null) {
                    fragment.fragmentBecameVisible();
                }
            }
        });
    }

    private void setupTabIcons(TabLayout tabLayout) {

        for (int i = 0; i < tabLayout.getTabCount(); i++) {
            // Inflate the Parent LinearLayout Container for the tab
            LinearLayout tab = (LinearLayout) LayoutInflater.from(this).inflate(R.layout.custom_tab_layout, null);

            TextView tabLabel = (TextView) tab.findViewById(R.id.tab_label);
            ImageView tabIcon = (ImageView) tab.findViewById(R.id.tab_icon);

            // Set the tab's label and icon
            tabLabel.setText(tabLabels[i]);
            tabIcon.setImageResource(tabIcons[i]);

            // Apply custom view
            tabLayout.getTabAt(i).setCustomView(tab);
        }
        // Set the thickness of the active tab indicator
        tabLayout.setSelectedTabIndicatorHeight((int) (5 * getResources().getDisplayMetrics().density));
    }

    // Get user data from Firebase DB
    private void getUserInfo() {

        db.collection("users")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            userId = iso.readFile(getApplicationContext(), "login_info").toString();

                            for (DocumentSnapshot userDoc : task.getResult()) {
                                if (userId.equals(userDoc.getId())){

                                    userName = userDoc.getData().get("full name").toString();
                                    userImage = userDoc.getData().get("picture").toString();
                                    userEmail = userDoc.getData().get("email").toString();
                                    searchCost = Integer.parseInt(userDoc.getData().get("cost").toString());
                                    searchRadius = Integer.parseInt(userDoc.getData().get("distance").toString());
                                    searchRating = Integer.parseInt(userDoc.getData().get("rating").toString());
                                    favorites = (HashMap<String, String>) userDoc.getData().get("favorites");
                                    forbidden = (HashMap<String, String>) userDoc.getData().get("forbidden");

                                    // Create filter, cuisine, and sort buttons
                                    setUpFilterButtons();
                                    setUpCuisineButtons();
                                    setUpSortButtons();

                                    // Set navigation drawer
                                    setUpNavDrawer(toolbar);
                                }
                            }
                        } else {
                            Log.w(TAG, "Error getting documents.", task.getException());
                        }
                    }
                });
    }

    private void setUpNavDrawer(Toolbar toolbar) {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar,
                R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setItemIconTintList(null);
        navigationView.setNavigationItemSelectedListener(this);

        // Set nav drawer user info
        Menu navMenu = navigationView.getMenu();
        final MenuItem profile = navMenu.getItem(0);
        profile.setTitle(userName);

        Target target = new Target() {
            @Override
            public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                Bitmap b = Bitmap.createScaledBitmap(bitmap, 100, 100, false);
                BitmapDrawable icon = new BitmapDrawable(b);
                profile.setIcon(icon);
            }

            @Override
            public void onBitmapFailed(Exception e, Drawable errorDrawable) {

            }

            @Override
            public void onPrepareLoad(Drawable placeHolderDrawable) {

            }
        };

        Picasso.get().load(userImage).into(target);
    }

    private void setUpSortButtons() {
        // Initialize sort button
        sortButtons = new FloatingActionButton[4];
        sortButtons[0] = (FloatingActionButton) findViewById(R.id.sort_btn);
        sortButtons[1] = (FloatingActionButton) findViewById(R.id.distance_sort_btn);
        sortButtons[2] = (FloatingActionButton) findViewById(R.id.price_sort_btn);
        sortButtons[3] = (FloatingActionButton) findViewById(R.id.rating_sort_btn);

       for (int i = 0; i < sortButtons.length; i++) {makeSortButton(i);}
    }

    private void makeSortButton(final int buttonNum) {
        sortButtons[buttonNum].setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (buttonNum == 0) {
                    if (!sortBtnClicked) {
                        animateSortButtons(false);
                        sortBtnClicked = true;
                    } else {
                        animateSortButtons(true);
                        sortBtnClicked = false;
                    }
                } else {
                    animateSortButtons(true);
                    sortBtnClicked = false;

                    switch (buttonNum) {
                        case 1:
                            Collections.sort(mTempRestaurantList, new CustomComparator(buttonNum));
                            break;
                        case 2:
                            Collections.sort(mTempRestaurantList, new CustomComparator(buttonNum));
                            break;
                        case 3:
                            Collections.sort(mTempRestaurantList, new CustomComparator(buttonNum));
                            break;
                    }
                    updateRecyclerView(removedList);
                }
            }
        });
    }

    private void animateSortButtons(boolean visible) {
        final Animation load = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.slide_in_from_right);
        final Animation remove = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.slide_out_to_right);

        for (int i = 1; i < sortButtons.length; i++) {
            if (!visible) {
                sortButtons[i].startAnimation(load);
                sortButtons[i].setVisibility(View.VISIBLE);
            } else {
                sortButtons[i].startAnimation(remove);
                sortButtons[i].setVisibility(View.GONE);
            }
        }
    }

    private void setUpFilterButtons() {
        // 4 filter buttons
        filterButtons = new ImageButton[NUM_FILTER_BUTTONS];

        ImageButton b = (ImageButton) findViewById(R.id.distance);
        Drawable d = getResources().getDrawable(R.drawable.walk);
        if (searchRadius != DEFAULT_SEARCH_RADIUS) {
            d = getResources().getDrawable(R.drawable.drive);
        }
        makeFilterButton(d, b,0, 2);

        b = (ImageButton) findViewById(R.id.cost);
        d = getResources().getDrawable(R.drawable.ic_4_dollar);
        if (searchCost == 1) {
            d = getResources().getDrawable(R.drawable.ic_1_dollar);
        } else if (searchCost == 2) {
            d = getResources().getDrawable(R.drawable.ic_2_dollar);
        } else if (searchCost == 3) {
            d = getResources().getDrawable(R.drawable.ic_3_dollar);
        }
        makeFilterButton(d, b,1, 4);

        b = (ImageButton) findViewById(R.id.rating);
        d = getResources().getDrawable(R.drawable.ic_1_star);
        if (searchRating == 2) {
            d = getResources().getDrawable(R.drawable.ic_2_star);
        } else if (searchRating == 3) {
            d = getResources().getDrawable(R.drawable.ic_3_star);
        } else if (searchRating == 4) {
            d = getResources().getDrawable(R.drawable.ic_4_star);
        } else if (searchRating == 5) {
            d = getResources().getDrawable(R.drawable.ic_5_star);
        }
        makeFilterButton(d, b, 2, 5);


        b = (ImageButton) findViewById(R.id.favorite);
        d = getResources().getDrawable(R.drawable.favorite_border);
        makeFavoriteButton(d, b, 3);
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
                filterButtons[buttonNum].getLocationOnScreen(location);
                showPopup(MainActivity.this, location[0], location[1],
                        filterButtons[buttonNum].getWidth(),
                        filterButtons[buttonNum].getHeight(), size);
            }
        });
    }

    private void makeFavoriteButton(Drawable drawable, ImageButton button,
                                    final int buttonNum) {
        filterButtons[buttonNum] = button;
        filterButtons[buttonNum].setImageDrawable(drawable);
        filterButtons[buttonNum].setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (!onlyFavorites) {
                    onlyFavorites = true;
                    Drawable d = getResources().getDrawable(R.drawable.favorite);
                    filterButtons[buttonNum].setImageDrawable(d);
                } else {
                    onlyFavorites = false;
                    Drawable d = getResources().getDrawable(R.drawable.favorite_border);
                    filterButtons[buttonNum].setImageDrawable(d);
                }

                filterBtnID = buttonNum;
                // Draw new circle and find restaurants nearby
                updateMapView();
                updateRecyclerView(removedList);

            }
        });
    }

    private void setUpCuisineButtons() {
        cuisineButtons = new Button[NUM_CUISINE_BUTTONS];
        cuisineButtonState = new boolean[NUM_CUISINE_BUTTONS];

        Button b = (Button) findViewById(R.id.all_btn);
        makeCuisineButton(b,0);

        b = (Button) findViewById(R.id.breakfast_btn);
        makeCuisineButton(b,1);

        b = (Button) findViewById(R.id.coffee_btn);
        makeCuisineButton(b,2);

        b = (Button) findViewById(R.id.american_btn);
        makeCuisineButton(b,3);

        b = (Button) findViewById(R.id.brazilian_btn);
        makeCuisineButton(b,4);

        b = (Button) findViewById(R.id.chinese_btn);
        makeCuisineButton(b,5);

        b = (Button) findViewById(R.id.french_btn);
        makeCuisineButton(b,6);

        b = (Button) findViewById(R.id.greek_btn);
        makeCuisineButton(b,7);

        b = (Button) findViewById(R.id.indian_btn);
        makeCuisineButton(b,8);

        b = (Button) findViewById(R.id.italian_btn);
        makeCuisineButton(b,9);

        b = (Button) findViewById(R.id.japanese_btn);
        makeCuisineButton(b,10);

        b = (Button) findViewById(R.id.korean_btn);
        makeCuisineButton(b,11);

        b = (Button) findViewById(R.id.mexican_btn);
        makeCuisineButton(b,12);

        b = (Button) findViewById(R.id.middle_eastern_btn);
        makeCuisineButton(b,13);

        b = (Button) findViewById(R.id.russian_btn);
        makeCuisineButton(b,14);

        b = (Button) findViewById(R.id.spanish_btn);
        makeCuisineButton(b,15);

        b = (Button) findViewById(R.id.thai_btn);
        makeCuisineButton(b,16);

        b = (Button) findViewById(R.id.brunch_btn);
        makeCuisineButton(b,17);
    }

    private void makeCuisineButton(Button button, final int buttonNum) {
        cuisineButtons[buttonNum] = button;

        // Set the "All" cuisine button as active.
        // Shows the user that all cuisines are looked at.
        if (buttonNum == 0) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                cuisineButtons[buttonNum].setBackgroundTintList(ContextCompat
                        .getColorStateList(getApplicationContext(), R.color.colorAccent));
            }
        }

        // "All" button (buttonNum == 0)
        // Rest of cuisine buttons
        if (buttonNum == 0) {

            cuisineButtons[buttonNum].setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    for (int i = 0; i < cuisineButtons.length; i++) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                            if (i == 0) {
                                cuisineButtons[buttonNum].setBackgroundTintList(ContextCompat
                                        .getColorStateList(getApplicationContext(), R.color.colorAccent));
                            } else {
                                cuisineButtons[i].setBackgroundTintList(ContextCompat
                                        .getColorStateList(getApplicationContext(), R.color.lightBlue));
                            }
                        }
                    }
                    cuisines.clear();

                    // Update map
                    updateMapView();
                    updateRecyclerView(removedList);
                }
            });

        } else {

            cuisineButtons[buttonNum].setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    String cuisineName = cuisineButtons[buttonNum].getText().toString();

                    // Check if cuisine button is already clicked or not
                    if (cuisineButtonState[buttonNum]) {

                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                            // Make the clicked button gray
                            cuisineButtons[buttonNum].setBackgroundTintList(ContextCompat
                                    .getColorStateList(getApplicationContext(), R.color.lightBlue));
                        }

                        // Remove the cuisine from the cuisines set
                        cuisineButtonState[buttonNum] = false;
                        cuisines.remove(cuisineName.toLowerCase());
                        if (cuisines.isEmpty()) {
                            // If no cuisine filters are in use, highlight "All"
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                                cuisineButtons[0].setBackgroundTintList(ContextCompat
                                        .getColorStateList(getApplicationContext(), R.color.colorAccent));
                            }
                        }

                    } else {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                            // Make "All" button gray
                            cuisineButtons[0].setBackgroundTintList(ContextCompat
                                    .getColorStateList(getApplicationContext(), R.color.lightBlue));

                            // Make the clicked button colored
                            cuisineButtons[buttonNum].setBackgroundTintList(ContextCompat
                                    .getColorStateList(getApplicationContext(), R.color.colorAccent));
                        }

                        cuisineButtonState[buttonNum] = true;
                        cuisines.add(cuisineName.toLowerCase());
                    }


                    // Update map
                    updateMapView();
                    updateRecyclerView(removedList);
                }
            });
        }
    }

    private void makePopupButton(final int radius,
                                 final int cost, final int rating, final int buttonNum,
                                 ImageButton button, final Drawable drawable) {

        popupButtons[buttonNum] = button;
        popupButtons[buttonNum].setImageDrawable(drawable);
        popupButtons[buttonNum].setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popup.dismiss();

                // Removes everything off the map
                // and looks for adjusted search parameters
                if (radius != 0) {setRadius(radius);}
                if (cost != 0) {setCost(cost);}
                if (rating != 0) {setRating(rating);}

                // Send info to Firebase DB
                HashMap<String, Object> user = new HashMap<>();
                user.put("cost", searchCost);
                user.put("rating", searchRating);
                user.put("distance", searchRadius);
                updateDB(user);

                // Draw new circle and find restaurants nearby
                updateMapView();
                filterButtons[filterBtnID].setImageDrawable(drawable);
                updateRecyclerView(false);
            }
        });
    }

    // The method that displays the popup.
    private void showPopup(final Activity context, int x, int y, int width, int height, int size) {

        int popupWidth = width;
        int popupHeight = height * size;

        // Inflate the popup_layout.xml
        LinearLayout viewGroup = null;
        LayoutInflater layoutInflater = null;
        View layout = null;
        ImageButton b = null;
        Drawable d = null;

        // 11 popup buttons
        popupButtons = new ImageButton[NUM_POPUP_BUTTONS];

        if (filterBtnID == 0) {
            viewGroup = (LinearLayout) context.findViewById(R.id.popup0);
            layoutInflater = (LayoutInflater) context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            layout = layoutInflater.inflate(R.layout.distance_popup_layout, viewGroup);

            b = (ImageButton) layout.findViewById(R.id.driveD);
            d = getResources().getDrawable(R.drawable.drive);
            makePopupButton(DEFAULT_SEARCH_RADIUS * 2, 0, 0, 0, b, d);

            b = (ImageButton) layout.findViewById(R.id.walkD);
            d = getResources().getDrawable(R.drawable.walk);
            makePopupButton(DEFAULT_SEARCH_RADIUS, 0, 0, 1, b, d);

        } else if (filterBtnID == 1) {
            viewGroup = (LinearLayout) context.findViewById(R.id.popup1);
            layoutInflater = (LayoutInflater) context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            layout = layoutInflater.inflate(R.layout.cost_popup_layout, viewGroup);

            b = (ImageButton) layout.findViewById(R.id.dollarSign4);
            d = getResources().getDrawable(R.drawable.ic_4_dollar);
            makePopupButton(0, 4, 0, 2, b, d);

            b = (ImageButton) layout.findViewById(R.id.dollarSign3);
            d = getResources().getDrawable(R.drawable.ic_3_dollar);
            makePopupButton(0, 3, 0, 3, b, d);

            b = (ImageButton) layout.findViewById(R.id.dollarSign2);
            d = getResources().getDrawable(R.drawable.ic_2_dollar);
            makePopupButton(0, 2, 0, 4, b, d);

            b = (ImageButton) layout.findViewById(R.id.dollarSign);
            d = getResources().getDrawable(R.drawable.ic_1_dollar);
            makePopupButton(0, 1, 0, 5, b, d);

        } else if (filterBtnID == 2) {
            viewGroup = (LinearLayout) context.findViewById(R.id.popup2);
            layoutInflater = (LayoutInflater) context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            layout = layoutInflater.inflate(R.layout.rating_popup_layout, viewGroup);

            b = (ImageButton) layout.findViewById(R.id.star5);
            d = getResources().getDrawable(R.drawable.ic_5_star);
            makePopupButton(0, 0, 5, 6, b, d);

            b = (ImageButton) layout.findViewById(R.id.star4);
            d = getResources().getDrawable(R.drawable.ic_4_star);
            makePopupButton(0, 0, 4, 7, b, d);

            b = (ImageButton) layout.findViewById(R.id.star3);
            d = getResources().getDrawable(R.drawable.ic_3_star);
            makePopupButton(0, 0, 3, 8, b, d);

            b = (ImageButton) layout.findViewById(R.id.star2);
            d = getResources().getDrawable(R.drawable.ic_2_star);
            makePopupButton(0, 0, 2, 9, b, d);

            b = (ImageButton) layout.findViewById(R.id.star);
            d = getResources().getDrawable(R.drawable.ic_1_star);
            makePopupButton(0, 0, 1, 10, b, d);
        }

        // Creating the PopupWindow
        popup = new PopupWindow(context);
        popup.setAnimationStyle(R.style.Animation);
        popup.setContentView(layout);
        popup.setWidth(popupWidth);
        popup.setHeight(popupHeight);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            popup.setOverlapAnchor(false);
        }
        popup.setFocusable(true);

        // Displaying the popup at the specified location, + offsets.
        popup.showAtLocation(layout, Gravity.NO_GRAVITY, x, y - popupHeight + 10);
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        }
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {

        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_profile) {
            
        } else if (id == R.id.nav_removed_restaurants) {

            Intent removedRestaurants = new Intent(MainActivity.this, RemovedRestaurantsActivity.class);
            removedRestaurants.putParcelableArrayListExtra("restaurants_list", mRestaurantList);
            removedRestaurants.putParcelableArrayListExtra("forbidden_list", mTempForbiddenList);
            removedRestaurants.putExtra("favorites", favorites);
            removedRestaurants.putExtra("forbidden", forbidden);
            removedRestaurants.putExtra("user_id", userId);
            removedRestaurants.putExtra("user_name", userName);
            removedRestaurants.putExtra("user_image", userImage);
            startActivityForResult(removedRestaurants, 1);

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
    public void setTempRestaurantList(ArrayList<RestaurantInfo> list) {
        mTempRestaurantList = list;
    }

    @Override
    public void setTempForbiddenList(ArrayList<RestaurantInfo> list) {
        mTempForbiddenList = list;
    }

    @Override
    public ArrayList<RestaurantInfo> getRestaurantList() {
        return mRestaurantList;
    }

    @Override
    public ArrayList<RestaurantInfo> getTempRestaurantList() {
        return mTempRestaurantList;
    }

    @Override
    public ArrayList<RestaurantInfo> getTempForbiddenList() {
        return mTempForbiddenList;
    }

    @Override
    public void setCuisines(HashSet<String> cuisines) {
        this.cuisines = cuisines;
    }

    @Override
    public HashSet<String> getCuisines() {
        return cuisines;
    }

    @Override
    public void setIsInitialized(boolean changed) {this.hasChanged = changed;}

    @Override
    public boolean getIsInitialized() {return hasChanged;}

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

    @Override
    public HashMap<String, String> getFavorites() {
        return favorites;
    }

    @Override
    public void setFavorites(HashMap<String, String> favorites) {
        this.favorites = favorites;
    }

    @Override
    public HashMap<String, String> getForbidden() {
        return forbidden;
    }

    @Override
    public void setForbidden(HashMap<String, String> forbidden) {
        this.forbidden = forbidden;
    }

    @Override
    public String getUserId() {
        return userId;
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
    public void updateRecyclerView(boolean isRemoved) {
        RLF.updateRecyclerView(isRemoved);
    }

    @Override
    public void updateMapView() {
        MVF.drawCircle(deviceLocation);
        MVF.showNearbyPlaces(mRestaurantList, onlyFavorites);
    }

    @Override
    public void showSortButton() {
        sortButtons[0].setVisibility(View.VISIBLE);
        Animation load = AnimationUtils.loadAnimation(this, R.anim.slide_in_up);
        sortButtons[0].startAnimation(load);
    }

    @Override
    public void hideSortButton() {
        for (int i = 0; i < sortButtons.length; i++) {
            sortButtons[i].setVisibility(View.GONE);
        }
    }

    @Override
    public boolean getIsRemovedList() {
        return removedList;
    }

    @Override
    public void setIsRemovedList(boolean removedList) {
        this.removedList = removedList;
    }
}
