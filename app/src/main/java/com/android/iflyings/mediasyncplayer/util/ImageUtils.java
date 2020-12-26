package com.android.iflyings.mediasyncplayer.util;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;

import java.io.IOException;
import java.io.InputStream;


public class ImageUtils {

    public static Bitmap createBitmap(String imagePath, int reqWidth, int reqHeight) {
        // 先获取原始照片的宽高
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(imagePath, options);
        int srcWidth  = options.outWidth;
        int srcHeight = options.outHeight;

        float ratioW = 1f * srcWidth  / reqWidth;
        float ratioH = 1f * srcHeight / reqHeight;
        float ratio  = Math.max(ratioW, ratioH);

        options.inJustDecodeBounds = false;
        options.inSampleSize = ratio < 1 ? 1 : (int) ratio;
        options.inPreferredConfig = Bitmap.Config.RGB_565 ;
        Bitmap origin = BitmapFactory.decodeFile(imagePath, options);
        Bitmap scale = Bitmap.createScaledBitmap(origin,reqWidth,reqHeight,true);
        if (scale != origin) {
            origin.recycle();
        }
        return scale;
    }

    public static Bitmap createBitmap(InputStream inputStream, int reqWidth, int reqHeight) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeStream(inputStream, null, options);
        int srcWidth  = options.outWidth;
        int srcHeight = options.outHeight;

        float ratioW = 1f * srcWidth  / reqWidth;
        float ratioH = 1f * srcHeight / reqHeight;
        float ratio  = Math.max(ratioW, ratioH);

        try {
            inputStream.reset();
        } catch (IOException e) {
            e.printStackTrace();
        }
        options.inJustDecodeBounds = false;
        options.inSampleSize = ratio < 1 ? 1 : (int) ratio;
        options.inPreferredConfig = Bitmap.Config.RGB_565 ;
        return BitmapFactory.decodeStream(inputStream, null, options);
    }

    public static Bitmap createBitmap(Drawable drawable) {

        if (drawable instanceof BitmapDrawable) {
            return ((BitmapDrawable)drawable).getBitmap();
        }

        Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);

        return bitmap;
    }
}
