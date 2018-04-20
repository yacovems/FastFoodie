package com.ykoa.yacov.fastfoodie;


        import android.content.ActivityNotFoundException;
        import android.content.Context;
        import android.content.Intent;
        import android.graphics.Canvas;
        import android.net.Uri;
        import android.os.Bundle;
        import android.support.annotation.Nullable;
        import android.support.v4.app.Fragment;
        import android.support.v7.widget.LinearLayoutManager;
        import android.support.v7.widget.RecyclerView;
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
    private SwipeController swipeController = null;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater,
                             @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.restaurant_list_fragment,
                container, false);

        mCallback.setIsInitialized(false);
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

    public void buildRecyclerView(View view) {
        mRecyclerView = view.findViewById(R.id.recyclerView);
        mRecyclerView.setHasFixedSize(true);
        RecyclerView.LayoutManager mLayoutManager =
                new LinearLayoutManager(getActivity());
        mAdapter = new RestaurantListAdapter(
                mCallback.getTempRestaurantList(), getContext());
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setAdapter(mAdapter);
        mAdapter.setOnItemClickListener(
                new RestaurantListAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(int position) {

            }

            @Override
            public void onCallClick(int position) {
                RestaurantInfo restaurant = mCallback.getTempRestaurantList()
                        .get(position);

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
                RestaurantInfo restaurant = mCallback.getTempRestaurantList()
                        .get(position);
                HashMap<String, String> favorites = mCallback.getFavorites();

                if (restaurant.getIsFavorite()) {
                    // Remove restaurant from favorites
                    // and update Firebase
                    restaurant.setIsFavorite(false);
                    favorites.remove(restaurant.getId());
                    user.put("favorites", favorites);
                    mCallback.updateDB(user);

                } else {
                    // Add restaurant to favorites
                    // and update Firebase
                    restaurant.setIsFavorite(true);
                    favorites.put(restaurant.getId(), restaurant.getName());
                    user.put("favorites", favorites);
                    mCallback.updateDB(user);
                }

                // Update map fragment
                mCallback.updateMapView();
            }
        });
    }

    public void swipeRecyclerView() {

        swipeController = new SwipeController(new SwipeControllerActions() {

            @Override
            public void onLeftClicked(final int position) {
            }

            @Override
            public void onRightClicked(int position) {
                // Task start immediately.
                Toast.makeText(getContext(),
                        "Restaurant removed from future searches", Toast.LENGTH_LONG).show();
                removeItem(position);

                // Update map fragment
                mCallback.updateMapView();
            }
        }, false, getResources());
        ItemTouchHelper itemTouchhelper = new ItemTouchHelper(swipeController);
        itemTouchhelper.attachToRecyclerView(mRecyclerView);

        mRecyclerView.addItemDecoration(new RecyclerView.ItemDecoration() {
            @Override
            public void onDraw(Canvas c, RecyclerView parent, RecyclerView.State state) {
                swipeController.onDraw(c);
            }
        });
    }

    public void updateRecyclerView() {
        buildRecyclerView(getView());
        if (!mCallback.getIsInitialized()) {
            // Swipe recyclerView items
            swipeRecyclerView();
            mCallback.setIsInitialized(true);
        }
    }

    public void removeItem(int position) {
        ArrayList<RestaurantInfo> mRestaurantsList = mCallback.getTempRestaurantList();
        RestaurantInfo restaurant = mRestaurantsList.get(position);
        HashMap<String, String> forbidden = mCallback.getForbidden();

        // Add restaurant to forbidden
        // and update Firebase\
        HashMap<String, Object> user = new HashMap<>();
        forbidden.put(restaurant.getId(), restaurant.getName());
        user.put("forbidden", forbidden);
        mCallback.updateDB(user);

        //Remove item from recycler view.
        mRestaurantsList.remove(position);

        // Update recycler view.
        mAdapter.notifyItemRemoved(position);
    }

    @Override
    public void fragmentBecameVisible() {
        // Show sort button
        mCallback.showSortButton();
    }
}

