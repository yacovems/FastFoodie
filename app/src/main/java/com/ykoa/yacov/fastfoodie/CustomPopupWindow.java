package com.ykoa.yacov.fastfoodie;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.PopupWindow;

import java.io.IOException;

/**
 * Created by Yacov on 3/22/2018.
 */

public class CustomPopupWindow {

    private View layout = null;
    private int popupWidth;
    private int popupHeight;
    private PopupWindow popup;

    public CustomPopupWindow(int width, int height, Activity context, View layout) {
        popupWidth = width;
        popupHeight = height;
        this.layout = layout;

        // Creating the PopupWindow
        popup = new PopupWindow(context);
        popup.setAnimationStyle(R.style.Animation);
        popup.setContentView(layout);
        popup.setWidth(popupWidth);
        popup.setHeight(popupHeight);
        popup.setFocusable(true);
    }

    public void showPopupWindow(int x, int y) {
        // Displaying the popup at the specified location, + offsets.
        popup.showAtLocation(layout, Gravity.NO_GRAVITY, x, y - popupHeight + 5);
    }

    public PopupWindow getPopup() {
        return popup;
    }
}
