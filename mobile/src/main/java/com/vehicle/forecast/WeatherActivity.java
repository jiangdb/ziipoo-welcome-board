package com.vehicle.forecast;

import android.content.ComponentName;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.JsonObject;
import com.vehicle.framework.api.ApiCallback;
import com.vehicle.framework.api.ApiError;
import com.vehicle.framework.api.models.Today;
import com.vehicle.framework.api.requests.EtouchWeatherRequest;
import com.vehicle.framework.api.responses.EtouchWeatherResponse;
import com.vehicle.utils.Constants;
import com.vehicle.utils.TodayUtil;

import java.util.Calendar;

public class WeatherActivity extends AppCompatActivity {
    private static String TAG = WeatherActivity.class.getSimpleName();
    private static long ThreeHours = 3 * 3600 * 1000;
    private ImageView mIVType;
    private TextView mTemp;
    private TextView mTempUp;
    private TextView mTempDown;
    private TextView mCity;

    private TextView mYear;
    private TextView mMonth;
    private TextView mDay;

    private Handler mHanlder = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    getWeatherByCity("上海");
                    break;
                default:
                    break;
            }
            super.handleMessage(msg);
        }
    };
    private Runnable weathertask = new Runnable() {
        @Override
        public void run() {
            /**
             * 此处执行任务
             * */
            mHanlder.sendEmptyMessage(1);
            mHanlder.postDelayed(this, ThreeHours);//延迟1小时,再次执行task本身,实现了循环的效果
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        hideBottomUIMenu();
        setContentView(R.layout.activity_weather);

        mDay = findViewById(R.id.tv_day);
        mMonth = findViewById(R.id.tv_month);
        mYear = findViewById(R.id.tv_year);

        mIVType = findViewById(R.id.iv_type);
        mTemp = findViewById(R.id.tv_temp);
        mTempUp = findViewById(R.id.tv_temp_up);
        mTempDown = findViewById(R.id.tv_temp_down);
        mCity = findViewById(R.id.tv_city);

        getWeatherByCity("上海");

        setDate();

        mHanlder.postDelayed(weathertask,ThreeHours);


    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mHanlder.removeCallbacks(weathertask);
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if (event.getKeyCode() == KeyEvent.KEYCODE_H && event.isLongPress()) {
            Intent intent = new Intent();
            intent.setComponent(new ComponentName("com.android.launcher3", "com.android.launcher3.Launcher"));
            startActivity(intent);
            return true;
        }else if (event.getKeyCode() == KeyEvent.KEYCODE_S && event.isLongPress()) {
            Intent intent = new Intent();
            intent.setComponent(new ComponentName("com.android.settings", "com.android.settings.Settings"));
            startActivity(intent);
            return true;
        }
        return super.dispatchKeyEvent(event);
    }

    private void getWeatherByCity(final String city) {
        EtouchWeatherRequest.load(city,new ApiCallback<EtouchWeatherResponse>() {
            @Override
            public void onResponse(EtouchWeatherResponse etouchResponse) {
                if (etouchResponse != null){
                    if (etouchResponse.getStatus().equals("1000")){
                        JsonObject today = etouchResponse.getToday();
                        String high = today.get(Constants.TODAY_HIGH).getAsString();
                        String low = today.get(Constants.TODAY_LOW).getAsString();
                        String type = today.get(Constants.TODAY_TYPE).getAsString();
                        String wendu = etouchResponse.getWenDu();
                        Today.getInstance().setHighTemperature(high)
                                            .setLowTemperature(low)
                                            .setWeatherType(type)
                                            .setCurrentTemperature(wendu);
                        mTempUp.setText(TodayUtil.getHighTemperature(high));
                        mTempDown.setText(TodayUtil.getLowTemperature(low));
                        mTemp.setText(wendu);
                        mIVType.setImageResource(TodayUtil.getWeatherTypeImage(type));
                        mCity.setText(city);


                    }
                }
            }

            @Override
            public void onError(ApiError error) {
                Toast toast = Toast.makeText(WeatherActivity.this,error.message,Toast.LENGTH_LONG);
                toast.setGravity(Gravity.TOP, 0, 0);
                toast.show();
            }
        });
    }

    private void setDate() {
        String[] monthArray = {"JAN.","FEB.","MAR.","APR.","MAY","JUNE","JULY","AUG.","SEPT.","OCT.","NOV.","DEC."};
//        String[] monthCnArray = {"一月","二月","三月","四月","五月","六月","七月","八月","九月","十月","十一月","十二月"};
        Calendar c = Calendar.getInstance();
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);
        mDay.setText(String.valueOf(day));
        if (month >=0 && month <12) {
            mMonth.setText(monthArray[month]);
        }
        mYear.setText(String.valueOf(year));
    }

    private void hideBottomUIMenu() {
        //隐藏虚拟按键，并且全屏
        if (Build.VERSION.SDK_INT > 11 && Build.VERSION.SDK_INT < 19) { // lower api
            View v = this.getWindow().getDecorView();
            v.setSystemUiVisibility(View.GONE);
        } else if (Build.VERSION.SDK_INT >= 19) {
            //for new api versions.
            View decorView = getWindow().getDecorView();
            int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY | View.SYSTEM_UI_FLAG_FULLSCREEN;
            decorView.setSystemUiVisibility(uiOptions);
        }
    }

}
