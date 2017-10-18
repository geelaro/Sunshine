package com.geelaro.sunshine.utils;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.widget.Toast;

import com.geelaro.sunshine.BuildConfig;

import java.util.List;

/**
 * Created by geelaro on 2017/8/11.
 */

public class ToolUtils {
    private static Context mContext = SunshineApp.getContext();
    private static Double mLat; //纬度
    private static Double mLon; //经度


    private static void getLocation() {
        LocationManager locationManager = (LocationManager)
                mContext.getSystemService(Context.LOCATION_SERVICE);
        //
        List<String> providerList = locationManager.getProviders(true);
        String provider = null;

        if (providerList.contains(LocationManager.NETWORK_PROVIDER)) {
            provider = LocationManager.NETWORK_PROVIDER;
        } else if (providerList.contains(LocationManager.GPS_PROVIDER)) {
            provider = LocationManager.GPS_PROVIDER;
        } else {
            Toast.makeText(mContext, "请开启定位.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (ActivityCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(mContext,
                Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling

            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        Location location = locationManager.getLastKnownLocation(provider);

        mLat = location.getLatitude();
        mLon = location.getLongitude();

    }

    private final LocationListener listener = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {

        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {

        }

        @Override
        public void onProviderEnabled(String provider) {

        }

        @Override
        public void onProviderDisabled(String provider) {

        }
    };

    /**
     * @return 返回经纬度
     */
    public static Double[] getLatAndLon() {
        getLocation();
        Double[] sNum = new Double[2];
        sNum[0] = mLat;
        sNum[1] = mLon;
        return sNum;
    }


    /**
     * 判断网络情况
     *
     * @param context
     * @return boolean
     */
    public static boolean isNetworkAvailable(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();

        return networkInfo.isAvailable();
    }
    /**
     * 获取当前设备屏幕宽度in pixel
     */
    public static int getWidthInPx(Context context){
        int width = context.getResources().getDisplayMetrics().widthPixels;
        return width;
    }

    public static int getHeightInPx(Context context){
        int height = context.getResources().getDisplayMetrics().heightPixels;
        return height;
    }

    public static float getWidth(Context context){
        float density =  context.getResources().getDisplayMetrics().density;
        return density;
    }


    public static Uri getWeatherURL() {
        String format = "json";
        String units = "metric";
        int numDays = 7;
        String param = "Nanjing";

        final String QUERY_PARAM = "q";
        final String FORMAT_PARAM = "mode";
        final String UNITS_PARAM = "units";
        final String DAYS_PARAM = "cnt";
        final String APPID_PARAM = "APPID";

        Uri builtUri = Uri.parse(Urls.WEATHER_BASE_URL).buildUpon()
                .appendQueryParameter(QUERY_PARAM, param)
                .appendQueryParameter(FORMAT_PARAM, format)
                .appendQueryParameter(UNITS_PARAM, units)
                .appendQueryParameter(DAYS_PARAM, Integer.toString(numDays))
                .appendQueryParameter(APPID_PARAM, BuildConfig.OPEN_WEATHER_API_KEY)
                .build();

        return builtUri;
    }




}
