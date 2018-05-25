package com.ykoa.yacov.fastfoodie;


        import android.content.ActivityNotFoundException;
        import android.content.Context;
        import android.content.Intent;
        import android.graphics.Canvas;
        import android.net.Uri;
        import android.os.Bundle;
        import android.support.annotation.Nullable;
        import android.support.v4.app.DialogFragment;
        import android.support.v4.app.Fragment;
        import android.support.v7.widget.LinearLayoutManager;
        import android.support.v7.widget.RecyclerView;
        import android.support.v7.widget.SimpleItemAnimator;
        import android.support.v7.widget.helper.ItemTouchHelper;
        import android.util.Log;
        import android.view.LayoutInflater;
        import android.view.View;
        import android.view.ViewGroup;
        import android.widget.ProgressBar;
        import android.widget.Toast;

        import com.google.android.gms.maps.model.LatLng;
        import com.google.android.gms.tasks.OnSuccessListener;
        import com.google.firebase.firestore.FirebaseFirestore;

        import java.util.ArrayList;
        import java.util.HashMap;
        import java.util.HashSet;

/**
 * Created by yacov on 3/7/2018.
 */

public class RestaurantListFragment extends Fragment implements FragmentInterface{

    private static final String TAG = "RestaurantListFragment";
    private RecyclerView mRecyclerView;
    private RestaurantListAdapter mAdapter;
    private FragmentCommunication mCallback;
    private View view;

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

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        view = inflater.inflate(R.layout.restaurant_list_fragment,
                container, false);

        mCallback.setIsInitialized(false);

        if (getActivity() instanceof RemovedRestaurantsActivity) {
            updateRecyclerView(true);
        }
        return view;
    }

    public void buildRecyclerView(final ArrayList<RestaurantInfo> list, final boolean showFavoriteBtn) {
        mRecyclerView = view.findViewById(R.id.restaurants_recyclerView);

        // Don't allow the dim animation on an item
        // when favoring a restaurant.
        ((SimpleItemAnimator) mRecyclerView.getItemAnimator()).setSupportsChangeAnimations(false);

        // Set all cards to the same size
        mRecyclerView.setHasFixedSize(true);
        RecyclerView.LayoutManager mLayoutManager =
                new LinearLayoutManager(getActivity());
        mAdapter = new RestaurantListAdapter(list, getContext(), showFavoriteBtn);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setAdapter(mAdapter);
        mAdapter.setOnItemClickListener(
                new RestaurantListAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(int position) {
                // Send user to the Yelp's restaurant page
                ArrayList<RestaurantInfo> restaurantList = null;
                if (showFavoriteBtn) {
                    restaurantList = mCallback.getTempRestaurantList();
                } else {
                    restaurantList = mCallback.getTempForbiddenList();
                }

                RestaurantInfo restaurant = restaurantList.get(position);

                Uri uri = Uri.parse(restaurant.getWebsite());
                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                startActivity(intent);
            }

            @Override
            public void onCallClick(int position) {
                ArrayList<RestaurantInfo> restaurantList = mCallback.getTempRestaurantList();
                RestaurantInfo restaurant = restaurantList.get(position);

                // If phone number not available, don't do anything.
                if (restaurant.getPhoneNumber().equals("phone not available")) {return;}

                try {
                    Intent callIntent = new Intent(Intent.ACTION_DIAL);
                    callIntent.setData(Uri.parse("tel:" + restaurant.getPhoneNumber()));
                    startActivity(callIntent);
                } catch (ActivityNotFoundException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFavoriteClick(int position) {

                HashMap<String, Object> user = new HashMap<>();
                ArrayList<RestaurantInfo> restaurantList = mCallback.getTempRestaurantList();
                RestaurantInfo restaurant = restaurantList.get(position);

                HashMap<String, String> favorites = mCallback.getFavorites();

                if (restaurant.getIsFavorite() == 1) {
                    // Remove restaurant from favorites
                    // and update Firebase
                    restaurant.setIsFavorite(0);
                    favorites.remove(restaurant.getId());
                } else {
                    // Add restaurant to favorites
                    // and update Firebase
                    restaurant.setIsFavorite(1);
                    favorites.put(restaurant.getId(), restaurant.getName());
                }

                // Update DB
                user.put("favorites", favorites);
                mCallback.updateDB(user);

                // Update card
                mAdapter.notifyItemChanged(position);

                // Update map fragment
                mCallback.updateMapView();
            }

            @Override
            public void onDirectionsClick(int position) {

                ArrayList<RestaurantInfo> restaurantList = mCallback.getTempRestaurantList();
                RestaurantInfo restaurant = restaurantList.get(position);
                LatLng location = restaurant.getLocation();
                String baseURL = "google.navigation:q=" +
                        location.latitude + "," + location.longitude;

                // Create a Uri from an intent string. Use the result to create an Intent.
                Uri gmmIntentUri = Uri.parse(baseURL);

                // Create an Intent from gmmIntentUri. Set the action to ACTION_VIEW
                Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);

                // Make the Intent explicit by setting the Google Maps package
                mapIntent.setPackage("com.google.android.apps.maps");

                // Attempt to start an activity that can handle the Intent
                startActivity(mapIntent);
            }
        });
    }

    public void swipeRecyclerView(final boolean remove) {

        final SwipeController swipeController = new SwipeController(new SwipeControllerActions() {

            @Override
            public void onRightClicked(int position) {
                // Task start immediately.
                if (remove) {
                    Toast.makeText(getContext(),
                            "Restaurant removed from future searches", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(getContext(),
                            "Restaurant added to future searches", Toast.LENGTH_LONG).show();
                }

                // Remove restaurant from list
                removeItem(position, remove);

                // Update map
                mCallback.updateMapView();
            }
        }, remove, getResources());
        ItemTouchHelper itemTouchhelper = new ItemTouchHelper(swipeController);
        itemTouchhelper.attachToRecyclerView(mRecyclerView);

        mRecyclerView.addItemDecoration(new RecyclerView.ItemDecoration() {
            @Override
            public void onDraw(Canvas c, RecyclerView parent, RecyclerView.State state) {
                swipeController.onDraw(c);
            }
        });
    }

    public void updateRecyclerView(boolean removedRes) {
        if (removedRes) {
            buildRecyclerView(mCallback.getTempForbiddenList(), false);
        } else {
            buildRecyclerView(mCallback.getTempRestaurantList(), true);
        }

        if (!mCallback.getIsInitialized()) {
            // Swipe recyclerView items
            swipeRecyclerView(!removedRes);
            mCallback.setIsInitialized(true);
        }
    }

    public void removeItem(int position, boolean remove) {
        ArrayList<RestaurantInfo> allRestaurants = mCallback.getRestaurantList();
        ArrayList<RestaurantInfo> mRestaurantsList = null;
        if (remove) {
            mRestaurantsList = mCallback.getTempRestaurantList();
        } else {
            mRestaurantsList = mCallback.getTempForbiddenList();
        }

        RestaurantInfo restaurant = mRestaurantsList.get(position);
        HashMap<String, String> forbidden = mCallback.getForbidden();
        HashMap<String, Object> user = new HashMap<>();

        if (remove) {
            // Add restaurant to forbidden
            forbidden.put(restaurant.getId(), restaurant.getName());
            restaurant.setIsForbidden(1);
        } else {
            // Remove restaurant from forbidden
            // and add to restaurants list
            forbidden.remove(restaurant.getId());
            restaurant.setIsForbidden(0);
        }

        // VERY BAD FOR UI THREAD!!
        for (RestaurantInfo res : allRestaurants) {
            if (res.getId().equals(restaurant.getId())) {
                res.setIsForbidden(restaurant.getIsForbidden());
            }
        }

        // update Firebase
        user.put("forbidden", forbidden);
        mCallback.updateDB(user);

        //Remove item from recycler view.
        mRestaurantsList.remove(position);

        // Update recycler view
        mAdapter.notifyItemRemoved(position);


    }

    @Override
    public void fragmentBecameVisible() {
        // Show sort button
        mCallback.showSortButton();
    }
}

