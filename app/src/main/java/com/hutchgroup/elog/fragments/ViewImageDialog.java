package com.hutchgroup.elog.fragments;


import android.app.Activity;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.hutchgroup.elog.R;
import com.hutchgroup.elog.common.BitmapUtility;
import com.hutchgroup.elog.common.LogFile;

public class ViewImageDialog extends DialogFragment implements View.OnClickListener{
    String TAG = ViewImageDialog.class.getName();

    ImageView imageView;
    ImageButton imgCancel;
    Bitmap bitmap;

    String path = "";

    public ViewImageDialog(){

    }

    public void setImagePath(String imagePath) {
        path = imagePath;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_view_image, container);
        try {

            initialize(view);
            //getDialog().setTitle("Defect Image");
            getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);
            this.setCancelable(false);

            if (path.equals("")) {
                dismiss();
            } else {
                if (bitmap != null) {
                    bitmap.recycle();
                    bitmap = null;
                }
                Configuration config = getResources().getConfiguration();

                //call to get bitmap with small size
                bitmap = BitmapUtility.decodeSampledBitmapFromFileWithOrientation(path, 416, 234, config.orientation);

                imageView.setPadding(2, 2, 2, 2);
                imageView.setImageBitmap(bitmap);
                //imageView.setScaleType(ImageView.ScaleType.FIT_XY);
                Log.i(TAG, bitmap.getWidth() + "x" + bitmap.getHeight());
            }
        } catch (Exception e) {
            LogFile.write(DefectSelectionDialog.class.getName() + "::onCreateView Error: " + e.getMessage(), LogFile.USER_INTERACTION, LogFile.ERROR_LOG);
        }
        return view;
    }


    @Override
    public void onResume() {
//        ViewGroup.LayoutParams params = getDialog().getWindow().getAttributes();
//        params.width =WindowManager.LayoutParams.MATCH_PARENT;
//        params.height = WindowManager.LayoutParams.MATCH_PARENT;
//        getDialog().getWindow().setAttributes((android.view.WindowManager.LayoutParams) params);

        super.onResume();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (bitmap != null) {
            bitmap.recycle();
            bitmap = null;
        }
    }

    private void initialize(View view) {
        try {
            imageView = (ImageView) view.findViewById(R.id.ivImage);
            imgCancel = (ImageButton) view.findViewById(R.id.imgCancel);
            imgCancel.setOnClickListener(this);
        } catch (Exception e) {
            LogFile.write(ViewImageDialog.class.getName() + "::initialize Error:" + e.getMessage(), LogFile.USER_INTERACTION, LogFile.ERROR_LOG);
        }
    }

    @Override
    public void onClick(View view) {
        try {
            switch (view.getId()) {
                case R.id.imgCancel:
                    imageView.setImageBitmap(null);
                    bitmap.recycle();
                    dismiss();
                    break;
            }
        } catch (Exception e) {
            LogFile.write(ViewImageDialog.class.getName() + "::onClick Error:" + e.getMessage(), LogFile.USER_INTERACTION, LogFile.ERROR_LOG);
        }
    }

}
