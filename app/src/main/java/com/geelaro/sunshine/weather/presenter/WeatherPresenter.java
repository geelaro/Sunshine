package com.geelaro.sunshine.weather.presenter;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.Preference;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;

import com.geelaro.sunshine.R;
import com.geelaro.sunshine.beans.WeatherBean;
import com.geelaro.sunshine.main.MainHomeActivity;
import com.geelaro.sunshine.main.SettingsActivity;
import com.geelaro.sunshine.utils.SunLog;
import com.geelaro.sunshine.utils.SunshineApp;
import com.geelaro.sunshine.utils.ToolUtils;
import com.geelaro.sunshine.weather.contract.WeatherContract;
import com.geelaro.sunshine.weather.model.WeatherModel;

import java.util.List;

/**
 * Created by LEE on 2017/6/19.
 */

public class WeatherPresenter implements WeatherContract.Presenter, WeatherModel.OnLoadWeatherListListener {
    private WeatherContract.WeatherView mView;
    private WeatherContract.Model mModel;
    private final static String TAG = WeatherPresenter.class.getSimpleName();

    public WeatherPresenter(WeatherContract.WeatherView view) {
        mView = view;
        mModel = new WeatherModel();
    }

    @Override
    public void loadWeatherList() {
        mModel.loadWeather(this); //获取数据
        SunLog.d(TAG, "loadWeatherList()");
    }

    @Override
    public void onSuccess(List<WeatherBean> list) {
        mView.addWeatherData(list);
        notifyWeather(SunshineApp.getContext(),list);
        SunLog.d(TAG, "addWeatherList");
    }

    @Override
    public void onFailure(String msg, Exception e) {

    }
    /** Notification **/
    private void notifyWeather(Context context, List<WeatherBean> list) {

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        String displayNotificationKey = context.getString(R.string.pref_enable_notifications_key);
        boolean displayNotification = prefs.getBoolean(displayNotificationKey,
                Boolean.parseBoolean(context.getString(R.string.pref_enable_notifications_default)));

        if (displayNotification){
            WeatherBean weatherBean = list.get(0);

            int weatherId = ToolUtils.getSmallWeatherImage(weatherBean.getWeatherId());
            String weatherDesc = weatherBean.getDesc();
            double low = weatherBean.getMinTemp();
            double high = weatherBean.getMaxTemp();
            String title = context.getString(R.string.app_name);
            String contentText = String.format(context.getString(R.string.format_notification),
                    weatherDesc,
                    ToolUtils.formatTemperature(context, high),
                    ToolUtils.formatTemperature(context, low));

            NotificationCompat.Builder mBuilder =
                    new NotificationCompat.Builder(context)
                            .setSmallIcon(weatherId)
                            .setContentTitle(title)
                            .setContentText(contentText)
                            .setAutoCancel(true);
            // Creates an explicit intent for an Activity in your app
            Intent resultIntent = new Intent(context, SettingsActivity.class);

            // The stack builder object will contain an artificial back stack for the
            // started Activity.
            // This ensures that navigating backward from the Activity leads out of
            // your application to the Home screen.
            TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
            // Adds the back stack for the Intent (but not the Intent itself)
            stackBuilder.addParentStack(SettingsActivity.class);
            // Adds the Intent that starts the Activity to the top of the stack
            stackBuilder.addNextIntent(resultIntent);
            PendingIntent resultPendingIntent =
                    stackBuilder.getPendingIntent(
                            0,
                            PendingIntent.FLAG_UPDATE_CURRENT
                    );
            mBuilder.setContentIntent(resultPendingIntent);
            NotificationManager mNotificationManager =
                    (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            // mId allows you to update the notification later on.
            mNotificationManager.notify(3400, mBuilder.build());
        }

    }


}
