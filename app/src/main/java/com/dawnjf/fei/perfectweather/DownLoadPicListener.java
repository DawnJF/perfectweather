package com.dawnjf.fei.perfectweather;

/**
 * Created by fei on 2017/3/10.
 */

public interface DownLoadPicListener {

    void showProgress(int progress);

    void onSuccess();

    void onFailed();

    void exists();

}
