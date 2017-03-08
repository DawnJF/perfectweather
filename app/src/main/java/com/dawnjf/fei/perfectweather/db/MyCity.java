package com.dawnjf.fei.perfectweather.db;

import com.google.gson.annotations.SerializedName;

import org.litepal.crud.DataSupport;

import java.io.Serializable;

/**
 * Created by fei on 2017/3/6.
 */

public class MyCity extends DataSupport implements Serializable{

    private int id;

    private int showId;

    private String cityName;

    private String weatherId;

    public int getId() {
        return id;
    }

    public int getShowId() {
        return showId;
    }

    public String getCityName() {
        return cityName;
    }

    public String getWeatherId() {
        return weatherId;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setShowId(int showId) {
        this.showId = showId;
    }

    public void setCityName(String cityName) {
        this.cityName = cityName;
    }

    public void setWeatherId(String weatherId) {
        this.weatherId = weatherId;
    }
}
