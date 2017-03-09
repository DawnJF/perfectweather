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
import android.view.Menu;
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
import com.dawnjf.fei.perfectweather.util.MyApplication;
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

    private Menu mMenu;

    private List<MyCity> mMyCityList;

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


        // 设置刷新组建
        mSwipeRefresh.setColorSchemeResources
                (R.color.refresh_1, R.color.refresh_2,R.color.refresh_3);
        mNavButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mDrawerLayout.openDrawer(GravityCompat.START);
            }
        });


        updateMyCityList();
        Log.i(TAG, "onCreate: open" + mMyCityList.size());
        // 添加了城市
        MyCity addCity = (MyCity) getIntent().getSerializableExtra("my_city");
        final String weatherId;
        // 判断三种状态
        if (addCity == null) {
            // 正常打开
            mWeatherLayout.setVisibility(View.INVISIBLE);
            mSwipeRefresh.setRefreshing(true);
            if (mMyCityList.size() == 0) {
                // 第一次打开
                firstOpenApp();
                Log.i(TAG, "onCreate: First" + mMyCityList.size());
            }
            updateNavMenu(DataSupport.findAll(MyCity.class));
            mMenu.findItem(0).setCheckable(true);
            mMenu.findItem(0).setChecked(true);
            weatherId = mMyCityList.get(0).getWeatherId();
        } else {
            weatherId = addCity.getWeatherId();
            updateNavMenu(mMyCityList);
            MenuItem addItem = mMenu.findItem(mMyCityList.size() - 1);
            addItem.setCheckable(true);
            addItem.setChecked(true);
        }
        requestWeather(weatherId);

        // 设置导航栏
        mNavView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.item_add_city:
                        startActivity(new Intent(WeatherActivity.this, MainActivity.class));
                        break;
                    case R.id.item_edit_city:
                        Intent intent = new Intent(WeatherActivity.this, EditCityActivity.class);
                        startActivityForResult(intent, 110);
                        break;
                    default:
                }
                for (MyCity city: mMyCityList) {
                    if (item.getItemId() ==city.getShowId()) {
                        MenuItem menuItem = mMenu.findItem(item.getItemId());
                        menuItem.setCheckable(true);
                        menuItem.setChecked(true);
                        requestWeather(city.getWeatherId());
                    }
                }
                mDrawerLayout.closeDrawers();
                return true;
            }
        });

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
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode != 110)
            return;
        if (resultCode != RESULT_OK)
            return;
        updateMyCityList();
        updateNavMenu(mMyCityList);
        Log.i(TAG, "onActivityResult: " + mMyCityList.size());
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


    /**
     *
     */
    private void updateMyCityList() {
        if (mMyCityList != null)
            mMyCityList.clear();
        mMyCityList = DataSupport.order("showId").find(MyCity.class);
    }

    /**
     * 更新显示城市
     * @param cities
     */
    private void updateNavMenu(List<MyCity> cities) {
        mMenu = mNavView.getMenu();
        mMenu.removeGroup(316);
        for (MyCity city : cities) {
            mMenu.add(316, city.getShowId(), 2, city.getCityName())
                    .setIcon(R.drawable.ic_panorama_fish_eye_white_24dp);
        }
        mMenu.setGroupCheckable(316, true, true);
    }

    /**
     * 处理第一次打开APP初始化三个城市
     */
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
        updateMyCityList();
    }
}
