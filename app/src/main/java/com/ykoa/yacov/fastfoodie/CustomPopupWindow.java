package com.ykoa.yacov.fastfoodie;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;

import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by Yacov on 3/22/2018.
 */

public class CustomPopupWindow {

    private View layout = null;
    private int popupWidth;
    private int popupHeight;
    private PopupWindow popup;
    private ArrayList<ImageButton> buttons;
    private ArrayList<ImageView> images;

    public CustomPopupWindow(int width, int height, Activity context, View layout) {

        buttons = new ArrayList<>();
        images = new ArrayList<>();

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
        popup.showAtLocation(layout, Gravity.NO_GRAVITY, 0, 0);

//        ImageButton leftArrow = (ImageButton) layout.findViewById(R.id.left_arrow_btn);
//        ImageButton rightArrow = (ImageButton) layout.findViewById(R.id.right_arrow_btn);
//
//        leftArrow.setOnClickListener(new View.OnClickListener(){
//
//            @Override
//            public void onClick(View view) {
//
//            }
//        });
//
//        rightArrow.setOnClickListener(new View.OnClickListener(){
//
//            @Override
//            public void onClick(View view) {
//
//            }
//        });
    }

    public void addButton(ImageButton button) {
        buttons.add(button);
//        button.setOnClickListener(new View.OnClickListener(){
//
//            @Override
//            public void onClick(View view) {
//                System.out.println("-------------99999999999999999999-------------");
//            }
//        });
    }

    public ImageButton getButton(int x) {
        return buttons.get(x);
    }

    public void addText(String label) {

    }

    public void addImage() {

    }

    public PopupWindow getPopup() {
        return popup;
    }
}
