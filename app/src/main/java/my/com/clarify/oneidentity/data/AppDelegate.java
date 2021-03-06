package my.com.clarify.oneidentity.data;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.os.Environment;
import android.os.Handler;
import android.provider.Settings;
import android.telephony.TelephonyManager;

import androidx.appcompat.app.AlertDialog;
import androidx.exifinterface.media.ExifInterface;
import androidx.multidex.MultiDexApplication;

import com.devs.acr.AutoErrorReporter;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;

import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import my.com.clarify.oneidentity.R;

public class AppDelegate extends MultiDexApplication {
    public static int BARCODE_REQUEST_CODE = 0;
    public static int SIGN_IN_REQUEST_CODE = 1;
    public static String SharedPreferencesTag = "OneIdentity";

    public static String appPath = "/OneIdentity";
    public static String tempFolder = "/tempFolder";
    public static String livenessToken = "58b3a2696b68a5e4f3887b60998503f417c7c927c7e782d7d908f43e478d49e9e0374ec0d33a3efa7f650dc7e4948fdb0724b2815c3de1660161ce167f8ea9b5";
    public static String apikey = "9f7b7a7ab2f65627c0dad91a8b7caf64";
    public static String credDefIdOneIdentity = "SbjTsmm9TkyppiNQx4GtK1:3:CL:270:TAG1";

    public static boolean shouldRefreshConnectionList = false;

    public Handler handler = new Handler();
    public int delay = 1000;
    public AppDelegate() {
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        AutoErrorReporter.get(this)
                .setEmailAddresses("hochankit@gmail.com")
                .setEmailSubject(getString(R.string.app_name) + " - Crash Report")
                .start();
    }

    public void createDirectoryIfNotExist(String directoryPath) {
        try {
            File dir = new File(directoryPath);
            if (!dir.exists()) {
                dir.mkdir();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public boolean isGooglePlayServicesAvailable(Activity activity) {
        GoogleApiAvailability googleApiAvailability = GoogleApiAvailability.getInstance();
        int status = googleApiAvailability.isGooglePlayServicesAvailable(activity);
        if(status != ConnectionResult.SUCCESS) {
            if(googleApiAvailability.isUserResolvableError(status)) {
                googleApiAvailability.getErrorDialog(activity, status, 2404).show();
            }
            return false;
        }
        return true;
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
    }

    public void alert(Activity activity, String title, String message)
    {
        AlertDialog alertDialog = new AlertDialog.Builder(activity).create();
        alertDialog.setTitle(title);
        alertDialog.setMessage(message);
        alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
        alertDialog.show();
    }

    public static String fileNameGenerator(Context context)
    {
        Calendar now = Calendar.getInstance();
        Date date = now.getTime();
        @SuppressLint("SimpleDateFormat") SimpleDateFormat format = new SimpleDateFormat("yyyyMMddhhmmss", Locale.US);
        String formattedDate = format.format(date);

        return getDeviceId(context) + "-" + formattedDate;
    }

    public static Bitmap encodeStringAsQRBitmap(String str) throws WriterException {
        BitMatrix result;
        try {
            result = new MultiFormatWriter().encode(str,
                    BarcodeFormat.QR_CODE, 500, 500, null);
        } catch (IllegalArgumentException iae) {
            // Unsupported format
            return null;
        }
        int w = result.getWidth();
        int h = result.getHeight();
        int[] pixels = new int[w * h];
        for (int y = 0; y < h; y++) {
            int offset = y * w;
            for (int x = 0; x < w; x++) {
                pixels[offset + x] = result.get(x, y) ? Color.BLACK : Color.WHITE;
            }
        }
        Bitmap bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        bitmap.setPixels(pixels, 0, 500, 0, 0, w, h);
        return bitmap;
    }

    @SuppressLint("HardwareIds")
    public static String getDeviceId(Context context)
    {
        return Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
    }

    public static String getResizedImagePathFromOriginalPath(Context context, String imagePath, int desiredWidth, int desiredHeight)
    {
        String resizedFilePath = Environment.getExternalStorageDirectory().toString() + AppDelegate.appPath + "/" + fileNameGenerator(context) + ".jpg";
        Bitmap scaledBitmap = loadResizedBitmap(imagePath, desiredWidth, desiredHeight, true);
        File file = new File(resizedFilePath);
        FileOutputStream fOut;
        try
        {
            fOut = new FileOutputStream(file);
            scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 80, fOut);
            fOut.flush();
            fOut.close();
        }
        catch (Exception e)
        { // TODO
            e.printStackTrace();
            //scaledBitmap.recycle();
        }
        if(scaledBitmap != null)
            scaledBitmap.recycle();
        return resizedFilePath;
    }

    public static Bitmap loadResizedBitmap(String filename, int width, int height, boolean exact ) {
        Bitmap bitmap = null;
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(filename, options);
        if (options.outHeight > 0 && options.outWidth > 0 ) {
            options.inJustDecodeBounds = false;
            options.inSampleSize = 2;

            int oriWidth = options.outWidth;
            int oriHeight = options.outHeight;
            while (options.outWidth  / options.inSampleSize > width &&
                    options.outHeight / options.inSampleSize > height ) {
                options.inSampleSize++;
            }
            options.inSampleSize--;
            bitmap = BitmapFactory.decodeFile(filename, options);
            if ( bitmap != null && exact )
            {
                int bWidth = bitmap.getWidth();
                int bHeight = bitmap.getHeight();
                int nHeight = (int) Math.round(width/( (1.0*bWidth) / bHeight ));

                //int nHeight = Math.round(height * parentRatio);
                bitmap = Bitmap.createScaledBitmap( bitmap, width, nHeight, false );

                try {
                    ExifInterface exif = new ExifInterface(filename);
                    int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, 1);
                    Matrix matrix = new Matrix();
                    if (orientation == 6) {
                        matrix.postRotate(90);
                    }
                    else if (orientation == 3) {
                        matrix.postRotate(180);
                    }
                    else if (orientation == 8) {
                        matrix.postRotate(270);
                    }
                    return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
                }
                catch (Exception e) {

                }
            }
        }
        return bitmap;
    }

    public static String formatString(String text)
    {
        StringBuilder json = new StringBuilder();
        String indentString = "";

        for (int i = 0; i < text.length(); i++) {
            char letter = text.charAt(i);
            switch (letter) {
                case '{':
                case '[':
                    json.append("\n" + indentString + letter + "\n");
                    indentString = indentString + "\t";
                    json.append(indentString);
                    break;
                case '}':
                case ']':
                    indentString = indentString.replaceFirst("\t", "");
                    json.append("\n" + indentString + letter);
                    break;
                case ',':
                    json.append(letter + "\n" + indentString);
                    break;

                default:
                    json.append(letter);
                    break;
            }
        }

        return json.toString();
    }
}