package com.ykoa.yacov.fastfoodie;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;

import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by Yacov on 3/22/2018.
 */

public class CustomPopupWindow {

    private View layout = null;
    private int popupWidth;
    private int popupHeight;
    private int deviceWidth;
    private int deviceHeight;
    private PopupWindow popup;
    private ArrayList<ImageButton> buttons;
    private Activity context;
    private ImageView tutorialImg = null;
    private ArrayList<Integer> images;
    private int imgCount;


    public CustomPopupWindow(int width, int height, int deviceWidth,
                             int deviceHeight, Activity context, View layout) {

        buttons = new ArrayList<>();
        images = new ArrayList<>();
        imgCount = 0;

        popupWidth = width;
        popupHeight = height;
        this.layout = layout;
        this.deviceWidth = deviceWidth;
        this.deviceHeight = deviceHeight;
        this.context = context;

        // Creating the PopupWindow
        popup = new PopupWindow(context);
        popup.setAnimationStyle(R.style.Animation);
        popup.setContentView(layout);
        popup.setWidth(popupWidth);
        popup.setHeight(popupHeight);
        popup.setFocusable(true);
        popup.showAtLocation(layout, Gravity.NO_GRAVITY, (deviceWidth - width) / 2, (deviceHeight - height) / 2);
    }

    public void showName(String name) {
        Animation load = AnimationUtils.loadAnimation(context, R.anim.slide_in_up);

        TextView userName = (TextView) layout.findViewById(R.id.user_name_text_view);
        userName.setText(name);
        userName.setVisibility(View.VISIBLE);
        userName.startAnimation(load);
    }

    public void showButtons() {
        Animation load = AnimationUtils.loadAnimation(context, R.anim.slide_in_up);

        ImageButton leftArrow = (ImageButton) layout.findViewById(R.id.left_arrow_btn);
        ImageButton rightArrow = (ImageButton) layout.findViewById(R.id.right_arrow_btn);

        leftArrow.setVisibility(View.VISIBLE);
        rightArrow.setVisibility(View.VISIBLE);

        leftArrow.startAnimation(load);
        rightArrow.startAnimation(load);

        leftArrow.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View view) {
                if (imgCount > 0) {
                    imgCount--;
                }
                switchImg(images.get(imgCount));
            }
        });

        rightArrow.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View view) {
                if (imgCount < images.size() - 1) {
                    imgCount++;
                }
                switchImg(images.get(imgCount));
            }
        });
    }

    public void showImg() {
        Animation load = AnimationUtils.loadAnimation(context, R.anim.slide_in_up);
        tutorialImg = (ImageView) layout.findViewById(R.id.tutorial_image_view);
        tutorialImg.setImageResource(images.get(0));
        imgCount++;
        tutorialImg.setVisibility(View.VISIBLE);
        tutorialImg.startAnimation(load);
    }

    public void switchImg(int img) {
        Animation remove = AnimationUtils.loadAnimation(context, R.anim.fade_out);
        tutorialImg.startAnimation(remove);
        Animation load = AnimationUtils.loadAnimation(context, R.anim.fade_in);
        tutorialImg.setImageResource(img);
        tutorialImg.startAnimation(load);
    }

    public void addImages(int img) {
        images.add(img);
    }
}
