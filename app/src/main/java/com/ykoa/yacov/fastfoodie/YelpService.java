package com.ykoa.yacov.fastfoodie;

import android.content.Context;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by Yacov on 3/13/2018.
 */

public class YelpService {

    public String setYelpRequest(Context context, double lat, double lon, int radius, int limit, int offset, String businessType) throws IOException {

        OkHttpClient client = new OkHttpClient.Builder()
                .build();

        HttpUrl.Builder urlBuilder = HttpUrl.parse(context.getResources().getString(R.string.yelp_base_url)).newBuilder();
        urlBuilder.addQueryParameter("term", businessType);
        urlBuilder.addQueryParameter("latitude", "" + lat);
        urlBuilder.addQueryParameter("longitude", "" + lon);
        urlBuilder.addQueryParameter("radius", "" + radius);
        urlBuilder.addQueryParameter("open_now", "true");
        urlBuilder.addQueryParameter("limit", "" + limit);
        urlBuilder.addQueryParameter("offset", "" + offset);
        String url = urlBuilder.build().toString();

        Request request= new Request.Builder()
                .url(url)
                .header("Authorization", context.getResources().getString(R.string.yelp_key))
                .build();

        Response response = client.newCall(request).execute();
        return response.body().string();
    }

}
