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
        import android.widget.Button;
        import android.widget.ImageButton;
        import android.widget.Toast;

        import java.util.ArrayList;

/**
 * Created by yacov on 3/7/2018.
 */

public class RestaurantListFragment extends Fragment implements FragmentInterface{

    private static final String TAG = "RestaurantListFragment";
    private RecyclerView mRecyclerView;
    private RestaurantListAdapter mAdapter;
    private ArrayList<RestaurantInfo> mRestaurantsList;
    private ImageButton favorite;
    private FragmentCommunication mCallback;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater,
                             @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.restaurant_list_fragment,
                container, false);

        // Retrieve tasks lists from bundle
        Bundle data = getArguments();
        if (data != null) {getBundleArgs(data);}

        buildRecyclerView(view);

        favorite = (ImageButton) view.findViewById(R.id.favorite);

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

//    View.OnClickListener imgButtonHandler = new View.OnClickListener() {
//
//        public void onClick(View v) {
//            favorite.setBackgroundResource(R.drawable.favorite);
//
//        }
//    };

    public void getBundleArgs(Bundle data) {

        mRestaurantsList = data.getParcelableArrayList("restaurant list");
    }

    public void buildRecyclerView(View view) {
        mRecyclerView = view.findViewById(R.id.recyclerView);
        mRecyclerView.setHasFixedSize(true);
        RecyclerView.LayoutManager mLayoutManager =
                new LinearLayoutManager(getActivity());
        mAdapter = new RestaurantListAdapter(mRestaurantsList, getContext());
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setAdapter(mAdapter);

        mAdapter.setOnItemClickListener(new RestaurantListAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(int position) {
//                Intent intent = new Intent(getActivity(), CreateTaskActivity.class);
//                intent.putExtra("position" ,position);
//                intent.putParcelableArrayListExtra("completedList", mCompletedTaskList);
//                startActivity(intent);
            }

            @Override
            public void onCallClick(int position) {
                RestaurantInfo restaurant = mRestaurantsList.get(position);

                try {
                    Log.d(TAG, "-------------------------> ca]l btn was clicked. num is: " + restaurant.getPhoneNumber());
                    Intent callIntent = new Intent(Intent.ACTION_CALL);
                    callIntent.setData(Uri.parse("tel:" + restaurant.getPhoneNumber()));
                    startActivity(callIntent);
                } catch (ActivityNotFoundException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFavoriteClick(int position) {

            }
        });
    }

    public void removeItem(int position) {
        RestaurantInfo restaurant = mRestaurantsList.get(position);
        String n = restaurant.getName();
        String c = restaurant.getCuisine();
        String a = restaurant.getAddress();

        //Remove item from recycler view.
        mRestaurantsList.remove(position);
//        mCallback.setCompletedTasks(mCompletedTaskList);

        // Update recycler view.
        mAdapter.notifyItemRemoved(position);
    }

    @Override
    public void fragmentBecameVisible() {
        // Update
        mRestaurantsList = mCallback.getRestaurantList();
        mAdapter = new RestaurantListAdapter(mRestaurantsList, getContext());
        mRecyclerView.setAdapter(mAdapter);
    }
}

