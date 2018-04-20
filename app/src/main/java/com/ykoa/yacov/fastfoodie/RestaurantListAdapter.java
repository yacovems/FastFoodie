package com.ykoa.yacov.fastfoodie;

        import android.content.Context;
        import android.graphics.Bitmap;
        import android.graphics.BitmapFactory;
        import android.graphics.drawable.BitmapDrawable;
        import android.graphics.drawable.Drawable;
        import android.os.AsyncTask;
        import android.support.v4.content.res.ResourcesCompat;
        import android.support.v7.widget.RecyclerView;
        import android.util.Log;
        import android.view.LayoutInflater;
        import android.view.View;
        import android.view.ViewGroup;
        import android.widget.ImageButton;
        import android.widget.ImageView;
        import android.widget.TextView;

        import java.io.IOException;
        import java.io.InputStream;
        import java.net.MalformedURLException;
        import java.net.URL;
        import java.util.ArrayList;

/**
 * Created by yacov on 3/7/2018.
 */

public class RestaurantListAdapter extends RecyclerView.Adapter<RestaurantListAdapter.ViewHolder> {

    private Context context;
    private ArrayList<RestaurantInfo> mRestaurantsList;
    private OnItemClickListener mListener;
    private ViewHolder vh;

    public interface OnItemClickListener {
        void onItemClick(int position);
        void onCallClick(int position);
        void onFavoriteClick(int position);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        mListener = listener;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        public TextView name;
        public TextView cuisine;
        public TextView address;
        public TextView distance;
        public ImageView rating;
        public TextView cost;
        public ImageView img;
        public ImageView call;
        public ImageView favorite;
        public TextView reviewCount;
        public boolean isFavorite;

        public ViewHolder(View itemView, final OnItemClickListener listener) {
            super(itemView);
            name = itemView.findViewById(R.id.restaurant_name);
            cuisine = itemView.findViewById(R.id.cuisine);
            address = itemView.findViewById(R.id.address);
            distance = itemView.findViewById(R.id.distance);
            rating = itemView.findViewById(R.id.rating);
            cost = itemView.findViewById(R.id.cost);
            img = itemView.findViewById(R.id.restaurant_img);
            reviewCount = itemView.findViewById(R.id.review_count);
            call = itemView.findViewById(R.id.call);
            call.setBackgroundResource(R.drawable.call);
            favorite = itemView.findViewById(R.id.favorite);

            call.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (listener != null) {
                        int position = getAdapterPosition();
                        if (position != RecyclerView.NO_POSITION) {
                            listener.onCallClick(position);
                        }
                    }
                }
            });

            favorite.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (isFavorite) {
                        favorite.setBackgroundResource(R.drawable.favorite_border);
                    } else {
                        favorite.setBackgroundResource(R.drawable.favorite);
                    }

                    if (listener != null) {
                        int position = getAdapterPosition();
                        if (position != RecyclerView.NO_POSITION) {
                            listener.onFavoriteClick(position);
                        }
                    }
                }
            });
        }

        public void setIsFavorite(boolean isFavorite) {
            this.isFavorite = isFavorite;
        }
    }

    public RestaurantListAdapter(ArrayList<RestaurantInfo> list, Context context) {
        this.mRestaurantsList = list;
        this.context = context;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.restaurant_list_item, parent, false);
        vh = new ViewHolder(v, mListener);
        return vh;
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        final RestaurantInfo currItem = mRestaurantsList.get(position);

        // Sets favorite button
        if (currItem.getIsFavorite()) {
            holder.favorite.setImageResource(R.drawable.favorite);
            vh.setIsFavorite(true);
        } else {
            holder.favorite.setImageResource(R.drawable.favorite_border);
            vh.setIsFavorite(false);
        }

        // Download image from url
        DownloadImageTask downloadImg = new DownloadImageTask(holder, position);
        downloadImg.execute(currItem.getImg());

        // Get the correct rating image
        Bitmap image = null;
        double rating = Double.parseDouble(currItem.getRating());
        if (rating < 1) {
            image = BitmapFactory.decodeResource(context.getResources(), R.drawable.stars_small_0);
        } else if (rating == 1) {
            image = BitmapFactory.decodeResource(context.getResources(), R.drawable.stars_small_1);
        } else if (rating < 2) {
            image = BitmapFactory.decodeResource(context.getResources(), R.drawable.stars_small_1_half);
        } else if (rating == 2) {
            image = BitmapFactory.decodeResource(context.getResources(), R.drawable.stars_small_2);
        } else if (rating < 3) {
            image = BitmapFactory.decodeResource(context.getResources(), R.drawable.stars_small_2_half);
        } else if (rating == 3) {
            image = BitmapFactory.decodeResource(context.getResources(), R.drawable.stars_small_3);
        } else if (rating < 4) {
            image = BitmapFactory.decodeResource(context.getResources(), R.drawable.stars_small_3_half);
        } else if (rating == 4) {
            image = BitmapFactory.decodeResource(context.getResources(), R.drawable.stars_small_4);
        } else if (rating < 5) {
            image = BitmapFactory.decodeResource(context.getResources(), R.drawable.stars_small_4_half);
        } else if (rating == 5) {
            image = BitmapFactory.decodeResource(context.getResources(), R.drawable.stars_small_5);
        }

        // Set item in recyclerView
        holder.name.setText(currItem.getName());
        holder.cuisine.setText(currItem.getCuisine());
        holder.address.setText(currItem.getAddress());
        holder.cost.setText(currItem.getCost());
        holder.distance.setText(currItem.getDistance() + " miles");
        holder.rating.setImageBitmap(image);
        holder.reviewCount.setText(currItem.getReviewCount() + " reviews");
    }

    @Override
    public int getItemCount() {
        return mRestaurantsList.size();
    }
}

