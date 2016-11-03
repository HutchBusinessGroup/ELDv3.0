package com.hutchgroup.elog.util;

import android.content.res.Resources;
import android.graphics.drawable.AnimationDrawable;
import android.widget.ImageView;

import com.hutchgroup.elog.R;

public class AnimationUtil {

    public static void startSetupProcessingAnimation(Resources res, ImageView imageView) {
        AnimationDrawable animation = new AnimationDrawable();
        animation.addFrame(res.getDrawable(R.drawable.ic_setup_processing), 300);
        animation.addFrame(res.getDrawable(R.drawable.ic_setup_processing_2), 300);
        animation.addFrame(res.getDrawable(R.drawable.ic_setup_processing_3), 300);
        animation.setOneShot(false);

        imageView.setBackgroundDrawable(animation);

        animation.start();
    }
}
