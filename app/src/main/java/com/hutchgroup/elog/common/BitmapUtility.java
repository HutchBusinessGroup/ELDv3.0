package com.hutchgroup.elog.common;

import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.util.Base64;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;

public class BitmapUtility {

    public BitmapUtility() {

    }

    //calculate sample size
    public static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while ((halfHeight / inSampleSize) > reqHeight
                    && (halfWidth / inSampleSize) > reqWidth) {
                inSampleSize *= 2;
            }
        }

        return inSampleSize;
    }

    //load sample bitmap
    public static String convertToBase64String(String fileName) {
        String encodedImage = "";
        try {
            Bitmap bm = BitmapFactory.decodeFile(fileName);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bm.compress(Bitmap.CompressFormat.JPEG, 50, baos);
            byte[] byteArrayImage = baos.toByteArray();
            baos.flush();
            baos.close();
            encodedImage = Base64.encodeToString(byteArrayImage, Base64.DEFAULT);
        } catch (Exception exe) {

        }
        return encodedImage;
    }

    //load sample bitmap
    public static boolean compressAndSaveBitmap(String filePath) {
        boolean status = false;
        try {
            final BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            // Calculate inSampleSize
            options.inSampleSize = 4;
            // Decode bitmap with inSampleSize set
            options.inJustDecodeBounds = false;
            Bitmap bitmap =BitmapFactory.decodeFile(filePath, options);
            File file = new File(filePath); // the File to save to
            OutputStream fOut = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 50, fOut);

            fOut.flush();
            fOut.close();

            status = true;
        } catch (Exception exe) {

        }
        return status;
    }


    //load sample bitmap
    public static Bitmap decodeSampledBitmapFromFile(String fileName, int reqWidth, int reqHeight) {

        // First decode with inJustDecodeBounds=true to check dimensions
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(fileName, options);

        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeFile(fileName, options);
    }

    //load sample bitmap by its orientation
    public static Bitmap decodeSampledBitmapFromFileWithOrientation(String fileName, int reqWidth, int reqHeight, int orientation) {

        // First decode with inJustDecodeBounds=true to check dimensions
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(fileName, options);

        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;

        Bitmap bitmap = BitmapFactory.decodeFile(fileName, options);
        if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
            if (bitmap.getWidth() < bitmap.getHeight()) {
                //need rotate
                if (bitmap.getWidth() > bitmap.getHeight()) {
                    Bitmap bMapRotate = null;
                    Matrix mat = new Matrix();
                    mat.postRotate(90);
                    bMapRotate = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), mat, true);
                    bitmap.recycle();
                    return bMapRotate;
                }
            }
        } else if (orientation == Configuration.ORIENTATION_PORTRAIT) {
            if (bitmap.getWidth() > bitmap.getHeight()) {
                Bitmap bMapRotate = null;
                Matrix mat = new Matrix();
                mat.postRotate(90);
                bMapRotate = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), mat, true);
                bitmap.recycle();
                return bMapRotate;
            }
        }

        return bitmap;//BitmapFactory.decodeFile(fileName, options);
    }
}

