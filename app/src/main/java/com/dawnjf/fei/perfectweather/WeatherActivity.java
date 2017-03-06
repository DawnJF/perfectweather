package com.dawnjf.fei.perfectweather;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.dawnjf.fei.perfectweather.db.MyCity;
import com.dawnjf.fei.perfectweather.gson.Forecast;
import com.dawnjf.fei.perfectweather.gson.Weather;
import com.dawnjf.fei.perfectweather.service.AutoUpdateService;
import com.dawnjf.fei.perfectweather.util.HttpUtil;
import com.dawnjf.fei.perfectweather.util.Utility;

import org.litepal.crud.DataSupport;

import java.io.IOException;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class WeatherActivity extends AppCompatActivity {

    private static final String TAG = "WWWWW";

    public SwipeRefreshLayout mSwipeRefresh;

    public DrawerLayout mDrawerLayout;

    private Button mNavButton;

    private ScrollView mWeatherLayout;

    private TextView mTitleCity;

    private TextView mTitleUpdateTime;

    private TextView mDegreeText;

    private TextView mWeatherInfoText;

    private LinearLayout mForecastLayout;

    private TextView mAQIText;

    private TextView mPM25Text;

    private TextView mComfortText;

    private TextView mCarWashText;

    private TextView mSportText;

    private ImageView mBingPicImg;

    private NavigationView mNavView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 如果版本号大于等于21，即5.0及以上设置状态栏与背景融合
        if (Build.VERSION.SDK_INT >= 21) {
            View decorView = getWindow().getDecorView();
            decorView.setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
            getWindow().setStatusBarColor(Color.TRANSPARENT);
        }
        setContentView(R.layout.activity_weather);
        // 初始化各种控件
        mBingPicImg = (ImageView) findViewById(R.id.bing_pic_img);
        mWeatherLayout = (ScrollView) findViewById(R.id.weather_layout);
        mTitleCity = (TextView) findViewById(R.id.title_city);
        mTitleUpdateTime = (TextView) findViewById(R.id.title_update_time);
        mDegreeText = (TextView) findViewById(R.id.degree_text);
        mWeatherInfoText = (TextView) findViewById(R.id.weather_info_text);
        mForecastLayout = (LinearLayout) findViewById(R.id.forecast_layout);
        mAQIText = (TextView) findViewById(R.id.aqi_text);
        mPM25Text = (TextView) findViewById(R.id.pm25_text);
        mComfortText = (TextView) findViewById(R.id.comfort_text);
        mCarWashText = (TextView) findViewById(R.id.car_wash_text);
        mSportText = (TextView) findViewById(R.id.sport_text);
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mNavButton = (Button) findViewById(R.id.nav_button);
        mSwipeRefresh = (SwipeRefreshLayout) findViewById(R.id.swipe_refresh);
        mNavView = (NavigationView) findViewById(R.id.nav_view);

        // 设置导航栏
        mNavView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.set_city:
                        startActivity(new Intent(WeatherActivity.this, MainActivity.class));
                        break;
                    default:
                }
                mDrawerLayout.closeDrawers();
                return true;
            }
        });
        // 设置刷新组建
        mSwipeRefresh.setColorSchemeResources
                (R.color.refresh_1, R.color.refresh_2,R.color.refresh_3);
        mNavButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mDrawerLayout.openDrawer(GravityCompat.START);
            }
        });


        MyCity showCity = DataSupport.where("showID = ?", "0").findFirst(MyCity.class);
        // 选择了城市
        final String weatherId = getIntent().getStringExtra("weather_id");
        // 判断三种状态
        if (showCity == null) {
            firstOpenApp();
        } else if (weatherId == null) {
            Log.i(TAG, "onCreate: " + showCity);
            requestWeather(showCity.getWeatherId());
        } else {
            mWeatherLayout.setVisibility(View.INVISIBLE);
            requestWeather(weatherId);
        }
//        // 选择城市后跳过解析缓存
//        final String weatherId;
//        if (intent_weather_id == null) {
//            // 有缓存时直接解析天气数据
//            Weather weather = Utility.handleWeatherResponse(weatherString);
//            weatherId = weather.basic.weatherId;
//            showWeatherInfo(weather);
//        } else {
//            // 无缓存时去服务器查询天气
//            weatherId = intent_weather_id;
//            mWeatherLayout.setVisibility(View.INVISIBLE);
//            requestWeather(weatherId);
//        }



        mSwipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                requestWeather(weatherId);
            }
        });

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        String bingPic = prefs.getString("bing_pic", null);
        if (bingPic != null) {
            Glide.with(this).load(bingPic).into(mBingPicImg);
        } else {
            loadBingPic();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        String weatherString = prefs.getString("weather", null);
        if (weatherString == null) return;
        Weather weather = Utility.handleWeatherResponse(weatherString);
        showWeatherInfo(weather);
    }

    /**
     * 根据天气id请求城市天气信息
     * @param weatherId
     */
    public void requestWeather(final String weatherId) {
        String weatherUrl = "http://guolin.tech/api/weather?cityid=" +
                weatherId + "&key=c1214de27b8d4e2793367eab33570d6b";
        HttpUtil.sendOkHttpRequest(weatherUrl, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(WeatherActivity.this, "获取天气信息失败",
                                Toast.LENGTH_SHORT).show();
                        mSwipeRefresh.setRefreshing(false);
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final String responseText = response.body().string();
                final Weather weather = Utility.handleWeatherResponse(responseText);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (weather != null && "ok".equals(weather.status)) {
                            SharedPreferences.Editor editor = PreferenceManager
                                    .getDefaultSharedPreferences(WeatherActivity.this)
                                    .edit();
                            editor.putString("weather", responseText);
                            editor.apply();
                            showWeatherInfo(weather);
                        } else {
                            Toast.makeText(WeatherActivity.this, "获取天气信息失败",
                                    Toast.LENGTH_SHORT).show();
                        }
                        mSwipeRefresh.setRefreshing(false);
                    }
                });
            }
        });
        loadBingPic();
    }

    /**
     * 加载必应每日一图
     */
    private void loadBingPic() {
        String requestBingPic = "http://guolin.tech/api/bing_pic";
        HttpUtil.sendOkHttpRequest(requestBingPic, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final String bingPic = response.body().string();
                SharedPreferences.Editor editor = PreferenceManager
                        .getDefaultSharedPreferences(WeatherActivity.this).edit();
                editor.putString("bing_pic", bingPic);
                editor.apply();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Glide.with(WeatherActivity.this).load(bingPic).into(mBingPicImg);
                    }
                });
            }
        });
    }

    /**
     * 处理并展示Weather实体类中的数据
     * @param weather
     */
    private void showWeatherInfo(Weather weather) {
        String cityName = weather.basic.cityName;
        String updateTime = "更新时间:" + weather.basic.update.updateTime.split(" ")[1];
        String degree = weather.now.temperature + "℃";
        String weatherInfo = weather.now.more.info;
        mTitleCity.setText(cityName);
        mTitleUpdateTime.setText(updateTime);
        mDegreeText.setText(degree);
        mWeatherInfoText.setText(weatherInfo);
        mForecastLayout.removeAllViews();
        for (Forecast forecast : weather.forecastList) {
            View view = LayoutInflater.from(this)
                    .inflate(R.layout.forecast_item, mForecastLayout, false);
            TextView dateText = (TextView) view.findViewById(R.id.date_text);
            TextView infoText = (TextView) view.findViewById(R.id.info_text);
            TextView maxText = (TextView) view.findViewById(R.id.max_text);
            TextView minText = (TextView) view.findViewById(R.id.min_text);
            Log.i(TAG, "showWeatherInfo: " + forecast.date + "/" + forecast.temperature.max);
            dateText.setText(forecast.date);
            infoText.setText(forecast.more.info);
            maxText.setText(forecast.temperature.max);
            minText.setText(forecast.temperature.min);
            mForecastLayout.addView(view);
        }
        if (weather.aqi != null) {
            mAQIText.setText(weather.aqi.city.aqi);
            mPM25Text.setText(weather.aqi.city.pm25);
        }
        String comfort = "舒适度： " + weather.suggestion.comfort.info;
        String carWash = "洗车指数： " + weather.suggestion.carWash.info;
        String sport = "运动建议：" + weather.suggestion.sport.info;
        mComfortText.setText(comfort);
        mCarWashText.setText(carWash);
        mSportText.setText(sport);
        mWeatherLayout.setVisibility(View.VISIBLE);
        // 启动 后台服务（尝试）
        MyApplication.getContext()
                .startService(new Intent(this, AutoUpdateService.class));
    }

    private void firstOpenApp(){
        // 第一次打开（没有选择城市从而存入缓存）添加三个待选择城市
        MyCity city1 = new MyCity();
        city1.setWeatherId("CN101010100");
        city1.setCityName("北京");
        city1.setShowId(0);
        city1.save();
        MyCity city2 = new MyCity();
        city2.setCityName("上海");
        city2.setShowId(1);
        city2.setWeatherId("CN101020100");
        city2.save();
        MyCity city3 = new MyCity();
        city3.setCityName("广州");
        city3.setShowId(2);
        city3.setWeatherId("CN101280101");
        city3.save();
    }
}
