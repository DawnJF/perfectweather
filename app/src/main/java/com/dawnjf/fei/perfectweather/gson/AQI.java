package com.dawnjf.fei.perfectweather.gson;

/**
 * Created by fei on 2017/3/5.
 */

public class AQI {

    public AQICity city;

    public class AQICity {
        public String aqi;

        public String pm25;
    }
}
