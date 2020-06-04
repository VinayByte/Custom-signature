package com.vinay.signature;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.icu.text.SimpleDateFormat;
import android.os.Environment;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.util.Date;
import java.util.Locale;

public class Utils {
    private static final float maxHeight = 1280.0f;
    private static final float maxWidth = 1280.0f;
    private Context context;

    public static void storePhotoOnDisk(final Bitmap capturedBitmap) {

        new Thread(new Runnable() {
            @Override
            public void run() {

                File folder = new File(Environment.getExternalStorageDirectory() +
                        File.separator + "Download");

                folder.mkdirs();

                SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HH_mm_SS", Locale.US);

                String format = sdf.format(new Date());

                File photoFile = new File(folder, format.concat(".jpg"));

                if (photoFile.exists()) {
                    photoFile.delete();
                }

                try {
                    FileOutputStream fos = new FileOutputStream(photoFile.getPath());

                    Bitmap newBitmap = Bitmap.createBitmap(capturedBitmap.getWidth(), capturedBitmap.getHeight(), capturedBitmap.getConfig());
                    Canvas canvas = new Canvas(newBitmap);
                    canvas.drawColor(Color.WHITE);
                    canvas.drawBitmap(capturedBitmap, 0, 0, null);


                    newBitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
                    fos.flush();
                    fos.close();
                } catch (java.io.IOException e) {
                    Log.e("PictureDemo", "Exception in photoCallback", e);
                }

            }
        }).start();
    }

    public static void storePhotoOnDisk1(final Bitmap capturedBitmap) {

        new Thread(new Runnable() {
            @Override
            public void run() {

                File folder = new File(Environment.getExternalStorageDirectory() +
                        File.separator + "Download");

                folder.mkdirs();

                SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HH_mm_SS", Locale.US);

                String format = sdf.format(new Date());

                File photoFile = new File(folder, format.concat(".jpg"));

                if (photoFile.exists()) {
                    photoFile.delete();
                }

                try {
                    FileOutputStream fos = new FileOutputStream(photoFile.getPath());

//                    Bitmap newBitmap = Bitmap.createBitmap(capturedBitmap.getWidth(), capturedBitmap.getHeight(), capturedBitmap.getConfig());
//                    Canvas canvas = new Canvas(newBitmap);
//                    canvas.drawColor(Color.WHITE);
//                    canvas.drawBitmap(capturedBitmap, 0, 0, null);


                    capturedBitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
                    fos.flush();
                    fos.close();
                } catch (java.io.IOException e) {
                    Log.e("PictureDemo", "Exception in photoCallback", e);
                }

            }
        }).start();
    }
//    public static Bitmap compressImage(Bitmap bmp) {
//        Bitmap scaledBitmap = null;
//
//        BitmapFactory.Options options = new BitmapFactory.Options();
//        options.inJustDecodeBounds = true;
//
//        int actualHeight = options.outHeight;
//        int actualWidth = options.outWidth;
//
//        float imgRatio = (float) actualWidth / (float) actualHeight;
//        float maxRatio = maxWidth / maxHeight;
//
//        if (actualHeight > maxHeight || actualWidth > maxWidth) {
//            if (imgRatio < maxRatio) {
//                imgRatio = maxHeight / actualHeight;
//                actualWidth = (int) (imgRatio * actualWidth);
//                actualHeight = (int) maxHeight;
//            } else if (imgRatio > maxRatio) {
//                imgRatio = maxWidth / actualWidth;
//                actualHeight = (int) (imgRatio * actualHeight);
//                actualWidth = (int) maxWidth;
//            } else {
//                actualHeight = (int) maxHeight;
//                actualWidth = (int) maxWidth;
//
//            }
//        }
//
//        options.inSampleSize = calculateInSampleSize(options, actualWidth, actualHeight);
//        options.inJustDecodeBounds = false;
//        options.inDither = false;
//        options.inPurgeable = true;
//        options.inInputShareable = true;
//        options.inTempStorage = new byte[16 * 1024];
//
//
//        try {
//            scaledBitmap = Bitmap.createBitmap(actualWidth, actualHeight, Bitmap.Config.RGB_565);
//        } catch (OutOfMemoryError exception) {
//            exception.printStackTrace();
//        }
//
//        float ratioX = actualWidth / (float) options.outWidth;
//        float ratioY = actualHeight / (float) options.outHeight;
//        float middleX = actualWidth / 2.0f;
//        float middleY = actualHeight / 2.0f;
//
//        Matrix scaleMatrix = new Matrix();
//        scaleMatrix.setScale(ratioX, ratioY, middleX, middleY);
//
//        Canvas canvas = new Canvas(scaledBitmap);
//        canvas.setMatrix(scaleMatrix);
//        canvas.drawBitmap(bmp, middleX - bmp.getWidth() / 2, middleY - bmp.getHeight() / 2, new Paint(Paint.FILTER_BITMAP_FLAG));
//
//        if(bmp!=null)
//        {
//            bmp.recycle();
//        }
//
//        ExifInterface exif;
//        try {
//            exif = new ExifInterface(imagePath);
//            int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, 0);
//            Matrix matrix = new Matrix();
//            if (orientation == 6) {
//                matrix.postRotate(90);
//            } else if (orientation == 3) {
//                matrix.postRotate(180);
//            } else if (orientation == 8) {
//                matrix.postRotate(270);
//            }
//            scaledBitmap = Bitmap.createBitmap(scaledBitmap, 0, 0, scaledBitmap.getWidth(), scaledBitmap.getHeight(), matrix, true);
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//
//
//        return scaledBitmap;
//    }
//
//    public static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
//        final int height = options.outHeight;
//        final int width = options.outWidth;
//        int inSampleSize = 1;
//
//        if (height > reqHeight || width > reqWidth) {
//            final int heightRatio = Math.round((float) height / (float) reqHeight);
//            final int widthRatio = Math.round((float) width / (float) reqWidth);
//            inSampleSize = heightRatio < widthRatio ? heightRatio : widthRatio;
//        }
//        final float totalPixels = width * height;
//        final float totalReqPixelsCap = reqWidth * reqHeight * 2;
//
//        while (totalPixels / (inSampleSize * inSampleSize) > totalReqPixelsCap) {
//            inSampleSize++;
//        }
//
//        return inSampleSize;
//    }


    public static byte[] getYV12(int inputWidth, int inputHeight, Bitmap scaled) {

        int[] argb = new int[inputWidth * inputHeight];

        scaled.getPixels(argb, 0, inputWidth, 0, 0, inputWidth, inputHeight);

        byte[] yuv = new byte[inputWidth * inputHeight * 3 / 2];
        encodeYV12(yuv, argb, inputWidth, inputHeight);

        scaled.recycle();

        return yuv;
    }

    private static void encodeYV12(byte[] yuv420sp, int[] argb, int width, int height) {
        final int frameSize = width * height;

        int yIndex = 0;
        int uIndex = frameSize;
        int vIndex = frameSize + (frameSize / 4);

        int a, R, G, B, Y, U, V;
        int index = 0;
        for (int j = 0; j < height; j++) {
            for (int i = 0; i < width; i++) {

                a = (argb[index] & 0xff000000) >> 24; // a is not used obviously
                R = (argb[index] & 0xff0000) >> 16;
                G = (argb[index] & 0xff00) >> 8;
                B = (argb[index] & 0xff) >> 0;

                // well known RGB to YUV algorithm
                Y = ((66 * R + 129 * G + 25 * B + 128) >> 8) + 16;
                U = ((-38 * R - 74 * G + 112 * B + 128) >> 8) + 128;
                V = ((112 * R - 94 * G - 18 * B + 128) >> 8) + 128;

                // YV12 has a plane of Y and two chroma plans (U, V) planes each sampled by a factor of 2
                //    meaning for every 4 Y pixels there are 1 V and 1 U.  Note the sampling is every other
                //    pixel AND every other scanline.
                yuv420sp[yIndex++] = (byte) ((Y < 0) ? 0 : ((Y > 255) ? 255 : Y));
                if (j % 2 == 0 && index % 2 == 0) {
                    yuv420sp[uIndex++] = (byte) ((V < 0) ? 0 : ((V > 255) ? 255 : V));
                    yuv420sp[vIndex++] = (byte) ((U < 0) ? 0 : ((U > 255) ? 255 : U));
                }

                index++;
            }
        }
    }

    // untested function
    public static byte[] getNV21(int inputWidth, int inputHeight, Bitmap scaled) {

        int[] argb = new int[inputWidth * inputHeight];

        scaled.getPixels(argb, 0, inputWidth, 0, 0, inputWidth, inputHeight);

        byte[] yuv = new byte[inputWidth * inputHeight * 3 / 2];
        encodeYUV420SP(yuv, argb, inputWidth, inputHeight);

        scaled.recycle();

        return yuv;
    }

    static void encodeYUV420SP(byte[] yuv420sp, int[] argb, int width, int height) {
        final int frameSize = width * height;

        int yIndex = 0;
        int uvIndex = frameSize;

        int a, R, G, B, Y, U, V;
        int index = 0;
        for (int j = 0; j < height; j++) {
            for (int i = 0; i < width; i++) {

                a = (argb[index] & 0xff000000) >> 24; // a is not used obviously
                R = (argb[index] & 0xff0000) >> 16;
                G = (argb[index] & 0xff00) >> 8;
                B = (argb[index] & 0xff) >> 0;

                // well known RGB to YUV algorithm
                Y = ((66 * R + 129 * G + 25 * B + 128) >> 8) + 16;
                U = ((-38 * R - 74 * G + 112 * B + 128) >> 8) + 128;
                V = ((112 * R - 94 * G - 18 * B + 128) >> 8) + 128;

                // NV21 has a plane of Y and interleaved planes of VU each sampled by a factor of 2
                //    meaning for every 4 Y pixels there are 1 V and 1 U.  Note the sampling is every other
                //    pixel AND every other scanline.
                yuv420sp[yIndex++] = (byte) ((Y < 0) ? 0 : ((Y > 255) ? 255 : Y));
                if (j % 2 == 0 && index % 2 == 0) {
                    yuv420sp[uvIndex++] = (byte) ((V < 0) ? 0 : ((V > 255) ? 255 : V));
                    yuv420sp[uvIndex++] = (byte) ((U < 0) ? 0 : ((U > 255) ? 255 : U));
                }

                index++;
            }
        }
    }


    public static byte[] compress(Bitmap bitmap) {
        int w = bitmap.getWidth();
        int h = bitmap.getHeight();
//        int[] argb = new int[w * h];
//        bitmap.getPixels(argb, 0, w, 0, 0, w, h);
//        byte[] ycc = new byte[w * h * 3 / 2];
//        argb = null; // let GC do its job
        ByteArrayOutputStream jpeg = new ByteArrayOutputStream();


        YuvImage yuvImage = new YuvImage(getNV21(bitmap.getWidth(), bitmap.getHeight(), bitmap), ImageFormat.NV21, w, h, null);
        yuvImage.compressToJpeg(new Rect(0, 0, w, h), 100, jpeg);
        return jpeg.toByteArray();
    }

}
