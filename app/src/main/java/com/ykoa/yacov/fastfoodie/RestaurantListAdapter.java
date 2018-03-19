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
        public ImageButton call;
        public ImageButton favorite;
        public TextView reviewCount;

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
            favorite = itemView.findViewById(R.id.favorite);
            favorite.setBackgroundResource(R.drawable.favorite_border);

            call.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Log.d("Res list adapter", "-------------------------> call btn was clicked");
                    if (listener != null) {
                        Log.d("Res list adapter", "Listener is not NULL!!");
                        int position = getAdapterPosition();
                        Log.d("Res list adapter", "Adapter position: " + position);
                        if (position != RecyclerView.NO_POSITION) {
                            listener.onCallClick(position);
                            Log.d("Res list adapter", "-------------------------> call btn was clicked");
                        }
                    }
                }
            });

            favorite.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Log.d("Res list adapter", "-------------------------> favorite btn was clicked");
                    favorite.setBackgroundResource(R.drawable.favorite);
                    if (listener != null) {
                        int position = getAdapterPosition();
                        if (position != RecyclerView.NO_POSITION) {
                            listener.onFavoriteClick(position);
                        }
                    }
                }
            });
        }
    }

    public RestaurantListAdapter(ArrayList<RestaurantInfo> list, Context context) {
        this.mRestaurantsList = list;
        this.context = context;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.restaurant_list_item, parent, false);
        ViewHolder vh = new ViewHolder(v, mListener);
        return vh;
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        final RestaurantInfo currItem = mRestaurantsList.get(position);

        // Download image from url
        DownloadImageTask downloadImg = new DownloadImageTask(holder, position);
        downloadImg.execute(currItem.getImg());

        // Get the correct rating image
        Bitmap image = null;
        if (currItem.getRating() < 1) {
            image = BitmapFactory.decodeResource(context.getResources(), R.drawable.stars_small_0);
        } else if (currItem.getRating() == 1) {
            image = BitmapFactory.decodeResource(context.getResources(), R.drawable.stars_small_1);
        } else if (currItem.getRating() < 2) {
            image = BitmapFactory.decodeResource(context.getResources(), R.drawable.stars_small_1_half);
        } else if (currItem.getRating() == 2) {
            image = BitmapFactory.decodeResource(context.getResources(), R.drawable.stars_small_2);
        } else if (currItem.getRating() < 3) {
            image = BitmapFactory.decodeResource(context.getResources(), R.drawable.stars_small_2_half);
        } else if (currItem.getRating() == 3) {
            image = BitmapFactory.decodeResource(context.getResources(), R.drawable.stars_small_3);
        } else if (currItem.getRating() < 4) {
            image = BitmapFactory.decodeResource(context.getResources(), R.drawable.stars_small_3_half);
        } else if (currItem.getRating() == 4) {
            image = BitmapFactory.decodeResource(context.getResources(), R.drawable.stars_small_4);
        } else if (currItem.getRating() < 5) {
            image = BitmapFactory.decodeResource(context.getResources(), R.drawable.stars_small_4_half);
        } else if (currItem.getRating() == 5) {
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

    private class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {

        ViewHolder holder;
        int position;
        DownloadImageTask(ViewHolder holder, int position) {
            this.holder = holder;
            this.position = position;
        }

        protected Bitmap doInBackground(String... address) {
            Bitmap image = null;
            try {
                URL url = new URL(address[0]);
                image = BitmapFactory.decodeStream(url.openConnection().getInputStream());
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

            return image;
        }

        protected void onPostExecute(Bitmap result) {
            holder.img.setImageBitmap(result);
        }
    }

    @Override
    public int getItemCount() {
        return mRestaurantsList.size();
    }
}

