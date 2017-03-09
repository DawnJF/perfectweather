package com.dawnjf.fei.perfectweather.adapter;

/**
 * Created by fei on 2017/3/9.
 */

public interface ItemTouchHelperAdapter {
    // 数据交换
    void onItemMove(int formPosition, int toPosition);
    // 数据删除
    void onItemDismiss(int position);
}
