package com.dawnjf.fei.perfectweather.gson;

import com.google.gson.annotations.SerializedName;

/**
 * Created by fei on 2017/3/5.
 */

public class Now {

    @SerializedName("tmp")
    public String temperature;

    @SerializedName("cond")
    public More more;

    public class More {

        @SerializedName("txt")
        public String info;
    }
}
