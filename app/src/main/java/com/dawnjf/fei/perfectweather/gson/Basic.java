package com.dawnjf.fei.perfectweather.gson;

import com.google.gson.annotations.SerializedName;

/**
 * Created by fei on 2017/3/5.
 */

public class Basic {

    @SerializedName("city")
    public String cityName;

    @SerializedName("id")
    public String weatherId;

    public Update update;

    public class Update {

        @SerializedName("loc")
        public String updateTime;
    }
}
