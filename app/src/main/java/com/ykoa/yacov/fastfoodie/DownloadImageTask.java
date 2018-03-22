package com.ykoa.yacov.fastfoodie;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.view.Menu;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by Yacov on 3/21/2018.
 */

public class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {

    RestaurantListAdapter.ViewHolder holder;
    int position;
    Menu navMenu = null;
    Resources res;

    public DownloadImageTask(Menu navMenu, Resources res) {
        this.navMenu = navMenu;
        this.res = res;
    }

   public DownloadImageTask(RestaurantListAdapter.ViewHolder holder, int position) {
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
        if (navMenu != null) {
            Drawable icon = new BitmapDrawable(res, result);
            navMenu.getItem(0).setIcon(icon);
        } else {
            holder.img.setImageBitmap(result);
        }
    }
}
